package bankrmi;
import bankrmi.*;
import java.rmi.*;
public class SClient {
    static final String USAGE = "java Client <bank_url> <client> <value>";
    String bankname = "NordBanken", clientname = "Vlad Vlassov";
    float value = 100;
    
    public SClient(String[] args) {
	    if ((args.length > 3) || (args.length > 0) && args[0].equals("-h")) {
	      System.out.println(USAGE);
	      System.exit(0);
      }
      if (args.length > 2)
  		  try {
		      value = (new Float(args[2])).floatValue();
		    } catch (NumberFormatException e) {
		      System.out.println(USAGE);
	        System.exit(0);
		    }
	    if (args.length > 1) clientname =  args[1];
      if (args.length > 0) bankname =  args[0];
	    try {
	      Bank bankobj = (Bank)Naming.lookup(bankname);
        Account account = bankobj.newAccount(clientname);
		    account.deposit(value);
		    System.out.println (clientname + "'s account: $" + account.balance());
	    } catch (Rejected e) {
	      System.out.println(e);
        System.exit(0);
      } catch (Exception se) {
	      System.out.println("The runtime failed: " + se);
	      System.exit(0);
      }
    }
    public static void main(String[] args) {
      new SClient(args);
    }
} 