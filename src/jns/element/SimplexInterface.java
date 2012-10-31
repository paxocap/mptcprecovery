
package jns.element;

import jns.Simulator;
import jns.agent.Agent;
import jns.command.*;
import jns.command.ElementUpdateCommand;
import jns.trace.Event;
import jns.trace.Trace;
import jns.util.EventGenerator;
import jns.util.IPAddr;
import jns.util.Preferences;
import jns.util.Status;


public class SimplexInterface extends Interface
{

    private Queue m_queue;
    private SimplexLink m_link;
    private int m_status;
    private int m_direction;


    public SimplexInterface(int direction, IPAddr addr)
    {
        super(addr);

        m_direction = direction;
        m_status = Status.UP;
        m_link = null;

        m_queue = new QueueDropTail(Preferences.default_maxqueuelength);
        m_queue.attach(this);
    }


    public SimplexInterface(int direction, IPAddr addr, int bandwidth)
    {
        super(addr, bandwidth);

        m_direction = direction;
        m_status = Status.UP;
        m_link = null;

        m_queue = new QueueDropTail(Preferences.default_maxqueuelength);
        m_queue.attach(this);
    }


    public void attach(Node node)
    {
        m_node = node;
    }

    public void attach(Link link, boolean inheritBandwidth)
    {
        if(!(link instanceof SimplexLink))
        {
            System.err.println("SIMULATOR ERROR: You can only attach a SimplexLink" +
                               " to a SimplexInterface!");
            System.exit(1);
        }

        SimplexLink simplexlink = (SimplexLink) link;

        switch(m_direction)
        {
            case Interface.RECEIVER:
                simplexlink.setIncoming(this);
                m_link = simplexlink;
                break;

            case Interface.SENDER:
                simplexlink.setOutgoing(this);
                m_link = simplexlink;
                break;
        }

    }

    public void attach(Queue queue)
    {
        m_queue = queue;
        m_queue.attach(this);
    }


    public void attach(Trace trace)
    {
        super.attach(trace);
        m_queue.attach(trace);
    }

    public int getType()
    {
        return m_direction;
    }

    public Node getNode()
    {
        return m_node;
    }

    public boolean canSend(IPAddr destination, int length)
    {
        return (!m_queue.isFull(length) && m_link.canSend());
    }

    public boolean isLinkDown()
    {
    	return (m_link.getStatus() == Status.DOWN);
    }
    /**
     send puts a packet to be sent in the send queue of this interface and
     schedules a call to the update() method. You are advised to use the
     canSend function to check if the packet can be sent first, otherwise
     it might be dropped.
     @param packet the packet to be sent
     */
    public void send(IPPacket packet)
    {
        if(m_direction != Interface.SENDER)
            Simulator.error("Trying to send a packet from a receiver interface");

        // Fill out current and next hop address in the packet

        packet.source_hop = m_addr;
        packet.destination_hop = m_link.getIncoming().getIPAddr();

        // Enqueue the packet in the send queue

        m_queue.enqueue(packet);

        // Schedule a call to the update function
        Simulator.getInstance().schedule(new ElementUpdateCommand(this,
                                                                  Simulator.getInstance().getTime() +
                                                                  Preferences.delay_iface_send_update));

    }


    public Object read(int unique_id)
    {
        if(m_direction != Interface.RECEIVER)
            Simulator.error("Trying to read a packet from a sender interface");

        IPPacket packet = m_queue.dequeue();
        if(packet == null)
        {
            Simulator.warning("No packets in queue when read() was called - " +
                              "Natural Overflow?");
            return null;
        }

        return packet;
    }


    public void dump()
    {
        System.out.println("SimplexInterface: " + m_addr);
        System.out.println("Status: " + m_status);
        System.out.println("Direction: " + m_direction);
    }


    public void indicate(int status, Object indicator)
    {
        // Only links may indicate to interfaces!
        if(!(indicator instanceof Link))
            Simulator.error("Interface received an indication from a non-Link");

        // We are clear to send, schedule a call to update
        if((status & Agent.READY_TO_SEND) != 0)
            Simulator.getInstance().schedule(new ElementUpdateCommand(this,
                                                                      Simulator.getInstance().getTime() +
                                                                      Preferences.delay_iface_send_update));

        // A packet is waiting for collection
        if((status & Agent.PACKET_AVAILABLE) != 0)
        {
            SimplexLink l = (SimplexLink) indicator;
            IPPacket packet = (IPPacket) l.read(0);

            m_queue.enqueue(packet);

            // Indicate to IP handler
            m_handler.indicate(Agent.PACKET_AVAILABLE, this);

            // Send receive event
            Event hopevent = EventGenerator.makePacketEvent("ReceiveEvent", packet);
            sendEvent(hopevent);
        }
    }

    /**
     update will do either of two things: If the interface is a SENDER, it
     will try to take packets off the queue and put them on the link.
     Conversely, if it is a receiver, it will take packets off the link and
     stick them in the queue. WRONG.
     */
    public void update()
    {
        Simulator.verbose("Interface update");

        if(m_link == null)
        {
            System.err.println("SIMULATOR ERROR: You are trying to send packets " +
                               "from an iface without a link!");
            System.exit(1);
        }

        if(m_direction == Interface.SENDER && m_link.canSend())
        {
            // TODO: Generate 'packet sent' event
            IPPacket packet = m_queue.dequeue();

            if(packet != null)
            {
                m_link.send(packet);

                // Generate a HopEvent

                Event hopevent = EventGenerator.makePacketEvent("HopEvent", packet);
                sendEvent(hopevent);

                // A packet is gone so the queue can't be full anymore. Indicate this
                // to the IP handler.

                m_handler.indicate(Agent.READY_TO_SEND, this);
            }
            else
                m_handler.indicate(Agent.READY_TO_SEND, this);
        }

    }


}


