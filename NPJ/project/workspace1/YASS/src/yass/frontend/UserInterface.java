package yass.frontend;

import java.io.File;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import yass.interfaces.Frontend;


public class UserInterface implements UserInterfaceInterface{ 
 	

	//Shared objects
	
	//private final  String DRIVE_A = "a:" + File.separator; 	private final  String DRIVE_B = "b:" + File.separator;

	private  final String WORKING_DIRECTORY = System.getProperty("user.dir"); //""; //"/home/joel/MyWorkbench/MySamples";
	private  final String welcomePicture = WORKING_DIRECTORY + "/Blank.png";
	
	
	private  Frontend myAI = null;
		
	/* UI elements */ 	
	public Display display; 
	private Shell shell;
	

	private Label numObjectsLabel;
	private Label diskSpaceLabel;
	
	private File currentDirectory = null;
	private boolean initial = true;
	
	////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Test Params ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	//
	private int TEST_MODE = -1;
	
	private static final Semaphore SYNC = new Semaphore(1);
	private static final int[]	TEST_CACHE_SIZE = {210*4, 50*4, 20*4};  //{R, W, D} 
	private static final int[]	TEST_CACHE_RATIO = {20, 5, 2};  //{R, W, D} 
	private int[]	testCacheAck = {0, 0, 0, 0, 0, 0};  //{R, W, D, RError, WError, DError}
	private int[]	testCacheStat = {0, 0, 0};  //{R, W, D}
	private int		testCacheOpId;
	private long	testCacheOpStart;
	
	private long testCacheStart;
	private long testCacheEnd;
	//
	////////////////////////////////////////////////////////////////////////////
		
	/* Combo view */
	private  final String COMBODATA_ROOTS = "Combo.roots";
		// File[]: Array of files whose paths are currently displayed in the combo
	private  final String COMBODATA_LASTTEXT = "Combo.lastText";
		// String: Previous selection text string

	private Combo searchPathCombo;

	/* Tree view */
	private IconCache iconCache = new IconCache();
	private  final String TREEITEMDATA_FILE = "TreeItem.file";
		// File: File associated with tree item
	private  final String TREEITEMDATA_IMAGEEXPANDED = "TreeItem.imageExpanded";
		// Image: shown when item is expanded
	private  final String TREEITEMDATA_IMAGECOLLAPSED = "TreeItem.imageCollapsed";
		// Image: shown when item is collapsed
	private  final String TREEITEMDATA_STUB = "TreeItem.stub";
		// Object: if not present or null then the item has not been populated

	private Tree tree;
	private Label treeScopeLabel;

	/* Table view */
	private  final DateFormat dateFormat = DateFormat.getDateTimeInstance(
		DateFormat.MEDIUM, DateFormat.MEDIUM);
	private  final String TABLEITEMDATA_FILE = "TableItem.file";
		// File: File associated with table row
	private  final String TABLEDATA_DIR = "Table.dir";
		// File: Currently visible directory
	
	/* Image view */
	
	private  Label fileDisplayArea;
	
	private  Button testButton;
	private  Button testCacheButton;
	
	private  Button storeButton;
	private  Button retrieveButton;
	private  Button removeButton;
	
	private Combo delayCombo;
	private String[] delays = new String[]{"0", "100", "500", "1000", "2000", "5000"};
	
	private  final int[] tableWidths = new int[] {150, 60};
	private  final String[] tableTitles = new String [] { "Name","Size"};
	
	private  String currentPicturePath; 
	
	private  Table table;
	private static Table remoteFileTable ;
	
	private  Composite composite;
	private  File currentLocalFile;
	private String lastKnownFileName; 
	
	private  Vector remoteFileVector = new Vector();
	
	
	private final int SETTINGS_WIDTH = 600;
	private final int SETTINGS_HEIGHT = 400;
	
	private Label tableContentsOfLabel;
		
	/* Table update worker */
	// Control data
	private final Object workerLock = new Object();
		// Lock for all worker control data and state
	private volatile Thread  workerThread = null;
		// The worker's thread
	private volatile boolean workerStopped = false;
		// True if the worker must exit on completion of the current cycle
	private volatile boolean workerCancelled = false;
		// True if the worker must cancel its operations prematurely perhaps due to a state update

	// Worker state information -- this is what gets synchronized by an update
	private volatile File workerStateDir = null;

	// State information to use for the next cycle
	private volatile File workerNextDir = null;

	// Shared objects
	
	private SashForm fileArea;
	//private TabFolder infoTabFolder;  
			
	
	
	
	
	//private  SettingsInterface mySI;
	//private  DatabaseInterface myDatabaseInterface;
//	private  PhotoSharingInterface myPSI;
//	private  MetadataTags myMetadataTags;

	private  TableItem newSelectedTagItem, lastSelectedTagItem;
	
	private Text propertyText; 
	private  Text valueText;
	
	private  Label nameLabel;
	private  Label sizeLabel;
	private  Label statusLabel;
	
	
	/**
	 * Runs main program.
	 */
	public  void run(Frontend applicationInterface, int testMode)
	{
		myAI = applicationInterface;
		TEST_MODE = testMode;
		//myAI.registerUI(UserInterface);
		
		display = new Display ();

		//currentTagsVector = new Vector();
		//display.asyncExec(applicationInterface);
		
		//UserInterface application = new UserInterface();
		
		//Shell shell = application.open(display);
		//System.out.println("Before death");
		Shell shell = this.open(display);
		//System.out.println("In death?");
		while (! shell.isDisposed()) {
			//System.out.println("More death?");
			if (! display.readAndDispatch()) display.sleep();
		}
		//application.close();
		this.close();
		display.dispose();
		
		
	}
	

	/**
	 * Opens the main program.
	 */
	public Shell open(Display display) {		
		// Create the window
		this.display = display;
		iconCache.initResources(display);
		shell = new Shell();
		createShellContents();
		refreshFiles(null);
		
		notifySelectedDirectory(new File(WORKING_DIRECTORY));
		shell.open();
		return shell;
	}

