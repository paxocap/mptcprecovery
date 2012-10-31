/**
 * DynamicScheduler.java
 *
 * 
 * 
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */
package jns.dynamic;

import java.rmi.RemoteException;
import java.rmi.Remote;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface DynamicScheduler extends Remote
{
    public void scheduleUnicast(InetAddress senderIPAddr, InetAddress receiverIPAddr, byte[] data) throws RemoteException;
    public void scheduleMulticast(InetAddress senderIPAddr, InetAddress multicastGroup, byte[] data) throws RemoteException;
    public byte[] receive(InetAddress receiverIPaddr) throws RemoteException;
    public void stop() throws RemoteException;
}
