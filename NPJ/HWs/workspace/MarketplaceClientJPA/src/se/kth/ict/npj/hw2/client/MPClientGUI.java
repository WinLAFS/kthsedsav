package se.kth.ict.npj.hw2.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import se.kth.ict.npj.hw2.Item;
import se.kth.ict.npj.hw2.client.objects.MPClientImpl;
import se.kth.ict.npj.hw2.server.objects.UserStatistics;

import javax.swing.JPasswordField;

public class MPClientGUI extends JFrame {

	//Client Logic object
	private MPClientLogic logic;
	private MPClientImpl impl;
	
	public static MPClientGUI thisClass;
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JDialog jDialog = null;  //  @jve:decl-index=0:visual-constraint="548,10"
	private JPanel jContentPane1 = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JTextField jTextField = null;
	private JTextField jTextField1 = null;
	private JTextField jTextField2 = null;
	private JButton jButton = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JLabel jLabel4 = null;
	private JButton jButton1 = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel7 = null;
	private JTextField jTextField3 = null;
	private JTextField jTextField4 = null;
	private JButton jButton2 = null;
	private JButton jButton3 = null;
	private JLabel jLabel8 = null;
	private JLabel jLabel9 = null;
	private JLabel jLabel10 = null;
	private JTextField jTextField5 = null;
	private JTextField jTextField6 = null;
	private JButton jButton4 = null;
	private JLabel jLabel11 = null;
	private JLabel jLabel12 = null;
	private JLabel jLabel13 = null;
	private JPasswordField jPasswordField = null;
	private JButton jButton5 = null;
	private JLabel jLabel14 = null;
	private JTextField jTextField7 = null;
	private JLabel jLabel15 = null;
	private JLabel jLabel16 = null;
	private JLabel jLabel17 = null;
	private JLabel jLabel18 = null;
	private JLabel jLabel19 = null;
	/**
	 * This method initializes jDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getJDialog() {
		if (jDialog == null) {
			jDialog = new JDialog(this);
			jDialog.setSize(new Dimension(282, 251));
			jDialog.setTitle("Connection options");
			jDialog.setResizable(false);
			jDialog.setContentPane(getJContentPane1());
		}
		return jDialog;
	}

	/**
	 * This method initializes jContentPane1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane1() {
		if (jContentPane1 == null) {
			jLabel13 = new JLabel();
			jLabel13.setBounds(new Rectangle(17, 69, 85, 16));
			jLabel13.setText("Password");
			jLabel12 = new JLabel();
			jLabel12.setBounds(new Rectangle(17, 202, 252, 16));
			jLabel12.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel12.setForeground(new Color(201, 45, 45));
			jLabel12.setText("");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(17, 134, 85, 16));
			jLabel3.setText("Server port");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(17, 103, 85, 16));
			jLabel2.setText("Server host");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(17, 39, 85, 16));
			jLabel1.setText("User name");
			jLabel = new JLabel();
			jLabel.setText("Enter connection details");
			jLabel.setBounds(new Rectangle(18, 6, 143, 16));
			jContentPane1 = new JPanel();
			jContentPane1.setLayout(null);
			jContentPane1.add(jLabel, null);
			jContentPane1.add(jLabel1, null);
			jContentPane1.add(jLabel2, null);
			jContentPane1.add(jLabel3, null);
			jContentPane1.add(getJTextField(), null);
			jContentPane1.add(getJTextField1(), null);
			jContentPane1.add(getJTextField2(), null);
			jContentPane1.add(getJButton(), null);
			jContentPane1.add(jLabel12, null);
			jContentPane1.add(jLabel13, null);
			jContentPane1.add(getJPasswordField(), null);
			jContentPane1.add(getJButton5(), null);
		}
		return jContentPane1;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setBounds(new Rectangle(109, 35, 128, 20));
			jTextField.setText("Andrei");
		}
		return jTextField;
	}

	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setBounds(new Rectangle(109, 99, 128, 20));
			jTextField1.setText("localhost");
		}
		return jTextField1;
	}

	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setBounds(new Rectangle(109, 130, 49, 20));
			jTextField2.setText("1099");
		}
		return jTextField2;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setBounds(new Rectangle(17, 167, 88, 22));
			jButton.setText("Connect");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(jTextField.getText()!=null && jPasswordField.getPassword()!=null && !jTextField.getText().equals("") && jPasswordField.getPassword().length>0){
						jButton.setEnabled(false);
						jLabel12.setText("Connecting to server...");
						
						logic.connectToServer(jTextField.getText(), jTextField1.getText(), jTextField2.getText(), new String(jPasswordField.getPassword()));
						
						try {
							System.out.println("[LOG] MPClient url: " + logic.getUserName());
							
							try {
								Naming.rebind(logic.getUserName() , new MPClientImpl(logic.getUserName(), thisClass));
							} catch (MalformedURLException e1) {
								System.out.println("[LOG] The url was not correct formed: " + e1.getMessage());
							}
							
						} catch (RemoteException e2) {
							System.out.println("[LOG] Could not start the MPClient: " + e2.getMessage());
						}
						setTitle(jTextField.getText()+" at Marketplace client v 0.1");
					} else {
						jLabel12.setText("Enter username and password");
					}
				}
			});
		}
		return jButton;
	}
	
	/**
	 * The method is called when client successfully connected to server
	 */
	public void connectionSuccessful(){
		jButton.setEnabled(true);
		getJDialog().setVisible(false);
		System.out.println("[LOG] Connected.");
	}
	
