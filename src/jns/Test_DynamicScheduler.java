/**
 * Test_DynamicScheduler.java
 *
 *
 *
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */

package jns;

import jns.command.Command;
import jns.command.StopCommand;
import jns.element.*;
import jns.trace.JavisTrace;
import jns.trace.Trace;
import jns.util.IPAddr;
import jns.util.Protocols;

import java.io.IOException;

import jns.dynamic.DynamicSchedulerImpl;

/**
 * This class tests the dynamic scheduler functionality
 */
public class Test_DynamicScheduler
{
//
//    public static void main(String args[])
//    {
//        IPAddr nodeA = new IPAddr(192,168,0,1);
//        IPAddr nodeB = new IPAddr(192,168,0,2);
//
//        DynamicSchedulerImpl sch = new DynamicSchedulerImpl("dynamicSchedulerTest", 500000, 0.008, 0.0, new IPAddr(255,255,255,0));
//        sch.addNode(nodeA);
//        sch.addNode(nodeB);
//        sch.addLink(nodeA, nodeB);
//        sch.start();
//        byte[] data = {0,1,2,3,4,5,6,7,8,9};
//      /*  try
//        {
//            Thread.sleep(3000);
//        }
//        catch(InterruptedException e)
//        {
//            e.printStackTrace();
//        }*/
//        sch.scheduleUnicast(nodeA, nodeB, data);
//      /*  try
//        {
//            Thread.sleep(3000);
//        }
//        catch(InterruptedException e)
//        {
//            e.printStackTrace();
//        }*/
//        sch.scheduleUnicast(nodeA, nodeB, data);
//       /* try
//        {
//            Thread.sleep(3000);
//        }
//        catch(InterruptedException e)
//        {
//            e.printStackTrace();
//        }*/
//        sch.stop();
//
//    }


}

