package jns.util;

import java.net.InetAddress;

/**
 A simple representation of an IP address (that is IPv4).
 */
public class IPAddr
{

    // The whole IP address as an integer
    private int m_addr;

    /**
     Create an IP address from the four byte parameters. They are integers
     but should be in range 0-255, otherwise they will be clipped. The
     address provided as (a1,a2,a3,a4) will be turned into an IP address
     of the form a1.a2.a3.a4.
     */
    public IPAddr(int a1, int a2, int a3, int a4)
    {
        m_addr = ((a1 & 0xff) << 24) | ((a2 & 0xff) << 16) | ((a3 & 0xff) << 8) | (a4 & 0xff);
    }


    /**
     Create a new IP address by taking a previously created raw IP
     address.
     */
    public IPAddr(int addr)
    {
        m_addr = addr;
    }

    /**
     * Create a new IP address from the java.net.InetAddress class
     *
     * @author Einar Vollset <einar.vollset@ncl.ac.uk>
     */
    public IPAddr(InetAddress addr)
    {
        byte[] a = addr.getAddress();
        m_addr = ((a[0] & 0xff) << 24) | ((a[1] & 0xff) << 16) | ((a[2] & 0xff) << 8) | (a[3] & 0xff);
    }

    /**
     Make a copy of an existing IP address.
     */
    public IPAddr(IPAddr other)
    {
        m_addr = other.m_addr;
    }


    /**
     Compare for equality.
     */
    public boolean equals(IPAddr other)
    {
        return m_addr == other.m_addr;
    }


    /**
     Return the raw representation of this IP address.
     */
    public int getIntegerAddress()
    {
        return m_addr;
    }



    public String toString()
    {
        return ((m_addr >> 24) & 0xff) + "." +
                ((m_addr >> 16) & 0xff) + "." +
                ((m_addr >> 8) & 0xff) + "." +
                (m_addr & 0xff);
    }

    /**
     * Added function to detect multicast addresses.
     */
    public boolean isMulticastAddress()
    {
        if(((m_addr >> 24) & 0xff) >= 224 && ((m_addr >> 24) & 0xff) < 240)
        {
            return true;
        }
        return false;
    }

}
