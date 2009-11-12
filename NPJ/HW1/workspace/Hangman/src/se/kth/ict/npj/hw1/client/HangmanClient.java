package se.kth.ict.npj.hw1.client;

import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JDialog;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JEditorPane;

public class HangmanClient extends JFrame {

	HangmanClientConnection hcc = null; //the TCP connection thread
	static HangmanClient thisClass = null; //the class
	ArrayList<Character> playedCharacters = null;
	ArrayList<Character> playedLostCharacters = null;
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JDialog jDialog = null;  //  @jve:decl-index=0:visual-constraint="351,10"
	private JPanel jContentPane1 = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JTextField jTextField = null;
	private JTextField jTextField1 = null;
	private JButton jButton = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JLabel jLabel5 = null;

	private JButton jButton1 = null;

	private JLabel jLabel6 = null;

	private JLabel jLabel7 = null;

	private JLabel jLabel8 = null;

	private JTextField jTextField2 = null;

	private JButton jButton2 = null;

	private JLabel jLabel9 = null;

	private JTextField jTextField3 = null;

	private JButton jButton3 = null;

	private JLabel jLabel10 = null;

	private JLabel jLabel11 = null;

	private JEditorPane jEditorPane = null;

	private JLabel jLabel12 = null;

	private JLabel jLabel13 = null;

	private JLabel jLabel14 = null;

