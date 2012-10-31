package jns.util;

import java.util.Enumeration;


/**
 PriorityQueue implements.. a priority queue. Items can be inserted together
 with a priority value. A smaller priority value equals a higher priority.
 When an object is popped off the end of a queue, it will be the one that
 has the smallest priority value (and thus the highest priority).
 */
public class PriorityQueue
{

    PriorityQueueItem m_first,m_last;
    int m_size;


    public PriorityQueue()
    {
        m_first = m_last = null;
        m_size = 0;
    }

    public void push(Object object, double priority)
    {
        PriorityQueueItem item = new PriorityQueueItem(object, priority);

        if(m_first == null)
        {
            m_first = m_last = item;
        }
        else
        {
            PriorityQueueItem run = m_first;

            while(run != null && run.priority > priority)
                run = run.next;

            // Item is the next last item
            if(run == null)
            {
                item.prev = m_last;
                m_last.next = item;
                m_last = item;
            }
            else
            {
                item.prev = run.prev;
                item.next = run;

                // Item is the new first item
                if(run.prev == null)
                {
                    m_first = item;
                    run.prev = item;
                }
                else
                {
                    run.prev.next = item;
                    run.prev = item;
                }
            }
        }

        m_size++;
    }

    public Object peek()
    {
        if(m_last == null) return null;
        return m_last.object;
    }

    public void pop()
    {
        if(m_last != null)
        {
            m_size--;

            // Item was also the first
            if(m_last.prev == null)
            {
                m_first = m_last = null;
            }
            else
            {
                m_last = m_last.prev;
                m_last.next = null;
            }
        }
    }

    public int size()
    {
        return m_size;
    }

    public Enumeration elements()
    {
        return new PriorityQueueEnumeration(this);
    }

}


class PriorityQueueItem
{
    public PriorityQueueItem next,prev;
    public Object object;
    public double priority;

    public PriorityQueueItem(Object object, double priority)
    {
        this.object = object;
        this.priority = priority;
        this.next = null;
        this.prev = null;
    }

}


class PriorityQueueEnumeration implements Enumeration
{

    private PriorityQueueItem current;

    public PriorityQueueEnumeration(PriorityQueue q)
    {
        current = q.m_last;
    }

    public boolean hasMoreElements()
    {
        return (current != null);
    }

    public Object nextElement()
    {
        if(current != null)
        {
            Object object = current.object;
            current = current.prev;
            return object;
        }
        else
            return null;
    }
}

