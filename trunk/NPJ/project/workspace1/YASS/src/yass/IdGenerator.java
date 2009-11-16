package yass;

public class IdGenerator {

public static void main(String[] args) {
	long space = 1048576;
	int nodes = 2048;
	int div = 2;
//	String pad = "=1000001";
	String pad = "";
//	int port = 23000;
	
	System.out.println("0"+pad);
	for(int i = 1; i<nodes; ) {
		
		for(int j=1; j<div && i<nodes; j= j+2, i++) {
			long tmp = space * j / div;
			System.out.println(tmp+pad);

//			for the lookup test
//			System.out.println("# " + (i+1));
//			System.out.println("startNiche $MY_CLASS join " + tmp + " " +  port + " kalle.sics.se");
//			port += 10;
//			System.out.println("sleep 1");
		}
		
		
		div *= 2; 
	}
}
}
