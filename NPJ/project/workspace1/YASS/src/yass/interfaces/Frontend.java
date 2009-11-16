package yass.interfaces;

import java.io.File;

import yass.storage.FileReadAck;
import yass.storage.FileWriteAck;
import yass.storage.FileWriteRequestAck;

public interface Frontend extends Runnable, FileWriteRequestAck,
FileWriteAck, FileReadAck {

	public static final int CACHE_TEST = 1;
	public static final int FAIL_TEST = 2;
	public static final int DEMO_MODE = 3;

	void store(File f);
	void remove(String fileName);
	void retrieve(String fileName);
}
