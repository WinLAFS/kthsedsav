import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class Test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		URL url = new URL("file:///home/shum/NPJ/project/workspace1/Jade/lib/jadeboot.jar");
		InputStream str = url.openStream();
		System.out.println(123);
	}

}
