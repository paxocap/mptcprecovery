package jns.dynamic;

/**
 * dynamic.DynamicSchedulerImpl.java
 *
 * This class allows for simple creation of a network of interconnected JNS nodes
 * with duplex links between them. It also allows for scheduling events dynamically
 * with the JNS simulator, through RMI calls. The main purpose of this class is to
 * be an RMI gateway for the fake.net.MulticastSocket classes used to test java implementations
 * of network protocols.
 *
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */

import jns.Simulator;
import jns.agent.Agent;
import jns.command.StopCommand;
import jns.element.*;
import jns.trace.JavisTrace;
import jns.trace.Trace;
import jns.util.IPAddr;
import jns.util.Protocols;
import jns.util.Queue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Set;

/**
 * This is the main class used to dynamically schedule events to the JNS
 * network simulator.
 * It will start the *modified* simulator, and then any event that gets
 * passed to it will be scheduled with it.
 * This will allow (hopefully) for "real" implementations of network protocols
 * to be simulated for correctness.
 *
 * To use this class, you need to add the nodes and links to the scheduler in the
 * main() function below, alternatively you can write your own main()..
 * (Remember to rmic the class!)
 *
 * Beware! This class has a narrow functionality, as I have implemented it for
 * my own purposes... :-D
 *
 *
 */
public class DynamicSchedulerImpl extends UnicastRemoteObject implements DynamicScheduler, Agent
{

    /**
     * This is a reference to the JNS simulator instance. This needs to
     * get passed to the
     */
    private Simulator m_simulator = null;

    /**
     * This is the Trace used to write the JVS file for use with Javis (or NAM)
     */
    private Trace m_trace = null;

    /**
     * This is a hashmap of message queues mapped onto the IP address of the
     * node which sent it.
     */
    private HashMap m_msgQueues = null;

    /**
     * This is a hashmap of the nodes in the simulator
     */
    private HashMap m_nodes = null;

    /**
     * The bandwidth of the links in this simulation
     */
    private int m_bandwidth;

    /**
     * The error rate of the links in this simulation
     */
    private double m_errorRate;

    /**
     * The link delay in this simulation
     */
    private double m_delay;

    /**
     * This holds a hashmap of all the links created.
     */
    private HashMap m_links = null;

    /**
     * This is the subnet mask used.
     */
    private IPAddr m_subnetMask = null;

    /**
     * This is the time the simulator was started
     */
    private double m_startTime;

    /**
     * This determines if the simulation has started, in which no more nodes can be
     * added to the simulation run
     */
    private boolean m_started = false;


    /**
     * Constructs a new dynamic.DynamicSchedulerImpl
     *
     * @param nameOfRun The name of this simulation run. Gives name to the JVS file.
     * @param linkBandwidth The bandwidth of all the links between the nodes (could change this to set it for each link added)
     * @param linkDelay The link delay of all the links between the nodes (could change this to set it for each link added)
     * @param linkErrorRate The link error rate of all the links between the nodes (could change this to set it for each link added)
     * @param subnetMask The subnet mask of all the routes given to the nodes (could change this to set it for each link added)
     */
    public DynamicSchedulerImpl(String nameOfRun, int linkBandwidth, double linkDelay, double linkErrorRate, IPAddr subnetMask) throws RemoteException
    {
        m_simulator = Simulator.getInstance();
        try
        {
            m_trace = new JavisTrace(nameOfRun + ".jvs");
        }
        catch(IOException e)
        {
            System.err.println("Could not create " + nameOfRun + ".jvs!");
            e.printStackTrace();
            System.exit(-1);
        }
        m_simulator.setTrace(m_trace);
        m_msgQueues = new HashMap();
        m_nodes = new HashMap();
        m_bandwidth = linkBandwidth;
        m_delay = linkDelay;
        m_errorRate = linkErrorRate;
        m_links = new HashMap();
        m_subnetMask = subnetMask;


    }

