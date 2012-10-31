package jns.element;

import jns.util.IPAddr;


/**
 IPPacket implements a standard IP packet, leaving away some unnecessary
 things. Almost all the header fields are present.<br>
 In order to understand this class completely, you need a reference book
 next to you.
 */
public class IPPacket
{

    // Header size of the packet in bytes
    public final static int HEADER_SIZE = 20;

    // Flags used in fragmentation
    public final static int FLAG_DO_NOT_FRAGMENT = 1;
    public final static int FLAG_MORE_FRAGMENTS = 2;

    public final static int OPTION_SECURITY = 2;
    public final static int OPTION_LOOSE_SOURCE_ROUTING = 3;
    public final static int OPTION_RECORD_ROUTE = 7;
    public final static int OPTION_STRICT_SOURCE_ROUTING = 9;
    public final static int OPTION_TIMESTAMP = 68;


    // Version of IP
    public int version;

    // Source and destination IP address
    public IPAddr source;
    public IPAddr destination;

    // The next two represent the IP "Service Type" field

    // Priority (0-7, 7 highest)
    public int precedence;

    // requested_service represents the bits (D,T,R). It is 1 for 'low delay',
    // 2 for 'high throughput' and 4 for 'reliability'. Feel free to OR them.
    public int requested_service;

    // Length of the complete packet
    public int length;

    // Unique packet id
    public int id;

    // Flags (for fragmentation)
    public int flags;

    // Offset of this fragment in the complete packet (given in units of 8
    // bytes, i.e. has to be multiplied *8 before use)
    public int fragment_offset;

    // Time to Live. In seconds, but not quite.
    public int ttl;

    // ID of the higher level protocol
    public int protocol;

    // crc stands for 'crc is corrupt'
    public boolean crc;

    // The next three are IP option flags

    // copy_options specifies whether the options should be copied into all
    // fragments if fragmentation occurs or just the first fragment
    public boolean copy_options;

    // option contains a combination of the OPTION constants defined
    // above. Note that this combines the IP 'option class' and 'option number'
    // into one variable;
    public int option;

    // The data carried by this packet. It does not have to be of size 'length'
    // and is intended for internal purposes
    public Object data;


    /*-----------------------------------------------------------------
        The following are not part of the standard IP header but are
	included for convenience and tracing purposes */

    public IPAddr source_hop;      // The current source (i.e. not original
    // source but last/this hop)

    public IPAddr destination_hop; // Current (i.e. not ultimate but next hop)
    // destination

    /*----------------------------------------------------------------*/

    public boolean NEG_ACK;
    public IPPacket()
    {
        version = 4;
        precedence = 0;
        requested_service = 0;
        length = 0;
        crc = true;
        id = 0;
        flags = 0;
        fragment_offset = 0;
        ttl = 0;
        protocol = 0;
        copy_options = false;
        option = 0;
        data = null;
        source_hop = new IPAddr(0, 0, 0, 0);
        destination_hop = new IPAddr(0, 0, 0, 0);
        NEG_ACK = false;
    }

    public IPPacket(IPPacket other)
    {
        source = new IPAddr(other.source);
        destination = new IPAddr(other.destination);
        source_hop = new IPAddr(other.source_hop);
        destination_hop = new IPAddr(other.destination_hop);
        version = other.version;
        precedence = other.precedence;
        requested_service = other.requested_service;
        length = other.length;
        crc = other.crc;
        id = other.id;
        flags = other.flags;
        fragment_offset = other.fragment_offset;
        ttl = other.ttl;
        protocol = other.protocol;
        copy_options = other.copy_options;
        option = other.option;
        data = other.data;
        NEG_ACK = other.NEG_ACK;
    }
}
