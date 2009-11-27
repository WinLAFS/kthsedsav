package hello;

import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class HelloMIDlet extends MIDlet implements CommandListener {

    private Command exitCommand; // The exit command
    private Display display;     // The display for this MIDlet

    public HelloMIDlet() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Exit", Command.EXIT, 0);
    }

    public void startApp() {
        TextBox t = new TextBox("Hello", "Hello, World!", 256, 0);

        t.addCommand(exitCommand);
        t.setCommandListener(this);
        try {
            SocketConnection con = (SocketConnection) Connector.open("socket://130.237.250.91:9900");
            DataOutputStream out = con.openDataOutputStream();
            out.writeChars("start");
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        display.setCurrent(t);
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
