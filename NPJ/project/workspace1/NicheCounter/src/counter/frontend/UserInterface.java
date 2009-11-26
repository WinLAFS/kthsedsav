package counter.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

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
    Random generator = new Random();

    public UserInterface(FrontendComponent applicationInterface) {
        myAI = applicationInterface;
        createGUI();
    }

    private void createGUI() {
        helloAnyButton = new JButton("Increase");
        helloAnyButton.setVerticalTextPosition(AbstractButton.CENTER);
        helloAnyButton.setHorizontalTextPosition(AbstractButton.LEADING);
        // helloAnyButton.setMnemonic(KeyEvent.VK_D);
        // helloAnyButton.setActionCommand("disable");

        //helloAllButton = new JButton("HelloAll");
        helloAllButton = new JButton("Increase 100");
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
        
        add(helloAnyButton);
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
        frame.setSize(340, 70);
    }

    private void helloAny() {
    	System.out.println("[ui]> Invoking Counter");
    	int roundId = generator.nextInt();
		correctCount++;
		myAI.increaseCounter(roundId);
		labelCounter.setText(correctCount+"");
    }

    private void helloAll() {//TODO
        System.out.println("[ui]> Invoking Counter");
        //myAI.helloAll();
        

//        myAI.increaseCounter("NICHE");
//        for (int i = 0; i < 100; i++) {
//        	myAI.increaseCounter("NICHE");
//        }

        for (int i = 0; i < 100; i++) {
        int roundId = generator.nextInt();
		correctCount++;
		myAI.increaseCounter(roundId);
		labelCounter.setText(correctCount+"");
	        try {
				Thread.sleep(50);
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
