/*
TODO:
   - Data transfer
   - Timeouts, timeouts, timeouts..
 */
package jns.agent;

import jns.Simulator;
import jns.command.Command;
import jns.element.IPPacket;
import jns.util.IPAddr;

import java.util.Vector;

/**
 This is a (reasonably) simple protocol implementation that uses a
 'go-back-n' strategy for providing a error-correcting connection-oriented
 service. If an acknowledgement does not arrive in time, all packets from
 that ACK on are sent again. This particular implementation actually
 provides a two-way service, data transfer in both directions.
 <p>
 In order to handle connection setup and teardown properly, I chose to use
 the TCP finite state machine. The state machine is implemented by having
 the current state in a variable and acting depending on the value of this
 variable in the different functions.
 <p>
 For an overview of the TCP finite state machine, please refer to the
 "Internetworking with TCP/IP" book series. You can find the diagram both
 in Volume I ("Principles, Protocols and Architectures") and Volume II
 ("Design, Implementation and Internals"). In any case, every good TCP/IP
 book should have the finite state machine in it.
 <p>

 There are some <b>missing features</b>:
 <ul>
 <li>No timeouts. If packets are lost, this protocol will go awry. This is
 because there was no time to finish the class
 <li>The same is true for out-of-sequence delivery. Things will go awry.
 <li>The window size is hardcoded to four.
 </ul>

 <p>
 Nevertheless, this protocol should give you a clue as to how agents can be
 written in JNS.
 */
public class SimpleGoBackNAgent implements CO_Agent
{

    // Define constants for the states this protocol can be in. This defines
    // the TCP finite state machine

    // Waiting in idleness...
    private final static int CLOSED = 1;

    // Passive open, waiting for SYNs (in TCP speak anyway..)
    private final static int LISTEN = 2;

    // Active open. We have just sent a SYN and are waiting for a SYN+ACK.
    private final static int SYN_SENT = 3;

    // In passive mode, if we receive a SYN we go to this state.
    private final static int SYN_RECVD = 4;

    // We are connected if we are in this state.
    private final static int ESTABLISHED = 5;

    // If we receive a FIN in the ESTABLISHED state we go to this state
    private final static int CLOSE_WAIT = 6;

    // After leaving CLOSE_WAIT, we send an ACK and go to this state. If we
    // receive it, we leave this state and go to CLOSED again.
    private final static int LAST_ACK = 7;

    // When we are actively ending a connection, we send a FIN and go from
    // ESTABLISHED (or SYN_RECVD) to this state.
    private final static int FIN_WAIT1 = 8;

    // If we receive an ACK in FIN_WAIT1, we go to FIN_WAIT2.
    private final static int FIN_WAIT2 = 9;

    // If we received a FIN in FIN_WAIT1, we send an ACK and go to this state
    private final static int CLOSING = 10;

    // From CLOSING, if we get an ACK and from FIN_WAIT2 if we get a FIN, we
    // go to this state. We wait in there for a while and go back to CLOSED.
    private final static int TIME_WAIT = 11;

    // The local port of this connection and the remote port
    private int m_local_port;
    private int m_remote_port;

    // Our IP address and the remote end IP address
    private IPAddr m_local_ip;
    private IPAddr m_remote_ip;

    // The agent we are using for sending packets.
    private SimpleGoBackN m_sgn;

    // The current state we are in, one of the constants above.
    private int m_state;

    // The agent that is using us to send packets
    private Agent m_user;

    // Current data sequence number
    private int m_seq_data;

    // Current acknowledgement sequence number
    private int m_seq_ack;

    private Vector m_send_array;
    private Vector m_recv_array;

    private int m_window_pos;
    private int m_window_size;

