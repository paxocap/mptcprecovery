package jns.agent;

import jns.Simulator;
import jns.command.*;
import jns.command.Command;
import jns.util.IPAddr;


/**
 RandomSource is an application layer agent that can run on top of a
 connection-oriented transport layer protocol (like SGN, TCP, ...). It is
 used in conjunction with RandomSink and sends data of random length at
 random time intervals.<br>
 It communicates at port 80.
 */
public class RandomSink implements Agent
{

    CO_Agent m_transport;
    IPAddr m_local_ip;
    IPAddr m_remote_ip;

    // Start and stop time
    double m_start;
    double m_end;

    /**
     Create a new random sink that will start listening for incoming
     connections at some point and stop listening at some other point.
     @param ip the ip address of this sink
     @param start_time the time at which to start listening
     @param stop_time the time at which to stop listening
     */
    public RandomSink(IPAddr ip, double start_time,
                      double stop_time)
    {
        m_local_ip = ip;
        m_remote_ip = null;

        m_start = start_time;
        m_end = stop_time;

        // Schedule a new command that will start listening at 'start time'

        Simulator.getInstance().schedule(
                new Command("RandomSinkStart", m_start)
                {
                    public void execute()
                    {
                        m_transport.listen(m_local_ip);
                    }
                });

        // Schedule another command that will take down the connection at
        // 'stop_time' (if there is a connection)

        Simulator.getInstance().schedule(
                new Command("RandomSinkStop", m_end)
                {
                    public void execute()
                    {
                        m_transport.disconnect();
                    }
                });
    }

    /**
     Do nothing. Nobody can attach to this agent and use it.
     */
    public void attach(Agent higher_level, int unique_id)
    {
    }

    public void attach(Agent lower_level)
    {
        m_transport = (CO_Agent) lower_level;
    }

    public void indicate(int status, Object indicator)
    {
    }

    /**
     This is a 'sink' that is there to receive packets, not send them. Thus
     this function always returns false.
     @param destination ignored
     @param length ignored
     @return false
     */
    public boolean canSend(IPAddr destination, int length)
    {
        return false;
    }

}