    /**
     * Adds a node to the simulator. The IP address is used as both
     * name and IP address.
     * This method can currently not be called remotely
     */
    public void addNode(IPAddr IPAddress)
    {
        if(m_started)
        {
            System.err.println("Can't add nodes after the simulation has started..");
            return;
        }
        //Create the node
        Node node = new Node(IPAddress.toString());
        //Attach to the simulator
        m_simulator.attachWithTrace(node, m_trace);
        //Create a queue of messages destined for the external node
        Queue incoming = new Queue();
        //Add the queue to the hashmap of incoming quueues
        m_msgQueues.put(IPAddress.toString(), incoming);
        //Attach the dynamic Scheduler (this) as the "higher level agent", so messages
        //can be passed up to it.
        node.attach(this, Protocols.UDP);
         //Add to hashmap of nodes
        m_nodes.put(IPAddress.toString(), node);

    }

    /**
     * Adds a link between the two nodes. Beware that this is a
     * two way link, so there is no need to add a link "the other way"
     * This method can currently not be called remotely
     *
     * @param nodeA The IP address of the first node to connect
     * @param nodeB The IP address of the second node to connect
     */
    public void addLink(IPAddr nodeA, IPAddr nodeB)
    {

        String linkName = nodeA.toString() + "-" + nodeB.toString();
        String invLinkName = nodeB.toString() + "-" + nodeA.toString();
        Node a = (Node) m_nodes.get(nodeA.toString());
        Node b = (Node) m_nodes.get(nodeB.toString());

        //First check that the nodes are there
        if(a == null || b == null)
        {
            System.err.println("Trying to add a link between two nodes (" + linkName + "), one or more which does not exits.");
            return;
        }

        //Now check that the link does not already exist.
        if(m_links.get(linkName) != null || m_links.get(invLinkName) != null)
        {
            System.err.println("Duplicate link: " + linkName + ". Link not added.");
            return;
        }

        //Need to create an interface on either node.
        Interface a2b = new DuplexInterface(nodeA);
        a.attach(a2b);
        m_simulator.attachWithTrace(a2b, m_trace);

        Interface b2a = new DuplexInterface(nodeB);
        b.attach(b2a);
        m_simulator.attachWithTrace(b2a, m_trace);

        //Then create the link
        Link link = new DuplexLink(m_bandwidth, m_delay, m_errorRate);

        //Attach the link to either interface
        a2b.attach(link, true);
        b2a.attach(link, true);

        //attach the link to the simulator
        m_simulator.attachWithTrace(link, m_trace);

        //Add the link to m_links
        m_links.put(linkName, link);

        //And finally add route between the two.
        a.addRoute(nodeB, m_subnetMask, a2b);
        b.addRoute(nodeA, m_subnetMask, b2a);


    }


    /**
     * This method schedules a sending of a given packet of data (an object) from
     * one node to the other. The sending is scheduled to happen at
     * (System.currentTimeMillis() - Time of start of simulator)
     */
    public synchronized void scheduleUnicast(InetAddress senderIPAddress, InetAddress receiverIPAddress, byte[] data) throws RemoteException
    {

        IPAddr senderIPAddr = new IPAddr(senderIPAddress);
        IPAddr receiverIPAddr = new IPAddr(receiverIPAddress);

        // Firsy check that the two nodes exist in the simulator..
        Node sender = (Node) m_nodes.get(senderIPAddr.toString());
        Node receiver = (Node) m_nodes.get(receiverIPAddr.toString());

        if(sender == null)
        {
            System.err.println("Trying to send a packet FROM a non-existant node: " + senderIPAddr);
            return;
        }
        if(receiver == null)
        {
            System.err.println("Trying to send a packet TO a non-existant node: " + receiverIPAddr);
            return;
        }

        //Determine the time to schedule
        double execTime = (System.currentTimeMillis() - m_startTime) / 1000.00;
        //Create the necessary Command object
        PacketSender sendEvent = new PacketSender(sender.getIPHandler(), receiverIPAddr, execTime, data);

        m_simulator.schedule(sendEvent);
    }

    /**
     * Schedules a multicast send on the simulator
     */
    public synchronized void scheduleMulticast(InetAddress senderIPAddress, InetAddress multicastGrp, byte[] data) throws RemoteException
   {
        IPAddr senderIPAddr = new IPAddr(senderIPAddress);
        IPAddr multicastGroup = new IPAddr(multicastGrp);

        // First check that the sender node exists in the simulator
        Node sender = (Node) m_nodes.get(senderIPAddr.toString());
        if(sender == null)
        {
            System.err.println("Trying to multicast a packet FROM a non-existant node: " + senderIPAddr);
            return;
        }
        //Could check that the multicastGroup is a multicast address? Todo perhaps...

        //Determine the time to schedule
        double execTime = (System.currentTimeMillis() - m_startTime) / 1000.00;

        //Create the necessary Command object
        PacketSender sendEvent = new PacketSender(sender.getIPHandler(), multicastGroup, execTime, data);

        m_simulator.schedule(sendEvent);
    }

