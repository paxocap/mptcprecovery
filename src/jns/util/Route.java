package jns.util;

import jns.element.Interface;

public class Route
{

    private IPAddr m_dest;
    private IPAddr m_netmask;
    private Interface m_iface;

    private int m_ttl;
    private Object m_metric;

    public Route(IPAddr dest, IPAddr netmask, Interface iface)
    {
        m_dest = dest;
        m_netmask = netmask;
        m_iface = iface;
        m_ttl = 0;
        m_metric = null;
    }

    /**
     Does this route lead to the target IP address?
     @param target the target IP address
     */
    public boolean match(IPAddr target)
    {
       /**
        * This isn't right for routing within a sub network.
        * return ((target.getIntegerAddress() & m_netmask.getIntegerAddress()) ==
        *       (m_dest.getIntegerAddress() & m_netmask.getIntegerAddress()));
        */
        return (target.getIntegerAddress() == m_dest.getIntegerAddress());
    }

    public IPAddr getDestination()
    {
        return m_dest;
    }

    public IPAddr getNetmask()
    {
        return m_netmask;
    }

    public Interface getInterface()
    {
        return m_iface;
    }

    public Object getMetric()
    {
        return m_metric;
    }

    public void setMetric(Object metric)
    {
        m_metric = metric;
    }

    public int getTTL()
    {
        return m_ttl;
    }

    public void setTTL(int ttl)
    {
        m_ttl = ttl;
    }

    public void decrementTTL(int amount)
    {
        m_ttl -= amount;
        if(m_ttl < 0) m_ttl = 0;
    }

    public boolean hasTimedOut()
    {
        return (m_ttl == 0);
    }

}