    /**
     The only constructor, creates a new agent at a specific local port. This
     constructor is private to the outside world. If you want a new agent,
     use jns.agent.SimpleGoBackN.createNewAgent. This function will also
     take care of attaching everything correctly.
     */
    protected SimpleGoBackNAgent(int port)
    {
        m_local_port = port;
        m_remote_port = 0;
        m_sgn = null;
        m_user = null;
        m_local_ip = m_remote_ip = null;
        m_state = CLOSED;
        m_seq_data = 0;
        m_seq_ack = 0;

        m_send_array = new Vector();
        m_recv_array = new Vector();

        m_window_pos = 0;
        m_window_size = 4;
    }


    /**
     Attach an application layer agent to this agent.
     @param higher_level the agent to attach
     @param unique_id an id that will identify the agent
     @see jns.agent.Agent
     */
    public void attach(Agent higher_level, int unique_id)
    {
        m_user = higher_level;

        m_user.attach(this);
    }


    /**
     Callback function by which lower-level agents attach themselves to this
     agent.
     @see jns.agent.Agent
     */
    public void attach(Agent lower_level)
    {
        if(!(lower_level instanceof SimpleGoBackN))
            Simulator.error("You may attach SimpleGoBackNAgent only to " +
                            "SimpleGoBackN");

        m_sgn = (SimpleGoBackN) lower_level;
    }


    /**
     Set up a connection with a remote host. All the details can be found
     in the CO_Agent class. Calling this function initiates a three-way
     handshake. Note that it takes some time to do the handshake.
     @see jns.agent.CO_Agent
     */
    public void connect(IPAddr source, IPAddr destination,
                        int destination_port)
    {
        if(m_state != CLOSED)
        {
            Simulator.warning("Call to SGN connect() ignored because the protocol" +
                              " was not in the idle state");
            return;
        }

        // Set up our connection variables

        m_local_ip = source;
        m_remote_ip = destination;
        m_remote_port = destination_port;

        Simulator.verbose("            SGN sending SYN");

        // Send a SYN packet to the destination

        sendPacket(SimpleGoBackNPacket.SYN, m_seq_data, 0);

        // Go to the new state, SYN_SENT

        m_state = SYN_SENT;
    }


    /**
     Break up a connection. This causes FIN packets to be sent and
     connection-teardown to be initialised. Note that it will take some
     time to tear down a connection.
     */
    public void disconnect()
    {

        // Act according to the current state
        Simulator.verbose("            disconnecting");

        switch(m_state)
        {
            case LISTEN:
            case SYN_SENT:
                m_state = CLOSED;
                break;

            case CLOSE_WAIT:
                m_state = LAST_ACK;
                sendPacket(SimpleGoBackNPacket.FIN, m_seq_data,
                           0);
                break;

            case ESTABLISHED:
            case SYN_RECVD:
                m_state = FIN_WAIT1;
                sendPacket(SimpleGoBackNPacket.FIN, m_seq_data, 0);
                break;

            default:
                Simulator.warning("Disconnect already in progress " +
                                  "when called in SGN");
        }
    }


    /**
     Start listening at a specific IP address. The port to listen at has
     already been specified when the agent was created.
     @param local_ip the IP address to listen at
     @see jns.agent.CO_Agent
     */
    public void listen(IPAddr local_ip)
    {
        if(m_state != CLOSED)
        {
            Simulator.warning("Call to SGN listen() ignored because the protocol " +
                              "was not in the idle state");
            return;
        }

        Simulator.verbose("            SGN listening");

        m_local_ip = local_ip;

        // This one is easy...

        m_state = LISTEN;
    }