	/**
	 * This method initializes jDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getJDialog() {
		if (jDialog == null) {
			jDialog = new JDialog(this);
			jDialog.setSize(new Dimension(265, 169));
			jDialog.setTitle("Connect to Hangman Server");
			jDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			jDialog.setContentPane(getJContentPane1());
		}
		return jDialog;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane1() {
		if (jContentPane1 == null) {
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(15, 105, 106, 16));
			jLabel3.setForeground(Color.red);
			jLabel3.setText("");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(15, 75, 46, 16));
			jLabel2.setText("Port:");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(15, 45, 46, 16));
			jLabel1.setText("Host:");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(15, 15, 166, 16));
			jLabel.setText("Insert connection data:");
			jContentPane1 = new JPanel();
			jContentPane1.setLayout(null);
			jContentPane1.add(jLabel, null);
			jContentPane1.add(jLabel1, null);
			jContentPane1.add(jLabel2, null);
			jContentPane1.add(getJTextField(), null);
			jContentPane1.add(getJTextField1(), null);
			jContentPane1.add(getJButton(), null);
			jContentPane1.add(jLabel3, null);
		}
		return jContentPane1;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setBounds(new Rectangle(90, 45, 136, 16));
			jTextField.setText("localhost");
		}
		return jTextField;
	}

	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setBounds(new Rectangle(90, 75, 46, 16));
			jTextField1.setText("9900");
		}
		return jTextField1;
	}

	/**
	 * This method initializes jButton, which is the connect button
	 * used to connect to the HangmanServer	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(135, 105, 106, 16));
			jButton.setText("Connect");
			
			//click listener for the connect button
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("[LOG] Connect button clicked."); // TODO Auto-generated Event stub actionPerformed()

					try {
						jLabel3.setText("Connecting..");
						getJButton().setEnabled(false);
						//try to establish a connection to the server
						hcc = new HangmanClientConnection(getJTextField().getText(), getJTextField1().getText(), thisClass);
						getJDialog().setVisible(false); //if connection established, go to the main (game) frame
						//that mean hide this dialog window
						
						System.out.println("[LOG] Connected.");
					}
					catch (Exception e1) {
						System.out.println("[LOG] Connection failed.");
						getJButton().setEnabled(true);
						jLabel3.setText("Failed"); //if connection failed, inform user
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setBounds(new Rectangle(195, 15, 106, 16));
			jButton1.setText("New Game");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("[LOG] New game button clicked."); // TODO Auto-generated Event stub actionPerformed()
					
					jLabel12.setText("Starting new game...");
					getJButton1().setEnabled(false);
					playedCharacters = new ArrayList<Character>();
					playedLostCharacters = new ArrayList<Character>();
					hcc.startNewGame();
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setBounds(new Rectangle(75, 135, 31, 16));
			jTextField2.setEnabled(false);
		}
		return jTextField2;
	}

	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setBounds(new Rectangle(210, 135, 91, 17));
			jButton2.setEnabled(false);
			jButton2.setText("Try!");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("[LOG] Letter send click."); // TODO Auto-generated Event stub actionPerformed()
					
					String character = getJTextField2().getText();
					if (character.length() != 1) {
						jLabel12.setText("Insert 1 character");
					}
					else {
						Character c = new Character(character.toLowerCase().toCharArray()[0]);
						if (c.compareTo('a') < 0 || c.compareTo('z') > 0) {
							jLabel12.setText("Insert alphabet character");
						}
						else {
							if (playedCharacters.contains(c)) {
								jLabel12.setText("You have played this character");
							}
							else {
								jLabel12.setText("Trying character " + c);
								hcc.tryCharacter(c);
							}
						}
					}
					
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes jTextField3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setBounds(new Rectangle(75, 165, 121, 16));
			jTextField3.setText("");
			jTextField3.setEnabled(false);
		}
		return jTextField3;
	}

	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setBounds(new Rectangle(210, 165, 91, 16));
			jButton3.setEnabled(false);
			jButton3.setText("Try!");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("[LOG] Try word button clicked."); // TODO Auto-generated Event stub actionPerformed()
					
					hcc.tryLetter(getJTextField3().getText());
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jEditorPane	
	 * 	
	 * @return javax.swing.JEditorPane	
	 */
	private JEditorPane getJEditorPane() {
		if (jEditorPane == null) {
			jEditorPane = new JEditorPane();
			jEditorPane.setBounds(new Rectangle(15, 255, 286, 46));
			jEditorPane.setEditable(false);
			jEditorPane.setFont(new Font("Dialog", Font.PLAIN, 16));
			jEditorPane.setText("");
		}
		return jEditorPane;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//HangmanClient thisClass = new HangmanClient();
				thisClass = new HangmanClient();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public HangmanClient() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(333, 381);
		this.setContentPane(getJContentPane());
		this.setTitle("Hangman v0.1");
		getJDialog().setVisible(true); //set the connection dialog
		//visible on startup
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel14 = new JLabel();
			jLabel14.setBounds(new Rectangle(270, 225, 31, 16));
			jLabel14.setText("?");
			jLabel13 = new JLabel();
			jLabel13.setBounds(new Rectangle(210, 225, 61, 16));
			jLabel13.setText("Tries left:");
			jLabel12 = new JLabel();
			jLabel12.setBounds(new Rectangle(15, 315, 286, 16));
			jLabel12.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
			jLabel12.setForeground(Color.red);
			jLabel12.setText("Start a new game");
			jLabel11 = new JLabel();
			jLabel11.setBounds(new Rectangle(15, 240, 91, 16));
			jLabel11.setForeground(new Color(160, 51, 51));
			jLabel11.setText("Wrong letters:");
			jLabel10 = new JLabel();
			jLabel10.setBounds(new Rectangle(135, 195, 38, 16));
			jLabel10.setFont(new Font("Dialog", Font.BOLD, 36));
			jLabel10.setText("__");
			jLabel9 = new JLabel();
			jLabel9.setBounds(new Rectangle(15, 165, 38, 16));
			jLabel9.setText("Word:");
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(15, 135, 46, 16));
			jLabel8.setText("Letter:");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(15, 60, 121, 16));
			jLabel7.setText("Target word:");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(15, 75, 298, 31));
			jLabel6.setFont(new Font("Dotum", Font.BOLD, 30));
			jLabel6.setBackground(new Color(193, 238, 238));
			jLabel6.setForeground(new Color(51, 129, 51));
			jLabel6.setText("start a game");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(90, 15, 31, 19));
			jLabel5.setFont(new Font("Dialog", Font.BOLD, 24));
			jLabel5.setText("0");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(15, 15, 76, 16));
			jLabel4.setText("Your Score:");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getJButton1(), null);
			jContentPane.add(jLabel6, null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(jLabel8, null);
			jContentPane.add(getJTextField2(), null);
			jContentPane.add(getJButton2(), null);
			jContentPane.add(jLabel9, null);
			jContentPane.add(getJTextField3(), null);
			jContentPane.add(getJButton3(), null);
			jContentPane.add(jLabel10, null);
			jContentPane.add(jLabel11, null);
			jContentPane.add(getJEditorPane(), null);
			jContentPane.add(jLabel12, null);
			jContentPane.add(jLabel13, null);
			jContentPane.add(jLabel14, null);
		}
		return jContentPane;
	}
	
	/**
	 * This method enables or disables all the inputs for a game.
	 * These are the letter and word input and the letter and word try buttons
	 * and the new game. It also clears the user inputs if they are becoming
	 * enabled.
	 * 
	 * @param enabled true to enable, false to disable
	 */
	private void gameInputSetEnabled(Boolean enabled) {
		getJButton1().setEnabled(!enabled);
		getJTextField2().setEnabled(enabled);
		getJTextField3().setEnabled(enabled);
		getJButton2().setEnabled(enabled);
		getJButton3().setEnabled(enabled);
		if (enabled) {
			getJTextField2().setText("");
			getJTextField3().setText("");
			getJEditorPane().setText("");
		}
	}
	
	/**
	 * Initializes the GUI when a new game starts.
	 * 
	 * @param score the current score of the player
	 * @param numOfAttempts the remaining number of attempts for this game
	 * @param word the target word
	 * 
	 */
	public void setGameStarted(String word, int numOfAttempts, int score) {
		System.out.println("[LOG] Game started.");
		
		gameInputSetEnabled(true); //enables the inputs
		jLabel12.setText("Game started"); //informs
		jLabel6.setText(word); //sets the target word 
		jLabel14.setText(numOfAttempts+ ""); //sets the number of attempts
		jLabel5.setText(score + ""); //sets the score
	}
	
	/**
	 * This method updates the GUI of the user in order to facilitate a correct or
	 * not character try. It updates the word, the tries left, the message to the
	 * user and the incorrect characters played so far. 
	 * 
	 * @param word the changed (or not) word
	 * @param numOfAttempts how many attempts the user still have
	 * @param c the character that user played, and as a response this method is call
	 * @param correctLetter boolean that indicates if the letter was correct or not
	 */
	public void setCharacterPlayed(String word, int numOfAttempts, Character c, boolean correctLetter) {
		System.out.println("[LOG] Character '" + c + "' played");
		
		jLabel6.setText(word); //update the word field
		playedCharacters.add(c); //add the played character to the played list
		getJTextField2().setText(""); //clear the input textfield
		if (correctLetter) { //if correct letter
			jLabel12.setText("Correct letter " + c); //inform user	
		}
		else { //else
			jLabel12.setText("Wrong letter " + c); //inform user 
			jLabel14.setText(numOfAttempts+ ""); //show the reduced number of attempts 
			playedLostCharacters.add(c); //add the character to the wrong characters
			getJEditorPane().setText(getJEditorPane().getText() + " " + c); //also preview the wrong
			//characters to the user
		}
	}
	
	/**
	 * Informs the user when the game (round) is over.
	 * 
	 * @param won boolean that indicates if won or lost
	 * @param word the target word
	 * @param score the updated score
	 * 
	 */
	public void setPlayerWonOrLost(Boolean won, String word, int score) {
		if (won) { //if player won
			System.out.println("[LOG] Game finished: won"); 
			jLabel12.setText("Found word " + word + "!"); //inform
			jLabel14.setText("?"); //tries remaining
		}
		else {
			System.out.println("[LOG] Game finished: lost"); 
			jLabel12.setText("Lost! The word was " + word); //inform
			jLabel14.setText("0"); //tries remaining
		}
		jLabel6.setText(word); //show the correct word
		jLabel5.setText(score + ""); //update score
		gameInputSetEnabled(false); //disable inputs
	}
	
	/**
	 * This method updates the GUI of the user in order to facilitate a correct or
	 * not word try. It updates the word, the tries left, the message to the
	 * user. 
	 * 
	 * @param word
	 * @param numOfAttempts
	 * @param wordPlayed
	 */
	public void setWordPlayed(String word, int numOfAttempts, String wordPlayed) {
		System.out.println("[LOG] Word '" + wordPlayed + "' played");
		
		jLabel6.setText(word); //update the word field
		getJTextField3().setText(""); //clear the input textfield
		
		jLabel12.setText("Wrong word " + wordPlayed); //inform user 
		jLabel14.setText(numOfAttempts+ ""); //show the reduced number of attempts 
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