    /**
     * Receives the next message for the IPAddr indicated, will block if
     * untill there is a message available
     *
     * TODO: return more information that might be required to properly set
     *       a datagram.
     */
    public byte[] receive(InetAddress receiverIPaddr) throws RemoteException
    {
        IPAddr recv = new IPAddr(receiverIPaddr);
        Queue incoming = (Queue) m_msgQueues.get(recv.toString());
        IPPacket ipPacket = null;
        synchronized(incoming)
        {
            try
            {
                while(incoming.size() == 0)
                {
                    incoming.wait();
                }
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
            ipPacket = (IPPacket) incoming.peekFront();
            incoming.popFront();
        }

        byte[] data = (byte[])ipPacket.data;

        return data;

    }

    /**
     * This method will stop the simulator.
     * TODO: Change this so the simulator does not stop untill all the participants have
     *       called stop()
     */
    public void stop() throws RemoteException
    {
        //Determine the time to schedule
        double execTime = (System.currentTimeMillis() - m_startTime) / 1000.00;
        m_simulator.schedule(new StopCommand(execTime));
    }

    /**
     * This method will start the simulator. This is called by the main() method in this class.
     */
    public synchronized void start()
    {
        new Thread(m_simulator).start();
        m_startTime = (double) System.currentTimeMillis();
        m_started = true;
    }

    //-----------------------------------------
    // The Agent interface functions below here
    //-----------------------------------------
    /**
     * Normally, this would attach a higher level agent to this agent interface. However, as
     * the dynamic scheduler propagates all its packets to external clients, trying to
     * attach a higher level agent does not make any sense.
     * So calling this function will result in a Simulator error.
     *
     */
    public void attach(Agent higher_level, int unique_id)
    {
        Simulator.error("A higher level agent tried to attach itself to the dynamic scheduler. This makes no sense. See documentation.");
    }

    /**
     *
     *   Attach a lower-level agent to this agent. This function is used as a
     *   callback. When a higher-level agent attaches itself to a lower level
     *   agent, the lower level agent will attach itself to the higher-level
     *   agent using this function.<br>
     *   The purpose of this is that the higher-level agent will implement this
     *   function in order to store some reference to the lower-level service
     *   which it can use to give packets to it.<br>
     *   Do NOT call this function as a user of the agent, only agents between
     *   themselves need to use it.
     *
     *   In the dynamic scheduler, this function gets called by the node's
     *   IPHandler when the the dynamic scheduler gets attached to the
     *   IPHandler. This is so that packets can be passed up to the dynamic
     *   scheduler
     *
     * @param lower_level the lower level agent to attach to this agent.
     */
    public void attach(Agent lower_level)
    {
        //Does nothing, as I have a reference to all the IPHandlers through
        //the m_nodes hashmap.
    }

    /**
     Indicate to this agent that a packet is waiting for collection. This
     is used by lower-level agents to indicate to higher-level agents.
     Note that higher-level agents may ignore any of the requests!
     @param status one of the constants defined above.
     @param indicator the object that is indicating, normally an Agent, but
     could be an Interface for example.
     */
    public void indicate(int status, Object indicator)
    {
        if(status == Agent.PACKET_AVAILABLE)
        {
            //It will be a node's IPHandler
            if(indicator instanceof IPHandler)
            {
                //Get the handler
                IPHandler ipHandler = (IPHandler) indicator;
                //Get the IP packet
                IPPacket packet = (IPPacket) ipHandler.read(Protocols.UDP);
                //Determine the destination
                IPAddr addr = ipHandler.getAddress();

                //Add the packet, needs to be synchronized.
                Queue incoming = (Queue) m_msgQueues.get(addr.toString());
                synchronized(incoming)
                {
                    incoming.pushBack(packet);
                    //Need to notify, so that if anyone waiting to receive, will know
                    //it has been updated....
                    incoming.notifyAll();

                }
            }
            else
            {
                Simulator.error("Something other than an IPHandler called indicate() on the DynamicScheduler!");
            }
        }

    }

    /**
     Query this agent if a packet of a given length could be sent. You must
     not call the 'send' function of any agent before calling this function.
     There are two possibilities:
     <ul>
     <li>The function returns 'true' - Go ahead and call 'send'
     <li>The function returns 'false' - In this case you are guaranteed to
     get a READY_TO_SEND indication by the lower level agent you are using.
     Do not try to send any packets but wait for a call to your indicate
     function with that flag set. When you do get that call, you <b>must</b>
     call canSend() again because someone else might take their turn before
     you!
     </ul>
     @param destination where to send the packet to
     @param length the length of the data to be sent
     */
    public boolean canSend(IPAddr destination, int length)
    {
        return false;
    }
    //--------------------------------
    // The main() function below here
    //--------------------------------


    /**
     * This function is an example of how to use the DynamicScheduler.
     */
    public static void main(String[] args)
    {

        try
        {
            //Create the DynamicSchedulerImpl
            DynamicSchedulerImpl scheduler = new DynamicSchedulerImpl("mySimulationRun", 500000, 0.008, 0.0, new IPAddr(255, 255, 255, 0));

            //Add nodes to this simulation run
            scheduler.addNode(new IPAddr(192, 168, 0, 1));
            scheduler.addNode(new IPAddr(192, 168, 0, 2));
            scheduler.addNode(new IPAddr(192, 168, 0, 3));
            /*scheduler.addNode(new IPAddr(192, 168, 0, 4));
            scheduler.addNode(new IPAddr(192, 168, 0, 5));
            scheduler.addNode(new IPAddr(192, 168, 0, 6));
            scheduler.addNode(new IPAddr(192, 168, 0, 7));
            scheduler.addNode(new IPAddr(192, 168, 0, 8));
            scheduler.addNode(new IPAddr(192, 168, 0, 9)); */
            scheduler.addLink(new IPAddr(192, 168, 0, 1), new IPAddr(192, 168, 0, 2));
            /*scheduler.addLink(new IPAddr(192, 168, 0, 1), new IPAddr(192, 168, 0, 3));
            scheduler.addLink(new IPAddr(192, 168, 0, 1), new IPAddr(192, 168, 0, 4));
            scheduler.addLink(new IPAddr(192, 168, 0, 2), new IPAddr(192, 168, 0, 4));
            scheduler.addLink(new IPAddr(192, 168, 0, 3), new IPAddr(192, 168, 0, 4));
            scheduler.addLink(new IPAddr(192, 168, 0, 3), new IPAddr(192, 168, 0, 6));
            scheduler.addLink(new IPAddr(192, 168, 0, 4), new IPAddr(192, 168, 0, 5));
            scheduler.addLink(new IPAddr(192, 168, 0, 4), new IPAddr(192, 168, 0, 6));
            scheduler.addLink(new IPAddr(192, 168, 0, 4), new IPAddr(192, 168, 0, 8));
            scheduler.addLink(new IPAddr(192, 168, 0, 5), new IPAddr(192, 168, 0, 8));
            scheduler.addLink(new IPAddr(192, 168, 0, 5), new IPAddr(192, 168, 0, 9));
            scheduler.addLink(new IPAddr(192, 168, 0, 6), new IPAddr(192, 168, 0, 8));
            scheduler.addLink(new IPAddr(192, 168, 0, 6), new IPAddr(192, 168, 0, 7));
            scheduler.addLink(new IPAddr(192, 168, 0, 8), new IPAddr(192, 168, 0, 7));
            scheduler.addLink(new IPAddr(192, 168, 0, 8), new IPAddr(192, 168, 0, 9));
            //scheduler.addLink(new IPAddr(192, 168, 0, 3), new IPAddr(192, 168, 0, 4));  */
             scheduler.addLink(new IPAddr(192, 168, 0, 2), new IPAddr(192, 168, 0, 3));
            //start the simulation run
            scheduler.start();
            //register the simulation run in the RMIRegistry
            Registry reg = LocateRegistry.createRegistry(3778);
            reg.rebind("DynamicScheduler", scheduler);
            System.out.println("The DynamicScheduler has started.");

        }
        catch(RemoteException e)
        {
            e.printStackTrace();
        }

    }
}