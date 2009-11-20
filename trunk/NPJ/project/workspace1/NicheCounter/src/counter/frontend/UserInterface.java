package counter.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class UserInterface extends JPanel {
    private FrontendComponent myAI = null;

    /* UI elements */
    JButton helloAnyButton, helloAllButton;

    public UserInterface(FrontendComponent applicationInterface) {
        myAI = applicationInterface;
        createGUI();
    }

    private void createGUI() {
        helloAnyButton = new JButton("HelloAny");
        helloAnyButton.setVerticalTextPosition(AbstractButton.CENTER);
        helloAnyButton.setHorizontalTextPosition(AbstractButton.LEADING);
        // helloAnyButton.setMnemonic(KeyEvent.VK_D);
        // helloAnyButton.setActionCommand("disable");

        //helloAllButton = new JButton("HelloAll");
        helloAllButton = new JButton("Increase");
        helloAllButton.setVerticalTextPosition(AbstractButton.CENTER);
        helloAllButton.setHorizontalTextPosition(AbstractButton.LEADING);
        // helloAllButton.setMnemonic(KeyEvent.VK_M);

        // Listen for actions on buttons 1 and 3.
        helloAnyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                helloAny();
            }
        });
        helloAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                helloAll();
            }
        });
        //add(helloAnyButton);
        add(helloAllButton);

        // Create and set up the window.
        JFrame frame = new JFrame("Counter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the content pane.
        setOpaque(true); // content panes must be opaque
        frame.setContentPane(this);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private void helloAny() {
        System.out.println("UI says: Invoking HelloAny");
        myAI.helloAny();
    }

    private void helloAll() {
        System.out.println("UI says: Invoking HelloAll");
        //myAI.helloAll();
        myAI.increaseCounter("a");
    }
}
