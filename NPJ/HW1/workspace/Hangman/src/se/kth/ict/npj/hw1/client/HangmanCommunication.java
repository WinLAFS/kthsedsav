package se.kth.ict.npj.hw1.client;

import java.net.Socket;


/**
 * This class is the skeleton for all communication classes.
 *
 */
public class HangmanCommunication extends Thread {
	Socket socket = null;
	String msg = null;
	HangmanClient hc = null;
	
	/**
	 * The new thread is initializes and started.
	 * 
	 * @param socket the socket we use
	 * @param msg the request msg to the server
	 * @param hc the hangman gui
	 */
	public HangmanCommunication(Socket socket, String msg, HangmanClient hc) {
		this.socket = socket;
		this.msg = msg;
		this.hc = hc;
		this.start();
	}

}
