package se.kth.ict.npj.hw1.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;

public class HangmanStartGame extends HangmanCommunication {

	public HangmanStartGame(Socket socket, String msg, HangmanClient hc) {
		super(socket, msg, hc);
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
		catch (IOException e1) {
			hc.setConnectionLost();
		}
		catch (Exception e) {
			hc.setCommunicationError();
		}
	}

}
