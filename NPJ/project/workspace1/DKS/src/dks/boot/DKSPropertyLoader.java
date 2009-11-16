/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.boot;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.log4j.Logger;

import dks.DKSParameters;

/**
 * The <code>PropertyLoader</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSPropertyLoader.java 122 2006-11-21 18:46:23Z Roberto $
 */
public class DKSPropertyLoader {

	/*#%*/ private static Logger log = Logger.getLogger(DKSPropertyLoader.class);
		
	private Properties prop ;
	
	
	/**
	 * 
	 */
	public DKSPropertyLoader() {
		String propertiesFile=System.getProperty("dks.propFile");
		prop = new Properties();
	    FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(propertiesFile);
			prop.load(fileInputStream);
		}catch (IOException e) {
			/*#%*/ log.error("Cannot read the properties");
			e.printStackTrace();
		} 	
	}
	
	public DKSParameters getDKSParameters(){
		
		int arity=Integer.parseInt(prop.getProperty("arity"));
		int levels=Integer.parseInt(prop.getProperty("levels"));
		
	    DKSParameters dksParameters= new DKSParameters(arity,levels);
	   
	    return dksParameters;
	}
	
	public int getPort(){
	    return Integer.parseInt(prop.getProperty("port"));
	}
	
	public InetAddress getIP() throws UnknownHostException{
		
		String ip=prop.getProperty("ip");
		
	    return InetAddress.getByName(ip);
	}
	
	public String getWebcacheAddress(){
		return prop.getProperty("publishAddress");
	}
}
