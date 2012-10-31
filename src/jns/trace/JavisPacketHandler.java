
package jns.trace;

import jns.util.IPAddr;
import jns.util.Protocols;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Hashtable;


public class JavisPacketHandler extends JavisHandler
{


    /**
     When a new Handler is created a hashtable with all the types of
     parameters that can occur in this event is added. The key is the
     name of the parameter and the value an integer that is later used
     in a switch statement thus enabling an appropriate response to each
     parameter.
     */
    public JavisPacketHandler()
    {

        m_parameter = new Hashtable();

        m_parameter.put("Source Hop", new Integer(0));
        m_parameter.put("Destination Hop", new Integer(1));
        m_parameter.put("Packet ID", new Integer(2));
        m_parameter.put("Packet Protocol", new Integer(3));
        m_parameter.put("Packet Length", new Integer(4));
        m_parameter.put("Queue Length", new Integer(5));
    }

    /**
     Looks at the event and outputs a line of text to the trace file
     describing the event in Javis format
     */
    public void handleEvent(Event e, BufferedWriter w) throws IOException
    {

        if(e.getName().equals("HopEvent"))
            w.write("h ");
        else if(e.getName().equals("ReceiveEvent"))
            w.write("r ");
        else if(e.getName().equals("EnqueueEvent"))
            w.write("+ ");
        else if(e.getName().equals("DequeueEvent"))
            w.write("- ");
        else if(e.getName().equals("QueueDropEvent"))
            w.write("d ");
        else if(e.getName().equals("LinkDropEvent")) w.write("d ");

        int index = 0;
        w.write("-t " + e.getTime());

        EventParameter parameter = e.getParameter(index);
        while(parameter != null)
        {
            Integer id = (Integer) m_parameter.get(parameter.name);

            if(id != null)
                switch(id.intValue())
                {
                    case 0:
                        w.write(" -s " + translateIP((IPAddr) parameter.value));
                        break;

                    case 1:
                        w.write(" -d " + translateIP((IPAddr) parameter.value));
                        break;

                    case 2:
                        w.write(" -i " + (Integer) parameter.value);
                        break;

                    case 3:
                        w.write(" -p ");
                        switch(((Integer) parameter.value).intValue())
                        {
                            case Protocols.TCP:
                                w.write("tcp");
                                break;
                            case Protocols.UDP:
                                w.write("udp");
                                break;
                            case Protocols.SGN:
                                w.write("sgn");
                                break;
                            default:
                                w.write(((Integer) parameter.value).intValue());
                        }
                        break;

                    case 4:
                        w.write(" -e " + (Integer) parameter.value);
                        break;
                }

            index++;
            parameter = e.getParameter(index);
        }

        w.write(" -a 0 ");
        w.write(" -c 0 ");

        w.newLine();
    }


}




