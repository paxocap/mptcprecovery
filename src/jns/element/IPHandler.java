/**
 TODO LIST FOR IPHANDLER

 - Process time to live field
 - look at copy_options field in ip packets
 -
 */

package jns.element;

import jns.Simulator;
import jns.agent.Agent;
import jns.agent.CL_Agent;
import jns.command.Command;
import jns.command.ElementUpdateCommand;
import jns.util.IPAddr;
import jns.util.Preferences;
import jns.util.RoutingTable;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class IPHandler extends Element implements CL_Agent
{

    private Vector<Interface> m_interfaces;

    private jns.util.Queue m_packets_send;  // Packets waiting to be sent
    private jns.util.Queue m_packets_recv;  // Received packets to be processed

    private Vector m_fragments;   // A vector of Fragment objects (See below)

    private RoutingTable m_route;

    private int m_packetid;         // Unique packet id

    private Hashtable m_protocols;  // Higher level protocols, stores instances
    // of 'HigherAgent'


    public IPHandler()
    {
        m_interfaces = new Vector();

        m_packets_send = new jns.util.Queue();
        m_packets_recv = new jns.util.Queue();

        m_route = new RoutingTable();

        // Get a new unique packet id start from the current time in millisecs
        // and a random number between 0-10000.

        //m_packetid=(int)((new Date()).getTime())+(int)(Math.random()*10000.0);
        m_packetid = 0;

        m_fragments = new Vector();


        m_protocols = new Hashtable();
    }


    public void attach(Interface iface)
    {

        // If the appropriate flag is set, make this iface the default route

        if(Preferences.first_iface_is_default_route && m_interfaces.isEmpty())
        {
            m_route.addDefaultRoute(iface);
        }

        // Make this handler that interfaces handler

        iface.setIPHandler(this);

        // Add to interface list

        m_interfaces.addElement(iface);
    }


    public void attach(Agent higher_level, int unique_id)
    {

        // Create a new receive queue, etc. for this agent

        HigherAgent newagent = new HigherAgent(higher_level);
        m_protocols.put(new Integer(unique_id), newagent);

        // Reverse attach ourselves to this agent (allow it to make a reference)

        higher_level.attach(this);
    }

    public void attach(Agent lower_level)
    {
        Simulator.error("You may not attach a lower level agent to an IP " +
                        "handler (because there is no such thing!)");
    }


    public void dump()
    {
        System.out.println("IP Handler");

        Enumeration e = m_interfaces.elements();
        while(e.hasMoreElements())
            ((Element) e.nextElement()).dump();
    }


    public IPAddr getAddress()
    {
        if(m_interfaces.size() > 0)
        {
            Interface iface = (Interface) m_interfaces.firstElement();
            return iface.getIPAddr();
        }
        else
            return null;
    }


    /**
     The update function will process the send queue and the receive queue
     of the IPHandler. Everything in the send queue will be given to an
     interface to send, if possible. Things in the receive queue will be
     forwarded to a higher level protocol or if this is a router, sent on
     to the next hop.
     */
    public void update()
    {
        Simulator.verbose("Updating IP Handler");

        // Process packets waiting to be sent

        while(m_packets_send.size() > 0)
        {

            // Remove a packet from the queue

            IPPacket curpacket = (IPPacket) m_packets_send.peekBack();
            m_packets_send.popBack();


            Vector targets = new Vector();

            if(curpacket.destination.isMulticastAddress())
            {

                //Get all the targets, as a multicast packet should be sent to all the
                //neigbouring nodes..
                targets = m_route.getAllRoutes();
                //deliver the packet to myself, as that is the required behaviour of
                //multicast
                m_packets_recv.pushBack(new IPPacket(curpacket));
            }
            else
            {
                targets.add(m_route.getRoute(curpacket.destination));
            }
            
            boolean canSend = true;
            for(int j = 0; j < targets.size(); j++)
            {
             	System.out.println("IPHandler at " + m_interfaces.get(0).getIPAddr() +
            			" destination is " + curpacket.destination + " size = " + targets.size());
            	if(targets.size() == 0)
                    Simulator.warning("No route to address " +
                                      curpacket.destination + " from " +
                                      curpacket.source);
           
                //check that we canSend() on all the interfaces..
            	else if(!((Interface)targets.get(j)).canSend(curpacket.destination, curpacket.length))
                    {
                	 	if(((Interface)targets.get(j)).isLinkDown())
                	 	{
                	 		IPAddr temp = curpacket.source;
                	 		curpacket.source = curpacket.destination;
                	 		curpacket.destination = temp;
                	 		curpacket.NEG_ACK = true;
                	 	}
                        m_packets_send.pushFront(curpacket);
                        canSend = false;
                        Simulator.verbose("The interface on which to send to: "+curpacket.destination+" is busy at " + m_interfaces.get(0).getIPAddr() + " , pushing packet onto msg queue again.");
                        break;
                    }
            }


            if(canSend)
            {
                for(int j = 0; j < targets.size(); j++)
                {

                    if(targets.size() == 0)
                        Simulator.warning("No route to address " +
                                          curpacket.destination + " from " +
                                          curpacket.source);
                    else
                    {

                       // Check if we have to fragment...

                        if(((Interface)targets.get(j)).getMTU() >= curpacket.length)
                        {
                            //Copy the packet, to use if we send more than one..
                            IPPacket sendPacket = new IPPacket(curpacket);
                            ((Interface)targets.get(j)).send(sendPacket);
                        }
                        else
                        {
                            // Maximum packet length (must be a multiple of 8 with IP)
                            int maximum_length = ((Interface)targets.get(j)).getMTU() & (~7);

                            // Number of fragments to generate
                            int num_packets = (curpacket.length / maximum_length) + 1;
                            int offset = 0;

                            Simulator.verbose("Fragmenting big packet into " + num_packets +
                                              " fragments.");

                            for(int i = 0; i < num_packets; i++)
                            {
                                // Copy the original packet
                                IPPacket new_fragment = new IPPacket(curpacket);

                                // Last fragment

                                if(i == (num_packets - 1))
                                {
                                    // Note that we do not handle the more_fragments bit here. If
                                    // we are refragmenting a fragmented packet, it's set already
                                    // otherwise it's unset, which is fine.. last packet..

                                    new_fragment.length = curpacket.length % maximum_length;
                                }
                                else
                                {
                                    new_fragment.flags |= IPPacket.FLAG_MORE_FRAGMENTS;
                                    new_fragment.length = maximum_length;
                                }

                                new_fragment.fragment_offset = (offset >> 3);
                                offset += new_fragment.length;

                                // Off it goes..
                                ((Interface)targets.get(j)).send(new_fragment);
                            }
                        }
                    }
                }
            }
        }

        // Received packets waiting to be sent on or given to higher level
        // protocols

        while(m_packets_recv.size() > 0)
        {
            IPPacket curpacket = (IPPacket) m_packets_recv.peekBack();
            m_packets_recv.popBack();

            // Check the packet's integrity

            if(!curpacket.crc)
            {
                // TODO: Generate drop packet event ?

                // Get next packet instead
                continue;
            }

            // Check if the packet's destination IP address equals on of our
            // interfaces addresses..

            boolean is_final_dest = false;

            Enumeration e = m_interfaces.elements();
            while(e.hasMoreElements())
            {
                Interface curiface = (Interface) e.nextElement();

                if(curiface.getIPAddr().equals(curpacket.destination))
                {
                    Simulator.verbose("Packet at final dest");
                    is_final_dest = true;
                    break;
                }
            }

            // If this is not a final destination, send it on
            //TODO: IMPORTANT: I'm pretty sure that the last conditional on this if statement
            // breaks the standard, fixed line IP handling of multicast addresses, but it's
            //the desired behaviour for 802.11b in ad hoc mode!
            if(!is_final_dest && !curpacket.destination.isMulticastAddress())
            {
                Simulator.verbose("Sending on packet");

                m_packets_send.pushFront(curpacket);

                Simulator.getInstance().schedule(new ElementUpdateCommand(this,
                                                                          Simulator.getInstance().getTime() +
                                                                          Preferences.delay_ip_to_ifacequeue));
            }
            else
            {
                // Not a fragment because offset=0 and no 'more fragments' flags set

                if(curpacket.fragment_offset == 0 &&
                        (curpacket.flags & IPPacket.FLAG_MORE_FRAGMENTS) == 0)
                {

                    // Pass on to higher level protocol
                    HigherAgent destagent = (HigherAgent)
                            m_protocols.get(new Integer(curpacket.protocol));
                    

                    if(destagent != null)
                    {
                        destagent.queue.pushFront(curpacket);
                        destagent.agent.indicate(Agent.PACKET_AVAILABLE, this);
                    }
                    else
                    {
                        // TODO: No destination protocol.. Generate a custom event ?
                    }
                }
                else
                {

                    // There's a fragment lurking around, perform reassembly.

                    Fragment frag_entry = null;

                    // Find if there is an entry in the fragment list

                    e = m_fragments.elements();
                    while(e.hasMoreElements())
                    {
                        Fragment curfragment = (Fragment) e.nextElement();

                        if(curfragment.getId() == curpacket.id)
                        {
                            frag_entry = curfragment;
                            break;
                        }
                    }

                    // No entry, create a new one, start timeout that will invalidate
                    // this fragment list after a while

                    if(frag_entry == null)
                    {
                        frag_entry = new Fragment(curpacket.id);
                        m_fragments.addElement(frag_entry);

                        // Schedule a timeout that will delete this id's fragments if
                        // they aren't complete yet by then

                        class FragmentTimeoutCommand extends Command
                        {
                            int m_id;

                            FragmentTimeoutCommand(int id, double time)
                            {
                                super("FragmentTimeout", time);
                                m_id = id;
                            }

                            public void execute()
                            {
                                // Look through fragments, they might not exist anymore
                                // because they are complete..
                                for(int i = 0; i < m_fragments.size(); i++)
                                {
                                    Fragment curfragment = (Fragment) m_fragments.elementAt(i);
                                    if(curfragment.getId() == m_id)
                                    {
                                        m_fragments.removeElementAt(i);
                                        return;
                                    }
                                }
                            }
                        }

                        Simulator.getInstance().schedule(new FragmentTimeoutCommand(
                                curpacket.id, Simulator.getInstance().getTime() +
                                              Preferences.ip_fragmentation_timeout));
                    }

                    // Add this packet to the list of fragments

                    frag_entry.addFragment(curpacket);

                    // If we are complete, reassemble

                    if(frag_entry.complete())
                    {
                        Simulator.verbose("Packet complete!!");

                        IPPacket packet = frag_entry.reassemble();

                        // Remove from fragment list

                        for(int i = 0; i < m_fragments.size(); i++)
                        {
                            Fragment curfragment = (Fragment) m_fragments.elementAt(i);
                            if(curfragment.getId() == packet.id)
                            {
                                m_fragments.removeElementAt(i);
                                return;
                            }
                        }

                        // Pass on to higher level protocol

                        HigherAgent destagent = (HigherAgent)
                                m_protocols.get(new Integer(curpacket.protocol));
                    
                        if(destagent != null)
                        {
                            destagent.queue.pushFront(curpacket);
                            destagent.agent.indicate(Agent.PACKET_AVAILABLE, this);
                        }
                        else
                        {
                            // TODO: No destination protocol.. Generate a custom event ?
                        }

                    }
                }

            }
        }
    }


    /**
     * Private function that processes a packet to be sent to a certain destination
     */




    /**
     Send an IP packet. This queues the packet into the send queue and
     schedules a call to update().
     */
    public void send(IPAddr source, IPAddr dest, int length, Object data,
                     int unique_id)
    {
        IPPacket packet = new IPPacket();

        packet.source = new IPAddr(source);
        packet.destination = new IPAddr(dest);
        packet.length = IPPacket.HEADER_SIZE + length;
        packet.data = data;
        packet.id = m_packetid++;
        packet.protocol = unique_id;
         // Put packet in the send queue
        m_packets_send.pushFront(packet);

        // Schedule a call to the update function
        Simulator.getInstance().schedule(new ElementUpdateCommand(this,
                                                                  Simulator.getInstance().getTime() +
                                                                  Preferences.delay_ip_to_ifacequeue));
    }


    public Object read(int unique_id)
    {

        HigherAgent destagent = (HigherAgent)
                m_protocols.get(new Integer(unique_id));
        if(destagent == null)
        {
            Simulator.warning("Higher level agent with unknown id called read()");
            return null;
        }

        IPPacket packet = (IPPacket) destagent.queue.peekBack();
        destagent.queue.popBack();

        return packet;
    }


    public boolean canSend(IPAddr destination, int length)
    {
        Interface target = m_route.getRoute(destination);
        return target.canSend(destination, length);
    }

    /**
     Indicate to this IP handler that a packet is waiting for collection
     @param status the status that is indicated, as defined in Agent
     @param indicator the object that is giving is the packet, must be an
     Interface object.
     @see Agent
     */
    public void indicate(int status, Object indicator)
    {
        if(!(indicator instanceof Interface))
            Simulator.error("IPHandler received an indication from a non-Interface");

        if((status & Agent.READY_TO_SEND) != 0)
        {
            Simulator.verbose("IPHandler got READY_TO_SEND");

            // Schedule a call to the update function
            Simulator.getInstance().schedule(new ElementUpdateCommand(this,
                                                                      Simulator.getInstance().getTime() +
                                                                      Preferences.delay_ifacequeue_to_ip));

            // Notify all agents above us that we're ready to send.
            for(Enumeration e = m_protocols.elements(); e.hasMoreElements();)
            {
                HigherAgent curagent = (HigherAgent) e.nextElement();

                curagent.agent.indicate(Agent.READY_TO_SEND, this);
            }
        }
        else if((status & Agent.PACKET_AVAILABLE) != 0)
        {
            IPPacket packet = (IPPacket) ((Interface) indicator).read(0);

            // Stick the packet in the receive queue
            m_packets_recv.pushFront(packet);

            // Schedule a call to the update function
            Simulator.getInstance().schedule(new ElementUpdateCommand(this,
                                                                      Simulator.getInstance().getTime() +
                                                                      Preferences.delay_ifacequeue_to_ip));
        }
        else
            Simulator.warning("IPHandler received an unhandled indication: " + status);
    }


    public void addRoute(IPAddr dest, IPAddr netmask, Interface iface)
    {
        m_route.addRoute(dest, netmask, iface);
    }


    public void addDefaultRoute(Interface iface)
    {
        m_route.addDefaultRoute(iface);
    }

    public RoutingTable getRoutingTable()
    {
        return m_route;
    }


    public Enumeration enumerateInterfaces()
    {
        return m_interfaces.elements();
    }

}


