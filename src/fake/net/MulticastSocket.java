/**
 * MulticastSocket.java
 *
 *
 *
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */
package fake.net;

import jns.dynamic.DynamicScheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;


/**
 * This is the "fake" socket which can be substituted a java.net.MulticastSocket.
 * Instead of sending and receiving messages onto a real network, this fake socket will
 * connect to and schedule sends and call receives on the JNS 1.7 network simulator.
 *
 * In order to use the fake.net.MulticastSocket in your code, replace all occurences of
 * "import java.net.MulticastSocket" with "fake.net.MulticastSocket".
 *
 * And change all calls to the MulticastSocket contructor to be MulticastSocket(port, InternetAddress)
 * where InternetAddress is the host which the socket should pretend to be. (port is some
 * bogus value).
 *
 * Then you need to start the DynamicScheduler with the correct network simulation information.
 * (See the documentation that comes with JNS-1.7 on how to do this).
 *
 * Finally, change fake.net.Preferences, to allow SERVER_HOST_NAME and SERVER_PORT_NO
 * to point to where the dynamic scheduler is running.
 *
 * After this, your code should run as normal (unless you're using some bits of the MulticastSocket
 * API that the fake version doesn't support).
 *
 * NOTE: If you use bits of the MulticastSocket API that I haven't yet implemented, then calling the
 *       method will cause an error message and System.exit(-1). Annoying, yes, but I don't have the
 *       time to emulate all the behaviour of a MulticastSocket, so I am implementing only the
 *       bits I need.
 *       Feel free to implement the missing features of this class...
 */
public class MulticastSocket extends java.net.MulticastSocket
{

    private DynamicScheduler m_scheduler = null;
    private InetAddress m_address = null;
    private InetAddress m_mcastGroup = null;
    private int m_port;

    /**
     * Create a multicast socket.
     *
     * In the fake.net implementation, this looks up the DynamicScheduler in RMI
     *
     */
    public MulticastSocket(int port, String ipAddress) throws IOException
    {
        try
        {
            Registry reg = LocateRegistry.getRegistry(3778);
            m_scheduler = (DynamicScheduler)reg.lookup("DynamicScheduler");
            m_address = InetAddress.getByName(ipAddress);
        }
        catch(NotBoundException e)
        {
            System.err.println("DynamicScheduler not bound on rmi://" + Preferences.SERVER_HOST_NAME + ":" + Preferences.SERVER_PORT_NO + "/DynamicScheduler");
            System.exit(-1);
        }
        catch(RemoteException e)
        {
            System.err.println("Remote exception caught trying to create multicast socket: "+ e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        m_port = port;

    }


    /**
     * Returns the string "fake.net.MulticastSocket"
     */
    public String toString()
    {
        return "fake.net.MulticastSocket";
    }


    /**
     * In fake.net.MulticastSocket, this schedules a send event with the
     * JNS simulator
     *
     * Note: This will currently also allow unicasts, just set the address in the DatagramPacket
     *       to be a unicast address instead.
     */
    public void send(DatagramPacket p) throws IOException
    {

        //Check if the multicast socket has been closed.
        if(m_scheduler == null)
        {
            throw new IOException("Socket is closed.");
        }

        //Check if the receiver is a multicast address, if not, then throw an IOException.
        InetAddress receiver = p.getAddress();

        //Get the data from the datagram packet
        byte[] data = p.getData();
        if(receiver.isMulticastAddress())
        {
          //scheduler the multicast
            m_scheduler.scheduleMulticast(m_address, receiver, data);
        }
        else
        {
            //schedule the unicast
            m_scheduler.scheduleUnicast(m_address, receiver, data);
        }

    }


    /**
     * In fake.net.MulticastSocket. This function calls the receive function
     * on the Dynamic Scheduler
     */
    public synchronized void receive(DatagramPacket p) throws IOException
    {
        if(m_scheduler == null)
        {
            throw new IOException("Socket is closed.");
        }
        try
        {
            byte[] data  = m_scheduler.receive(m_address);
            p.setData(data);
            p.setLength(data.length);
        }
        catch(RemoteException e)
        {
            throw new IOException("Exception doing receive on DynamicScheduler. Exception: " + e.getMessage());
        }
    }

    /**
     * In fake.net.MulticastSocket, this simply sets the local variable.
     */
    public void joinGroup(InetAddress mcastaddr) throws IOException
    {
        m_mcastGroup = mcastaddr;
    }


    /**
     * In fake.net.MulticastSocket, this simply sets the local variable to null.
     */
    public void leaveGroup(InetAddress mcastaddr) throws IOException
    {
        m_mcastGroup = null;
    }


    /**
     * In fake.net.MulticastSocket, this simply sets the local variable.
     */
    public void setInterface(InetAddress inf) throws SocketException
    {
        m_address = inf;
    }


    /**
     * In fake.net.MulticastSocket, this simply sets the local variable.
     */
    public InetAddress getInterface() throws SocketException
    {
        return m_address;
    }

    /**
     * Returns the port number on the local host to which this socket is bound.
     *
     * In fake.net.MulticastSocket, this simply returns the local variable
     */
    public int getLocalPort()
    {
        return m_port;
    }

    /**
     * Closes this datagram socket.
     *
     * In fake.net.MulticastSocket, this sets the scheduler reference to null
     */
    public void close()
    {
        try
        {
            m_scheduler.stop();
        }
        catch(RemoteException e)
        {
            //ah well. ignoring... :-)
        }
        m_scheduler = null;
    }



    //----------------------------------------------------------------------------------
    // NOT IMPLEMENTED FUNCTIONALITY THAT NEEDS TO BE OVERRIDDEN WITH AN ERROR MESSAGE
    //----------------------------------------------------------------------------------

    public synchronized int getReceiveBufferSize()
            throws SocketException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return -1;
    }


    public MulticastSocket() throws IOException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }


    public void connect(InetAddress address, int port)
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }


    public void setTTL(byte ttl) throws IOException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }


    public void disconnect()
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }


    public void setTimeToLive(int ttl) throws IOException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }

    public InetAddress getInetAddress()
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return null;
    }


    public byte getTTL() throws IOException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return (byte) 0;
    }


    public int getPort()
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return 0;
    }


    public int getTimeToLive() throws IOException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return 0;
    }


    public InetAddress getLocalAddress()
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return null;
    }


    public void send(DatagramPacket p, byte ttl)
            throws IOException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }


    public synchronized void setSoTimeout(int timeout) throws SocketException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }


    public synchronized int getSoTimeout() throws SocketException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return 0;
    }


    public synchronized void setSendBufferSize(int size)
            throws SocketException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }


    public synchronized int getSendBufferSize() throws SocketException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
        return 0;
    }


    public synchronized void setReceiveBufferSize(int size)
            throws SocketException
    {
        System.err.println("Operation not currently supported in fake.net.MulticastSocket..");
        System.exit(-1);
    }
}
