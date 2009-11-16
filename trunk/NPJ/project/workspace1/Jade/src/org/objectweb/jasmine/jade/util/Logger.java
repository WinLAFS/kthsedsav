/**
 * Copyright (C) : INRIA - Domaine de Voluceau, Rocquencourt, B.P. 105, 
 * 78153 Le Chesnay Cedex - France 
 * 
 * contributor(s) : SARDES project - http://sardes.inrialpes.fr
 *
 * Contact : jade@inrialpes.fr
 *
 * This software is a computer program whose purpose is to provide a framework
 * to build autonomic systems, following an architecture-based approach.
 *
 * This software is governed by the CeCILL-C license under French law and 
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated with 
 * loading,  using,  modifying and/or developing or reproducing the software by 
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate,  and  that  also therefore means  that it is
 * reserved for developers  and  experienced professionals having in-depth 
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling 
 * the security of their systems and/or data to be ensured and,  more generally,
 * to use and operate it in the same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had 
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package org.objectweb.jasmine.jade.util;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author <a href="mailto:Daniel.Hagimont@imag.fr">Daniel Hagimont
 *
 */
public class Logger extends UnicastRemoteObject implements RLogger {

	/**
     * 
     */
    private static final long serialVersionUID = 6506034997410672758L;
    
    // remote or local logger
	static final boolean remoteLogger = false;
	static final boolean log = true;
	
    static RLogger rlog = null;
    static String registryHost;
    static int registryPort;
    
    

    // public static void start() {
    // if (log) {
    // if (remoteLogger){
    //        
    //            
    // try {
    // Properties p = Environment.getProperties();
    // registryHost = p.getProperty(Environment.loggerRegistryNode);
    // registryPort = (new
    // Integer(p.getProperty(Environment.loggerRegistryPort))).
    // intValue();
    //             
    // }catch (Exception e){
    // System.out.println("Logger: Remote Logging activated : Cannot read logger
    // properties. " +
    // "Please set LOGGER_PORT and LOGGER NODE in Jade.properties");
    // e.printStackTrace();
    // System.exit(0);
    // }
    //    	
    // try {
    //    			
    // Registry r = LocateRegistry.createRegistry(registryPort);
    // } catch (Exception e) {
    // System.out.println(e.getLocalizedMessage());
    // }
    //    		
    // try{
    // // we start only one logger
    // // check if there is a previous one
    // Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
    // }catch (Exception e0){
    // try{
    // Naming.rebind("//"+registryHost+":"+registryPort+"/Logger",new Logger());
    // } catch (Exception e) {
    // System.out.println(e.getLocalizedMessage());
    //    	    			System.exit(0);
    //    	       }
    //    		 }
    //    		    		 
    //         }
    //       }
    //    }
    
    
    /**
     * print a trace to the logger
     * 
     * @param s
     */
    public static void print(String s) {
        if (log) {
    	  if (remoteLogger)
    		try {
    			if (rlog == null)
    				rlog = (RLogger)Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
    			rlog.prin(s);
    		} catch (Exception e) {
    			e.printStackTrace();
    			System.exit(0);
    		}
    	  else
    		System.out.print(s);
        }
    }
    
    /**
     * print a nested exception in the logger
     * @param ex the exception to print
     */
    public static void print(Exception ex) {
        if (log) {
    	  if (remoteLogger){
    		try {
    			if (rlog == null)
    				rlog = (RLogger)Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
    			
    			Exception last_ex = ex;
                while (ex!= null) {
                    rlog.prin(ex.toString());
                    last_ex = ex;
                    if (ex instanceof JadeException){
                        ex = ((JadeException)ex).getNestedException();
                    }else {
                        ex = null; // stop printing the nested exception
                    } 
                }
                printStackTrace(last_ex);
    			
    		} catch (Exception e) {
    			e.printStackTrace();
    			System.exit(0);
    		}
    	  } else{
    	      Exception last_ex = ex;
              while (ex!= null) {      
                System.out.print(ex.toString());
                last_ex = ex;
                if (ex instanceof JadeException){
                    ex = ((JadeException)ex).getNestedException();
                }else {
                    ex = null; // stop printing the nested exception
                }
              
              } 
              printStackTrace(last_ex);
    	  }
        }      
    }
    
