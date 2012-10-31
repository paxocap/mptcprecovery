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
public class RandomSource implements Agent
{

    CO_Agent m_transport;
    IPAddr m_source_ip;
    IPAddr m_dest_ip;

    // Start and stop time
    double m_start;
    double m_end;

    /**
     Create a new random source that will start sending at some point and
     stop sending at some other point.
     @param source the source IP from which to send (one of the IP addresses
     of the node this is at).
     @param dest the IP to send to. Make sure this IP has a RandomSink
     attached and listening before you start sending!
     @param start_time the time at which to start sending
     @param stop_time the time at which to stop sending
     */
    public RandomSource(IPAddr source, IPAddr dest, double start_time,
                        double stop_time)
    {
        m_source_ip = source;
        m_dest_ip = dest;

        m_start = start_time;
        m_end = stop_time;

        // Schedule a new command that will set up a connection at 'start_time'

        Simulator.getInstance().schedule(
                new Command("RandomSourceStart", m_start)
                {
                    public void execute()
                    {
                        m_transport.connect(m_source_ip, m_dest_ip, 80);
                    }
                });

        // Schedule another command that will take down the connection at
        // 'stop_time'

        Simulator.getInstance().schedule(
                new Command("RandomSourceStop", m_end)
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
        if(status == Agent.CONNECTION_ESTABLISHED)
        {
            System.out.println("Random source connected");

            Simulator.getInstance().schedule(
                    new RandomSourceSendCommand(Simulator.getInstance().getTime() + 0.001,
                                                this));
        }
    }

    /**
     RandomSource is supposed to be a top-level agent. It sends data by
     itself and no one is supposed to use it to send data. Thus this function
     will always return 'false'.
     @param destination ignored
     @param length ignored
     @return false
     */
    public boolean canSend(IPAddr destination, int length)
    {
        return false;
    }

}


class RandomSourceSendCommand extends Command
{

    RandomSource m_source;

    public RandomSourceSendCommand(double time, RandomSource source)
    {
        super("RandomSourceSend", time);
        m_source = source;
    }

    public void execute()
    {
        m_source.m_transport.send(500, this, 0);
        System.out.println("sending packets at " + Simulator.getInstance().getTime());
        if(Simulator.getInstance().getTime() + 0.05 < m_source.m_end)
            Simulator.getInstance().schedule(
                    new
                            RandomSourceSendCommand(Simulator.getInstance().getTime() + 0.05,
                                                    m_source));
    }

}
