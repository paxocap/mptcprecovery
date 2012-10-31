
import jns.element.*;
import jns.util.*;

public class TestDuplexAttach {

    public static void main(String args[]) {
      DuplexLink l1=new DuplexLink(100000,0.1);

      DuplexInterface i1=new DuplexInterface(new IPAddr(192,168,1,1));
      DuplexInterface i2=new DuplexInterface(new IPAddr(192,168,1,2));

      i1.attach(l1,true);
      i2.attach(l1,true);

    }
}
