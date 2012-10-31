package jns.trace;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;


public abstract class Traceable
{

    /**
     List of Trace objects that are listening to us.
     */
    Vector m_traces = null;

    public Traceable()
    {
        m_traces = new Vector();
    }

    public void attach(Trace trace)
    {
        m_traces.addElement(trace);
    }

    public void detach(Trace trace)
    {
    }


    /**
     Used by subclasses to send events to a Trace object. This gets rid of
     lots of if/else code by making the decision if anyone is listening
     in here.
     */
    protected void sendEvent(Event event)
    {
        Enumeration e = m_traces.elements();
        while(e.hasMoreElements())
        {
            Trace trace = (Trace) e.nextElement();

            try
            {
                trace.handleEvent(event);
            }
            catch(IOException xept)
            {
                System.out.println("ERROR: An I/O exception occured while writing" +
                                   " an event!");
            }
        }


    }


}
