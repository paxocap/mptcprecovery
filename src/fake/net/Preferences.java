/**
 * Preferences.java
 *
 * 
 * 
 * @author Einar Vollset <einar.vollset@ncl.ac.uk>
 *
 */
package fake.net;

public class Preferences
{

    private Preferences(){}//prevents instantiation

    /**
     * This should give the hostname of the machine where the JNS dynamic
     * scheduler is running.
     */
    public static final String SERVER_HOST_NAME = "localhost";
    /**
     * This should give the port no on which the RMIRegistry is running on the
     * above machine.
     */
    public static final int SERVER_PORT_NO = 3778;

}
