/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hangman;

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
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.*;

/**
 * @author saibbot
 */
public class HangmanClientMIDlet extends MIDlet implements CommandListener {
 private Command exitCommand; // The exit command
    private Display display;     // The display for this MIDlet

    public HangmanClientMIDlet() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Exit", Command.EXIT, 0);

    }

    public void startApp() {
        try {
           Image hangmanImage = Image.createImage("/images/hangman.png");
  
            Alert welcomeScreen = new Alert("Hangman Mobile!", "Welcome to Hangman for Mobile", hangmanImage, AlertType.CONFIRMATION);
            welcomeScreen.setTimeout(10000);
            display.setCurrent(welcomeScreen);
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
        }
    }

}
