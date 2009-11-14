package bankrmi;
import java.rmi.*;
public interface Account extends Remote {
  public float balance()
    throws RemoteException;
  public void deposit(float value)
    throws RemoteException, bankrmi.Rejected;
  public void withdraw(float value)
      throws RemoteException, bankrmi.Rejected;
}