package se.kth.ict.npj.hw2;
import java.rmi.*;
public interface Bank extends Remote {
      public Account newAccount(String name)
          throws RemoteException, Rejected;
      public Account getAccount (String name)
          throws RemoteException;
      public boolean deleteAccount(Account acc)
          throws RemoteException, Rejected;
}