    public void send(int length, Object data, int unique_id)
    {
        if(m_state != ESTABLISHED)
        {
            Simulator.warning("Tried to send a packet using SGN without setting " +
                              "up a connection first. Packet LOST!");
            return;
        }

        // Create a properly filled out packet
        SimpleGoBackNPacket packet = new SimpleGoBackNPacket();

        // Todo: set sequence numbers+acks properly
        packet.source_port = m_local_port;
        packet.destination_port = m_local_port;
        packet.sequence = m_seq_data++;
        packet.ack = 0;
        packet.flags = 0;
        packet.length = SimpleGoBackNPacket.HEADER_SIZE + length;
        packet.data = data;

        // Put the packet in the send queue
        m_send_array.addElement(packet);
        //      m_sgn.send(m_local_ip,m_remote_ip,length,packet,m_local_port);

        Simulator.getInstance().schedule(new SGNUpdateCommand(
                Simulator.getInstance().getTime() + 0.0001, this));
    }


    public Object read(int unique_id)
    {
        return null;
    }


    /**
     This is an agent that serves the application layer. We will thus always
     allow packets to be sent for convenience and buffer them ourselves.
     @param destination ignored
     @param length ignored
     @return true
     */
    public boolean canSend(IPAddr destination, int length)
    {
        return true;
    }


    /**
     Indicate an event to this agent.
     @see jns.agent.Agent
     */
    public void indicate(int status, Object indicator)
    {
        if(status == Agent.READY_TO_SEND)
        {
            //	System.out.println("         SGN RTS");

            if(m_send_array.size() > 0 && m_window_pos < m_window_size &&
                    m_window_pos < m_send_array.size())
            {
                Simulator.getInstance().schedule(new SGNUpdateCommand(
                        Simulator.getInstance().getTime() + 0.0001, this));
            }
        }
        else if(status == Agent.PACKET_AVAILABLE)
        {

            // Get a packet from the multiplexer

            IPPacket ippacket = (IPPacket) m_sgn.read(m_local_port);
            SimpleGoBackNPacket packet = (SimpleGoBackNPacket) ippacket.data;

            // Treat the packet according to the current state. Note that it is
            // not a terribly good use of ones time to try to understand this
            // piece of code without having the chart of the tcp finite state
            // machine within sight.

            switch(m_state)
            {
                case CLOSED:
                    Simulator.warning("SGN received a packet in the closed" +
                                      " state. Ignored.");
                    break;

                case LISTEN:
                    if(packet.flags == SimpleGoBackNPacket.SYN)
                    {
                        // Extract the port & IP of the sender
                        m_remote_port = packet.source_port;
                        m_remote_ip = ippacket.source;

                        Simulator.verbose("            SGN got SYN in listen");
                        sendPacket(SimpleGoBackNPacket.SYN |
                                   SimpleGoBackNPacket.ACK,
                                   m_seq_data, packet.sequence);
                        m_state = SYN_RECVD;
                    }
                    break;

                case SYN_SENT:
                    if(packet.flags == SimpleGoBackNPacket.SYN)
                    {
                        sendPacket(SimpleGoBackNPacket.SYN |
                                   SimpleGoBackNPacket.ACK, m_seq_data,
                                   packet.sequence);
                        m_state = SYN_RECVD;
                    }
                    else if(packet.flags == (SimpleGoBackNPacket.SYN |
                            SimpleGoBackNPacket.ACK))
                    {
                        sendPacket(SimpleGoBackNPacket.ACK, m_seq_data,
                                   packet.sequence);
                        m_state = ESTABLISHED;
                        m_user.indicate(Agent.CONNECTION_ESTABLISHED, this);
                        Simulator.verbose("            SGN connection established");
                    }
                    break;

                case SYN_RECVD:
                    if(packet.flags == SimpleGoBackNPacket.ACK)
                    {
                        m_state = ESTABLISHED;
                        m_user.indicate(Agent.CONNECTION_ESTABLISHED, this);
                        Simulator.verbose("            SGN connection established");
                    }
                    break;

                case ESTABLISHED:
                    // If it's a FIN packet, send an ACK and go to the next
                    // state.

                    if(packet.flags == SimpleGoBackNPacket.FIN)
                    {
                        sendPacket(SimpleGoBackNPacket.ACK, m_seq_data, 0);
                        m_state = CLOSE_WAIT;
                    }

                    // If it's an ACK for a packet we sent, move the window

                    if(packet.flags == SimpleGoBackNPacket.ACK &&
                            m_send_array.size() > 0)
                    {
                        if(packet.ack == ((SimpleGoBackNPacket)
                                m_send_array.elementAt(0)).sequence)
                        {
                            m_send_array.removeElementAt(0);

                            if(m_window_pos > 0)
                                m_window_pos--;
                        }
                    }

                    // Otherwise we just received some data. Send back an
                    // ACK and stick it in the receive queue

                    if(packet.data != null)
                    {
                        sendPacket(SimpleGoBackNPacket.ACK, m_seq_data,
                                   packet.sequence);
                        m_recv_array.addElement(packet);
                    }
                    break;

                case CLOSE_WAIT:
                    sendPacket(SimpleGoBackNPacket.FIN, m_seq_data, 0);
                    m_state = LAST_ACK;
                    break;

                case LAST_ACK:
                    if(packet.flags == SimpleGoBackNPacket.ACK)
                        m_state = CLOSED;
                    break;

                case FIN_WAIT1:
                    if(packet.flags == SimpleGoBackNPacket.FIN)
                    {
                        sendPacket(SimpleGoBackNPacket.ACK, m_seq_data,
                                   packet.sequence);
                        m_state = CLOSING;
                    }
                    else if(packet.flags == SimpleGoBackNPacket.ACK)
                    {
                        m_state = FIN_WAIT2;
                    }
                    else if(packet.flags == (SimpleGoBackNPacket.FIN |
                            SimpleGoBackNPacket.ACK))
                    {
                        sendPacket(SimpleGoBackNPacket.ACK, m_seq_data,
                                   packet.sequence);
                        m_state = TIME_WAIT;
                    }
                    break;

                case FIN_WAIT2:
                    if(packet.flags == SimpleGoBackNPacket.FIN)
                        m_state = TIME_WAIT;
                    break;

                case CLOSING:
                    if(packet.flags == SimpleGoBackNPacket.ACK)
                        m_state = TIME_WAIT;
                    break;
            }

        }
    }


