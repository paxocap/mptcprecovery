package jns.util;

import java.util.Enumeration;

public class Queue
{

    QueueItem m_first,m_last;
    int m_size;


    public Queue()
    {
        m_first = m_last = null;
        m_size = 0;
    }

    public void pushFront(Object object)
    {
        QueueItem item = new QueueItem(object);

        if(m_first == null)
        {
            m_first = m_last = item;
        }
        else
        {
            item.next = m_first;
            m_first.prev = item;
            m_first = item;
        }

        m_size++;
    }

    public void pushBack(Object object)
    {
        QueueItem item = new QueueItem(object);

        if(m_last == null)
        {
            m_first = m_last = item;
        }
        else
        {
            item.prev = m_last;
            m_last.next = item;
            m_last = item;
        }

        m_size++;
    }

    public Object peekFront()
    {
        if(m_first == null) return null;
        return m_first.object;
    }

    public Object peekBack()
    {
        if(m_last == null) return null;
        return m_last.object;
    }

    public void popFront()
    {
        if(m_first != null)
        {
            m_size--;

            m_first = m_first.next;
            if(m_first == null) m_last = null;
        }
    }

    public void popBack()
    {
        if(m_last != null)
        {
            m_size--;

            m_last = m_last.prev;
            if(m_last == null) m_first = null;
        }
    }

    public int size()
    {
        return m_size;
    }

    public Enumeration elements()
    {
        return new QueueEnumeration(this);
    }

}


class QueueItem
{
    public QueueItem next,prev;
    public Object object;

    public QueueItem(Object object)
    {
        this.object = object;
        this.next = null;
        this.prev = null;
    }

}


class QueueEnumeration implements Enumeration
{

    private QueueItem current;

    public QueueEnumeration(Queue q)
    {
        current = q.m_first;
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
            current = current.next;
            return object;
        }
        else
            return null;
    }
}

