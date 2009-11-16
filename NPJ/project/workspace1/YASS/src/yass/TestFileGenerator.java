package yass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class TestFileGenerator {
	
	public static int FILES_TO_GENERATE = 500;
	public static String FILE_NAME = "test";
	private static final int MIN_SIZE = 1 * 8; //bytes
	private static final int MAX_SIZE = 1 * 1024; //bytes
	public static String PATH = "/home/joel/MyWorkspace/Jade/testFiles/";
	private static final String BLOCK = "all work and no play makes jack a dull boy!\n";

	
	public static void main(String[] args) throws IOException {
		
		for(int i = 0; i< FILES_TO_GENERATE; i++) {
			File file = new File(PATH+FILE_NAME + i);
			file.createNewFile();
			Random rand = new Random();
			int size = MIN_SIZE + rand.nextInt(MAX_SIZE-MIN_SIZE);
			FileWriter out = new FileWriter(file);
			writeString(size, out);
			out.close();
		}
		
	}
	
	private static  void writeString(int size, FileWriter out) throws IOException {
		for(int i=0; i < ((int)(size/BLOCK.length())) ; i++) {
			out.write(BLOCK);
		}
	}

}
