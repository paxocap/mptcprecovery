
package jns.trace;

import jns.Simulator;

import java.util.Vector;

public class Event
{

    // The event name, e.g. 'NodeEvent'
    private String m_name;

    // The time at which the event occured
    private double m_time;

    // The parameters of the event, a vector of 'EventParameter' objects
    private Vector m_parameters;


    public Event(String name)
    {
        m_name = name;
        m_time = Simulator.getInstance().getTime();
        m_parameters = new Vector();
    }

    public Event(String name, double time)
    {
        m_name = name;
        m_time = time;
        m_parameters = new Vector();
    }

    public void addParameter(String name, Object value)
    {
        m_parameters.addElement(new EventParameter(name, value));
    }

    public void addParameter(EventParameter p)
    {
        m_parameters.addElement(p);
    }

    public void setTime(double time)
    {
        m_time = time;
    }

    public double getTime()
    {
        return m_time;
    }

    public String getName()
    {
        return m_name;
    }

    public EventParameter getParameter(int index)
    {
        if(index < m_parameters.size())
            return (EventParameter) m_parameters.elementAt(index);
        return null;
    }
}
