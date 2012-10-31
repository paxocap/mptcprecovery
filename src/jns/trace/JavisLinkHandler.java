
package jns.trace;

import jns.util.IPAddr;
import jns.util.Status;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Hashtable;


public class JavisLinkHandler extends JavisHandler
{

    public JavisLinkHandler()
    {
        m_parameter = new Hashtable();

        m_parameter.put("Source Address", new Integer(0));
        m_parameter.put("Destination Address", new Integer(1));
        m_parameter.put("State", new Integer(2));
    }

    public void handleEvent(Event e, BufferedWriter w) throws IOException
    {

        int index = 0;
        w.write("l -t " + e.getTime());

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
                        switch(((Integer) parameter.value).intValue())
                        {
                            case Status.UP:
                                w.write(" -S UP ");
                                break;
                            case Status.DOWN:
                                w.write(" -S DOWN ");
                                break;
                        }
                        break;
                }

            index++;
            parameter = e.getParameter(index);
        }

        w.newLine();
    }

}
