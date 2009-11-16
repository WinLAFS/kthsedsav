package yacs.zemulation;

import java.io.*;

import yacs.job.TaskContainer;

/**
 * Just to emulate the serialization and deserialization that occurs
 * @author LTDATH
 *
 */
public class NetworkSimulator {

	public static TaskContainer serialize( TaskContainer task ) throws Exception {
		
		byte[] serialized = null;
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			ObjectOutputStream oos = new ObjectOutputStream( bos );
			oos.writeObject( task );
			oos.close();
			
			serialized = bos.toByteArray();
		}
		
		TaskContainer deserialized = null;
		{
			ByteArrayInputStream bis = new ByteArrayInputStream( serialized );
			ObjectInputStream ois = new ObjectInputStream( bis );
			
			deserialized = (TaskContainer)ois.readObject();
		}
		
		return deserialized;
	}
	
	public static Object serializeAny( Object object ) throws Exception {
		
		byte[] serialized = null;
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			ObjectOutputStream oos = new ObjectOutputStream( bos );
			oos.writeObject( object );
			oos.close();
			
			serialized = bos.toByteArray();
		}
		
		Object deserialized = null;
		{
			ByteArrayInputStream bis = new ByteArrayInputStream( serialized );
			ObjectInputStream ois = new ObjectInputStream( bis );
			
			return ois.readObject();
		}
	}
}
