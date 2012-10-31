package jns.element;


public abstract class Link extends Element
{


    /**
     Set the status of this link, either 'up' or 'down'. Use the integer
     constants provided in jns.util.Status.
     @param status the new status of the link
     */
    public abstract void setStatus(int status);

    /**
     Return the status of this link, either 'up' or 'down'. Returns one of
     the constans provided in jns.util.Status.
     @return Status.UP or Status.DOWN.
     */
    public abstract int getStatus();

    /**
     Return the bandwidth of this link in bits per second (bps);
     */
    public abstract int getBandwidth();

    /**
     Return the delay of this link in seconds (This will normally be a
     fraction though).
     */
    public abstract double getDelay();

    public abstract Interface getIncomingInterface();

    public abstract Interface getOutgoingInterface();
}

