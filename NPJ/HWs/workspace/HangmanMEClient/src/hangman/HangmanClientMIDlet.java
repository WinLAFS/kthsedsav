/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hangman;

import hangman.utils.StringTokenizer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;
import javax.microedition.media.control.TempoControl;
import javax.microedition.midlet.*;

/**
 * @author saibbot
 */
public class HangmanClientMIDlet extends MIDlet implements CommandListener {
  private Command exitCommand; // The exit command
    private Display display;     // The display for this MIDlet
    private Command connectCommand;
    private Form connectionForm;
    private Form mainPanelForm;
    private TextField scoreField;
    private StringItem wordItem;
    private Gauge triesLeftGauge;
    private TextField insertField;
    private Command tryLetter;
    private Command tryWord;
    private Command newGame;
    private DataOutputStream out;
    private DataInputStream in;

    public HangmanClientMIDlet() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Exit", Command.EXIT, 1);
        connectCommand = new Command("Connect", Command.OK, 0);
        tryLetter = new Command("Try letter", Command.ITEM, 0);
        tryWord = new Command("Try word", Command.OK, 1);
        newGame = new Command("New game", Command.OK, 0);

    }

    public void startApp() {
        //Connection Screen Form!
        connectionForm = new Form("Connection settings");
        TextField hostField = new TextField("Host", "localhost", 20, TextField.URL);
        hostField.setLayout(TextField.LAYOUT_LEFT);
        connectionForm.append(hostField);
        TextField portField = new TextField("Port", "9900", 5, TextField.DECIMAL);
        connectionForm.append(portField);
        connectionForm.addCommand(connectCommand);
        
        connectionForm.addCommand(exitCommand);
        connectionForm.setCommandListener(this);

        //Main Game Form!
        mainPanelForm = new Form("Play Hangman");

        scoreField = new TextField("Your Score:", "0", 4, TextField.DECIMAL);
        scoreField.setConstraints(TextField.UNEDITABLE);
        scoreField.setLayout(TextField.LAYOUT_LEFT);
        
        mainPanelForm.append(scoreField);

        wordItem = new StringItem("Word:", "Start a game");
        wordItem.setLayout(wordItem.LAYOUT_NEWLINE_AFTER);
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        wordItem.setFont(font);

        mainPanelForm.append(wordItem);

        triesLeftGauge = new Gauge("Tries left: ", false, 5, 5);// TODO
        triesLeftGauge.setLayout(triesLeftGauge.LAYOUT_CENTER);

        mainPanelForm.append(triesLeftGauge);

        insertField = new TextField("Try: ", "", 10, TextField.ANY);
        insertField.setLayout(TextField.LAYOUT_LEFT);

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

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }  else if (c == connectCommand){
            connectToServer(c, s);
        }
        else if (c == newGame) {
            startNewGame();
        }
        else if (c == tryLetter) {
            tryLetter();
        }
        else if (c == tryWord) {
            //tryWord();
        }
    }

    private void connectToServer(Command c, Displayable s){
        try {
            Form myForm = (Form)s;

            TextField hostField = (TextField)myForm.get(0);
            TextField portField = (TextField)myForm.get(1);

            String hostStr = hostField.getString();
            String portStr = portField.getString();

            String conStr = "socket://"+hostStr+":"+portStr;

            //SocketConnection con = (SocketConnection) Connector.open("socket://130.237.250.91:9900");

            SocketConnection con = (SocketConnection) Connector.open(conStr);
      
            out = con.openDataOutputStream();
            in = con.openDataInputStream();

            display.setCurrent(mainPanelForm);
        } catch (IOException ex) {
           informCannotConnectToServer();
        }
    }

    private void informCannotConnectToServer() {
         Alert alert = new Alert("Error", "Can't connect to the server", null, AlertType.ERROR);
         alert.setTimeout(5000);
         display.vibrate(500);
         display.setCurrent(alert , connectionForm);
    }

    private void startNewGame() {
        try {
                System.err.println("OOO sending new word");
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

                wordItem.setText(word);
                triesLeftGauge.setMaxValue(triesLeft);
                triesLeftGauge.setValue(triesLeft);
                scoreField.setString(score);

                mainPanelForm.removeCommand(newGame);
                mainPanelForm.addCommand(tryLetter);
                mainPanelForm.addCommand(tryWord);
            } catch (IOException ex) {
                informCannotConnectToServer();
            }
    }

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
        } catch (IOException ex) {
            informCannotConnectToServer();
        }

        /*
         * fail
         *
         */

        String wordR = "aa"; //to be removed
         Alert alert = new Alert("Lost!", "The correct word was: " + wordR, null, AlertType.INFO);
         alert.setTimeout(4000);
         display.setCurrent(alert);
         wordItem.setText(wordR);
         triesLeftGauge.setValue(0);
         String scoreR = "" ; //to be remoced
         scoreField.setString(scoreR);
         insertField.setString("");
         mainPanelForm.removeCommand(tryWord);
         mainPanelForm.removeCommand(tryLetter);
         mainPanelForm.addCommand(newGame);
    }
}
