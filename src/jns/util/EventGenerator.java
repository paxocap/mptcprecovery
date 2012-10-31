package jns.util;

import jns.element.IPPacket;
import jns.trace.Event;


/**
 EventGenerator provides a number of static utility functions that help with
 generating different kinds of events by filling in the common bits.
 */
public class EventGenerator
{


    /**
     makePacketEvent will create a packet event of type 'name', where name
     is HopEvent, ReceiveEvent, etc. from the IP packet that is passed.
     @param name the event name of the packet event to be generated, e.g.
     "HopEvent"
     @param packet the IP packet from which to take event information
     @return a new packet event of the desired type.
     */
    public static Event makePacketEvent(String name, IPPacket packet)
    {

        Event event = new Event(name);

        event.addParameter("Source Address", packet.source);
        event.addParameter("Destination Address", packet.destination);
        event.addParameter("Source Hop", packet.source_hop);
        event.addParameter("Destination Hop", packet.destination_hop);
        event.addParameter("Packet ID", new Integer(packet.id));
        event.addParameter("Packet Length", new Integer(packet.length));
        event.addParameter("Packet Protocol",
                           new Integer(packet.protocol));

        return event;
    }


}
