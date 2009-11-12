package se.kth.ict.npj.hw1.client;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

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
	
	public void run() {
		try {
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			pw.println(msg);
			pw.flush();
			// TODO Socket timeout---------------
			String input = br.readLine();
			if (input != null) {
				StringTokenizer st = new StringTokenizer(input, HangmanClientConnection.REQUEST_DELIMITER);
				String fixed = st.nextToken();
				String word = st.nextToken();
				int numOfAttempts = new Integer(st.nextToken());
				int score = new Integer(st.nextToken());
				
				if (!fixed.equals(HangmanClientConnection.SERVER_PLAY)) {
					throw new Exception("Wrong message");
				}
					
				
				hc.setGameStarted(word, numOfAttempts, score);
			}
			else {
				throw new Exception("Could not start game");
			}
			
		}
		catch (Exception e) {
		}
	}

}
