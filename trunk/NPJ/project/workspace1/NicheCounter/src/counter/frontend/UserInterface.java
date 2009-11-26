package counter.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UserInterface extends JPanel {
    private FrontendComponent myAI = null;

    /* UI elements */
    JButton helloAnyButton, helloAllButton;
    JLabel labelCounter;
    
    int correctCount = 0;

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
        
        labelCounter = new JLabel("");
        labelCounter.setText("0");
        
        //add(helloAnyButton);
        add(helloAllButton);
        add(labelCounter);

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

    private void helloAll() {//TODO
        System.out.println("[ui]> Invoking Counter");
        //myAI.helloAll();
        
        for (int i = 0; i < 100; i++) {
	        myAI.increaseCounter("NICHE");
	        correctCount++;
	        labelCounter.setText(correctCount+"");
	        try {
				Thread.sleep(70);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
//        for (int i = 0; i < 100; i++) {
//        	myAI.increaseCounter("NICHE");
//        }
    }
}
