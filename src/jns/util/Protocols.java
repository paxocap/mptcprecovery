package jns.util;

/**
 This class defines a unique id for every protocol above IP. Those
 identifiers are used when determining what higher-level protocol to give
 a packet to.
 If you write your own, add it here.
 */
public class Protocols
{

    public static final int TCP = 1;

    public static final int UDP = 2;

    // 'Simple go-back-n'
    public static final int SGN = 3;

    //The "pseduo protocol" used with the dynamic scheduler
    public static final int DYNAMIC_SCHEDULER = 4;
    
    public static final int MPTCP = 4;

}
