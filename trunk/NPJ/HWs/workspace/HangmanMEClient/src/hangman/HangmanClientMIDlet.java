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
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.*;

/**
 * @author saibbot
 */
public class HangmanClientMIDlet extends MIDlet implements CommandListener {
  private Command exitCommand; // The exit command
    private Display display;     // The display for this MIDlet
    private Command connectCommand;
    private Form connectionForm;

    public HangmanClientMIDlet() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Exit", Command.EXIT, 0);
        connectCommand = new Command("Connect", Command.OK, 0);

    }

    public void startApp() {
        connectionForm = new Form("Connection settings");
        TextField hostField = new TextField("Host", "localhost", 20, TextField.URL);
        hostField.setLayout(TextField.LAYOUT_CENTER);
        connectionForm.append(hostField);
        TextField portField = new TextField("Port", "9900", 5, TextField.DECIMAL);
        connectionForm.append(portField);
        connectionForm.addCommand(connectCommand);
        
        connectionForm.addCommand(exitCommand);
        connectionForm.setCommandListener(this);

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

            DataOutputStream out = con.openDataOutputStream();

            out.writeChars("start");
            out.flush();
        } catch (IOException ex) {
            display.setCurrent(new Alert("Error", "Can't connect to the server", null, AlertType.ERROR), connectionForm);
            //ex.printStackTrace();
        }
    }

}
