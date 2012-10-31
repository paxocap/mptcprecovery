
import jns.util.*;
import jns.element.*;

public class TestRouting {

    public static void main(String args[]) {
      RoutingTable rt=new RoutingTable();
      Interface iface=new SimplexInterface(Interface.SENDER,
					   new IPAddr(128,116,11,20));
      
      rt.addRoute(new IPAddr(192,168,0,0),new IPAddr(255,255,0,0),iface);
      rt.addRoute(new IPAddr(116,16,80,20),new IPAddr(255,255,255,255),iface);
      rt.addRoute(new IPAddr(62,63,64,0),new IPAddr(255,255,255,0),iface);
	
      rt.dump();

      if (rt.getRoute(new IPAddr(192,168,23,16))!=null) 
      System.out.println("Route 1 found");
      if (rt.getRoute(new IPAddr(192,169,23,16))!=null) 
      System.out.println("Route 2 found");
      if (rt.getRoute(new IPAddr(116,16,80,13))!=null) 
      System.out.println("Route 3 found");
      if (rt.getRoute(new IPAddr(62,63,64,15))!=null) 
      System.out.println("Route 4 found");
      if (rt.getRoute(new IPAddr(62,63,64,38))!=null) 
      System.out.println("Route 5 found");
    }

}
