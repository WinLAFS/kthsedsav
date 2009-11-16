package dks.utils.serialization;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;
// import java.io.ObjectOutputStream;
// import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.Externalizable;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//import dks.comm.mina.CommunicationComponent;

// if this one is *not* Serializable or Externalizable, then
// the SuperPL's seq/seq2 fields do not appear in the PL's representation in the stream;
class SuperPL {
// class SuperPL implements Serializable {
// class SuperPL implements Externalizable {
	static final long serialVersionUID = 0L;
	int seq;
	int seq2;
	public SuperPL() {
		seq = seq2 = 10;
	}

	//
	public void writeExternal(ObjectOutput out) throws IOException  {
		;
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		;
	}

	void print() {
		System.out.println("seq=" + seq + ", seq2=" + seq2 + " (serialVersionUID=" + serialVersionUID + ")");
	}
};

// class ObjPL implements Serializable {
class ObjPL implements Externalizable {
	static final long serialVersionUID = 2L;
	private int internal;
	int num;
	int num2;
	int num3;

	public ObjPL(int in) {
		internal = in;
	}
	public ObjPL() {
		internal = 0;
	}

	//
	public void writeExternal(ObjectOutput out) throws IOException  {
		out.writeInt(internal);
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		internal = in.readInt();
	}

	void print() {
		System.out.println("internal=" + internal + ", num=" + num + ", num2=" + num2 + ", num3=" + num3);
	}
};

// class PL extends SuperPL implements Serializable {
class PL extends SuperPL implements Externalizable {
	static final long serialVersionUID = 1L;
	ObjPL objpl;
	// ObjPL objpl2;
	char payload[];

	public PL() {
		;
	}
	public PL(int size) {
		objpl = new ObjPL(1);
		// payload = new char[size];

		objpl.num3 = 2;
		seq2 = 3;
		// payload[0] = 'a'; payload[1] = 'z'; payload[2] = 0;
		payload = "az".toCharArray();
	}

	void print() {
		super.print();
		if (objpl != null) 
			objpl.print();
		else 
			System.out.println("objpl is null");
		if (payload != null)
			System.out.println("payload=" + new String(payload));
		else
			System.out.println("payload is null");
		System.out.println("(serialVersionUID=" + serialVersionUID + ")");
	}

	// serialization's writeReplace() override the serialized representation
	// of the fields of this object (not of superclass'es), but not the names
	// of the fields! 
//	private void writeObject(java.io.ObjectOutputStream out) {
//		;
//	}

	// 
	public Object writeReplace() {
		System.out.println("writeReplace: " + this.getClass().getCanonicalName());
		return (this);
	}

	//
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(objpl);
	}
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		objpl = (ObjPL) in.readObject();
	}
}

public class Serialization {
	protected static Logger log = Logger.getLogger(Serialization.class);
	public static void main(String[] args)
	throws FileNotFoundException, ClassNotFoundException, IOException {
	    Args argsObj = new Args(args);
		final PL message = new PL(argsObj.msgsize);
		message.print();

        PropertyConfigurator.configure(System.getProperty("org.apache.log4j.config.file"));
//		ObjectOutputStream out =
//			new ObjectOutputStream(new FileOutputStream("/tmp/kost/serial.img"));
		DebugObjectOutputStream out =
			new DebugObjectOutputStream(log, new FileOutputStream("/tmp/kost/serial.img"), null);
		out.writeObject(message);
		out.writeObject(new PL(argsObj.msgsize));
		out.close();

//		ObjectInputStream in =
//			new ObjectInputStream(new FileInputStream("/tmp/kost/serial.img"));
		DebugObjectInputStream in =
			new DebugObjectInputStream(log, new FileInputStream("/tmp/kost/serial.img"));
		PL msgBack = (PL) in.readObject();
		PL msgBack2 = (PL) in.readObject();
		msgBack.print();
		in.close();
	}
};
