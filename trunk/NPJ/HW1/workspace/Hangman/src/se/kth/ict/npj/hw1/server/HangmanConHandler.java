package se.kth.ict.npj.hw1.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Connection handler for Hangman server.
 *
 */
public class HangmanConHandler extends Thread {
	private Socket clientSocket;
	private HangmanServerLogic logic = new HangmanServerLogic();
	
	/**
	 * Constructor
	 * @param clientSocket The client socket to communicate with client.
	 */
	public HangmanConHandler (Socket clientSocket){
		this.clientSocket = clientSocket;
	}
	
	/*
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		PrintWriter pw = null;
		BufferedReader br = null;
		
		try {
			pw = new PrintWriter(clientSocket.getOutputStream());
			br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			logic.processRequests(pw, br);
			
			//Closing everything
			pw.close();
			br.close();
			clientSocket.close();
		} catch (IOException e) {
			System.err.println("Can't open client socket");
			e.printStackTrace();
			return;
		}
		
	}
	 
}