	/**
	 * Method is called in case of error while connecting to server
	 * 
	 * @param errorMsg Error message to be displayed
	 */
	public void connectionError(String errorMsg){
		jLabel12.setText(errorMsg);
		jButton.setEnabled(true);
	}
	

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(19, 42, 253, 149));
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			
			jTable = new JTable();
			jTable.setBounds(new Rectangle(0, 0, 450, 80));
			
			
		}
		return jTable;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setBounds(new Rectangle(188, 196, 80, 21));
			jButton1.setText("Refresh");
			
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					logic.updateItems();
				}
			});
			
		}
		return jButton1;
	}
	
	/**
	 * Method updates list of selling items on GUI
	 * 
	 * @param items ArrayList of selling items
	 */
	public void updateItemsList(Vector<se.kth.ict.npj.hw2.server.objects.Item> items){
		Vector<String> columns = new Vector<String>();
		columns.add("Name");
		columns.add("Price");
		columns.add("Owner");
		columns.add("Quantity");
		columns.add("FullOwner");
		
		Vector<Vector> products = new Vector<Vector>();
	
		for(int i=0; i<items.size(); i++){
			se.kth.ict.npj.hw2.server.objects.Item item= items.get(i);
			Vector<String> product = new Vector<String>();
			product.add(item.getItemName());
			product.add(item.getPrice()+"");
			product.add(item.getSeller().getUsername());
			product.add(new Integer(item.getQuantity()).toString());
			product.add(item.getSeller().getUserURL());
			
			products.add(product);
		}
		
		jTable.setModel(new DefaultTableModel(products,columns));
		jTable.getColumnModel().getColumn(4).setMaxWidth(0);
		jTable.revalidate();
	  	jTable.repaint();
	}
	
	/**
	 * Method writes a notification message on the GUI
	 * 
	 * @param notification Notification message
	 */
	public void setNotificationMessage(String notification){
		jLabel11.setText(notification);
	}

	/**
	 * This method initializes jTextField3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setBounds(new Rectangle(354, 45, 106, 20));
		}
		return jTextField3;
	}

	/**
	 * This method initializes jTextField4	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setBounds(new Rectangle(354, 74, 106, 20));
		}
		return jTextField4;
	}

	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setBounds(new Rectangle(295, 140, 84, 19));
			jButton2.setText("Sell item");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String qStr = jTextField7.getText();
					int q = new Integer(qStr);
					
					if(q>0){
						logic.sellItem(getJTextField3().getText(), getJTextField4().getText(), q);
						logic.updateItems();
					} else {
						jLabel11.setText("Quantity should be positive number");
					}
				}
			});
		}
		return jButton2;
	}
	
	/**
	 * Method clears field of selling item form
	 */
	public void clearSellItemForm(){
		getJTextField3().setText("");
		getJTextField4().setText("");
		getJTextField7().setText("");
	}

	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setBounds(new Rectangle(69, 196, 109, 21));
			jButton3.setText("Buy selected");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int selrow = jTable.getSelectedRow();
					if(selrow>=0){
						String itemName = jTable.getModel().getValueAt(selrow, 0).toString();
						String itemPrice = jTable.getModel().getValueAt(selrow, 1).toString();
						String itemOwner = jTable.getModel().getValueAt(selrow, 3).toString();
						
						logic.buyItem(itemName, itemPrice, itemOwner);
						
						
					} else {
						setNotificationMessage("Select item to sell first");
					}
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jTextField5	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField5() {
		if (jTextField5 == null) {
			jTextField5 = new JTextField();
			jTextField5.setBounds(new Rectangle(355, 259, 106, 20));
		}
		return jTextField5;
	}

	/**
	 * 
	 * This method initializes jTextField6	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField6() {
		if (jTextField6 == null) {
			jTextField6 = new JTextField();
			jTextField6.setBounds(new Rectangle(355, 285, 106, 20));
		}
		return jTextField6;
	}

	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setBounds(new Rectangle(297, 315, 84, 19));
			jButton4.setText("Submit");
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					logic.placeWish(jTextField5.getText(), jTextField6.getText());
				}
			});
		}
		return jButton4;
	}
	
	/**
	 * Method clears fields of wish item form
	 */
	public void clearWishForm(){
		getJTextField5().setText("");
		getJTextField6().setText("");
	}

	/**
	 * This method initializes jPasswordField	
	 * 	
	 * @return javax.swing.JPasswordField	
	 */
	private JPasswordField getJPasswordField() {
		if (jPasswordField == null) {
			jPasswordField = new JPasswordField();
			jPasswordField.setBounds(new Rectangle(109, 68, 128, 20));
		}
		return jPasswordField;
	}

	/**
	 * This method initializes jButton5	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton5() {
		if (jButton5 == null) {
			jButton5 = new JButton();
			jButton5.setBounds(new Rectangle(153, 167, 88, 22));
			jButton5.setText("Register");
			
			jButton5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(jTextField.getText()!=null && jPasswordField.getPassword()!=null && !jTextField.getText().equals("") && jPasswordField.getPassword().length>0){
						jButton.setEnabled(false);
						jLabel12.setText("Connecting to server...");
						
						logic.registerOnServer(jTextField.getText(), jTextField1.getText(), jTextField2.getText(), new String(jPasswordField.getPassword()));
						
						try {
							System.out.println("[LOG] MPClient url: " + logic.getUserName());
							
							try {
								Naming.rebind(logic.getUserName() , new MPClientImpl(logic.getUserName(), thisClass));
							} catch (MalformedURLException e1) {
								System.out.println("[LOG] The url was not correct formed: " + e1.getMessage());
							}
							
						} catch (RemoteException e2) {
							System.out.println("[LOG] Could not start the MPClient: " + e2.getMessage());
						}
						setTitle(jTextField.getText()+" at Marketplace client v 0.1");
					} else {
						jLabel12.setText("Enter username and password");
					}
				}
			});
		}
		return jButton5;
	}

	/**
	 * This method initializes jTextField7	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField7() {
		if (jTextField7 == null) {
			jTextField7 = new JTextField();
			jTextField7.setBounds(new Rectangle(354, 105, 106, 20));
		}
		return jTextField7;
	}

	/**
	 * Main method of the client
	 * 
	 * @param args List of arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				thisClass = new MPClientGUI();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public MPClientGUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(493, 434);
		this.setContentPane(getJContentPane());
		this.setTitle("Marketplace client v 0.1");
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				logic.unregisterUser();
			}
		});
		
		this.getJDialog().setVisible(true);
		
		logic = new MPClientLogic(this);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel19 = new JLabel();
			jLabel19.setBounds(new Rectangle(124, 286, 38, 16));
			jLabel19.setText("0");
			jLabel18 = new JLabel();
			jLabel18.setBounds(new Rectangle(124, 262, 38, 16));
			jLabel18.setText("0");
			jLabel17 = new JLabel();
			jLabel17.setBounds(new Rectangle(19, 286, 87, 16));
			jLabel17.setText("Items Bought:");
			jLabel16 = new JLabel();
			jLabel16.setBounds(new Rectangle(19, 262, 70, 16));
			jLabel16.setText("Items Sold:");
			jLabel15 = new JLabel();
			jLabel15.setBounds(new Rectangle(82, 238, 61, 16));
			jLabel15.setText("Statistics:");
			jLabel14 = new JLabel();
			jLabel14.setBounds(new Rectangle(295, 107, 51, 16));
			jLabel14.setText("Quantity");
			jLabel11 = new JLabel();
			jLabel11.setBounds(new Rectangle(14, 360, 448, 27));
			jLabel11.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel11.setText("");
			jLabel11.setText("NOTIFICATION AREA");
			jLabel10 = new JLabel();
			jLabel10.setBounds(new Rectangle(296, 286, 38, 16));
			jLabel10.setText("Price");
			jLabel9 = new JLabel();
			jLabel9.setBounds(new Rectangle(296, 262, 38, 16));
			jLabel9.setText("Name");
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(296, 238, 91, 16));
			jLabel8.setText("Place a 'wish':");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(295, 76, 38, 16));
			jLabel7.setText("Price");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(295, 47, 38, 16));
			jLabel6.setText("Name");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(295, 22, 63, 16));
			jLabel5.setText("Sell Item:");
			jLabel4 = new JLabel();
			jLabel4.setBounds(new Rectangle(19, 22, 98, 16));
			jLabel4.setText("Items for sale:");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJScrollPane(), null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(getJButton1(), null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(jLabel6, null);
			jContentPane.add(jLabel7, null);
			jContentPane.add(getJTextField3(), null);
			jContentPane.add(getJTextField4(), null);
			jContentPane.add(getJButton2(), null);
			jContentPane.add(getJButton3(), null);
			jContentPane.add(jLabel8, null);
			jContentPane.add(jLabel9, null);
			jContentPane.add(jLabel10, null);
			jContentPane.add(getJTextField5(), null);
			jContentPane.add(getJTextField6(), null);
			jContentPane.add(getJButton4(), null);
			jContentPane.add(jLabel11, null);
			jContentPane.add(jLabel14, null);
			jContentPane.add(getJTextField7(), null);
			jContentPane.add(jLabel15, null);
			jContentPane.add(jLabel16, null);
			jContentPane.add(jLabel17, null);
			jContentPane.add(jLabel18, null);
			jContentPane.add(jLabel19, null);
		}
		return jContentPane;
	}

	/**
	 * This method is used to inform the user for a sold item.
	 * 
	 * @param item
	 */
	public synchronized void notifyItemSold(se.kth.ict.npj.hw2.server.objects.Item item) {
		jLabel11.setText("The item " + item.getItemName() + " / " + item.getPrice() + " was sold.");
	}
	
	/**
	 * This method is used to inform the user for an item that match one
	 * item from his wish list.
	 * 
	 * @param item
	 */
	public synchronized void notifyWishListItemFound(se.kth.ict.npj.hw2.server.objects.Item item) {
		jLabel11.setText("The item " + item.getItemName() + " / " + item.getPrice() + " is matching an item from your wish list.");
	}
	
	public synchronized void notifyStatisticsChanged(UserStatistics statistics) {
		jLabel18.setText(new Integer(statistics.getSellsNumber()).toString());
		jLabel19.setText(new Integer(statistics.getBuysNumber()).toString());
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
