package yacs.job.tasks.helpers;

import java.nio.*;

public class TaskClassLoader extends ClassLoader {
	
	public TaskClassLoader( ClassLoader parent ){
		super( parent );
	}
	
	public void loadClass(String name, ByteBuffer classdata){
		byte[] buffer = classdata.array();
		this.defineClass(name, buffer, 0, buffer.length);
	}
	
	public Class loadClass(String name, byte[] classdata){
		return this.defineClass(name, classdata, 0, classdata.length,null);
	}
}
