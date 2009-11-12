package se.kth.ict.npj.hw1.client;

import java.io.IOException;
import java.net.Socket;

/**
 * HangmanClientConnection is the responsible object for the TCP
 * connection of the client with the Hangman Server. It provides
 * the interface to GUI to start a new game, try a word and
 * try a letter
 */
public class HangmanClientConnection {
	//the communication with server 'protocol'
	public static final String REQUEST_DELIMITER=","; 
	public static final String CLIENT_NEW_GAME="start";
	public static final String CLIENT_SEND_LETTER="letter";
	public static final String CLIENT_SEND_WORD="word";
	public static final String CLIENT_END="end";
	public static final String SERVER_PLAY="play";
	public static final String SERVER_FAIL="fail";
	public static final String SERVER_WON="won";
	
	Socket socket = null;
	HangmanClient hc = null;
	
	/**
	 * The Constructor we use to establish the connection with the
	 * Hangman server
	 * 
	 * @param host the host of the server
	 * @param port the port we want to connect
	 * @param hc the gui object
	 * @throws Exception
	 */
	public HangmanClientConnection(String host, String port, HangmanClient hc) throws Exception {
		try {
			socket = new Socket(host, Integer.parseInt(port));
			socket.setSoTimeout(5000);
			this.hc = hc;
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Starts a new game by creating a new thread for starting a new game.
	 */
	public void startNewGame() {
		new HangmanStartGame(socket, HangmanClientConnection.CLIENT_NEW_GAME, hc);
	}
	
	/**
	 * Tries a character by creating a new thread for trying the character.
	 * 
	 * @param character the character that will be tried
	 */
	public void tryCharacter(Character character) {
		String msg = HangmanClientConnection.CLIENT_SEND_LETTER + HangmanClientConnection.REQUEST_DELIMITER + character;
		new HangmanLetterTry(socket, msg, hc);
	}
	
	/**
	 * Tries a word by creating a new thread for trying the character.
	 * 
	 * @param text the word that will be tried
	 */
	public void tryWord(String text) {
		String msg = HangmanClientConnection.CLIENT_SEND_WORD + HangmanClientConnection.REQUEST_DELIMITER + text;
		new HangmanWordTry(socket, msg, hc);
	}
	
	/**
	 * The method closes client socket.
	 */
	public void closeSocket(){
		try {
			socket.close();
			System.out.println("[LOG] Client socket closed");
		} catch (IOException e) {
			System.err.println("Error closing socket!");
			e.printStackTrace();
		}
	}
	
}
