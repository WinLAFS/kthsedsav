package counter.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dks.niche.exceptions.OperationTimedOutException;

/**
 * The class draws GUI and invokes {@link FrontendComponent} to increase counter
 * 
 */
public class UserInterface extends JPanel {
	private FrontendComponent myAI = null;

	/* UI elements */
	JButton countButton, count100Button;
	JLabel labelCounter;

	int correctCount = 0;
	Random generator = new Random();

	/**
	 * Constructor
	 * 
	 * @param applicationInterface
	 *            {@link FrontendComponent} objects which is linked with the GUI
	 */
	public UserInterface(FrontendComponent applicationInterface) {
		myAI = applicationInterface;
		createGUI();
	}

	/**
	 * The method creates a frame with 2 buttons and label
	 */
	private void createGUI() {
		countButton = new JButton("Increase");
		countButton.setVerticalTextPosition(AbstractButton.CENTER);
		countButton.setHorizontalTextPosition(AbstractButton.LEADING);

		count100Button = new JButton("Increase 100");
		count100Button.setVerticalTextPosition(AbstractButton.CENTER);
		count100Button.setHorizontalTextPosition(AbstractButton.LEADING);
		// helloAllButton.setMnemonic(KeyEvent.VK_M);

		// Listen for actions on buttons 1 and 3.
		countButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				count();
			}
		});
		count100Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				count100();
			}
		});

		labelCounter = new JLabel("");
		labelCounter.setText("0");

		add(countButton);
		add(count100Button);
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

	/**
	 * Method creates a single increase counter call to
	 * {@link FrontendComponent}
	 */
	private void count() {
		System.out.println("[ui]> Invoking Counter");
		int roundId = generator.nextInt();
		correctCount++;
		try {
			myAI.increaseCounter(roundId);
			labelCounter.setText(correctCount + "");
		}
		catch (OperationTimedOutException e) {
			labelCounter.setText("Error.");
			correctCount--;
		}
	}

	/**
	 * Method makes 100 increase counter calls to {@link FrontendComponent}.
	 * Calls are made witha small delay.
	 */
	private void count100() {
		System.out.println("[ui]> Invoking Counter 100");

		for (int i = 0; i < 100; i++) {
			int roundId = generator.nextInt();
			correctCount++;
			myAI.increaseCounter(roundId);
			labelCounter.setText(correctCount + "");
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				System.err.println("[ui]> InterruptedException in GUI");
			}
		}
	}
}
