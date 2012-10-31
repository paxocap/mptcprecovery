
/*
  This file contains a primitive test of IP fragmentation. A packet is sent
  through a router to a link with a small MTU and consequently fragmented.
*/

import jns.Simulator;

import jns.trace.*;
import jns.element.*;
import jns.util.*;
import jns.command.*;

import java.io.IOException;


class PacketSender extends Command {

    IPHandler m_ip;

    public PacketSender(IPHandler handler,double time) {
      super("PacketSender",time);
      m_ip=handler;
    }

    public void execute() {
      m_ip.send(m_ip.getAddress(),new IPAddr(128,116,11,20),1000,null,
		Protocols.TCP);
    }
}



public class Test_Packet {

  public static void main(String args[]) {

    // Get a simulator

    Simulator sim=Simulator.getInstance();

    // Create a trace object

    Trace trace=null;
    try {
      trace=new JavisTrace("test_packet.jvs");
    }
    catch (IOException e) {
      System.out.println("Could not create test_packet.jvs!");
      System.exit(1);
    }

      
    // Set up three nodes

    Node src=new Node("Source node");
    Node router=new Node("Router");
    Node dest=new Node("Destination node");

    sim.attachWithTrace(src,trace);
    sim.attachWithTrace(router,trace);
    sim.attachWithTrace(dest,trace);


    // Give source and dest node a duplex network interface

    Interface src_iface=new DuplexInterface(new IPAddr(192,168,1,10));
    src.attach(src_iface);
    src.addDefaultRoute(src_iface);

    Interface dest_iface=new DuplexInterface(new IPAddr(128,116,11,20));
    dest.attach(dest_iface);
    dest.addDefaultRoute(dest_iface);

    sim.attachWithTrace(src_iface,trace);
    sim.attachWithTrace(dest_iface,trace);


    // The router needs two duplex interfaces, for obvious reasons

    Interface route_iface192=new DuplexInterface(new IPAddr(192,168,1,1));
    Interface route_iface128=new DuplexInterface(new IPAddr(128,116,11,1));
    router.attach(route_iface192);
    router.attach(route_iface128);
    router.addRoute(new IPAddr(192,168,1,0),new IPAddr(255,255,255,0),
	            route_iface192);
    router.addRoute(new IPAddr(128,116,11,0),new IPAddr(255,255,255,0),
		    route_iface128);

    // Cunningly force the router to fragment the packet we're sending by
    // setting a small MTU.
    
    route_iface128.setMTU(600);

    sim.attachWithTrace(route_iface192,trace);
    sim.attachWithTrace(route_iface128,trace);
    

    // All we need now is two links

    Link link1=new DuplexLink(10000,0.01);
    Link link2=new DuplexLink(5000,0.03);

    route_iface192.attach(link1,true);
    route_iface128.attach(link2,true);

    src_iface.attach(link1,true);
    dest_iface.attach(link2,true);

    sim.attachWithTrace(link1,trace);
    sim.attachWithTrace(link2,trace);


    // Stop the simulator after 4 seconds

    sim.schedule(new StopCommand(4));
    sim.schedule(new PacketSender(src.getIPHandler(),0.1));

    //    sim.dump();
    
    // Create a trace object and start simulating

    try {

      trace.writePreamble();

      sim.run();

      trace.writePostamble();
    }
    catch (IOException e) {
      System.err.println("An I/O exception occured during the simulation!");
      System.exit(1);
    }

  }

}

