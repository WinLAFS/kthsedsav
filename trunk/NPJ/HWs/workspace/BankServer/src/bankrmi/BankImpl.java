package bankrmi;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;
import bankrmi.*;
public class BankImpl extends UnicastRemoteObject
  implements bankrmi.Bank {
  private String _bankname = "Noname";
  private Hashtable _accounts = new Hashtable();

  /** Construct a persistently named object. */
  public BankImpl(java.lang.String name) throws RemoteException {
	  super();
	  _bankname = name;
  }

  /** Construct a transient object. */
  public BankImpl()  throws RemoteException {
	  super();
  }

  public synchronized Account newAccount(java.lang.String name)
	  throws RemoteException, Rejected {
    AccountImpl account = (AccountImpl) _accounts.get(name);
	  if (account != null) {
      System.out.println("Account [" + name + "] exists!!!");
      throw new Rejected("Rejected: Bank: " +
						_bankname +
						" Account for: " +
						name +
						" already exists: " +
						account);
    }
    account = new AccountImpl(name);
    _accounts.put(name, account);
    System.out.println("Bank: " + _bankname +
			   " Account: " + name +
			   " Created for " + name);
    return (Account)account;
  }

  public synchronized Account getAccount(java.lang.String name)
     throws RemoteException {
	  return (Account) _accounts.get(name);
  }

  public synchronized boolean deleteAccount(Account acc)
    throws RemoteException {
	  if ((acc != null) && (_accounts.contains(acc))) {
      java.util.Enumeration en = _accounts.keys();
	    while(en.hasMoreElements()) {
        String name = (String)en.nextElement();
		    AccountImpl ai = (AccountImpl)_accounts.get(name);
		    Account cmp = (Account)ai;
		    if (cmp.equals(acc)) {
		      _accounts.remove(name);
          System.out.println("Bank: " + _bankname +
				       " Account for: " + name +
				       " is deleted");
          return true;
		    }
      }
	  }
    return false;
  }
}
