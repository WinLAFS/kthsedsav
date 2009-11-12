package se.kth.ict.npj.hw1.server.objects;

public class HangmanGame {
	private String realWord;
	private String currentWord;
	private int attemptsLeft;
	private boolean won = false;
	private boolean failed = false;
	
	public boolean isFailed() {
		return failed;
	}
	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	public boolean isWon() {
		return won;
	}
	public void setWon(boolean won) {
		this.won = won;
	}
	public String getRealWord() {
		return realWord;
	}
	public void setRealWord(String realWord) {
		this.realWord = realWord;
	}
	public String getCurrentWord() {
		return currentWord;
	}
	public void setCurrentWord(String currentWord) {
		this.currentWord = currentWord;
	}
	public int getAttemptsLeft() {
		return attemptsLeft;
	}
	public void setAttemptsLeft(int attemptsLeft) {
		this.attemptsLeft = attemptsLeft;
	}
	
	
}
