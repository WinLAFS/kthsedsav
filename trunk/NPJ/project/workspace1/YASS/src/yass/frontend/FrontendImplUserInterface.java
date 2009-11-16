package yass.frontend;

import java.io.File;
import java.util.ArrayList;

public interface FrontendImplUserInterface {

	public void store(File f);
	public void retrieve(String name);
	public void remove(String name);
	public ArrayList<String> list();
	
	
}
