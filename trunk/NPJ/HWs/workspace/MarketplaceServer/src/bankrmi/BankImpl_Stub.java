// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package bankrmi;

public final class BankImpl_Stub
    extends java.rmi.server.RemoteStub
    implements bankrmi.Bank, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
	new java.rmi.server.Operation("boolean deleteAccount(bankrmi.Account)"),
	new java.rmi.server.Operation("bankrmi.Account getAccount(java.lang.String)"),
	new java.rmi.server.Operation("bankrmi.Account newAccount(java.lang.String)")
    };
    
    private static final long interfaceHash = 6320950446057048460L;
    
    private static final long serialVersionUID = 2;
    
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_deleteAccount_0;
    private static java.lang.reflect.Method $method_getAccount_1;
    private static java.lang.reflect.Method $method_newAccount_2;
    
    static {
	try {
	    java.rmi.server.RemoteRef.class.getMethod("invoke",
		new java.lang.Class[] {
		    java.rmi.Remote.class,
		    java.lang.reflect.Method.class,
		    java.lang.Object[].class,
		    long.class
		});
	    useNewInvoke = true;
	    $method_deleteAccount_0 = bankrmi.Bank.class.getMethod("deleteAccount", new java.lang.Class[] {bankrmi.Account.class});
	    $method_getAccount_1 = bankrmi.Bank.class.getMethod("getAccount", new java.lang.Class[] {java.lang.String.class});
	    $method_newAccount_2 = bankrmi.Bank.class.getMethod("newAccount", new java.lang.Class[] {java.lang.String.class});
	} catch (java.lang.NoSuchMethodException e) {
	    useNewInvoke = false;
	}
    }
    
    // constructors
    public BankImpl_Stub() {
	super();
    }
    public BankImpl_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of deleteAccount(Account)
    public boolean deleteAccount(bankrmi.Account $param_Account_1)
	throws bankrmi.Rejected, java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_deleteAccount_0, new java.lang.Object[] {$param_Account_1}, -976483724134808943L);
		return ((java.lang.Boolean) $result).booleanValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
		try {
		    java.io.ObjectOutput out = call.getOutputStream();
		    out.writeObject($param_Account_1);
		} catch (java.io.IOException e) {
		    throw new java.rmi.MarshalException("error marshalling arguments", e);
		}
		ref.invoke(call);
		boolean $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readBoolean();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of getAccount(String)
    public bankrmi.Account getAccount(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_getAccount_1, new java.lang.Object[] {$param_String_1}, 1508703509376378959L);
		return ((bankrmi.Account) $result);
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
		try {
		    java.io.ObjectOutput out = call.getOutputStream();
		    out.writeObject($param_String_1);
		} catch (java.io.IOException e) {
		    throw new java.rmi.MarshalException("error marshalling arguments", e);
		}
		ref.invoke(call);
		bankrmi.Account $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = (bankrmi.Account) in.readObject();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} catch (java.lang.ClassNotFoundException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of newAccount(String)
    public bankrmi.Account newAccount(java.lang.String $param_String_1)
	throws bankrmi.Rejected, java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_newAccount_2, new java.lang.Object[] {$param_String_1}, -1587428431675975284L);
		return ((bankrmi.Account) $result);
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 2, interfaceHash);
		try {
		    java.io.ObjectOutput out = call.getOutputStream();
		    out.writeObject($param_String_1);
		} catch (java.io.IOException e) {
		    throw new java.rmi.MarshalException("error marshalling arguments", e);
		}
		ref.invoke(call);
		bankrmi.Account $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = (bankrmi.Account) in.readObject();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} catch (java.lang.ClassNotFoundException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}