package jns.command;

import jns.element.Link;


/**
 LinkStateCommand can be used to schedule a change to the state of a link
 at any time. Links can be taken down or brought up like this.
 */
public class LinkStateCommand extends Command
{

    private Link m_link;
    private int m_state;

    /**
     @param state either Status.UP or Status.DOWN, as defined in
     jns.util.Status.
     */
    public LinkStateCommand(double time, Link link, int state)
    {
        super("LinkState", time);
        m_link = link;
        m_state = state;
    }


    public void execute()
    {
        m_link.setStatus(m_state);
    }

}
