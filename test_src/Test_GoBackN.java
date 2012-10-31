
import jns.Simulator;

import jns.agent.*;
import jns.trace.*;
import jns.element.*;
import jns.util.*;
import jns.command.*;

import java.io.IOException;


/**
   This test program sets up a random source application and a random sink
   and starts packet transfer between them (using the SGN protocol).
   When animated in Javis, you will see the three-way handshake and the
   dependence on the window size of 4..
*/
public class Test_GoBackN {

  public static void main(String args[]) {

    // Get a simulator

    Simulator sim=Simulator.getInstance();
    
    // Create a trace object to record events

    Trace trace=null;
    try {
      trace=new JavisTrace("test_gobackn.jvs");
    }
    catch (IOException e) {
      System.out.println("Could not create test_gobackn.jvs!");
    }
      sim.setTrace(trace);
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

    Link link1=new DuplexLink(10000000,0.005);
    Link link2=new DuplexLink(500000,0.008);

    route_iface192.attach(link1,true);
    route_iface128.attach(link2,true);

    src_iface.attach(link1,true);
    dest_iface.attach(link2,true);

    sim.attachWithTrace(link1,trace);
    sim.attachWithTrace(link2,trace);


    // First of all, the communicating parties need a transport layer protocol
    // because the random source and sink only run on top of the transport
    // layer

    SimpleGoBackN client_transport=new SimpleGoBackN();
    src.attach(client_transport,Protocols.SGN);

    SimpleGoBackN server_transport=new SimpleGoBackN();
    dest.attach(server_transport,Protocols.SGN);


    // Now we can create the random sink at the client side by creating a 
    // new transport agent at port 80 (which is the one we are going to use)
    // and attaching the source to it
    
    client_transport.createNewAgent(80).attach(
	             new RandomSource(new IPAddr(192,168,1,10),
				      new IPAddr(128,116,11,20),
				      0.3,3.0),0);

    // Now create a random sink at the server side by first creating a 
    // transport agent for it at port 80 and then attaching it to that

    server_transport.createNewAgent(80).attach(
		     new RandomSink(new IPAddr(128,116,11,20),
				    0.2,3.2),0);
    
   
    // Stop the simulator after 1.5 seconds

    sim.schedule(new StopCommand(4));

    
    // Start simulating

    sim.run();

  }

}

