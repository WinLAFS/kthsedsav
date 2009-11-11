package se.kth.ict.npj.hw1.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import se.kth.ict.npj.hw1.server.constants.HangmanServerConstants;
import se.kth.ict.npj.hw1.server.objects.HangmanGame;
import se.kth.ict.npj.hw1.server.objects.HangmanStatistics;

/**
 * The class have methods with server's logic. They analyze request type and
 * perform necessary actions.
 * 
 */
public class HangmanServerLogic {

	private HangmanGame game;
	private HangmanStatistics statistics = new HangmanStatistics();
	private boolean currentLetterCorrect=false;

	public void processRequests(PrintWriter pw, BufferedReader br)
			throws IOException {
		String inputLine = null;
		while ((inputLine = br.readLine()) != null) {
			if (inputLine.equalsIgnoreCase(HangmanServerConstants.CLIENT_END)) {
				break;
			}
			processSingleRequest(inputLine, pw);
		}
	}

	/**
	 * The method process request from the client. It defines request type,
	 * checks for the word and sends corresponding responce message.
	 * 
	 * @param requestString
	 *            String of client request
	 * @param pw
	 *            PrintWriter to socket to send responce
	 */
	protected void processSingleRequest(String requestString, PrintWriter pw) {
		StringTokenizer st = new StringTokenizer(requestString,
				HangmanServerConstants.REQUEST_DELIMITER);
		String requestType = st.nextToken();
		if (requestType
				.equalsIgnoreCase(HangmanServerConstants.CLIENT_NEW_GAME)) {
			game = new HangmanGame();
			String word = generateWord();
			
			game.setRealWord(word.toLowerCase());
			game.setCurrentWord(generateHiddenWord(word.length()));
			game.setAttemptsLeft(word.length() / 2);

			String resultString = HangmanServerConstants.SERVER_PLAY
					+ HangmanServerConstants.REQUEST_DELIMITER
					+ game.getCurrentWord()
					+ HangmanServerConstants.REQUEST_DELIMITER
					+ game.getAttemptsLeft()
					+ HangmanServerConstants.REQUEST_DELIMITER
					+ statistics.getScore();
			pw.println(resultString);
			pw.flush();
		} else if (requestType.equalsIgnoreCase(HangmanServerConstants.CLIENT_SEND_LETTER)) {
			String resultString = "";
			String letter = st.nextToken().toLowerCase();
			String newWord = checkLetter(game.getRealWord(), game
					.getCurrentWord(), letter);

			game.setCurrentWord(newWord);

			if (newWord.indexOf("-") < 0) {
				game.setWon(true);
				resultString += HangmanServerConstants.SERVER_WON;
				statistics.setScore(statistics.getScore() + 1);
			} else if (game.getAttemptsLeft() < 1) {
				resultString += HangmanServerConstants.SERVER_FAIL;
				statistics.setScore(statistics.getScore() - 1);
			} else {
				resultString += HangmanServerConstants.SERVER_PLAY;
			}

			resultString += HangmanServerConstants.REQUEST_DELIMITER
					+ game.getCurrentWord()
					+ HangmanServerConstants.REQUEST_DELIMITER
					+ game.getAttemptsLeft()
					+ HangmanServerConstants.REQUEST_DELIMITER
					+ statistics.getScore()
					+ HangmanServerConstants.REQUEST_DELIMITER
					+ letter
					+ HangmanServerConstants.REQUEST_DELIMITER
					+ currentLetterCorrect;
			pw.println(resultString);
			pw.flush();
		} else if (requestType.equalsIgnoreCase(HangmanServerConstants.CLIENT_SEND_WORD)) {
			String resultString = "";
			String proposedWord = st.nextToken().toLowerCase();
			boolean correctWord = checkWord(game.getRealWord(), proposedWord);

			if (game.getAttemptsLeft() < 1) {
				resultString += HangmanServerConstants.SERVER_FAIL
						+ HangmanServerConstants.REQUEST_DELIMITER
						+ game.getCurrentWord()
						+ HangmanServerConstants.REQUEST_DELIMITER
						+ game.getAttemptsLeft()
						+ HangmanServerConstants.REQUEST_DELIMITER
						+ statistics.getScore();
				statistics.setScore(statistics.getScore() - 1);
			} else {
				if (correctWord) {
					game.setWon(true);
					resultString += HangmanServerConstants.SERVER_WON
							+ HangmanServerConstants.REQUEST_DELIMITER
							+ game.getRealWord()
							+ HangmanServerConstants.REQUEST_DELIMITER
							+ game.getAttemptsLeft()
							+ HangmanServerConstants.REQUEST_DELIMITER
							+ statistics.getScore();
					statistics.setScore(statistics.getScore() + 1);
				} else {
					resultString += HangmanServerConstants.SERVER_PLAY
							+ HangmanServerConstants.REQUEST_DELIMITER
							+ game.getCurrentWord()
							+ HangmanServerConstants.REQUEST_DELIMITER
							+ game.getAttemptsLeft()
							+ HangmanServerConstants.REQUEST_DELIMITER
							+ statistics.getScore();
				}
			}

			pw.println(resultString);
			pw.flush();
		}
	}

	/**
	 * The method generates string like '-----' with a given length
	 * 
	 * @param length
	 *            Length of the needed string
	 * @return Needed length string.
	 */
	protected String generateHiddenWord(int length) {
		String word = "";
		for (int i = 0; i < length; i++) {
			word += "-";
		}
		return word;
	}

	/**
	 * The method checks if the word has a given letter.
	 * 
	 * @param realWord
	 *            The real word to guess.
	 * @param currentWord
	 *            The string with current progress of opening the word.
	 * @param letter
	 *            Letter that user wants to check.
	 * @return New currentWord. It may have new open letters
	 */
	protected String checkLetter(String realWord, String currentWord,
			String letter) {
		String newWord = currentWord;
		String temp = realWord;
		int prevIndex = 0;
		boolean letterFound = false;

		while (temp.indexOf(letter, prevIndex) >= 0) {
			int position = temp.indexOf(letter, prevIndex);
			prevIndex = position + 1;
			newWord = newWord.substring(0, position) + letter
					+ newWord.substring(position + 1);
			letterFound = true;
		}

		if (!letterFound) {
			game.setAttemptsLeft(game.getAttemptsLeft() - 1);
			currentLetterCorrect=false;
		} else {
			currentLetterCorrect=true;
		}

		return newWord;
	}

	/**
	 * The method check if the proposed word is equal to a real word.
	 * 
	 * @param realWord
	 *            Real word to be open.
	 * @param proposedWord
	 *            Proposed by user word
	 * @return True if the words match
	 */
	protected boolean checkWord(String realWord, String proposedWord) {
		if (!realWord.equalsIgnoreCase(proposedWord)) {
			game.setAttemptsLeft(game.getAttemptsLeft() - 1);
			return false;
		} else {
			return true;
		}
	}
	
	protected String generateWord(){
		return "programming";
	}
}
