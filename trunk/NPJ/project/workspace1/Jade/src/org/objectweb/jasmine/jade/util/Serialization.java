package org.objectweb.jasmine.jade.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serialization {
	public static String serialize(Object o) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        byte[] bs = baos.toByteArray();
        baos.close();
        StringBuffer sb = new StringBuffer(bs.length);
        for (int i = 0; i < bs.length; i++) {
            sb.append((char)(bs[i] & 255));
        }
        return sb.toString();
    }
	public static Object deserialize(String s) throws IOException, ClassNotFoundException {
        byte[] bs = new byte[s.length()];
        for (int i = 0; i < bs.length; i++) {
            bs[i] = (byte)s.charAt(i);
        }
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bs));
        Object o = ois.readObject();
        ois.close();
        return o;
    }
}
