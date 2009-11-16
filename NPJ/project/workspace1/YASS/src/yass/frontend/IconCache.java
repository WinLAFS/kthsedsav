/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package yass.frontend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

/**
 * Manages icons for the application.
 * This is necessary as we could easily end up creating thousands of icons
 * bearing the same image.
 */
class IconCache {
	
	// Path to graphic library
	private final String PathToGraphics = "etc/graphics/";
	// Stock images
	public final int
		shellIcon = 0,
		iconClosedDrive = 1,
		iconClosedFolder = 2,
		iconFile = 3,
		iconOpenDrive = 4,
		iconOpenFolder = 5,
		cmdCopy = 6,
		cmdCut = 7,
		cmdDelete = 8,
		cmdParent = 9,
		cmdPaste = 10,
		cmdPrint = 11,
		cmdRefresh = 12,
		cmdRename = 13,
		cmdSearch = 14;
	public final String[] stockImageLocations = {
		PathToGraphics+"generic_example.gif",
		PathToGraphics+"icon_ClosedDrive.gif",
		PathToGraphics+"icon_ClosedFolder.gif",
		PathToGraphics+"icon_File.gif",
		PathToGraphics+"icon_OpenDrive.gif",
		PathToGraphics+"icon_OpenFolder.gif",
		PathToGraphics+"cmd_Copy.gif",
		PathToGraphics+"cmd_Cut.gif",
		PathToGraphics+"cmd_Delete.gif",
		PathToGraphics+"cmd_Parent.gif",
		PathToGraphics+"cmd_Paste.gif",
		PathToGraphics+"cmd_Print.gif",
		PathToGraphics+"cmd_Refresh.gif",
		PathToGraphics+"cmd_Rename.gif",
		PathToGraphics+"cmd_Search.gif"
	};
	public Image stockImages[];
	
	// Stock cursors
	public final int
		cursorDefault = 0,
		cursorWait = 1;
	public Cursor stockCursors[];
	// Cached icons
	private Hashtable iconCache; /* map Program to Image */
	
	public IconCache() {
	}
	/**
	 * Loads the resources
	 * 
	 * @param display the display
	 */
	public void initResources(Display display) {
		if (stockImages == null) {
			stockImages = new Image[stockImageLocations.length];
				
			for (int i = 0; i < stockImageLocations.length; ++i) {
				//System.out.println("this is i: " + i+ " "+stockImageLocations[i]);
				Image image = createStockImage(display, stockImageLocations[i]);
				if (image == null) {
					freeResources();
					throw new IllegalStateException(
						"error - CouldNotLoadResources");
				}
				stockImages[i] = image;
			}
		}	
		if (stockCursors == null) {
			stockCursors = new Cursor[] {
				null,
				new Cursor(display, SWT.CURSOR_WAIT)
			};
		}
		iconCache = new Hashtable();
	}
	/**
	 * Frees the resources
	 */
	public void freeResources() {
		if (stockImages != null) {
			for (int i = 0; i < stockImages.length; ++i) {
				final Image image = stockImages[i];
				if (image != null) image.dispose();
			}
			stockImages = null;
		}
		if (iconCache != null) {
			for (Enumeration it = iconCache.elements(); it.hasMoreElements(); ) {
				Image image = (Image) it.nextElement();
				image.dispose();
			}
		}
		if (stockCursors != null) {
			for (int i = 0; i < stockCursors.length; ++i) {
				final Cursor cursor = stockCursors[i];
				if (cursor != null) cursor.dispose();
			}
			stockCursors = null;
		}
	}
	/**
	 * Creates a stock image
	 * 
	 * @param display the display
	 * @param path the relative path to the icon
	 */
	private Image createStockImage(Display display, String path) {
		InputStream stream = null;//IconCache.class.getResourceAsStream (path);
		
		try{
			stream = new FileInputStream(path); //
		}
		catch(IOException e)
		{
			e.printStackTrace();
			//System.out.println("nehe?: "+path+" " +stream+" "+e);
		}
		
		//System.out.println(path);
		
		ImageData imageData = new ImageData (stream);
		ImageData mask = imageData.getTransparencyMask ();
		Image result = new Image (display, imageData, mask);
		try {
			stream.close ();
		} catch (IOException e) {
			e.printStackTrace ();
		}
		return result;
	}
	/**
	 * Gets an image for a file associated with a given program
	 *
	 * @param program the Program
	 */
	public Image getIconFromProgram(Program program) {
		Image image = (Image) iconCache.get(program);
		if (image == null) {
			ImageData imageData = program.getImageData();
			if (imageData != null) {
				image = new Image(null, imageData, imageData.getTransparencyMask());
				iconCache.put(program, image);
			} else {
				image = stockImages[iconFile];
			}
		}
		return image;
	}
}
