package se.kth.ict.npj.hw1.client;

import java.net.Socket;

/**
 * HangmanClientConnection is the responsible object for the TCP
 * connection of the client with the Hangman Server. It provides
 * the interface to GUI to start a new game, try a word and
 * try a letter
 * 
 */
public class HangmanClientConnection {
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
	 * 
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
	 * 
	 */
	public void startNewGame() {
		new HangmanStartGame(socket, HangmanClientConnection.CLIENT_NEW_GAME, hc);
	}
	
	/**
	 * 
	 */
	public void tryCharacter(Character character) {
		String msg = HangmanClientConnection.CLIENT_SEND_LETTER + HangmanClientConnection.REQUEST_DELIMITER + character;
		new HangmanLetterTry(socket, msg, hc);
	}

	public void tryWord(String text) {
		String msg = HangmanClientConnection.CLIENT_SEND_WORD + HangmanClientConnection.REQUEST_DELIMITER + text;
		new HangmanWordTry(socket, msg, hc);
	}
	
}
