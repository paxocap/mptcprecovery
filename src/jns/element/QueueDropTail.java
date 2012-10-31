
package jns.element;

import jns.trace.Event;
import jns.trace.EventParameter;
import jns.util.EventGenerator;

/**
 QueueDropTail implements a very simple drop-tail queue with the following
 behaviour.
 <ul><li>If a packet gets enqueue and is bigger than the maximum queue size,
 it is rejected.
 <li>If a packet gets enqueued, is smaller than the maximum queue size, but
 there is not enough space for it at the moment, packets at the back of
 the queue get dropped until there is enough space. The packet then gets
 enqueued in the front of the queue.
 <li>If there is enough space for a packet, it simply gets enqueue in the
 front of the queue.
 </ul>
 */
public class QueueDropTail extends Queue
{

    private jns.util.Queue m_packets;


    /**
     Generate a new drop-tail queue with a maximum size given in bytes. If
     the queue exceeds this maximum size, packets will be dropped.
     @param maxlength the maximum queue length in <b>bytes</b>
     */
    public QueueDropTail(int maxlength)
    {
        super(maxlength);

        m_packets = new jns.util.Queue();
    }


    /**
     Enqueue a packet in this queue. If the packet is too big to fit in as
     a whole, it will not be enqueued. If the packet does fit in, but there
     is not enough space, packets will be dropped at the end of the queue
     before enqueueing it and a number of drop events will be generated.
     <p>If the packet does fit in, it simply goes in the front of the queue.
     @param packet the IP packet to enqueue
     */
    public void enqueue(IPPacket packet)
    {

        // If the queue is not infinite, check if the packet fits

        if(m_maxlength != 0)
        {

            // Packet is bigger than the whole queue, drop it
            if(packet.length > m_maxlength)
            {
            }

            // Queue is too full for this packet, start dropping from the back
            while(m_curlength + packet.length > m_maxlength)
            {
                drop();
            }
        }

        if(m_maxlength == 0 || m_curlength + packet.length < m_maxlength)
        {
            m_packets.pushFront(packet);
            m_curlength += packet.length;
        }

        // Send an enqueue event

        Event event = EventGenerator.makePacketEvent("EnqueueEvent", packet);
        event.addParameter(new EventParameter("Queue Length",
                                              new Integer(m_curlength)));
        sendEvent(event);
    }


    /**
     A private helper function that will drop the packet at the end of the
     queue and generate a drop event.
     */
    private void drop()
    {

        // Remove the packet from the queue and subtract its size from the queue
        // length

        IPPacket packet = (IPPacket) m_packets.peekBack();
        m_packets.popBack();

        m_curlength -= packet.length;

        // Send packet drop event

        Event event = EventGenerator.makePacketEvent("QueueDropEvent", packet);
        event.addParameter(new EventParameter("Queue Length",
                                              new Integer(m_curlength)));
        sendEvent(event);
    }


    /**
     Dequeue a packet from this queue. The queue policy is that the packet
     at the end of the queue will be returned. In addition, a dequeue event
     will be generated.
     @return the packet at the end of the queue or <b>null</b> if the queue
     is empty.
     */
    public IPPacket dequeue()
    {
        if(m_packets.size() != 0)
        {

            // Dequeue a packet and decrease queue length

            IPPacket packet = (IPPacket) m_packets.peekBack();
            m_packets.popBack();

            m_curlength -= packet.length;

            // Send a dequeue event

            Event event = EventGenerator.makePacketEvent("DequeueEvent", packet);
            event.addParameter(new EventParameter("Queue Length",
                                                  new Integer(m_curlength)));
            sendEvent(event);

            // Return packet

            return packet;
        }
        else
            return null;
    }


    public void dump()
    {
        System.out.println("Drop Tail Queue");
    }


    /**
     Do nothing. In a more sophisticated queue we could schedule clean-up
     runs to take place in here...
     */
    public void update()
    {
    }

}

