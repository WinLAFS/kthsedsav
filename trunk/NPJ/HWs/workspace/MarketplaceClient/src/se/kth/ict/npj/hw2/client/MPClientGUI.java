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

public class MPClientGUI extends JFrame {

	//Client Logic object
	private MPClientLogic logic;
	private MPClientImpl impl;
	
	public static MPClientGUI thisClass;
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JDialog jDialog = null;  //  @jve:decl-index=0:visual-constraint="547,12"
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

	/**
	 * This method initializes jDialog	
	 * 	
	 * @return javax.swing.JDialog	
	 */
	private JDialog getJDialog() {
		if (jDialog == null) {
			jDialog = new JDialog(this);
			jDialog.setSize(new Dimension(291, 204));
			jDialog.setTitle("Connection options");
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
			jLabel12 = new JLabel();
			jLabel12.setBounds(new Rectangle(16, 128, 145, 16));
			jLabel12.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel12.setForeground(new Color(201, 45, 45));
			jLabel12.setText("");
			jLabel3 = new JLabel();
			jLabel3.setBounds(new Rectangle(17, 99, 85, 16));
			jLabel3.setText("Server port");
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(17, 68, 84, 16));
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
			jTextField1.setBounds(new Rectangle(109, 64, 129, 20));
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
			jTextField2.setBounds(new Rectangle(109, 95, 49, 20));
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
			jButton.setBounds(new Rectangle(166, 124, 88, 22));
			jButton.setText("Connect");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					jButton.setEnabled(false);
					jLabel12.setText("Connecting to server...");
					
					logic.connectToServer(jTextField.getText(), jTextField1.getText(), jTextField2.getText());
					
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
			
//			  String data[][] = {{"CD", "234"},
//	                     {"laptop", "234"},
//	                     {"Phone", "123"},
//	                     {"WWW", "10000000"},
//			  };
//
//			  String fields[] = {"Item", "Price"};
//			 
//	  
//			jTable = new JTable(data, fields);
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
//					Item i1 = new Item();
//					i1.setName("name01");
//					i1.setPrice(1000);
//					i1.setOwner("me");
//					
//					ArrayList<Item> items = new ArrayList<Item>();
//					items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);items.add(i1);
//					 updateItemsList(items);
//					 
//					 int selrow = jTable.getSelectedRow();
//					 System.out.println( jTable.getModel().getValueAt(1, 1));
					
					logic.updateItems();
				}
			});
			
		}
		return jButton1;
	}
	
	public void updateItemsList(ArrayList<Item> items){
		Vector<String> columns = new Vector<String>();
		columns.add("Name");
		columns.add("Price");
		columns.add("Owner");
		
		Vector<Vector> products = new Vector<Vector>();
	
		for(int i=0; i<items.size(); i++){
			Item item= items.get(i);
			Vector<String> product = new Vector<String>();
			product.add(item.getName());
			product.add(item.getPrice()+"");
			product.add(item.getOwnerPretty());
			
			products.add(product);
		}
		
		jTable.setModel(new DefaultTableModel(products,columns));
		jTable.revalidate();
	  	jTable.repaint();
	}
	
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
			jTextField3.setBounds(new Rectangle(355, 45, 106, 20));
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
			jTextField4.setBounds(new Rectangle(355, 74, 106, 20));
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
			jButton2.setBounds(new Rectangle(307, 100, 84, 19));
			jButton2.setText("Sell item");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					logic.sellItem(getJTextField3().getText(), getJTextField4().getText());
					logic.updateItems();
				}
			});
		}
		return jButton2;
	}
	
	public void clearSellItemForm(){
		getJTextField3().setText("");
		getJTextField4().setText("");
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
						String itemOwner = jTable.getModel().getValueAt(selrow, 2).toString();
						
						logic.buyItem(itemName, itemPrice, itemOwner);
						
						logic.updateItems();
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
			jTextField5.setBounds(new Rectangle(355, 171, 106, 20));
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
			jTextField6.setBounds(new Rectangle(355, 197, 106, 20));
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
			jButton4.setBounds(new Rectangle(308, 227, 84, 19));
			jButton4.setText("Submit");
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					logic.placeWish(jTextField5.getText(), jTextField6.getText());
				}
			});
		}
		return jButton4;
	}
	
	public void clearWishForm(){
		getJTextField5().setText("");
		getJTextField6().setText("");
	}

	/**
	 * @param args
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
		this.setSize(491, 333);
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
			jLabel11 = new JLabel();
			jLabel11.setBounds(new Rectangle(38, 260, 362, 27));
			jLabel11.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel11.setBounds(new Rectangle(6, 276, 459, 19));
			jLabel11.setText("");
			jLabel11.setText("NOTIFICATION AREA");
			jLabel10 = new JLabel();
			jLabel10.setBounds(new Rectangle(307, 198, 38, 16));
			jLabel10.setText("Price");
			jLabel9 = new JLabel();
			jLabel9.setBounds(new Rectangle(307, 174, 38, 16));
			jLabel9.setText("Name");
			jLabel8 = new JLabel();
			jLabel8.setBounds(new Rectangle(307, 150, 91, 16));
			jLabel8.setText("Place a 'wish':");
			jLabel7 = new JLabel();
			jLabel7.setBounds(new Rectangle(307, 76, 38, 16));
			jLabel7.setText("Price");
			jLabel6 = new JLabel();
			jLabel6.setBounds(new Rectangle(307, 47, 38, 16));
			jLabel6.setText("Name");
			jLabel5 = new JLabel();
			jLabel5.setBounds(new Rectangle(307, 22, 63, 16));
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
		}
		return jContentPane;
	}

	/**
	 * This method is used to inform the user for a sold item.
	 * 
	 * @param item
	 */
	public synchronized void notifyItemSold(Item item) {
		jLabel11.setText("The item " + item.getName() + " / " + item.getPrice() + " was sold.");
	}
	
	/**
	 * This method is used to inform the user for an item that match one
	 * item from his wish list.
	 * 
	 * @param item
	 */
	public synchronized void notifyWishListItemFound(Item item) {
		jLabel11.setText("The item " + item.getName() + " / " + item.getPrice() + " is matching an item from your wish list.");
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
