package jns.agent;

/**
 SimpleGoBackNPacket implements the packets that are sent by the
 SimpleGoBackN class. Packets of this type are put inside IP packets for
 transfer.
 */
public class MPTCPPacket
{

    // Header size in bytes. 16 sounds like a good number
    public final static int HEADER_SIZE = 16;

    // Connection setup and teardown flags
    public final static int SYN = 1;
    public final static int ACK = 2;
    public final static int FIN = 4;

    // Source port number
    public int source_port;

    // Destination port number
    public int destination_port;

    // Sequence number
    public int sequence;

    // ACK number
    public int ack;

    // Total length of this packet in bytes (including header)
    public int length;

    public double pktTime; //ak
    // Flags consisting of the constants defined above (for connection
    // setup and teardown)
    public int flags;

    public Object data;


    public MPTCPPacket()
    {
        source_port = destination_port = 0;
        sequence = ack = 0;
        flags = 0;
        data = null;
    }
}
