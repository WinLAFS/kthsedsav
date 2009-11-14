package se.kth.ict.npj.hw2;
import java.rmi.*;
public interface Account extends Remote {
  public float balance()
    throws RemoteException;
  public void deposit(float value)
    throws RemoteException, Rejected;
  public void withdraw(float value)
      throws RemoteException, Rejected;
}