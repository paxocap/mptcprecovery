
package jns.trace;

import jns.Simulator;
import jns.element.Node;
import jns.util.IPAddr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 JavisHandler is an abstract superclass for every class that wants to handle
 events and output information about them in javis/nam trace-file format.
 */
public abstract class JavisHandler
{

    protected Hashtable m_parameter;

    public JavisHandler()
    {

    }

    /**
     Do the actual event handling.
     @param e the event to handle
     @param w where to write the information
     */
    public abstract void handleEvent(Event e, BufferedWriter w) throws IOException;

    /**
     translateIP is used by subclasses to translate JNS's IP addresses to
     Javis'/NAM's inferior node-numbering model.
     @param addr the IP address to translate
     @return a node number
     */
    protected static int translateIP(IPAddr addr)
    {
        for(Enumeration e = Simulator.getInstance().enumerateElements();
            e.hasMoreElements();)
        {
            Object curelement = e.nextElement();
            if(curelement instanceof Node)
            {
                Node curnode = (Node) curelement;
                if(curnode.hasIPAddress(addr)) return curnode.getNumber();
            }
        }
        return 0;
    }

}
