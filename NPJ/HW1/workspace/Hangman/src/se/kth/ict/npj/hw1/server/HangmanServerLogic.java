package se.kth.ict.npj.hw1.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import se.kth.ict.npj.hw1.server.objects.HangmanGame;
import se.kth.ict.npj.hw1.server.objects.HangmanStatistics;

/**
 * The class have methods with server's logic.
 * They analyze request type and perform necessary actions.
 *
 */
public class HangmanServerLogic {
	
	private HangmanGame game;
	private HangmanStatistics statistics = new HangmanStatistics();
	
	public void processRequests(PrintWriter pw, BufferedReader br) throws IOException{
		String inputLine = null;
		while ((inputLine = br.readLine()) != null) {	
		   if(inputLine.equalsIgnoreCase(HangmanServerConstants.CLIENT_END)){
			   break;
		   }
		   processSingleRequest(inputLine, pw);
		}
	}
	
	protected void processSingleRequest(String requestString, PrintWriter pw){
		StringTokenizer st = new StringTokenizer(requestString, HangmanServerConstants.REQUEST_DELIMITER);
		String requestType=st.nextToken();
		if(requestType.equalsIgnoreCase(HangmanServerConstants.CLIENT_NEW_GAME)){
			game = new HangmanGame();
			//TODO change words generation
			String word = "programming";
			game.setRealWord(word);
			game.setCurrentWord(generateHiddenWord(word.length()));
			game.setAttemptsLeft(word.length()/2);
			
			String resultString = HangmanServerConstants.SERVER_PLAY+HangmanServerConstants.REQUEST_DELIMITER+
					game.getCurrentWord()+HangmanServerConstants.REQUEST_DELIMITER+
					game.getAttemptsLeft()+HangmanServerConstants.REQUEST_DELIMITER+
					statistics.getScore();
			pw.println(resultString);
			pw.flush();
		} else if(requestType.equalsIgnoreCase(HangmanServerConstants.CLIENT_SEND_LETTER)){
			String resultString = "";
			String letter = st.nextToken();
			String newWord = checkLetter(game.getRealWord(), game.getCurrentWord(), letter);
			
			game.setCurrentWord(newWord);
			
			if(newWord.indexOf("-")<0){
				game.setWon(true);
				resultString+=HangmanServerConstants.SERVER_WON;
			}else if(game.getAttemptsLeft()<1){
				resultString+=HangmanServerConstants.SERVER_FAIL;
			}else{
				resultString+= HangmanServerConstants.SERVER_PLAY;
			}
			
			resultString += HangmanServerConstants.REQUEST_DELIMITER+
					game.getCurrentWord()+HangmanServerConstants.REQUEST_DELIMITER+
					game.getAttemptsLeft()+HangmanServerConstants.REQUEST_DELIMITER+
					statistics.getScore();
			pw.println(resultString);
			pw.flush();
		} else if(requestType.equalsIgnoreCase(HangmanServerConstants.CLIENT_SEND_WORD)){
			
		}
	}
	
	protected String generateHiddenWord(int length){
		String word = "";
		for(int i=0;i<length;i++){
			word+="-";
		}
		return word;
	}
	
	protected String checkLetter(String realWord, String currentWord, String letter){
		String newWord=currentWord;
		String temp=realWord;
		int prevIndex=0;
		boolean letterFound = false;
		
		while(temp.indexOf(letter, prevIndex)>=0){
			int position = temp.indexOf(letter, prevIndex);
			prevIndex = position+1;
			newWord = newWord.substring(0, position)+letter+newWord.substring(position+1);
			letterFound=true;
		}
		
		if(!letterFound){
			game.setAttemptsLeft(game.getAttemptsLeft()-1);
		}
		
		return newWord;
	}
}
