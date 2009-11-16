package yacs.job.tasks.helpers;
import java.io.*;


public class TaskObjectInputStream extends ObjectInputStream {
	
	private TaskClassLoader loader;
	
	public TaskObjectInputStream( InputStream in, TaskClassLoader loader ) throws Exception {
		super(in);
		this.loader = loader;
	}
	
	protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
		return Class.forName(desc.getName(), false, loader);
    }

}