    /**
     Internal function to which calls are scheduled with the simulator.
     On each call, update will try to send a packet (if the window size
     and ACKs permit it)
     */
    protected void update()
    {
        if(m_send_array.size() > 0 && m_window_pos < m_window_size &&
                m_window_pos < m_send_array.size())
        {
            SimpleGoBackNPacket packet =
                    (SimpleGoBackNPacket) m_send_array.elementAt(m_window_pos);

            m_sgn.send(m_local_ip, m_remote_ip, packet.length, packet, m_local_port);
            m_window_pos++;
        }
    }

    // ------------- Convenience functions to make life easy -----------

    /**
     sendPacket is used to send control packets (SYN, etc.). Those packets
     contain no data.
     */
    private void sendPacket(int flags, int seq, int ack)
    {

        // Create a properly filled out packet
        SimpleGoBackNPacket packet = new SimpleGoBackNPacket();

        // Todo: set sequence numbers+acks properly
        packet.source_port = m_local_port;
        packet.destination_port = m_remote_port;
        packet.sequence = seq;
        packet.ack = ack;
        packet.flags = flags;
        packet.length = SimpleGoBackNPacket.HEADER_SIZE;
        packet.data = null;

        // Send the packet
        m_sgn.send(m_local_ip, m_remote_ip, packet.length, packet, m_local_port);
    }


}


/**
 A command class that will simply call the update() function of the
 SimpleGoBackNAgent at a specific time.
 */
class SGNUpdateCommand extends Command
{
    SimpleGoBackNAgent m_agent;

    public SGNUpdateCommand(double time, SimpleGoBackNAgent agent)
    {
        super("SGN update", time);
        m_agent = agent;
    }

    public void execute()
    {
        m_agent.update();
    }
}
