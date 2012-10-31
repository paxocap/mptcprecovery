/**
 * PacketSender.java
 *
 * 
 * 
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */
package jns.dynamic;

import jns.command.Command;
import jns.element.IPHandler;
import jns.util.IPAddr;
import jns.util.Protocols;

/**
 * Very simple generice jns.command.Command that will send an UDP/IP packet from the node who's
 * IPHandler gets passed in, to the receiverIPAddress node.
 * The data in the packet is the data parameter to the constructor.
 */
public class PacketSender extends Command
{

    private IPHandler m_ip = null;
    private byte[] m_data = null;
    private IPAddr m_receiverIPAddr = null;

    public PacketSender(IPHandler handler, IPAddr receiverIPAddress, double time, byte[] data)
    {
        super("PacketSender", time);
        m_ip = handler;
        m_data = data;
        m_receiverIPAddr = receiverIPAddress;
    }

    public void execute()
    {
        m_ip.send(m_ip.getAddress(), m_receiverIPAddr, m_data.length, m_data, Protocols.UDP);
    }
}