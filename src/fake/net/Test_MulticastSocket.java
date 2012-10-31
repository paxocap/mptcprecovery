/**
 * Test_MulticastSocket.java
 *
 *
 *
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */
package fake.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Test_MulticastSocket implements Runnable
{

    private MulticastSocket m_socket = null;
    private boolean m_initiator = false;

    public Test_MulticastSocket(String addr, boolean initiator) throws Exception
    {
        m_socket = new MulticastSocket(3778,addr);
        m_socket.setInterface(InetAddress.getByName(addr));
        m_socket.joinGroup(InetAddress.getByName("224.0.0.1"));
        m_initiator = initiator;

    }


    public void run()
    {
        try
        {
            if(m_initiator)
            {
                //Send a multicast message..
                byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
                DatagramPacket toBeSent = new DatagramPacket(data, data.length, InetAddress.getByName("224.0.0.1"), 3778);
                m_socket.send(toBeSent);
                System.out.println("Sent1");

                //...and receive from self..
                byte[] buf = new byte[8192];
                DatagramPacket received = new DatagramPacket(buf, 8192);
                m_socket.receive(received);
                System.out.println("Received1");

                //..from node 2..
                byte[] buf2 = new byte[8192];
                DatagramPacket received2 = new DatagramPacket(buf2, 8192);
                m_socket.receive(received2);
                System.out.println("Received2");


                //..from node 3..
                byte[] buf3 = new byte[8192];
                DatagramPacket received3 = new DatagramPacket(buf3, 8192);
                m_socket.receive(received3);
                System.out.println("Received3");


                //..and node 4
                 byte[] buf4 = new byte[8192];
                DatagramPacket received4 = new DatagramPacket(buf4, 8192);
                m_socket.receive(received4);
                System.out.println("Received4");


                byte[] data2 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
                DatagramPacket toBeSent2 = new DatagramPacket(data2, data2.length, InetAddress.getByName("224.224.224.224"), 3778);
                m_socket.send(toBeSent2);
                System.out.println("Sent close");

                m_socket.close();
            }
            else
            {
                byte[] buf = new byte[8192];
                DatagramPacket received = new DatagramPacket(buf, 8192);
                m_socket.receive(received);
                byte[] data = received.getData();
                DatagramPacket toBeSent = new DatagramPacket(data, data.length, InetAddress.getByName("224.0.0.1"), 3778);
                m_socket.send(toBeSent);
                System.out.println("Sent packet back");

                /*  byte[] buf2 = new byte[8192];
                  DatagramPacket received2 = new DatagramPacket(buf2, 8192);
                  m_socket.receive(received2);
                  byte[] data2 = received2.getData();
                  DatagramPacket toBeSent2 = new DatagramPacket(data2, data2.length, InetAddress.getByName("192.168.0.1"), 3778);
                  m_socket.send(toBeSent2);
                  System.out.println("Sent packet back");*/
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void main(String[] args)
    {

        try
        {
            Test_MulticastSocket test1 = new Test_MulticastSocket("192.168.0.1", true);
            Test_MulticastSocket test2 = new Test_MulticastSocket("192.168.0.2", false);
            Test_MulticastSocket test3 = new Test_MulticastSocket("192.168.0.3", false);
             Test_MulticastSocket test4 = new Test_MulticastSocket("192.168.0.4", false);
            new Thread(test2).start();
            new Thread(test1).start();
            new Thread(test4).start();
            new Thread(test3).start();
            System.out.println("Started all tests");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

//    DatagramPacket datagram = new DatagramPacket(packet.getBytes(), packet.getBytes().length, m_multicastAddress, Preferences.portNo);
//    m_multicastSocket.send(datagram);
//
//    byte[] buf = new byte[Preferences.maxPacketSize];
//    datagram = new DatagramPacket(buf, Preferences.maxPacketSize);
//    m_multicastSocket.receive(datagram);
}

