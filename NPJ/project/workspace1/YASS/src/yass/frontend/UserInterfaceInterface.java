package yass.frontend;

import java.io.File;

import yass.interfaces.Frontend;

public interface UserInterfaceInterface {
	public void storeAck(final String name, final long step1, final long step1_2, final long step2, final long step3, final int hopsUsed, final boolean success);
	public void retrieveAck(final String name, final File file);
	public void removeAck(final String name, final boolean success);
	public void run(Frontend applicationInterface, int mode);
	public void setName(String name);
}
