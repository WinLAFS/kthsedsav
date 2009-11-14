package se.kth.ict.npj.hw1.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;


/**
 * This thread is responsible for trying one letter in a Hangman game.
 *
 */
public class HangmanLetterTry extends HangmanCommunication {
	
	/**
	 * The constructor inherited by {@link HangmanCommunication}
	 * 
	 * @param socket
	 * @param msg
	 * @param hc
	 */
	public HangmanLetterTry(Socket socket, String msg, HangmanClient hc) {
		super(socket, msg, hc);
	}
	
	public void run() {
		try {
			//getting the streams
			PrintWriter pw = new PrintWriter(socket.getOutputStream()); 
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			//sending the request message
			pw.println(msg);
			pw.flush();
			
			//receiving the response
			String input = br.readLine();
			if (input != null) {
				//breaking the response into parts
				StringTokenizer st = new StringTokenizer(input, HangmanClientConnection.REQUEST_DELIMITER);
				String fixed = st.nextToken(); //the fixed part of the response - identifier
				String word = st.nextToken(); //the word with whom we are playing
				int numOfAttempts = new Integer(st.nextToken()); //attempts remaining
				int score = new Integer(st.nextToken()); //current score
				Character c = st.nextToken().toCharArray()[0]; //the character playing
				boolean correctLetter = (st.nextToken().equals("true")) ? true : false; //if the letter was correct
				
				if (!fixed.equals(HangmanClientConnection.SERVER_PLAY)) { //if not still in game
					if (fixed.equals(HangmanClientConnection.SERVER_WON)) { //if won
						hc.setPlayerWonOrLost(true, word, score); //inform user that won
					}
					else if (fixed.equals(HangmanClientConnection.SERVER_FAIL)) { //if lost
						hc.setPlayerWonOrLost(false ,word, score); //inform user that it lost
					}
					else {
						throw new Exception("Wrong message from server"); //else uknown msg
					}
				}
				else {
					hc.setCharacterPlayed(word, numOfAttempts, c, correctLetter); //inform for the result of the letter
				}
			}
			else {
				throw new Exception("Could not try the character"); //else error
			}
		}
		catch (IOException e1) { //socket problem
			hc.setConnectionLost(); //handle the error
		}
		catch (Exception e) { //other error
			hc.setCommunicationError(false); //handle the error
		}
	}

}
