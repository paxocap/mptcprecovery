package jns.trace;

import java.io.IOException;

/**
 Trace defines an abstract class that can be extended to implement a class
 that can receive messages from the network simulator and convert it to
 human readable (or machine readable) format.
 An example of this would be a trace file output class.
 @see JavisTrace
 */
public abstract class Trace
{

    /**
     Attach to a Traceable object. The Trace class does not actually
     contain a list of objects and every Traceable might have many Trace
     classes attached. So we just 'attach ourselves' to the traceable
     instead.
     @param t The Traceable object to attach to
     */
    public void attach(Traceable t)
    {
        t.attach(this);
    }


    /**
     Detach from a Traceable object. This will forward the call to the detach
     method of the Traceable.
     @param t the Traceable to detach from.
     */
    public void detach(Traceable t)
    {
        t.detach(this);
    }


    /**
     Write the leading section of the output that should appear before the
     actual trace data. Very useful if a given file-format has to be used.
     */
    public abstract void writePreamble() throws IOException;


    /**
     Handle an event. The way an event from the simulator is handled depends
     entirely on the subclass. It could be added to a statistic our just
     be displayed on the screen.
     */
    public abstract void handleEvent(Event e) throws IOException;

    /**
     Write any trailing output that has to be written. Useful if a given
     file-format has to be used.
     */
    public abstract void writePostamble() throws IOException;
}