    public static void print(boolean level,String s) {
        if (log) {
            if (level){
              if (remoteLogger)
                try {
                    if (rlog == null)
                        rlog = (RLogger)Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
                    rlog.prin(s);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
              else
                System.out.print(s);
            }
        }
    }

    /**
     * print a trace to the logger (with carriage return)
     * 
     * @param s
     */
    public static void println(String s) {
        if (log) {
        	if (remoteLogger)
        		try {
        			if (rlog == null)
        				rlog = (RLogger)Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
        			rlog.prinln(s);
        		} catch (Exception e) {
        			e.printStackTrace();
        			System.exit(0);
        		}
        	else
        		System.out.println(s);
        }
    }
    
    /**
     * Print a nested exception  (with carriage return).
     * Print all the nested exception.
     * 
     * @param ex
     */
    public static void println(Exception ex) {
        if (log) {
        	if (remoteLogger){
        		try {
        			if (rlog == null)
        				rlog = (RLogger)Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
        			
        			
        			Exception last_ex = ex;
                    while (ex!= null) {
                        rlog.prinln(ex.toString());
                        last_ex = ex;
                        if (ex instanceof JadeException){
                            ex = ((JadeException)ex).getNestedException();
                        }else {
                            ex = null; // stop printing the nested exception
                        }
                    } 
                    printStackTrace(last_ex);
                    
        		} catch (Exception e) {
        			e.printStackTrace();
        			System.exit(0);
        		}
        	}else{
        	    Exception last_ex = ex;
                while (ex!= null) {      
                  System.out.println(ex.toString());
                  last_ex = ex;
                  if (ex instanceof JadeException){
                      ex = ((JadeException)ex).getNestedException();
                  }else {
                      ex = null; // stop printing the nested exception
                  }               
                }
                printStackTrace(last_ex);
        	}
        }
    }
    
    public static void println(boolean level,String s) {
        if (log) {
            if (level){
        	  if (remoteLogger)
        		try {
        			if (rlog == null)
        				rlog = (RLogger)Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
        			rlog.prinln(s);
        		} catch (Exception e) {
        			e.printStackTrace();
        			System.exit(0);
        		}
        	  else
        		System.out.println(s);
            }
        }
    }
    
    public static void println(boolean level,Exception s) {
        if (log) {
            if (level){
        	  if (remoteLogger){
        		try {
        			if (rlog == null)
        				rlog = (RLogger)Naming.lookup("//"+registryHost+":"+registryPort+"/Logger");
        			Exception ex = s;
                    while (ex!= null) {
                        rlog.prinln(ex.toString());
                        if (ex instanceof JadeException){
                            ex = ((JadeException)ex).getNestedException();
                        }else {
                            ex = null; // stop printing the nested exception
                        }
                        
                    } 
                    
        		} catch (Exception e) {
        			e.printStackTrace();
        			System.exit(0);
        		}
        	  }else{
        	      Exception ex = s;
                  while (ex!= null) {      
                    System.out.println(ex.toString());
                    if (ex instanceof JadeException){
                        ex = ((JadeException)ex).getNestedException();
                    }else {
                        ex = null; // stop printing the nested exception
                    }          
                  } 
        	  
        	  }
            }
        }
    }
    
   public Logger() throws RemoteException {
   }
    	
   public void prinln(String s) throws RemoteException {
    		System.out.println(s);
   }
    
    public void prin(String s) throws RemoteException {
    	System.out.print(s);
    }
    
    public static void printStackTrace(Exception e) {
        if (e!=null){
    	  StackTraceElement ste[] = e.getStackTrace();
    	  for (int i=0; i<ste.length;i++) println(ste[i].toString());
    	
          if (e instanceof JadeException){
            printStackTrace(((JadeException)e).getNestedException());
          }
        }
    }
}
