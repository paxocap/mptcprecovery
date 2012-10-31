/**
 * StopSimulation.java
 *
 *
 *
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */
package jns.dynamic;

import fake.net.Preferences;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StopSimulation
{

    public static void main(String[] args)
    {
        try
        {

                Registry reg = LocateRegistry.getRegistry(3778);
                DynamicScheduler m_scheduler = (DynamicScheduler) reg.lookup("DynamicScheduler");
                m_scheduler.stop();
                System.out.println("Stopped simulation!");
        }

        catch(RemoteException e)
            {
                e.printStackTrace();
            }



        catch(NotBoundException e)
        {
            System.err.println("DynamicScheduler not bound on rmi://" + Preferences.SERVER_HOST_NAME + ":" + Preferences.SERVER_PORT_NO + "/DynamicScheduler");
            System.exit(-1);
        }


    }
}
