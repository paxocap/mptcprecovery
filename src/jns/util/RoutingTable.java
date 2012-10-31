package jns.util;

import jns.element.Interface;

import java.util.Enumeration;
import java.util.Vector;


/**
 RoutingTable implements a generic IP routing table. There are routes to
 networks associated with interfaces and a default route.
 Note: The current implementation uses a vector for the routes. A tree
 structure would be faster, but there are no efficiency concers. Feel free
 to change it.
 */
public class RoutingTable
{

    private Route m_default;      // Default route
    private Vector m_routes;      // List of 'Route' objects

    public RoutingTable()
    {
        m_default = null;
        m_routes = new Vector();
    }

    public Interface getRoute(IPAddr dest)
    {
        Enumeration e = m_routes.elements();

        // Search through routes
        while(e.hasMoreElements())
        {
            Route curroute = (Route) e.nextElement();

            if(curroute.match(dest))
            {
                return curroute.getInterface();
            }
        }

        //TODO: Have now changed the above to only check for a direct route, should I maybe
        //      look for someone in the same subnetwork as the destination if no direct
        //      route is available.


        // Nothing found, return default route interface
        if(m_default != null) return m_default.getInterface();

        // No default route, give up
        return null;
    }


    /**
     Add a route to the routing table.
     @param dest the destination address (network or host)
     @param netmask the netmask to use when comparing against targets
     @param iface the interface to send packets to when sending to dest
     */
    public void addRoute(IPAddr dest, IPAddr netmask, Interface iface)
    {
        Route route = new Route(dest, netmask, iface);
        // TODO: Check for duplicate routes and give a warning
        m_routes.addElement(route);
    }


    /**
     Set the default route to use when no other route can be matched. Note
     that repeated calls will override the previous default route.
     @param iface the interface to send packets to when they can't be routed
     */
    public void addDefaultRoute(Interface iface)
    {
        Route route = new Route(new IPAddr(0, 0, 0, 0), new IPAddr(255, 255, 255, 255),
                                iface);
        m_default = route;
    }

    /**
     Delete every route to the given destination.
     */
    public void deleteRoute(IPAddr dest)
    {

    }

    public Enumeration enumerateEntries()
    {
        return m_routes.elements();
    }

    /**
     Delete the default route.
     */
    public void deleteDefaultRoute()
    {
        m_default = null;
    }

    public void dump()
    {
        Enumeration e = m_routes.elements();
        while(e.hasMoreElements())
        {
            Route curroute = (Route) e.nextElement();
            System.out.println(curroute.getDestination() + "      " +
                               curroute.getNetmask());
        }
    }

    /**
     * Returns all the interfaces in this routing table.
     */
    public Vector getAllRoutes()
    {
        Vector interfaces = new Vector();
        for(int i = 0; i < m_routes.size(); i++)
        {
            interfaces.add(((Route)m_routes.get(i)).getInterface());
        }
        return interfaces;
    }

}

