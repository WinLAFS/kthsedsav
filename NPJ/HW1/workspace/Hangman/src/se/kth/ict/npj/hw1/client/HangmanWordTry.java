package se.kth.ict.npj.hw1.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class HangmanWordTry extends HangmanCommunication {

	public HangmanWordTry(Socket socket, String msg, HangmanClient hc) {
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
				String wordPlayed = st.nextToken();
				//Character c = st.nextToken().toCharArray()[0];
				//boolean correctLetter = (st.nextToken().equals("true")) ? true : false;
				
				if (!fixed.equals(HangmanClientConnection.SERVER_PLAY)) {
					if (fixed.equals(HangmanClientConnection.SERVER_WON)) {
						hc.setPlayerWonOrLost(true, word, score);
					}
					else if (fixed.equals(HangmanClientConnection.SERVER_FAIL)) {
						hc.setPlayerWonOrLost(false ,word, score);
					}
					else {
						throw new Exception("Wrong message from server");
					}
				}
				else {
					hc.setCharacterPlayedWord(word, numOfAttempts, wordPlayed);
				}
			}
			else {
				throw new Exception("Could not try the word");
			}
			
		}
		catch (Exception e) {
		}
	}

}
