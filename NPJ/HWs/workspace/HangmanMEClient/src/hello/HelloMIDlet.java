package hello;

import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.amms.control.PanControl;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class HelloMIDlet extends MIDlet implements CommandListener {

    private Command exitCommand; // The exit command
    private Display display;     // The display for this MIDlet
    private Command connectCommand;
    private Form connectionForm;

    public HelloMIDlet() {
        display = Display.getDisplay(this);
        exitCommand = new Command("Exit", Command.EXIT, 0);
        connectCommand = new Command("Connect", Command.OK, 0);
    }

    public void startApp() {
        TextBox t = new TextBox("Hello", "Hello, World!", 256, 0);

        t.addCommand(exitCommand);
        t.setCommandListener(this);

        connectionForm = new Form("Connection settings");
        TextField hostField = new TextField("Host", "localhost", 20, TextField.URL);
        hostField.setLayout(TextField.LAYOUT_CENTER);
        connectionForm.append(hostField);
        TextField portField = new TextField("Port", "9900", 5, TextField.DECIMAL);
        connectionForm.append(portField);
        connectionForm.addCommand(connectCommand);
        connectionForm.setCommandListener(this);

        
        display.setCurrent(connectionForm);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
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

    public void commandAction(Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }  else if (c==connectCommand){
            connectToServer(c, s);
        }
    }

}
