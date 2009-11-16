package org.objectweb.jasmine.jade.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Invocation implements Externalizable {

	/** The name of the method, case sensitive. */
	protected String method_name;

	/** The arguments of the method. */
	protected Object[] args;

	/** The Method of the call. */
	protected Method method;

	public Invocation() {
	}
	public Invocation(Method method, Object[] arguments) {
		this.method = method;
		method_name = method.getName();
		if (arguments != null)
			args = arguments;
	}

	/**
	 * returns the name of the method to be invoked using this method call object
	 * @return a case sensitive name, can be null for an invalid method call
	 */
	public String getName() {
		return method_name;
	}

	/**
	 * returns an ordered list of arguments used for the method invokation
	 * @return returns the list of ordered arguments
	 */
	public Object[] getArgs() {
		return args;
	}

	public Method getMethod() {
		return method;
	}

	
	/**
	 *
	 * @param target_class
	 * @return
	 * @throws Exception
	 */
	Method findMethod(Class target_class) throws Exception {
		int len = args != null ? args.length : 0;
		Method m;

		Method[] methods = getAllMethods(target_class);
		for (int i = 0; i < methods.length; i++) {
			m = methods[i];
			if (m.getName().equals(method_name)) {
				if (m.getParameterTypes().length == len)
					return m;
			}
		}

		return null;
	}

	/**
	 * The method walks up the class hierarchy and returns <i>all</i> methods of this class
	 * and those inherited from superclasses and superinterfaces.
	 */
	Method[] getAllMethods(Class target) {
		Class superclass = target;
		List methods = new ArrayList();
		int size = 0;

		while (superclass != null) {
			try {
				Method[] m = superclass.getDeclaredMethods();
				methods.add(m);
				size += m.length;
				superclass = superclass.getSuperclass();
			} catch (SecurityException e) {
				// if it runs in an applet context, it won't be able to retrieve
				// methods from superclasses that belong to the java VM and it will
				// raise a security exception, so we catch it here.
				throw new RuntimeException(
						"unable to enumerate methods of superclass "
								+ superclass + " of class " + target);
			}
		}

		Method[] result = new Method[size];
		int index = 0;
		for (Iterator i = methods.iterator(); i.hasNext();) {
			Method[] m = (Method[]) i.next();
			System.arraycopy(m, 0, result, index, m.length);
			index += m.length;
		}
		return result;
	}

	/**
	 * Returns the first method that matches the specified name and parameter types. The overriding
	 * methods have priority. The method is chosen from all the methods of the current class and all
	 * its superclasses and superinterfaces.
	 *
	 * @return the matching method or null if no mathching method has been found.
	 */
	Method getMethod(Class target, String methodName, Class[] types) {

		if (types == null) {
			types = new Class[0];
		}

		Method[] methods = getAllMethods(target);
		methods: for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (!methodName.equals(m.getName())) {
				continue;
			}
			Class[] parameters = m.getParameterTypes();
			if (types.length != parameters.length) {
				continue;
			}
			for (int j = 0; j < types.length; j++) {
				if (!types[j].equals(parameters[j])) {
					continue methods;
				}
			}
			return m;
		}
		return null;
	}

	/**
	 * Invokes the method with the supplied arguments against the target object.
	 * If a method lookup is provided, it will be used. Otherwise, the default
	 * method lookup will be used.
	 * @param target - the object that you want to invoke the method on
	 * @return an object
	 */
	public Object invoke(Object target) throws Throwable {
		Class cl;
		Method meth = null;
		Object retval = null;

		if (method_name == null || target == null) {
			return null;
		}
		cl = target.getClass();
		try {
			meth = findMethod(cl);
			if (meth != null) {
				retval = meth.invoke(target, args);
			} else {
				throw new NoSuchMethodException(method_name);
			}
			return retval;
		} catch (InvocationTargetException inv_ex) {
			throw inv_ex.getTargetException();
		} catch (NoSuchMethodException no) {
			StringBuffer sb = new StringBuffer();
			sb.append("found no method called ").append(method_name).append(
					" in class ");
			sb.append(cl.getName()).append(" with (");
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (i > 0)
						sb.append(", ");
					sb.append((args[i] != null) ? args[i].getClass().getName()
							: "null");
				}
			}
			sb.append(") formal parameters");
			throw new RuntimeException(sb.toString());
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			throw e;
		}
	}

	public Object invoke(Object target, Object[] args) throws Throwable {
		if (args != null)
			this.args = args;
		return invoke(target);
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		boolean first = true;
		if (method_name != null)
			ret.append(method_name);
		ret.append('(');
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (first)
					first = false;
				else
					ret.append(", ");
				ret.append(args[i]);
			}
		}
		ret.append(')');
		return ret.toString();
	}

	public String toStringDetails() {
		StringBuffer ret = new StringBuffer();
		ret.append("Invocation ");
		if (method_name != null)
			ret.append("name=").append(method_name);
		ret.append(", number of args=")
				.append((args != null ? args.length : 0)).append(')');
		if (args != null) {
			ret.append("\nArgs:");
			for (int i = 0; i < args.length; i++) {
				ret.append("\n[").append(args[i]).append(" (").append(
						(args[i] != null ? args[i].getClass().getName()
								: "null")).append(")]");
			}
		}
		return ret.toString();
	}

	public void writeExternal(ObjectOutput out) throws IOException {

		out.writeUTF(method_name);
		out.writeObject(args);

	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

		method_name = in.readUTF();
		args = (Object[]) in.readObject();
	}

}