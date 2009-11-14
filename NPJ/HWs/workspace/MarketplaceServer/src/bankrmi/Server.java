package bankrmi;
import java.rmi.*;
import bankrmi.*;
public class Server extends Thread {
  static final String USAGE = "java bankrmi.Server <bank_url>";
  static final String BANK = "NordBanken";

  public Server(String[] args) {
    String bankname = (args.length > 0)? args[0] : BANK;
    if (args.length > 1 || bankname.equalsIgnoreCase("-h")) {
      System.out.println(USAGE);
      System.exit(1);
    }
    try {
      Bank bankobj = new BankImpl(bankname);
      // Register the newly created object at rmiregistry.
      Naming.rebind(bankname, bankobj);
      System.out.println(bankobj + " is ready.");
    } catch (Exception e) {
      System.out.println(e);
    }
  }
  public void run () {
    // Wait for incoming requests
    try {
      while (true) {
        try {
          sleep(10);
        }   catch (InterruptedException e) { }
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }
  public static void main(String[] args) {
    new Server(args).run();
  }
}