	/**
	 * Closes the main program.
	 */
	void close() {
		workerStop();
		iconCache.freeResources();
	}
	

	/**
	 * Construct the UI
	 * 
	 * @param container the ShellContainer managing the Shell we are rendering inside
	 */
	private void createShellContents() {
		shell.setText("Title"); //JEDIT	
		shell.setImage(iconCache.stockImages[iconCache.shellIcon]);
		Menu bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);
	
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3; //yo 3
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		shell.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.widthHint = 185;
		createComboView(shell, gridData);
		
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		
		
		SashForm sashForm = new SashForm(shell, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gridData.horizontalSpan = 3; //3
		
		sashForm.setLayoutData(gridData);
		createTreeView(sashForm);
		createTableView(sashForm);
		createFileView(sashForm);
		createNextTableView(sashForm);
		
		sashForm.setWeights(new int[] { 1, 1, 2, 1}); //
		
		
		numObjectsLabel = new Label(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 185;
		numObjectsLabel.setLayoutData(gridData);
		
		diskSpaceLabel = new Label(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		diskSpaceLabel.setLayoutData(gridData);
	}
	
	
		
		/**
	
	/**
	 * Creates the combo box view.
	 * 
	 * @param parent the parent control
	 */
	private void createComboView(Composite parent, Object layoutData) {
		searchPathCombo = new Combo(parent, SWT.NONE);
		searchPathCombo.setLayoutData(layoutData);
		
		
		searchPathCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				final String lastText = (String) searchPathCombo.getData(COMBODATA_LASTTEXT);
				String text = searchPathCombo.getText();
				if (text == null) return;
				if (lastText != null && lastText.equals(text)) return;
				searchPathCombo.setData(COMBODATA_LASTTEXT, text);
				
				notifySelectedDirectory(new File(text));
			}
		});
		
	}

	/**
	 * Creates the file tree view.
	 * 
	 * @param parent the parent control
	 */
	private void createTreeView(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);

		treeScopeLabel = new Label(composite, SWT.BORDER);
		treeScopeLabel.setText("Local folders");
		treeScopeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		tree = new Tree(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				final TreeItem[] selection = tree.getSelection();
				if (selection != null && selection.length != 0) {
					TreeItem item = selection[0];
					File file = (File) item.getData(TREEITEMDATA_FILE);
				
					notifySelectedDirectory(file);
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				final TreeItem[] selection = tree.getSelection();
				if (selection != null && selection.length != 0) {
					TreeItem item = selection[0];
					item.setExpanded(true);
					treeExpandItem(item);
				}
			}
		});
		tree.addTreeListener(new TreeAdapter() {
			public void treeExpanded(TreeEvent event) {
				final TreeItem item = (TreeItem) event.item;
				final Image image = (Image) item.getData(TREEITEMDATA_IMAGEEXPANDED);
				if (image != null) item.setImage(image);
				treeExpandItem(item);
			}
			public void treeCollapsed(TreeEvent event) {
				final TreeItem item = (TreeItem) event.item;
				final Image image = (Image) item.getData(TREEITEMDATA_IMAGECOLLAPSED);
				if (image != null) item.setImage(image);
			}
		});
		//createTreeDragSource(tree);
		//createTreeDropTarget(tree);
	}


	/**
	 * Handles expand events on a tree item.
	 * 
	 * @param item the TreeItem to fill in
	 */
	private void treeExpandItem(TreeItem item) {
		shell.setCursor(iconCache.stockCursors[iconCache.cursorWait]);
		final Object stub = item.getData(TREEITEMDATA_STUB);
		if (stub == null) treeRefreshItem(item, true);
		shell.setCursor(iconCache.stockCursors[iconCache.cursorDefault]);
	}
	
	/**
	 * Traverse the entire tree and update only what has changed.
	 * 
	 * @param roots the root directory listing
	 */
	private void treeRefresh(File[] masterFiles) {
		TreeItem[] items = tree.getItems();
		int masterIndex = 0;
		int itemIndex = 0;
		for (int i = 0; i < items.length; ++i) {
			final TreeItem item = items[i];
			final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterIndex == masterFiles.length)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			final File masterFile = masterFiles[masterIndex];
			int compare = compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(item, false);
				++itemIndex;
				++masterIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(tree, SWT.NONE, itemIndex);
				treeInitVolume(newItem, masterFile);
				new TreeItem(newItem, SWT.NONE); // placeholder child item to get "expand" button
				++itemIndex;
				++masterIndex;
				--i;
			} else {
				// should appear after file, delete stale item
				item.dispose();
			}
		}
		for (;masterIndex < masterFiles.length; ++masterIndex) {
			final File masterFile = masterFiles[masterIndex];
			TreeItem newItem = new TreeItem(tree, SWT.NONE);
			treeInitVolume(newItem, masterFile);
			new TreeItem(newItem, SWT.NONE); // placeholder child item to get "expand" button
		}		
	}
	
	/**
	 * Traverse an item in the tree and update only what has changed.
	 * 
	 * @param dirItem the tree item of the directory
	 * @param forcePopulate true iff we should populate non-expanded items as well
	 */
	private void treeRefreshItem(TreeItem dirItem, boolean forcePopulate) {
		final File dir = (File) dirItem.getData(TREEITEMDATA_FILE);
		
		if (! forcePopulate && ! dirItem.getExpanded()) {
			// Refresh non-expanded item
			if (dirItem.getData(TREEITEMDATA_STUB) != null) {
				treeItemRemoveAll(dirItem);
				new TreeItem(dirItem, SWT.NONE); // placeholder child item to get "expand" button
				dirItem.setData(TREEITEMDATA_STUB, null);
			}
			return;
		}
		// Refresh expanded item
		dirItem.setData(TREEITEMDATA_STUB, this); // clear stub flag

		/* Get directory listing */
		File[] subFiles = (dir != null) ? this.getDirectoryList(dir) : null;
		if (subFiles == null || subFiles.length == 0) {
			/* Error or no contents */
			treeItemRemoveAll(dirItem);
			dirItem.setExpanded(false);
			return;
		}

		/* Refresh sub-items */
		TreeItem[] items = dirItem.getItems();
		final File[] masterFiles = subFiles;
		int masterIndex = 0;
		int itemIndex = 0;
		File masterFile = null;
		for (int i = 0; i < items.length; ++i) {
			while ((masterFile == null) && (masterIndex < masterFiles.length)) {
				masterFile = masterFiles[masterIndex++];
				if (! masterFile.isDirectory()) masterFile = null;
			}

			final TreeItem item = items[i];
			final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterFile == null)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			int compare = compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(item, false);
				masterFile = null;
				++itemIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(dirItem, SWT.NONE, itemIndex);
				treeInitFolder(newItem, masterFile);
				new TreeItem(newItem, SWT.NONE); // add a placeholder child item so we get the "expand" button
				masterFile = null;
				++itemIndex;
				--i;
			} else {
				// should appear after file, delete stale item
				item.dispose();
			}
		}
		while ((masterFile != null) || (masterIndex < masterFiles.length)) {
			if (masterFile != null) {
				TreeItem newItem = new TreeItem(dirItem, SWT.NONE);
				treeInitFolder(newItem, masterFile);
				new TreeItem(newItem, SWT.NONE); // add a placeholder child item so we get the "expand" button
				if (masterIndex == masterFiles.length) break;
			}
			masterFile = masterFiles[masterIndex++];
			if (! masterFile.isDirectory()) masterFile = null;
		}
	}

	/**
	 * Foreign method: removes all children of a TreeItem.
	 * @param treeItem the TreeItem
	 */
	private  void treeItemRemoveAll(TreeItem treeItem) {
		final TreeItem[] children = treeItem.getItems();
		for (int i = 0; i < children.length; ++i) {
			children[i].dispose();
		}
	}

	/**
	 * Initializes a folder item.
	 * 
	 * @param item the TreeItem to initialize
	 * @param folder the File associated with this TreeItem
	 */
	private void treeInitFolder(TreeItem item, File folder) {
		item.setText(folder.getName());
		item.setImage(iconCache.stockImages[iconCache.iconClosedFolder]);
		item.setData(TREEITEMDATA_FILE, folder);
		item.setData(TREEITEMDATA_IMAGEEXPANDED, iconCache.stockImages[iconCache.iconOpenFolder]);
		item.setData(TREEITEMDATA_IMAGECOLLAPSED, iconCache.stockImages[iconCache.iconClosedFolder]);
	}

	/**
	 * Initializes a volume item.
	 * 
	 * @param item the TreeItem to initialize
	 * @param volume the File associated with this TreeItem
	 */
	private void treeInitVolume(TreeItem item, File volume) {
		item.setText(volume.getPath());
		item.setImage(iconCache.stockImages[iconCache.iconClosedDrive]);
		item.setData(TREEITEMDATA_FILE, volume);
		item.setData(TREEITEMDATA_IMAGEEXPANDED, iconCache.stockImages[iconCache.iconOpenDrive]);
		item.setData(TREEITEMDATA_IMAGECOLLAPSED, iconCache.stockImages[iconCache.iconClosedDrive]);
	}

	/**
	 * Creates the file details table.
	 * 
	 * @param parent the parent control
	 */
	private void createTableView(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		tableContentsOfLabel = new Label(composite, SWT.BORDER);
		tableContentsOfLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		for (int i = 0; i < tableTitles.length; ++i) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(tableTitles[i]);
			column.setWidth(tableWidths[i]);
		}
		table.setHeaderVisible(true);
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				notifySelectedFiles(getSelectedFiles());
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				doDefaultFileAction(getSelectedFiles());
			}
			private File[] getSelectedFiles() {
				final TableItem[] items = table.getSelection();
				final File[] files = new File[items.length];
				
				for (int i = 0; i < items.length; ++i) {
					files[i] = (File) items[i].getData(TABLEITEMDATA_FILE);
				}
				return files;
			}
		});

	}
	
	/**
	 * Creates the remote file details table.
	 * 
	 * @param parent the parent control
	 */
	private void createNextTableView(Composite parent) {
		
		composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		
		Label remoteFileLabel = new Label(composite, SWT.BORDER);
		
		remoteFileLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		remoteFileLabel.setText("Stored files");
		
		remoteFileTable = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		remoteFileTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		
		TableColumn rSubject = new TableColumn(remoteFileTable, SWT.NONE);
		rSubject.setText("Name");
		rSubject.setWidth(200);
		
		remoteFileTable.setHeaderVisible(true);
		//remoteFileTable.setVisible(true);
		
//		remoteFileTable.addListener(SWT.SetData, new Listener() {
//		    public void handleEvent(Event e) {
//		      TableItem item = (TableItem)e.item;
//		      int index = remoteFileTable.indexOf(item);
//		      item.setText("Item ");
//		      
//		    }
//
//			
//		  });
		
		remoteFileTable.addMouseListener(
				new MouseAdapter ()
				{
					
					public void mouseDoubleClick(MouseEvent e)
					{
						String uniqueName = (String)remoteFileVector.get(remoteFileTable.getSelectionIndex());
						//System.out.println("uniqueName: "+uniqueName);
						retrieveRemoteFile(uniqueName);
					}
				});
		
		Group retrieveFileGroup = new Group(composite, SWT.NONE);
		retrieveFileGroup.setLayout (new RowLayout ());
		retrieveFileGroup.setText("");		
		retrieveButton = new Button(retrieveFileGroup, SWT.PUSH);
		retrieveButton.setText("Retrieve file");
		retrieveButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				
				int index = remoteFileTable.getSelectionIndex();
				if(index >= 0) {
					String uniqueName = (String)remoteFileVector.get(remoteFileTable.getSelectionIndex());
					System.out.println("File to retrieve: "+uniqueName);
					retrieveRemoteFile(uniqueName);
				}
				else {
					System.out.println("Select a file first");
				}
					
			}
		});
		
		Group removeFileGroup = new Group(composite, SWT.NONE);
		removeFileGroup.setLayout (new RowLayout ());
		removeFileGroup.setText("");		
		removeButton = new Button(removeFileGroup, SWT.PUSH);
		removeButton.setText("Remove file");
		removeButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				
				int index = remoteFileTable.getSelectionIndex();
				if(index >= 0) {
					String uniqueName = (String)remoteFileVector.get(remoteFileTable.getSelectionIndex());
					//System.out.println("File to remove: "+uniqueName);
					removeRemoteFile(uniqueName);
				}
				else {
					System.out.println("Select a file first");
				}
					
			}
		});
				
		

	}

	
	/**
	 * Creates the file details table.
	 * 
	 * @param parent the parent control
	 */
	private void createFileView(Composite parent)
	{
	    //System.out.println("java.library.path:" + System.getProperty("java.library.path"));
	    //System.load("/usr/lib/swt-mozilla-gtk-3349.so");
	    //System.out.println("pos1");
	    //System.loadLibrary("swt-mozilla-gtk-3349.so");
	    //System.loadLibrary("swt-mozilla-gtk-3349");
	    //System.out.println("pos2");
		
		fileArea= new SashForm (parent, SWT.VERTICAL | SWT.FILL );
		
		//fileTabFolder = new TabFolder(fileArea, SWT.NONE);
		
		
		

		/***************************** SHOW CURRENT FILE INFO *************************************************/
		
		//TabFolder infoTabFolder = new TabFolder(fileArea, SWT.NONE);

		/**
		 * Here follows the code for the file data
		 * 
		 */
		
		SashForm allInfoArea = new SashForm (fileArea, SWT.NONE);
		allInfoArea.setLayout (new FillLayout ());
		
		Group fileInformationItem = new Group(allInfoArea , SWT.NONE);
		fileInformationItem.setText ("File information");
		fileInformationItem.setLayout (new RowLayout (SWT.VERTICAL | SWT.FILL));
		
		//fileInformationItem.setControl(allInfoArea);
		    	   
		
				
		Group currentTagsGroup1 = new Group(fileInformationItem, SWT.FILL);
		currentTagsGroup1.setLayout (new GridLayout(2, false));
		
		//currentTagsGroup.setText("Current tags");
		
		//SashForm metaArea= new SashForm(fileInformationItem, SWT.FILL); //HORIZONTAL
		//metaArea.setLayout(new RowLayout());
		
		Label fixedNameLabel = new Label(currentTagsGroup1 , SWT.NONE);
		fixedNameLabel.setText("File name:   ");
		//nameLabel.setBounds(0, 0, 200, 30);
		nameLabel = new Label(currentTagsGroup1 , SWT.NONE);
		nameLabel.setText("File name                                                                                    ");
		
		
		Group currentTagsGroup2 = new Group(fileInformationItem, SWT.FILL);
		currentTagsGroup2.setLayout  (new GridLayout(2, false));
		
		Label fixedSizeLabel = new Label(currentTagsGroup2 , SWT.NONE);
		fixedSizeLabel.setText("Size:\t   ");
		
		sizeLabel = new Label(currentTagsGroup2 , SWT.NONE);
		sizeLabel.setText("Size                                                                                             ");

		Group currentTagsGroup3 = new Group(fileInformationItem, SWT.FILL);
		currentTagsGroup3.setLayout  (new GridLayout(2, false));
		
		Label fixedStatusLabel = new Label(currentTagsGroup3 , SWT.NONE);
		fixedStatusLabel.setText("Status:\t   ");
		
		statusLabel = new Label(currentTagsGroup3 , SWT.NONE);
		statusLabel.setText("Status                                                                                          ");
	
		storeButton = new Button(fileInformationItem, SWT.PUSH);
		storeButton.setText("Store");
		storeButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				storeCurrentFile();
			}
		});
		

		if(TEST_MODE != Frontend.DEMO_MODE) {
			Group batchTest = new Group(fileInformationItem, SWT.NONE);
			batchTest .setLayout (new RowLayout (SWT.HORIZONTAL | SWT.FILL));
	
			testButton = new Button(batchTest , SWT.PUSH);
			testButton.setText("Test");
			testButton.addSelectionListener(new SelectionAdapter () {
				public void widgetSelected(SelectionEvent e) {
					runTest(delayCombo.getSelectionIndex());
				}
			});
			delayCombo = new Combo (batchTest , SWT.READ_ONLY);
			delayCombo.setItems(delays);
		}

		

		
		if(TEST_MODE == Frontend.CACHE_TEST) {
			testCacheButton = new Button(fileInformationItem, SWT.PUSH);
			testCacheButton.setText("Test Cache");
			testCacheButton.addSelectionListener(new SelectionAdapter () {
				public void widgetSelected(SelectionEvent e) {
					(new Thread(new RunTestCache())).start();
				}
			});
		}
		
		/**************************** SHOW PICTURE TAB **********************************************************/
		
