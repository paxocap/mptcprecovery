
package jns.element;

import jns.Simulator;
import jns.agent.Agent;
import jns.util.IPAddr;
import jns.util.Preferences;


/**
 Interface represents a generic network interface in JNS. It cannot be used
 directly. Rather than that, the subclasses SimplexInterface and
 DuplexInterface should be used.
 */
public abstract class Interface extends Element implements Agent
{

    // Constants to be used with m_direction. They may be OR'd together
    public final static int RECEIVER = 1;
    public final static int SENDER = 2;

    protected IPAddr m_addr;            // IP address of this interface
    protected int m_bandwidth;          // Bandwidth
    protected IPHandler m_handler;      // The IP handler that uses this iface
    protected int m_mtu;                // Maximum Transfer Unit in bytes
    protected Node m_node;              // The node this is attached to


    /**
     This constructor will generate a new interface with all values set to
     default. Bandwidth will be zero and the MTU taken from the Preferences
     class.
     @param addr the IP address of this interface
     @see jns.util.Preferences
     */
    public Interface(IPAddr addr)
    {
        m_addr = addr;
        m_bandwidth = 0;
        m_mtu = Preferences.default_iface_MTU;
        m_handler = null;
        m_node = null;
    }


    /**
     This constructor will generate a new interface with all values set to
     default and with the bandwidth set from the parameter.
     @param addr the IP address of this interface
     @param bandwidth the bandwidth this interface will have
     @see jns.util.Preferences
     */
    public Interface(IPAddr addr, int bandwidth)
    {
        m_addr = addr;
        m_bandwidth = bandwidth;
        m_mtu = Preferences.default_iface_MTU;
        m_handler = null;
        m_node = null;
    }


    /**
     Attach a node to this interface. This is really just a callback because
     normally interfaces are attached to nodes and not this way round.
     @param node the node to attach to
     */
    public abstract void attach(Node node);

    /**
     Attach a link to this interface. The bandwidth of the link can be
     inherited optionally.
     @param link the link to attach to this interface
     @param inheritBandwidth if true, this interface's bandwidth will be
     overridden by the link's.
     */
    public abstract void attach(Link link, boolean inheritBandwidth);


    public abstract void attach(Queue queue);

    public abstract void send(IPPacket packet);

    public abstract Object read(int unique_id);

    /**
     getType returns the type of this interface, i.e. SENDER or RECEIVER,
     or both.
     */
    public abstract int getType();


    public void setIPHandler(IPHandler handler)
    {
        m_handler = handler;
    }


    public IPAddr getIPAddr()
    {
        return m_addr;
    }


    public int getBandwidth()
    {
        return m_bandwidth;
    }


    public abstract Node getNode();

    public void setMTU(int mtu)
    {
        if(mtu < 0) Simulator.error("Tryed to set an MTU smaller than zero!");
        if(mtu < 16)
            Simulator.warning("You are setting an MTU smaller than the " +
                              "standard IP header");
        m_mtu = mtu;
    }

    /**
     Return the maximum transfer unit of this interface in bytes.
     (That's BYTES, not bits, careful).
     */
    public int getMTU()
    {
        return m_mtu;
    }


    public void attach(Agent higher_level, int unique_id)
    {
        Simulator.error("Only IP can attach to interfaces");
    }

    public void attach(Agent lower_level)
    {
        Simulator.error("Only IP can attach to interfaces");
    }


	public abstract boolean isLinkDown() ;

}