class HigherAgent
{
    public jns.util.Queue queue;
    public Agent agent;

    public HigherAgent(Agent agent)
    {
        this.agent = agent;
        queue = new jns.util.Queue();
    }
}


/**
 Auxiliary class to represent the fragments of one IP packet. Contains the
 ID of the packet and a number of fragment packets.
 */
class Fragment
{

    private int m_id;
    private Vector m_packets;

    public Fragment(int id)
    {
        m_id = id;
        m_packets = new Vector();
    }

    public int getId()
    {
        return m_id;
    }

    public boolean complete()
    {

        int offset = 0;

        for(int i = 0; i < m_packets.size(); i++)
        {
            IPPacket curpacket = (IPPacket) m_packets.elementAt(i);

            // Offsets don't match, there a packet missing, fail
            if(curpacket.fragment_offset != offset) return false;

            // Last packet, but 'more fragments' set, so fail
            if(i == m_packets.size() - 1)
                if((curpacket.flags & IPPacket.FLAG_MORE_FRAGMENTS) != 0)
                    return false;

            offset = offset + (curpacket.length >> 3);
        }

        return true;
    }

    public void addFragment(IPPacket packet)
    {
        int i = 0;

        Simulator.verbose(m_packets.size());
        Simulator.verbose(packet.fragment_offset);

        if(m_packets.size() == 0)
        {
            m_packets.addElement(packet);
            return;
        }

        while(i < m_packets.size() &&
                (((IPPacket) m_packets.elementAt(i)).fragment_offset <
                packet.fragment_offset))
            i++;

        // Ignore duplicates
        if(i < m_packets.size() &&
                ((IPPacket) m_packets.elementAt(i)).fragment_offset ==
                packet.fragment_offset)
            return;

        m_packets.insertElementAt(packet, i);
    }


    /**
     Reassemble a broken up IP packet. This is easy because the data does
     not actually ever get split up.. :)
     */
    public IPPacket reassemble()
    {
        int length = 0;

        // Simply copy the first IP packet because it contains almost all of the
        // original packt
        IPPacket packet = new IPPacket((IPPacket) m_packets.elementAt(0));

        // Reset the flags and  length
        packet.length = 0;
        packet.flags = 0;

        // Rebuild the original length field
        Enumeration e = m_packets.elements();
        while(e.hasMoreElements())
        {
            IPPacket curpacket = (IPPacket) e.nextElement();

            packet.length += curpacket.length;
        }

        return packet;
    }



}

