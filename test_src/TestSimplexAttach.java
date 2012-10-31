
import jns.element.*;
import jns.util.*;

public class TestSimplexAttach {

    public static void main(String args[]) {
      SimplexLink l1=new SimplexLink(100000,0.1);

      SimplexInterface i1=new SimplexInterface(Interface.SENDER,
					       new IPAddr(192,168,1,1));
      SimplexInterface i2=new SimplexInterface(Interface.RECEIVER,
					       new IPAddr(192,168,1,2));

      i1.attach(l1,true);
      i2.attach(l1,true);

    }
}
