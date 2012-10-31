package jns.util;


public class Preferences
{


    public static String VERSION = "1.7";

    /**
     Default maximum queue length for interfaces in bytes.
     */
    public static int default_maxqueuelength = 100000;

    // Amount of time it takes the IP handler to put a packet in an iface
    public static double delay_ip_to_ifacequeue = 0.000001;

    // Amount of time it takes the IP handler to read a packet from an iface
    public static double delay_ifacequeue_to_ip = 0.000001;

    // The timeout of the interface before it retries to put a packet on the
    // link
    public static double delay_iface_send_update = 0.001;

    // Flag if the interface that is first added to an IP handler in a node
    // will be used as a default route automatically
    public static boolean first_iface_is_default_route = false;

    public static final int default_iface_MTU = 1500;

    // Time interval by which a collection of fragments has to be complete,
    // otherwise they all get thrown away
    public static final double ip_fragmentation_timeout = 1.0;

    public static final boolean verbose = false;
}
