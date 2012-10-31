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
package jns;

import jns.command.StopCommand;
import jns.element.*;
import jns.trace.JavisTrace;
import jns.trace.Trace;
import jns.util.IPAddr;
import jns.dynamic.PacketSender;

import java.io.IOException;

/**
 * This class tests IP protocol. It sets up four nodes
 * and sends packets from A-B-C-D
 */
public class Test_IP
{

    public static void main(String args[])
    {


        // Create a trace object to record events
        Trace trace = null;
        try
        {
            trace = new JavisTrace("test_new_ip.jvs");
        }
        catch(IOException e)
        {
            System.out.println("Could not create test_ip.jvs!");
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

        //Setup the interfaces for all the 4 nodes, attach to the corresponding
        //nodes and attach with trace to the simulator
        Interface ABIface = new DuplexInterface(new IPAddr(192, 168, 0, 1));
        A.attach(ABIface);
        sim.attachWithTrace(ABIface, trace);
        Interface ACIface = new DuplexInterface(new IPAddr(192, 168, 0, 1));
        A.attach(ACIface);
        sim.attachWithTrace(ACIface, trace);
        Interface ADIface = new DuplexInterface(new IPAddr(192, 168, 0, 1));
        A.attach(ADIface);
        sim.attachWithTrace(ADIface, trace);

        Interface BAIface = new DuplexInterface(new IPAddr(192, 168, 0, 2));
        B.attach(BAIface);
        sim.attachWithTrace(BAIface, trace);

        Interface BCIface = new DuplexInterface(new IPAddr(192, 168, 0, 2));
        B.attach(BCIface);
        sim.attachWithTrace(BCIface, trace);

        Interface BDIface = new DuplexInterface(new IPAddr(192, 168, 0, 2));
        B.attach(BDIface);
        sim.attachWithTrace(BDIface, trace);


        Interface CAIface = new DuplexInterface(new IPAddr(192, 168, 0, 3));
        C.attach(CAIface);
        sim.attachWithTrace(CAIface, trace);

        Interface CBIface = new DuplexInterface(new IPAddr(192, 168, 0, 3));
        C.attach(CBIface);
        sim.attachWithTrace(CBIface, trace);

        Interface CDIface = new DuplexInterface(new IPAddr(192, 168, 0, 3));
        C.attach(CDIface);
        sim.attachWithTrace(CDIface, trace);

        Interface DAIface = new DuplexInterface(new IPAddr(192, 168, 0, 4));
        D.attach(DAIface);
        sim.attachWithTrace(DAIface, trace);
        Interface DBIface = new DuplexInterface(new IPAddr(192, 168, 0, 4));
        D.attach(DBIface);
        sim.attachWithTrace(DBIface, trace);
        Interface DCIface = new DuplexInterface(new IPAddr(192, 168, 0, 4));
        D.attach(DCIface);
        sim.attachWithTrace(DCIface, trace);


        //Create the 3 links needed between the nodes, attach them to the
        //appropriate interfaces and attach with trace to the simulator
        Link a = new DuplexLink(500000, 0.008);
        Link b = new DuplexLink(500000, 0.008);
        Link c = new DuplexLink(500000, 0.008);
        Link d = new DuplexLink(500000, 0.008);
        Link e = new DuplexLink(500000, 0.008);
        Link f = new DuplexLink(500000, 0.008);

        //attach the links, WILL THIS WORK?? No..
        ABIface.attach(a, true);
        ACIface.attach(f, true);
        ADIface.attach(c, true);

        BAIface.attach(a, true);
        BCIface.attach(b, true);
        BDIface.attach(e, true);

        CAIface.attach(f, true);
        CBIface.attach(b, true);
        CDIface.attach(d, true);

        DAIface.attach(c, true);
        DBIface.attach(e, true);
        DCIface.attach(d, true);


        //attach to simulator
        sim.attachWithTrace(a, trace);
        sim.attachWithTrace(b, trace);
        sim.attachWithTrace(c, trace);
        sim.attachWithTrace(d, trace);
        sim.attachWithTrace(e, trace);
        sim.attachWithTrace(f, trace);

        //add routes
        A.addRoute(new IPAddr(192, 168, 0, 2), new IPAddr(255, 255, 255, 0), ABIface);
        A.addRoute(new IPAddr(192, 168, 0, 3), new IPAddr(255, 255, 255, 0), ACIface);
        A.addRoute(new IPAddr(192, 168, 0, 4), new IPAddr(255, 255, 255, 0), ADIface);

        B.addRoute(new IPAddr(192, 168, 0, 1), new IPAddr(255, 255, 255, 0), BAIface);
        B.addRoute(new IPAddr(192, 168, 0, 3), new IPAddr(255, 255, 255, 0), BCIface);
        B.addRoute(new IPAddr(192, 168, 0, 4), new IPAddr(255, 255, 255, 0), BDIface);

        C.addRoute(new IPAddr(192, 168, 0, 1), new IPAddr(255, 255, 255, 0), CAIface);
        C.addRoute(new IPAddr(192, 168, 0, 2), new IPAddr(255, 255, 255, 0), CBIface);
        C.addRoute(new IPAddr(192, 168, 0, 4), new IPAddr(255, 255, 255, 0), CDIface);

        D.addRoute(new IPAddr(192, 168, 0, 1), new IPAddr(255, 255, 255, 0), DAIface);
        D.addRoute(new IPAddr(192, 168, 0, 2), new IPAddr(255, 255, 255, 0), DBIface);
        D.addRoute(new IPAddr(192, 168, 0, 3), new IPAddr(255, 255, 255, 0), DCIface);


        new Thread(sim).start();

        byte[] data = {0,1,2,3,4};
        sim.schedule(new PacketSender(A.getIPHandler(),new IPAddr(192, 168, 0, 2), 0.1,data));
        sim.schedule(new PacketSender(A.getIPHandler(),new IPAddr(192, 168, 0, 3), 0.2, data));
        sim.schedule(new StopCommand(1));




    }





}

