package se.sics.kompics.p2p.peer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

public final class PeerConfiguration {

	private final int downloadBW;
	private final int uploadBW;
	private final int indegree;
	private final int outdegree;
	private final int numOfPieces;
	private final int pieceSize;

//-------------------------------------------------------------------	
	public PeerConfiguration(int downloadBW, int uploadBW, int indegree, int outdegree, int numOfPieces, int pieceSize) {
		super();
		this.downloadBW = downloadBW;
		this.uploadBW = uploadBW;
		this.indegree = indegree;
		this.outdegree = outdegree;
		this.numOfPieces = numOfPieces;
		this.pieceSize = pieceSize;
	}

//-------------------------------------------------------------------	
	public int getDownloadBW() {
		return this.downloadBW;
	}

//-------------------------------------------------------------------	
	public int getUploadBW() {
		return this.uploadBW;
	}

//-------------------------------------------------------------------	
	public int getIndegree() {
		return this.indegree;
	}

//-------------------------------------------------------------------	
	public int getOutdegree() {
		return this.outdegree;
	}

//-------------------------------------------------------------------	
	public int getNumOfPieces() {
		return this.numOfPieces;
	}

//-------------------------------------------------------------------	
	public int getPieceSize() {
		return this.pieceSize;
	}

//-------------------------------------------------------------------	
	public void store(String file) throws IOException {
		Properties p = new Properties();
		p.setProperty("downloadBW", "" + this.downloadBW);
		p.setProperty("uploadBW", "" + this.uploadBW);
		p.setProperty("indegree", "" + this.indegree);
		p.setProperty("outdegree", "" + this.outdegree);
		p.setProperty("numOfPieces", "" + this.numOfPieces);
		p.setProperty("pieceSize", "" + this.pieceSize);
		
		Writer writer = new FileWriter(file);
		p.store(writer, "se.sics.kompics.p2p.ms");
	}

//-------------------------------------------------------------------	
	public static PeerConfiguration load(String file) throws IOException {
		Properties p = new Properties();
		Reader reader = new FileReader(file);
		p.load(reader);

		int downloadBW = Integer.parseInt(p.getProperty("downloadBW"));
		int uploadBW = Integer.parseInt(p.getProperty("uploadBW"));
		int indegree = Integer.parseInt(p.getProperty("indegree"));
		int outdegree = Integer.parseInt(p.getProperty("outdegree"));
		int numOfPieces = Integer.parseInt(p.getProperty("numOfPieces"));
		int pieceSize = Integer.parseInt(p.getProperty("pieceSize"));
		
		return new PeerConfiguration(downloadBW, uploadBW, indegree, outdegree, numOfPieces, pieceSize);
	}
}
