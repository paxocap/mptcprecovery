/**
 * Test_IP.java
 *
 * Copyright (c) 2002 Hewlett-Packard Company
 * Hewlett-Packard Company Confidential
 *
 * Created on 23-May-02
 *
 * @author Einar Vollset <einar_vollset@non.hp.com>
 *
 */

import jns.Simulator;
import jns.agent.MPTCP;
import jns.agent.MPTCPAgent;
import jns.agent.RandomSink;
import jns.agent.RandomSource;
import jns.command.StopCommand;
import jns.element.*;
import jns.trace.JavisTrace;
import jns.trace.Trace;
import jns.util.IPAddr;
import jns.util.Protocols;
import java.io.IOException;

/**
 * This class tests IP protocol. It sets up four nodes
 * and sends packets from A-B-C-D
 */
public class Test_MPTCP
{

    public static void main(String args[])
    {


        // Create a trace object to record events
        Trace trace = null;
        try
        {
            trace = new JavisTrace("test_mptcp.jvs");
        }
        catch(IOException e)
        {
            System.out.println("Could not create test_mptcp.jvs!");
        }

        //Get the simulator
        Simulator sim = Simulator.getInstance();
        sim.setTrace(trace);

        //Create the 4 nodes, and attach with tracing to the simulator
        Node A = new Node("A");
        sim.attachWithTrace(A, trace);
        Node B = new Node("B");
        sim.attachWithTrace(B, trace);
        Node C = new Node("C");
        sim.attachWithTrace(C, trace);
        Node D = new Node("D");
        sim.attachWithTrace(D, trace);
        
        Node E = new Node("E");
        sim.attachWithTrace(E, trace);
        Node F = new Node("F");
        sim.attachWithTrace(F, trace);
        Node G = new Node("G");
        sim.attachWithTrace(G, trace);
        Node H = new Node("H");
        sim.attachWithTrace(H, trace);

        //Setup the interfaces for all the 4 nodes, attach to the corresponding
        //nodes and attach with trace to the simulator
        Interface ABIface = new DuplexInterface(new IPAddr(192, 168, 0, 1));
        A.attach(ABIface);
        sim.attachWithTrace(ABIface, trace);
        Interface ACIface = new DuplexInterface(new IPAddr(192, 168, 0, 10));
        A.attach(ACIface);
        sim.attachWithTrace(ACIface, trace);
        Interface ADIface = new DuplexInterface(new IPAddr(192, 168, 0, 20));
        A.attach(ADIface);
        sim.attachWithTrace(ADIface, trace);

        Interface BAIface = new DuplexInterface(new IPAddr(192, 168, 0, 2));
        B.attach(BAIface);
        sim.attachWithTrace(BAIface, trace);

        Interface BEIface = new DuplexInterface(new IPAddr(192, 168, 0, 2));
        B.attach(BEIface);
        sim.attachWithTrace(BEIface, trace);

        Interface CAIface = new DuplexInterface(new IPAddr(192, 168, 0, 3));
        C.attach(CAIface);
        sim.attachWithTrace(CAIface, trace);

        Interface CFIface = new DuplexInterface(new IPAddr(192, 168, 0, 3));
        C.attach(CFIface);
        sim.attachWithTrace(CFIface, trace);


        Interface DAIface = new DuplexInterface(new IPAddr(192, 168, 0, 4));
        D.attach(DAIface);
        sim.attachWithTrace(DAIface, trace);
        
        Interface DGIface = new DuplexInterface(new IPAddr(192, 168, 0, 4));
        D.attach(DGIface);
        sim.attachWithTrace(DGIface, trace);
        
        Interface EBIface = new DuplexInterface(new IPAddr(192, 168, 0, 5));
        E.attach(EBIface);
        sim.attachWithTrace(EBIface, trace);
        
        Interface EHIface = new DuplexInterface(new IPAddr(192, 168, 0, 5));
        E.attach(EHIface);
        sim.attachWithTrace(EHIface, trace);
        
        Interface FCIface = new DuplexInterface(new IPAddr(192, 168, 0, 6));
        F.attach(FCIface);
        sim.attachWithTrace(FCIface, trace);
        
        Interface FHIface = new DuplexInterface(new IPAddr(192, 168, 0, 6));
        F.attach(FHIface);
        sim.attachWithTrace(FHIface, trace);
        
        Interface GDIface = new DuplexInterface(new IPAddr(192, 168, 0, 7));
        G.attach(GDIface);
        sim.attachWithTrace(GDIface, trace);
        
        Interface GHIface = new DuplexInterface(new IPAddr(192, 168, 0, 7));
        G.attach(GHIface);
        sim.attachWithTrace(GHIface, trace);
        
        Interface HEIface = new DuplexInterface(new IPAddr(192, 168, 0, 8));
        H.attach(HEIface);
        
        sim.attachWithTrace(HEIface, trace);
        
        Interface HFIface = new DuplexInterface(new IPAddr(192, 168, 0, 90));
        H.attach(HFIface);
        sim.attachWithTrace(HFIface, trace);
        
        Interface HGIface = new DuplexInterface(new IPAddr(192, 168, 0, 100));
        H.attach(HGIface);
        sim.attachWithTrace(HGIface, trace);


        //Create the 3 links needed between the nodes, attach them to the
        //appropriate interfaces and attach with trace to the simulator
        Link ab = new DuplexLink(500000, 0.008);
        Link ac = new DuplexLink(500000, 0.008);
        Link ad = new DuplexLink(500000, 0.008);
        Link be = new DuplexLink(500000, 0.008);
        Link cf = new DuplexLink(500000, 0.008);
        Link dg = new DuplexLink(500000, 0.008);
        Link eh = new DuplexLink(500000, 0.008);
        Link fh = new DuplexLink(500000, 0.008);
        Link gh = new DuplexLink(500000, 0.008);
        
        //attach the links, WILL THIS WORK?? No..
        ABIface.attach(ab, true);
        ACIface.attach(ac, true);
        ADIface.attach(ad, true);

        BAIface.attach(ab, true);
        BEIface.attach(be, true);

        CAIface.attach(ac, true);
        CFIface.attach(cf, true);

        DAIface.attach(ad, true);
        DGIface.attach(dg, true);
        
        EBIface.attach(be, true);
        EHIface.attach(eh, true);

        FCIface.attach(cf, true);
        FHIface.attach(fh, true);

        GDIface.attach(dg, true);
        GHIface.attach(gh, true);

        
        HEIface.attach(eh, true);
        HFIface.attach(fh, true);
        HGIface.attach(gh, true);


        //attach to simulator
        sim.attachWithTrace(ab, trace);
        sim.attachWithTrace(ac, trace);
        sim.attachWithTrace(ad, trace);
        sim.attachWithTrace(be, trace);
        sim.attachWithTrace(cf, trace);
        sim.attachWithTrace(dg, trace);
        sim.attachWithTrace(eh, trace);
        sim.attachWithTrace(fh, trace);
        sim.attachWithTrace(gh, trace);

        //add routes
        A.addRoute(new IPAddr(192, 168, 0, 8), new IPAddr(255, 255, 255, 0), ABIface);
        A.addRoute(new IPAddr(192, 168, 0, 90), new IPAddr(255, 255, 255, 0), ACIface);
        A.addRoute(new IPAddr(192, 168, 0, 100), new IPAddr(255, 255, 255, 0), ADIface);

        B.addRoute(new IPAddr(192, 168, 0, 1), new IPAddr(255, 255, 255, 0), BAIface);
        B.addRoute(new IPAddr(192, 168, 0, 8), new IPAddr(255, 255, 255, 0), BEIface);

        C.addRoute(new IPAddr(192, 168, 0, 10), new IPAddr(255, 255, 255, 0), CAIface);
        C.addRoute(new IPAddr(192, 168, 0, 90), new IPAddr(255, 255, 255, 0), CFIface);
 
        D.addRoute(new IPAddr(192, 168, 0, 20), new IPAddr(255, 255, 255, 0), DAIface);
        D.addRoute(new IPAddr(192, 168, 0, 100), new IPAddr(255, 255, 255, 0), DGIface);
        
        E.addRoute(new IPAddr(192, 168, 0, 1), new IPAddr(255, 255, 255, 0), EBIface);
        E.addRoute(new IPAddr(192, 168, 0, 8), new IPAddr(255, 255, 255, 0), EHIface);

        F.addRoute(new IPAddr(192, 168, 0, 10), new IPAddr(255, 255, 255, 0), FCIface);
        F.addRoute(new IPAddr(192, 168, 0, 90), new IPAddr(255, 255, 255, 0), FHIface);
 
        G.addRoute(new IPAddr(192, 168, 0, 20), new IPAddr(255, 255, 255, 0), GDIface);
        G.addRoute(new IPAddr(192, 168, 0, 100), new IPAddr(255, 255, 255, 0), GHIface);

        H.addRoute(new IPAddr(192, 168, 0, 1), new IPAddr(255, 255, 255, 0), HEIface);
        H.addRoute(new IPAddr(192, 168, 0, 10), new IPAddr(255, 255, 255, 0), HFIface);
        H.addRoute(new IPAddr(192, 168, 0, 20), new IPAddr(255, 255, 255, 0), HGIface);
        

        MPTCP client_transport=new MPTCP();



        
        // Now we can create the random sink at the client side by creating a 
        // new transport agent at port 80 (which is the one we are going to use)
        // and attaching the source to it
        
        MPTCPAgent c_agent = client_transport.createNewAgent(80);
        c_agent.addMapping(new IPAddr(192,168,0,8), new IPAddr(192,168,0,8));
        c_agent.addMapping(new IPAddr(192,168,0,8), new IPAddr(192,168,0,90));
        c_agent.addMapping(new IPAddr(192,168,0,8), new IPAddr(192,168,0,100));
        c_agent.addMapping(new IPAddr(192,168,0,90), new IPAddr(192,168,0,8));
        c_agent.addMapping(new IPAddr(192,168,0,90), new IPAddr(192,168,0,90));
        c_agent.addMapping(new IPAddr(192,168,0,90), new IPAddr(192,168,0,100));
        c_agent.addMapping(new IPAddr(192,168,0,100), new IPAddr(192,168,0,8));
        c_agent.addMapping(new IPAddr(192,168,0,100), new IPAddr(192,168,0,90));
        c_agent.addMapping(new IPAddr(192,168,0,100), new IPAddr(192,168,0,100));
        c_agent.attach(new RandomSource(new IPAddr(192,168,0,1),
        								new IPAddr(192,168,0,8),
        								0.2,3.0),0);
        A.attach(client_transport,Protocols.MPTCP);
     
        // Now create a random sink at the server side by first creating a 
        // transport agent for it at port 80 and then attaching it to that

        MPTCP server_transport=new MPTCP();

        MPTCPAgent s_agent = server_transport.createNewAgent(80);
        s_agent.addMapping(new IPAddr(192,168,0,1), new IPAddr(192,168,0,1));
        s_agent.addMapping(new IPAddr(192,168,0,1), new IPAddr(192,168,0,10));
        s_agent.addMapping(new IPAddr(192,168,0,1), new IPAddr(192,168,0,20));
        s_agent.addMapping(new IPAddr(192,168,0,10), new IPAddr(192,168,0,1));
        s_agent.addMapping(new IPAddr(192,168,0,10), new IPAddr(192,168,0,10));
        s_agent.addMapping(new IPAddr(192,168,0,10), new IPAddr(192,168,0,20));
        s_agent.addMapping(new IPAddr(192,168,0,20), new IPAddr(192,168,0,1));
        s_agent.addMapping(new IPAddr(192,168,0,20), new IPAddr(192,168,0,10));
        s_agent.addMapping(new IPAddr(192,168,0,20), new IPAddr(192,168,0,20));
        s_agent.attach( new RandomSink(new IPAddr(192,168,0,8),
    				    0.1,3.2),0);
        H.attach(server_transport,Protocols.MPTCP);

        new Thread(sim).start();

        /*byte[] data = {0,1,2,3,4};
        sim.schedule(new PacketSender(A.getIPHandler(),new IPAddr(192, 168, 0, 8), 0.1,data));
        sim.schedule(new PacketSender(A.getIPHandler(),new IPAddr(192, 168, 0, 90), 0.2, data));
        */
        
        sim.schedule(new StopCommand(4));


    }

}

