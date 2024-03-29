/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hangman;

import hangman.utils.StringTokenizer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.m3g.Transform;
import javax.microedition.midlet.*;

/**
 * Midlet
 * Client for Hangman game for mobile devices..
 */
public class HangmanClientMIDlet extends MIDlet implements CommandListener {

    private Command exitCommand; // The exit command
    private Display display;     // The display for this MIDlet
    private Command connectCommand;
    private Form connectionForm;
    private Form mainPanelForm;
    private StringItem scoreField;
    private StringItem wordItem;
    private Gauge triesLeftGauge;
    private TextField insertField;
    private Command tryLetter;
    private Command tryWord;
    private Command newGame;
    private DataOutputStream out;
    private DataInputStream in;
    private SocketConnection con;

    private int imageNormalHeight;
    private int overallNumberOfAttempts;
    private Image mutableImage;
    private ImageItem imgItem;

    /**
     * Constructor.
     * Initialization of commands and display goes here.
     */
    public HangmanClientMIDlet() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Exit", Command.EXIT, 1);
        connectCommand = new Command("Connect", Command.OK, 0);
        tryLetter = new Command("Try letter", Command.ITEM, 0);
        tryWord = new Command("Try word", Command.OK, 1);
        newGame = new Command("New game", Command.OK, 0);

    }

    /**
     * startApp method of midlet.
     * Initializes items and forms, displays welcome screen.
     */
    public void startApp() {
        //Connection Screen Form!
        connectionForm = new Form("Connection settings");
        TextField hostField = new TextField("Host", "130.237.81.100", 20, TextField.URL);
        hostField.setLayout(TextField.LAYOUT_LEFT);
        connectionForm.append(hostField);
        TextField portField = new TextField("Port", "9900", 5, TextField.DECIMAL);
        connectionForm.append(portField);
        connectionForm.addCommand(connectCommand);

        connectionForm.addCommand(exitCommand);
        connectionForm.setCommandListener(this);

        //Main Game Form!
        mainPanelForm = new Form("Play Hangman");

        scoreField = new StringItem("Your score: ", "0", StringItem.PLAIN);
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        scoreField.setFont(font);
        scoreField.setLayout(wordItem.LAYOUT_NEWLINE_AFTER);

        mainPanelForm.append(scoreField);
//        mainPanelForm.append("\n");

        wordItem = new StringItem("Word:  ", "Start a game");
        wordItem.setLayout(wordItem.LAYOUT_NEWLINE_AFTER);
        wordItem.setFont(font);

        mainPanelForm.append(wordItem);
//        mainPanelForm.append("\n");

        
        try {
            mutableImage = Image.createImage("/images/hangman.png");
            imageNormalHeight = mutableImage.getHeight();
            Image chImage = Image.createImage(mutableImage, 0,0, mutableImage.getWidth(), mutableImage.getHeight(), Sprite.TRANS_NONE);
            imgItem = new ImageItem("", chImage, ImageItem.LAYOUT_CENTER, "");
            mainPanelForm.append(imgItem);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        triesLeftGauge = new Gauge("Tries left:  ", false, 5, 5);
        triesLeftGauge.setLayout(triesLeftGauge.LAYOUT_CENTER);

        mainPanelForm.append(triesLeftGauge);

        insertField = new TextField("Try: ", "", 10, TextField.ANY);
        insertField.setLayout(TextField.LAYOUT_LEFT);
        insertField.setInitialInputMode("UCB_BASIC_LATIN");

        mainPanelForm.append(insertField);

        mainPanelForm.addCommand(newGame);
        mainPanelForm.addCommand(exitCommand);

        mainPanelForm.setCommandListener(this);


        try {
            Image hangmanImage = Image.createImage("/images/hangman.png");

            Alert welcomeScreen = new Alert("Hangman Mobile!", "Welcome to Hangman for Mobile", hangmanImage, AlertType.CONFIRMATION);
            welcomeScreen.setTimeout(10000);

            display.setCurrent(welcomeScreen, connectionForm);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * pauseApp method of midlet
     */
    public void pauseApp() {
    }

    /**
     * destroyApp method of midlet
     * Closing connection, streams.
     */
    public void destroyApp(boolean unconditional) {
        try {
            out.close();
            in.close();
            con.close();
        } catch (Exception ex) {}
    }

    /**
     * Method handles user commands and invokes corresponding methods
     * of the midlet.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if (c == connectCommand) {
            connectToServer(c, s);
        } else if (c == newGame) {
            startNewGame();
        } else if (c == tryLetter) {
            tryLetter();
        } else if (c == tryWord) {
            tryWord();
        }
    }

    /**
     * Method opens socket connection to the server and creates input
     * and output streams to communicate with server.
     */
    private void connectToServer(Command c, Displayable s) {
        try {
            Form myForm = (Form) s;

            TextField hostField = (TextField) myForm.get(0);
            TextField portField = (TextField) myForm.get(1);

            String hostStr = hostField.getString();
            String portStr = portField.getString();

            String conStr = "socket://" + hostStr + ":" + portStr;

            //SocketConnection con = (SocketConnection) Connector.open("socket://130.237.250.91:9900");

            con = (SocketConnection) Connector.open(conStr);

            out = con.openDataOutputStream();
            in = con.openDataInputStream();

            display.setCurrent(mainPanelForm);
        } catch (IOException ex) {
            informCannotConnectToServer();
        }
    }

    /**
     * Method shows error alert when connection to the server fails.
     */
    private void informCannotConnectToServer() {
        Alert alert = new Alert("Error", "Can't connect to the server", null, AlertType.ERROR);
        alert.setTimeout(5000);
        display.vibrate(500);
        display.setCurrent(alert, connectionForm);
    }

    /**
     * Method starts a new game. It sends start message to the server and,
     * parses input string and updates display.
     */
    private void startNewGame() {
        try {
            out.writeUTF("start\n");

            out.flush();

            String input = "";
            int ch = 0;

            while (ch != 0x0d) {
                ch = in.read();
                input += (char) ch;
                System.err.print((char) ch);
            }
            System.err.println();

            StringTokenizer st = new StringTokenizer(input, ",");
            st.nextToken();
            String word = st.nextToken();
            String attemptsLeft = st.nextToken();
            int triesLeft = Integer.parseInt(attemptsLeft);
            String score = st.nextToken();
            overallNumberOfAttempts = triesLeft;

            wordItem.setText(word);
            triesLeftGauge.setMaxValue(triesLeft);
            triesLeftGauge.setValue(triesLeft);
            scoreField.setText(score);

            mainPanelForm.removeCommand(newGame);
            mainPanelForm.addCommand(tryLetter);
            mainPanelForm.addCommand(tryWord);

            imgItem.setImage(null);
        } catch (IOException ex) {
            informCannotConnectToServer();
        }
    }

    /**
     * Method sends a message to the server with a proposed word.
     */
    private void tryWord() {
        try {
            String word = insertField.getString();
            if (word.length() == 1) {
                tryLetter();
                return;
            }
            String sendStr = "word," + word + "\n";

            out.writeUTF(sendStr);

            handleResponce();
        } catch (IOException e) {
            informCannotConnectToServer();
        }
    }

    /**
     * Method handles response from the server.
     * Depending on operation result dispay is updated.
     */
    private void handleResponce() throws IOException {
        String inputStr = "";
        int ch = 0;

        while (ch != 0x0d) {
            ch = in.read();
            inputStr += (char) ch;
        }
        inputStr = inputStr.trim();

        StringTokenizer st = new StringTokenizer(inputStr, ",");
        String operation = st.nextToken();

        String wordR = st.nextToken();
        String attemptsR = st.nextToken();
        String scoreR = st.nextToken();
        String letterR = "";
        String correctLetterR = "";

        try {
            letterR = st.nextToken();
            correctLetterR = st.nextToken();
        } catch (NoSuchElementException e) {
        }

        if (operation.equalsIgnoreCase("won")) {
            Alert alert = new Alert("Won!", "You found the word: " + wordR, null, AlertType.INFO);
            alert.setTimeout(4000);
            display.setCurrent(alert);
            wordItem.setText(wordR);
            triesLeftGauge.setValue(0);
            scoreField.setText(scoreR);
            insertField.setString("");
            mainPanelForm.removeCommand(tryWord);
            mainPanelForm.removeCommand(tryLetter);
            mainPanelForm.addCommand(newGame);
        } else if (operation.equalsIgnoreCase("play")) {
            handleCorrectAttempt(wordR, attemptsR, scoreR, letterR, correctLetterR);
        } else if (operation.equalsIgnoreCase("fail")) {
            Alert alert = new Alert("Lost!", "The correct word was: " + wordR, null, AlertType.INFO);
            alert.setTimeout(4000);
            display.setCurrent(alert);
            wordItem.setText(wordR);
            triesLeftGauge.setValue(0);
            scoreField.setText(scoreR);
            insertField.setString("");
            mainPanelForm.removeCommand(tryWord);
            mainPanelForm.removeCommand(tryLetter);
            mainPanelForm.addCommand(newGame);
        }
    }

    /**
     * Method handles result of the operation that not leads to the end of the game.
     * It shows alert and updates display values.
     */
    private void handleCorrectAttempt(String wordR, String attemptsR, String scoreR, String letterR, String correctLetterR) {
        Alert alert;
        if (correctLetterR.equalsIgnoreCase("true")) {
            alert = new Alert("Correct!", letterR + " is correct", null, AlertType.INFO);
        } else {
            alert = new Alert("Inorrect!", letterR + " is incorrect", null, AlertType.INFO);
            Image newImage = Image.createImage( mutableImage, 0, imageNormalHeight*Integer.parseInt(attemptsR)/overallNumberOfAttempts, mutableImage.getWidth(), imageNormalHeight-imageNormalHeight*Integer.parseInt(attemptsR)/overallNumberOfAttempts, Sprite.TRANS_NONE);
            imgItem.setImage(newImage);
        }

        alert.setTimeout(2000);

        wordItem.setText(wordR);
        triesLeftGauge.setValue(Integer.parseInt(attemptsR));
        scoreField.setText(scoreR);
        insertField.setString("");

        display.setCurrent(alert);
    }

    /**
     * Method sends a message to the server with a proposed letter.
     */
    private void tryLetter() {
        String userInput = insertField.getString();
        if (userInput.length() != 1) {
            Alert alert = new Alert("Error input", "You should insert only one character!", null, AlertType.WARNING);
            alert.setTimeout(4000);
            display.setCurrent(alert);
            return;
        }

        String request = "letter," + userInput + "\n";
        try {
            out.writeUTF(request);
            out.flush();
            handleResponce();
        } catch (IOException ex) {
            informCannotConnectToServer();
        }

    }
}
