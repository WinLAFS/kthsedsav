package se.kth.ict.npj.hw1.client;

import java.net.Socket;

public class HangmanCommunication extends Thread {
	Socket socket = null;
	String msg = null;
	HangmanClient hc = null;
	
	public HangmanCommunication(Socket socket, String msg, HangmanClient hc) {
		this.socket = socket;
		this.msg = msg;
		this.hc = hc;
		this.start();
	}

}
