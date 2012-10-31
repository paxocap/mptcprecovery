package jns.agent;

import jns.Simulator;
import jns.element.IPHandler;
import jns.element.IPPacket;
import jns.util.IPAddr;
import jns.util.Protocols;
import jns.util.Queue;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 This class merely serves as a multiplexer class that sits on top of IP and
 forwards packets that are coming in to a SimpleGoBackNAgent class at a
 specific port. It only manages objects of that other class and does not do
 very much else.<br>
 Note that this class is not as general as the multiplexer that TCP uses
 because it only allows one agent per port (whereas TCP allows different
 tuples of (ip address, port number))
 */
public class SimpleGoBackN implements CL_Agent
{

    // A hashtable containing AgentPort objects, indexed by port
    // numbers as Integer objects
    private Hashtable m_ports;

    // The IP service we are using (note that we are using a CL_Agent reference
    // so any connection-less service will do for us!)
    private CL_Agent m_ip;


    public SimpleGoBackN()
    {
        m_ports = new Hashtable();
    }

    /**
     Return a new agent that can be used to open a connection from a
     specific source port. This basically takes care of setting up the
     agent, attaching it to this class, etc. You can just go ahead and send
     data afterwards (after connecting, etc.)
     @param local_port the port at which to open the agent. If you use a
     duplicate port you will get a simulator error displayed.
     */
    public SimpleGoBackNAgent createNewAgent(int local_port)
    {
        SimpleGoBackNAgent newagent = new SimpleGoBackNAgent(local_port);

        attach(newagent, local_port);

        return newagent;
    }


    /**
     Attach a higher level agent to this multiplexer. Only SimpleGoBackNAgent
     objects may be attached.
     @param higher_level an instance of SimpleGoBackNAgent
     @param unique_id the port number of the agent.
     */
    public void attach(Agent higher_level, int unique_id)
    {
        // Check for right agent type
        if(!(higher_level instanceof SimpleGoBackNAgent))
            Simulator.error("SimpleGoBackN can only attach SimpleGoBackNAgent");

        // Check for duplicate port assignments
        if(m_ports.get(new Integer(unique_id)) != null)
            Simulator.error("SGN can only attach one agent per port");

        higher_level.attach(this);

        m_ports.put(new Integer(unique_id), new AgentPort(higher_level));
    }


    /**
     Attach callback for lower level agents. SimpleGoBackN can only run on
     top of IP.
     @param lower_level an instance of IPHandler
     */
    public void attach(Agent lower_level)
    {

        // Check for correct lower-level agent (IP)

        if(!(lower_level instanceof IPHandler))
        {
            System.out.println(lower_level);
            Simulator.error("SGN can only run on top of IP!");
        }

        m_ip = (CL_Agent) lower_level;
    }


    /**
     Indicate an event to SGN.
     @see jns.agent.Agent
     */
    public void indicate(int status, Object indicator)
    {
        if(status == Agent.READY_TO_SEND)
        {
            for(Enumeration e = m_ports.elements(); e.hasMoreElements();)
                ((AgentPort) e.nextElement()).agent.indicate(Agent.READY_TO_SEND,
                                                             this);
        }
        else if(status == Agent.PACKET_AVAILABLE)
        {

            // Get a packet from the IP handler

            IPPacket ippacket = (IPPacket) ((IPHandler) indicator).read(Protocols.SGN);

            // Do a check if this is the right packet type

            if(!(ippacket.data instanceof SimpleGoBackNPacket))
                Simulator.warning("IP packet passed to SGN with wrong packet content");

            // Find the right agent listening to the destination port of this
            // packet

            SimpleGoBackNPacket packet = (SimpleGoBackNPacket) ippacket.data;
            AgentPort agentport =
                    (AgentPort) m_ports.get(new Integer(packet.destination_port));

            if(agentport == null)
            {
                Simulator.warning("SGN Packet sent to port noone's listening to");
                return;
            }

            // Put the packet in that port's receive queue and indicate

            agentport.packets.pushFront(ippacket);
            agentport.agent.indicate(Agent.PACKET_AVAILABLE, this);
        }
    }

    /**
     This agent will always accept packets from the higher level and buffer
     them.
     */
    public boolean canSend(IPAddr destination, int length)
    {
        return true;
    }


    /**
     The send function is used by SimpleGoBackNAgent objects to send data
     (and noone else, it's internal to this protocol).
     @param data an instance of SimpleGoBackNPacket.
     */
    public void send(IPAddr source, IPAddr dest, int length, Object data,
                     int unique_id)
    {
        m_ip.send(source, dest, length, data, Protocols.SGN);
    }


    public Object read(int unique_id)
    {
        AgentPort port = (AgentPort) m_ports.get(new Integer(unique_id));
        if(port == null)
            Simulator.error("SGN: Confused; someone who's not attached is reading" +
                            "from a port");

        Object packet = port.packets.peekBack();
        port.packets.popBack();

        return packet;
    }
}


class AgentPort
{

    // The higher-level agent attached to this port
    public Agent agent;

    // Queue of packets waiting for collection
    public Queue packets;

    public AgentPort(Agent a)
    {
        agent = a;
        packets = new Queue();
    }
}


