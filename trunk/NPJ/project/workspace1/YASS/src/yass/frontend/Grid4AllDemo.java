package yass.frontend;

import java.net.MalformedURLException;


public class Grid4AllDemo {

	/**
	 * @param args
	 */
	//private static UserInterface myFileBrowserModule;
	
	public static void main(String[] args) throws MalformedURLException
	{
		
		FrontendImpl fi = new FrontendImpl();
		//UserInterface ui = new UserInterface();
		//ui.run(new FrontendImpl());
		System.out.println("Trying to start GUI");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Started");
		fi.retrieve("File Name");
		//UserInterface.run(new FrontendImpl());
		
		
	}
	

}
