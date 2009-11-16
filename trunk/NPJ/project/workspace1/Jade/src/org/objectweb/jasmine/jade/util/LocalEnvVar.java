/***
 * Created 06/01/05
 * Management of environment variables. These variable are managed in a file
 * on the local file system and can therefore intended to be used between
 * co-localized entities. This facility has been introduced in replacement
 * of Unix processes' environment variables. We wanted to use them for sharing
 * between a wrapper and the legacy code which is wrapped.
 * However, System.getenv() is deprecated in Java and Java properties do not
 * provide the required functionality.
 * NB: this code is reused in the legacy code which is wrapped (which is patched)
 * e.g. Config.java in Rubis. Therefore, it must not depend on the whole JADE
 * environment (e.g. JadeException).
 * Contact: Daniel.Hagimont@imag.fr
 * Author: Daniel Hagimont
 */
package org.objectweb.jasmine.jade.util;

import java.io.File;
import java.io.RandomAccessFile;

public class LocalEnvVar {
    
    final static String DIR = "/tmp/LocalEnvVar";
    
    /** set a property
     * @param key The name of the property.
     * @param value The value to set.
     */
    public static void setProperty(String key, String value) {
        try {
            File f = new File(DIR);
            if (!f.exists()) ExecutableCmd.syncExec("mkdir "+DIR,null);
            ExecutableCmd.syncExec("rm -rf "+DIR+"/"+key,null);
            RandomAccessFile in = new RandomAccessFile(DIR+"/"+key, "rw");
            in.writeBytes(value);
            in.close();
            ExecutableCmd.syncExec("chmod 777 -R "+DIR,null);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }      
    }
    
    /** get a property
     * @param key The name of the property.
     * @return The value of that property.
     */
    public static String getProperty(String key) {
        try {
            File f = new File(DIR);
            if (!f.exists())
                    return null;
            f = new File(DIR+"/"+key);
            if (!f.exists())
                return null;        
            RandomAccessFile in = new RandomAccessFile(DIR+"/"+key, "r");       
            String s = in.readLine();
            in.close();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }      
    }
}