//		SashForm fileSubArea = new SashForm (fileTabFolder, SWT.NONE);
//		TabItem item = new TabItem (fileTabFolder, SWT.NONE);
//		item.setText ("File");
//		item.setControl(fileSubArea);
		SashForm someInfoArea = new SashForm (fileArea, SWT.NONE);
		
		fileDisplayArea = new Label(someInfoArea, SWT.CENTER); //new Browser(

	}
	
	
	
	/*
	 * INTERFACE STUFF!
	 * 
	 */
	
	class RunTestCache implements Runnable {
		public void run() {
			int r=0, w=0, d=0, i=0;
			ArrayList<String> filesList = new ArrayList<String>();
			
			testCacheStart = System.currentTimeMillis();
			
			while(r < TEST_CACHE_SIZE[0] || w < TEST_CACHE_SIZE[1] || d < TEST_CACHE_SIZE[2]) {
				
				//Write files
				for(i=0; i<TEST_CACHE_RATIO[1] && w < TEST_CACHE_SIZE[1]; i++, w++) {
					testCacheOpId = 1;
					testCacheOpStart = System.currentTimeMillis();
					myAI.store(new File(WORKING_DIRECTORY+"/testFiles/test"+w));
					try {
						SYNC.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					filesList.add("test"+w);
				}
				
				//Read files
				for(i=0; i<TEST_CACHE_RATIO[0] && r < TEST_CACHE_SIZE[0]; i++, r++) {
					int fileId = (int)(Math.random() * filesList.size());
					testCacheOpId = 0;
					testCacheOpStart = System.currentTimeMillis();
					myAI.retrieve(filesList.get(fileId));
					try {
						SYNC.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				//Delete files
				for(i=0; i<TEST_CACHE_RATIO[2] && d < TEST_CACHE_SIZE[2]; i++, d++) {
					int fileId = (int)(Math.random() * filesList.size());
					testCacheOpId = 2;
					testCacheOpStart = System.currentTimeMillis();
					myAI.remove(filesList.remove(fileId));
					try {
						SYNC.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}	
			}	
		}
	}

	
	private void testCacheDone() {
	
		//Calc Stat
		testCacheStat[testCacheOpId] += System.currentTimeMillis() - testCacheOpStart;
	
		System.err.println("CACHE TEST acks: " +  Arrays.toString(testCacheAck));
		if(TEST_CACHE_SIZE[0] == testCacheAck[0] && TEST_CACHE_SIZE[1] == testCacheAck[1] && TEST_CACHE_SIZE[2] == testCacheAck[2]) {
			testCacheEnd = System.currentTimeMillis();
			
			//get Average;
			testCacheStat[0] = testCacheStat[0] / TEST_CACHE_SIZE[0];
			testCacheStat[1] = testCacheStat[1] / TEST_CACHE_SIZE[1];
			testCacheStat[2] = testCacheStat[2] / TEST_CACHE_SIZE[2];
			System.err.println("CACHE TEST DONE IN " + (testCacheEnd - testCacheStart) + " milliseconds!");
			System.err.println("CACHE TEST AVERAGE TIME " + Arrays.toString(testCacheStat));
		}
		
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		SYNC.release();
	}

	

	void runTest(int delayIndex) {

		int TEST_SIZE = 20;
		if(delayIndex < 0 || delays.length < delayIndex) {
			delayIndex = 1;
		}
		int delay = Integer.parseInt(delays[delayIndex]);
		for(int i = 0; i< TEST_SIZE; i++) {
			
			myAI.store(new File(WORKING_DIRECTORY+"/test"+i));
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	void storeCurrentFile() {
		if(currentLocalFile == null) return;

		nameLabel.setText("");
		sizeLabel.setText("");
		statusLabel.setText("UI says: Storing file");
		
		//System.out.println("storing file");
		lastKnownFileName = currentLocalFile.getName();
		//applicationInterface.	
		
//		remoteFileVector.add(result);
//		updateRemoteFileTable();
		myAI.store(currentLocalFile);
		
		//FIXME
	}
	
	
	void retrieveRemoteFile(String uniqueName) {
		statusLabel.setText("Requesting stored file");
		myAI.retrieve(uniqueName);
		
//		File remoteFile = myAI.retrieve(uniqueName);
//		if(remoteFile == null) {
//			
//		}
//		else{
//			loadFile(remoteFile, false);
//		}
	}
	
	void removeRemoteFile(String uniqueName) {
		statusLabel.setText("Removing stored file");
		myAI.remove(uniqueName);
		
//		File remoteFile = myAI.retrieve(uniqueName);
//		if(remoteFile == null) {
//			
//		}
//		else{
//			loadFile(remoteFile, false);
//		}
	}

	
	public void storeAck(final String name, final long step1, final long step1_2, final long step2, final long step3, final int hops, final boolean success) {
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				internalStoreAck(name, success);
			}
		});
	}

	private void internalStoreAck(String name, boolean success) {
		
		if(success) {
			remoteFileVector.add(name);
			updateRemoteFileTable();
			statusLabel.setText("File "+name+ " successfully stored");
			
			
		}
		else {
			statusLabel.setText("File "+name+ " could not be stored, please try again later");
			testCacheAck[4]++;
		}

		if(TEST_MODE == Frontend.CACHE_TEST) {
			testCacheAck[1]++;
			testCacheDone();
		}
			
		
	}

	public void retrieveAck(final String name, final File file) {
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				internalRetrieveAck(name, file);
			}
		});
	}

	public void internalRetrieveAck(String name, File file) {

		if(file != null) {
			//storeFile(file);
			//loadFile(file, false);
			loadFile2(file);
			
			
		}
		else {
			statusLabel.setText("File "+name+ " could not be retrieved, please try again later");
			testCacheAck[3]++;
		}
		if(TEST_MODE == Frontend.CACHE_TEST) {
			testCacheAck[0]++;
			testCacheDone();
		}

		
	}

	public  void removeAck(final String name, final boolean success) {

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				internalRemoveAck(name, success);
			}
		});
		
	}
	
	public  void internalRemoveAck(String name, boolean success) {

		if(success) {
			remoteFileVector.remove(name);
			updateRemoteFileTable();
			statusLabel.setText("File "+name+ " successfully removed");
			
			
		}
		else {
			statusLabel.setText("File "+name+ " could not be removed, please try again later");
			testCacheAck[5]++;
		}
		
		if(TEST_MODE == Frontend.CACHE_TEST) {
			testCacheAck[2]++;
			testCacheDone();
		}

		
	}

	
	


	private void storeFile(File f) {
		
		//f.renameTo(new File("test"));
		
		//FileOutputStream fos = new FileOutputStream("test");
		//f.g
		//fos.write(b)
	}
	//boolean textEditorsNotDisposed()	{		return !(subjectText.isDisposed() || propertyText.isDisposed() || valueText.isDisposed());	}
	
	/** 
	 * Disposes the editors without placing their contents
	 * into the table.
	 */
	void disposeEditors ()
	{
		//textEditor.dispose();
		if(!propertyText.isDisposed())
		{
			propertyText.dispose();
			valueText.dispose();
		}
		
	}
	
	
	
	//So, I assume the content of the vecor is, what??
	
	 void updateRemoteFileTable()	{
		
		remoteFileTable.removeAll();
//		remoteFileTable.redraw();
//		remoteFileVector.clear();

				
		for (int i = 0; i < remoteFileVector.size(); i++)	{
			
			String item = (String) remoteFileVector.get(i);
			new TableItem(remoteFileTable, 0).setText(item);
			
		}
		//remoteFileTable.setd
		//remoteFileTable.setVisible(true);
		remoteFileTable.update();
		
		//imageAndResultsTabFolder.setSelection(1);

	}
	
	
	 void loadFile(File file, boolean local) {
			
			if(file == null) {
				System.out.println("File error");
				return;
			}
			if(local) {
				currentLocalFile = file;
			}
			
			String path = file.getAbsolutePath();
			String loadPath;
			
			int dot = path.lastIndexOf(".");
			if(dot > 0) {
				String suffix = path.substring(dot+1);
				
				if(suffix.equals("jpg") || suffix.equals("png") ) {
					loadPath = path;
				}
				else {
					loadPath = welcomePicture;
				}
			}
			
			else {
				loadPath = welcomePicture;
			}
			
			nameLabel.setText(file.getName());
			sizeLabel.setText(file.length()+" ");
			
			if(local) {
				statusLabel.setText("Local file loaded");
			}
			else {
				statusLabel.setText("Remote file successfully retrieved");
			}
			
			fileDisplayArea.setText(nameLabel.getText());
			currentPicturePath = path;
			
								
		}
	 
	 void loadFile2(File file) {
			
			if(file == null) {
				System.out.println("File error");
				return;
			}
			//System.out.println("File loaded");
			String path = file.getAbsolutePath();
			String loadPath;
			
			int dot = path.lastIndexOf(".");
			if(dot > 0) {
				String suffix = path.substring(dot+1);
				
				if(suffix.equals("jpg") || suffix.equals("png") ) {
					loadPath = path;
				}
				else {
					loadPath = welcomePicture;
				}
			}
			
			else {
				loadPath = welcomePicture;
			}
			
			nameLabel.setText(file.getName());
			sizeLabel.setText(file.length()+" ");
			
			statusLabel.setText("Remote file successfully retrieved");
			
			
			fileDisplayArea.setText(nameLabel.getText());
			
			//currentPicturePath = path;
			
								
		}
		
	
	
	/**
	 * Notifies the application components that a new current directory has been selected
	 * 
	 * @param dir the directory that was selected, null is ignored
	 */
	void notifySelectedDirectory(File dir)
	{
		if (dir == null) return;
		if (currentDirectory != null && dir.equals(currentDirectory)) return;
		currentDirectory = dir;
		notifySelectedFiles(null);
		
		/* Shell:
		 * Sets the title to indicate the selected directory
		 */
		shell.setText(currentDirectory.getPath());

		/* Table view:
		 * Displays the contents of the selected directory.
		 */
		workerUpdate(dir, false);

		/* Combo view:
		 * Sets the combo box to point to the selected directory.
		 */
		final File[] comboRoots = (File[]) searchPathCombo.getData(COMBODATA_ROOTS);
		int comboEntry = -1;
		if (comboRoots != null) {		
			for (int i = 0; i < comboRoots.length; ++i) {
				if (dir.equals(comboRoots[i])) {
					comboEntry = i;
					break;
				}
			}
		}
		if (comboEntry == -1) searchPathCombo.setText(dir.getPath());
		else searchPathCombo.select(comboEntry);

		/* Tree view:
		 * If not already expanded, recursively expands the parents of the specified
		 * directory until it is visible.
		 */
		Vector /* of File */ path = new Vector();
		// Build a stack of paths from the root of the tree
		while (dir != null) {
			path.add(dir);
			dir = dir.getParentFile();
		}
		// Recursively expand the tree to get to the specified directory
		TreeItem[] items = tree.getItems();
		TreeItem lastItem = null;
		for (int i = path.size() - 1; i >= 0; --i) {
			final File pathElement = (File) path.elementAt(i);

			// Search for a particular File in the array of tree items
			// No guarantee that the items are sorted in any recognizable fashion, so we'll
			// just sequential scan.  There shouldn't be more than a few thousand entries.
			TreeItem item = null;
			for (int k = 0; k < items.length; ++k) {
				item = items[k];
				if (item.isDisposed()) continue;
				final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
				if (itemFile != null && itemFile.equals(pathElement)) break;
			}
			if (item == null) break;
			lastItem = item;
			if (i != 0 && !item.getExpanded()) {
				treeExpandItem(item);
				item.setExpanded(true);
			}
			items = item.getItems();
		}
		tree.setSelection((lastItem != null) ? new TreeItem[] { lastItem } : new TreeItem[0]);
	}
	
	/**
	 * Notifies the application components that files have been selected
	 * 
	 * @param files the files that were selected, null or empty array indicates no active selection
	 */
	void notifySelectedFiles(File[] files) {
		/* Details:
		 * Update the details that are visible on screen.
		 */
		if ((files != null) && (files.length != 0)) {
			numObjectsLabel.setText("NumberOfSelectedFiles:"+files.length);
			long fileSize = 0L;
			for (int i = 0; i < files.length; ++i) {
				fileSize += files[i].length();
			}
			diskSpaceLabel.setText("Filesize: " + fileSize);
		} else {
			// No files selected
			diskSpaceLabel.setText("");
			if (currentDirectory != null) {
				int numObjects = getDirectoryList(currentDirectory).length;
				numObjectsLabel.setText(numObjects+" items");
			} else {
				numObjectsLabel.setText("");
			}
		}
	}

	/**
	 * Notifies the application components that files must be refreshed
	 * 
	 * @param files the files that need refreshing, empty array is a no-op, null refreshes all
	 */
	void refreshFiles(File[] files) {
		if (files != null && files.length == 0) return;
		
						
		shell.setCursor(iconCache.stockCursors[iconCache.cursorWait]);

		/* Table view:
		 * Refreshes information about any files in the list and their children.
		 */
		boolean refreshTable = false;

		if (refreshTable) workerUpdate(currentDirectory, true);

		/* Combo view:
		 * Refreshes the list of roots
		 */
		final File[] roots = getRoots();

		if (files == null) {
			boolean refreshCombo = false;
			final File[] comboRoots = (File[]) searchPathCombo.getData(COMBODATA_ROOTS);
		
			if ((comboRoots != null) && (comboRoots.length == roots.length)) {
				for (int i = 0; i < roots.length; ++i) {
					if (! roots[i].equals(comboRoots[i])) {
						refreshCombo = true;
						break;
					}
				}
			} else refreshCombo = true;

			if (refreshCombo) {
				searchPathCombo.removeAll();
				searchPathCombo.setData(COMBODATA_ROOTS, roots);
				for (int i = 0; i < roots.length; ++i) {
					final File file = roots[i];
					searchPathCombo.add(file.getPath());
				}
			}
		}

		/* Tree view:
		 * Refreshes information about any files in the list and their children.
		 */
		treeRefresh(roots);
		
		// Remind everyone where we are in the filesystem
		final File dir = currentDirectory;
		currentDirectory = null;
		notifySelectedDirectory(dir);

		shell.setCursor(iconCache.stockCursors[iconCache.cursorDefault]);
	}

	/**
	 * Performs the default action on the first of a set of files.
	 * 
	 * @param files the array of files to process
	 */
	void doDefaultFileAction(File[] files) {
		// only uses the 1st file (for now)
		if (files.length == 0) return;
		final File file = files[0];

		if (file.isDirectory()) {
			notifySelectedDirectory(file);
		} else {
			//final String fileName = file.getAbsolutePath();
			loadFile(file, true);
		}
	}

	/**
	 * Navigates to the parent directory
	 */
	void doParent() {
		if (currentDirectory == null) return;
		File parentDirectory = currentDirectory.getParentFile();
		notifySelectedDirectory(parentDirectory);
	}
	 
	

	/**
	 * Gets filesystem root entries
	 * 
	 * @return an array of Files corresponding to the root directories on the platform,
	 *         may be empty but not null
	 */
	File[] getRoots() {
		
		File root = new File(File.separator);
		if (initial) {
			currentDirectory = root;
			initial = false;
		}
		return new File[] { root };
	}

	/**
	 * Gets a directory listing
	 * 
	 * @param file the directory to be listed
	 * @return an array of files this directory contains, may be empty but not null
	 */
	 File[] getDirectoryList(File file) {
		File[] list = file.listFiles();

		/*
		 * put parse for file-extensions here
		 *
		Vector tv = new Vector();
		
		for(int i=0; i<list.length; i++)
		{
			int dot = nameString.lastIndexOf('.');
			if (dot != -1) {
				String extension = nameString.substring(dot);
				
				typeString = extension;
				iconImage = iconCache.stockImages[iconCache.iconFile];
				
			} 
			File[]newlist = new File[tv.size()];
			
			i.copyInto(newlist)
			
		}
		 */
		
		if (list == null) return new File[0];
		sortFiles(list);
		return list;
	}
	

	
	
	/**
	 * Sorts files lexicographically by name.
	 * 
	 * @param files the array of Files to be sorted
	 */
	 void sortFiles(File[] files) {
		/* Very lazy merge sort algorithm */
		sortBlock(files, 0, files.length - 1, new File[files.length]);
	}
	private  void sortBlock(File[] files, int start, int end, File[] mergeTemp) {
		final int length = end - start + 1;
		if (length < 8) {
			for (int i = end; i > start; --i) {
				for (int j = end; j > start; --j)  {
					if (compareFiles(files[j - 1], files[j]) > 0) {
					    final File temp = files[j]; 
					    files[j] = files[j-1]; 
					    files[j-1] = temp;
					}
			    }
			}
			return;
		}
		final int mid = (start + end) / 2;
		sortBlock(files, start, mid, mergeTemp);
		sortBlock(files, mid + 1, end, mergeTemp);
		int x = start;
		int y = mid + 1;
		for (int i = 0; i < length; ++i) {
			if ((x > mid) || ((y <= end) && compareFiles(files[x], files[y]) > 0)) {
				mergeTemp[i] = files[y++];
			} else {
				mergeTemp[i] = files[x++];
			}
		}
		for (int i = 0; i < length; ++i) files[i + start] = mergeTemp[i];
	}
	private  int compareFiles(File a, File b) {
//		boolean aIsDir = a.isDirectory();
//		boolean bIsDir = b.isDirectory();
//		if (aIsDir && ! bIsDir) return -1;
//		if (bIsDir && ! aIsDir) return 1;

		// sort case-sensitive files in a case-insensitive manner
		int compare = a.getName().compareToIgnoreCase(b.getName());
		if (compare == 0) compare = a.getName().compareTo(b.getName());
		return compare;
	}
	
	/*
	 * This worker updates the table with file information in the background.
	 * <p>
	 * Implementation notes:
	 * <ul>
	 * <li> It is designed such that it can be interrupted cleanly.
	 * <li> It uses asyncExec() in some places to ensure that SWT Widgets are manipulated in the
	 *      right thread.  Exclusive use of syncExec() would be inappropriate as it would require a pair
	 *      of context switches between each table update operation.
	 * </ul>
	 * </p>
	 */

	/**
	 * Stops the worker and waits for it to terminate.
	 */
	void workerStop() {
		if (workerThread == null) return;
		synchronized(workerLock) {
			workerCancelled = true;
			workerStopped = true;
			workerLock.notifyAll();
		}
		while (workerThread != null) {
			if (! display.readAndDispatch()) display.sleep();
		}
	}

	/**
	 * Notifies the worker that it should update itself with new data.
	 * Cancels any previous operation and begins a new one.
	 * 
	 * @param dir the new base directory for the table, null is ignored
	 * @param force if true causes a refresh even if the data is the same
	 */
	void workerUpdate(File dir, boolean force) {
		if (dir == null) return;
		if ((!force) && (workerNextDir != null) && (workerNextDir.equals(dir))) return;

		synchronized(workerLock) {
			workerNextDir = dir;
			workerStopped = false;
			workerCancelled = true;
			workerLock.notifyAll();
		}
		if (workerThread == null) {
			workerThread = new Thread(workerRunnable);
			workerThread.start();
		}
	}

	/**
	 * Manages the worker's thread
	 */
	private final Runnable workerRunnable = new Runnable() {
		public void run() {
			while (! workerStopped) {
				synchronized(workerLock) {
					workerCancelled = false;
					workerStateDir = workerNextDir;
				}
				workerExecute();
				synchronized(workerLock) {
					try {
						if ((!workerCancelled) && (workerStateDir == workerNextDir)) workerLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
			workerThread = null;
			// wake up UI thread in case it is in a modal loop awaiting thread termination
			// (see workerStop())
			display.wake();
		}
	};
	
	/**
	 * Updates the table's contents
	 */
	private void workerExecute() {
		File[] dirList;
		// Clear existing information
		display.syncExec(new Runnable() {
			public void run() {
				tableContentsOfLabel.setText(workerStateDir.getPath());
				table.removeAll();
				table.setData(TABLEDATA_DIR, workerStateDir);
			}
		});
		dirList = getDirectoryList(workerStateDir);
		
		for (int i = 0; (! workerCancelled) && (i < dirList.length); i++) {
			workerAddFileDetails(dirList[i]);
		}

	}
		
	/**
	 * Adds a file's detail information to the directory list
	 */
	private void workerAddFileDetails(final File file) {
		final String nameString = file.getName();
		final String dateString = dateFormat.format(new Date(file.lastModified()));
		final String sizeString;
		final String typeString;
		final Image iconImage;
		
		if (file.isDirectory()) {
			typeString = "Folder";
			sizeString = "";
			iconImage = iconCache.stockImages[iconCache.iconClosedFolder];
		} else {
			sizeString = (file.length() + 512) / 1024 +" kb";
			
			int dot = nameString.lastIndexOf('.');
			if (dot != -1) {
				String extension = nameString.substring(dot);
				
				typeString = extension;
				iconImage = iconCache.stockImages[iconCache.iconFile];
				
			} else {
				typeString = "no filetype given";
				iconImage = iconCache.stockImages[iconCache.iconFile];
			}
		}
		final String[] strings = new String[] { nameString, sizeString, typeString, dateString };

		display.syncExec(new Runnable() {
			public void run () {
				// guard against the shell being closed before this runs
				if (shell.isDisposed()) return;
				TableItem tableItem = new TableItem(table, 0);
				tableItem.setText(strings);
				tableItem.setImage(iconImage);
				tableItem.setData(TABLEITEMDATA_FILE, file);
			}
		});
	}
	
	
	/*
	 * Subclass/subwindow for managing user preferences
	 * 
	 */
	
	 public class SettingsWindow extends Dialog {
		 Object result;
		 public SettingsWindow(Shell parent, int style)
		 {
			 super (parent, style);
		 } 
		 public SettingsWindow(Shell parent) {
			 this (parent, 0); // your default style bits go here (not the Shell's style bits)
		}
		 
		public Object open () {
			Shell parent = getParent();
			Shell dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
			dialogShell.setLayout(new FillLayout());
			dialogShell.setText("Settings");
			
			
			// Your code goes here (widget creation, set result, etc).
			dialogShell.setSize(SETTINGS_WIDTH, SETTINGS_HEIGHT);
			
			SashForm allSashForm = new SashForm(dialogShell, SWT.FILL);
			
			//GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);			gridData.horizontalSpan = 3; //3			
			//allSashForm.setLayoutData(gridData);

			
			Group allGroup = new Group(allSashForm, SWT.FILL);
			allGroup.setLayout (new FillLayout ());
			allGroup.setText("Settings");
			
			Label shareDescriptionLabel = new Label(allGroup, SWT.NONE);
			shareDescriptionLabel.setText("How much discspace would you like to share with the community?");
			Text shareInputText = new Text(allGroup, SWT.NONE);
			shareInputText.setText("Enter amount in MB");
			System.out.println("ANNOYING");
			
			Button acceptButton = new Button(allGroup, SWT.PUSH);
			acceptButton.addSelectionListener(new SelectionAdapter () {
				public void widgetSelected(SelectionEvent e) {
					//dialogShell.close();
				}
			});
			
			dialogShell.open();
			Display display = parent.getDisplay();
			
			//msgbox:Remember, you have to restart your connection for the changes to take effect!
			
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch()) display.sleep();
			}
			return result;
		
		}
		 
	 }

		public void setName(String name) {
	//		this.name = name;
			
		}
	 
		
}
