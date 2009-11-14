package bankrmi;
import java.rmi.*;
public interface Bank extends Remote {
      public bankrmi.Account newAccount(String name)
          throws RemoteException, bankrmi.Rejected;
      public bankrmi.Account getAccount (String name)
          throws RemoteException;
      public boolean deleteAccount(bankrmi.Account acc)
          throws RemoteException, bankrmi.Rejected;
}