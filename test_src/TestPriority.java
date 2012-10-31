
import jns.util.PriorityQueue;
import java.util.Enumeration;

public class TestPriority {

    public static void main(String args[]) {
	PriorityQueue p=new PriorityQueue();

	double randomnums[]=new double[100];
        for (int i=0;i<100;i++) 
        randomnums[i]=(double)(Math.random()*2000);
         
	for (int i=0;i<100;i++)
        p.push(new Double(randomnums[i]),randomnums[i]);

	Enumeration e=p.elements();
        while (e.hasMoreElements()) {
          Double l=(Double)e.nextElement();
	  System.out.println(l);
        }
	
	System.out.println("---");
	while (p.size()>0) {
	  Double l=(Double)p.peek();
	  p.pop();
	  System.out.println(l);
	}

	
    }




}
