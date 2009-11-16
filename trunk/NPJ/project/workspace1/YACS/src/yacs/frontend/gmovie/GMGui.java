package yacs.frontend.gmovie;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import java.awt.event.*;
import javax.swing.*;

import yacs.frontend.FrontendInterface;
import yacs.frontend.FrontendClientInterface;
import yacs.frontend.gmovie.utils.*;
import yacs.interfaces.YACSNames;
import yacs.interfaces.YACSSettings;
import yacs.job.*;
import yacs.job.tasks.*;
import yacs.job.tasks.helpers.ProcessStreamReader;
import yacs.utils.YacsUtils;


public class GMGui extends JFrame implements KeyListener, FrontendClientInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GenericListModel logMessages = new GenericListModel();
	
	private JLabel submitterName;
	private JTextField movie;
	private JTextField parts;
	private JComboBox video_codec;
	private JComboBox video_bitrate;
	private JComboBox audio_codec;
	private JComboBox audio_bitrate;
	private JComboBox scale;
	private TaskCanvas canvas;
	private JLabel tasks;
	private JButton submit;
	private JButton delete;
	private JButton play;
	private JCheckBox advanced;
	private JTextField classloc;
	private JTextField nfsRelative;
	private JTextField gmovieTranscodingScript;
	private JTextField gmoviePlayScript;
	private JTextField directedTaskCommandfile;
	
	private Vector<TaskDisplay> taskDisplays = new Vector<TaskDisplay>();
	private Hashtable<String,TaskDisplay> taskDisplaysHash = new Hashtable<String,TaskDisplay>();
	
	private FrontendInterface frontend;
	private Job job;
	private Submitter submitter;
	private int jobUid=1;
	
	private Job jobResult;
	private Vector<TaskContainer> changes = new Vector<TaskContainer>();
	private String transcodedFileLocation = null;
	private long submitTime;
	
	// settings; locations and commands
	//private String NFS_RELATIVE_PATH = "/transcoding/demonstrator";
	//private String NFS_RELATIVE_PATH = "/yacs/task_commands";
	private String NFS_RELATIVE_PATH = "/transcoding/demonstrator";
	//private String NFS_RELATIVE_PATH = "/testing/task_commands";
	
	private String TRANSCODING_DIR = "z:\\transcoding\\demonstrator"; 
	private String INITIAL_FILE = TRANSCODING_DIR+"\\parts.txt";
	
	private String DT_COMMAND_FILE = NFS_RELATIVE_PATH + "/commands.txt" ;
	
	private String CLASS_DIR = "C:\\SEDS\\Lokaverkefni\\code3\\YACS\\bin\\yacs\\job\\tasks";
	private String INITIAL_TASK_CLASS = CLASS_DIR+"\\MovieTranscodingDirectedTask.class";
	//private String INITIAL_TASK_CLASS = CLASS_DIR+"\\DirectedSleepTask.class";
	
	//private String VLC_TRANSCODING_COMMAND_AND_LOC = TRANSCODING_DIR+"\\yacs_gmovie_vlc.bat";
	//private String VLC_PLAY_COMMAND_AND_LOC = TRANSCODING_DIR+"\\yacs_vlc_start_movie.bat";
	private String VLC_TRANSCODING_COMMAND_AND_LOC = NFS_RELATIVE_PATH+"/yacs_gmovie_vlc";
	private String VLC_PLAY_COMMAND_AND_LOC = NFS_RELATIVE_PATH+"/yacs_vlc_start_movie.bat";
		
	private Class SUBMITTER_CLASS = PresplitTranscoder.class;
	//private Class SUBMITTER_CLASS = DirectedTaskSubmitter.class;
	private String SUBMITTER_TEXT = "Submitter: "+(SUBMITTER_CLASS==null?"NULL":SUBMITTER_CLASS.getSimpleName());
	private Submitter getSubmitterInstance(){ 
		//return new LetterCounter();
		//return new DuplicateTranscoder();
		//return new SleepTaskSubmitter();
		return new PresplitTranscoder();
		//return new DirectedTaskSubmitter();
	}
	
	private int PART_COUNT = 5;
	
	public GMGui( FrontendInterface frontend ){
		super();
		try {
			initGui();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		this.frontend = frontend;
	}
	
	private void initGui(){
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.getContentPane().setLayout(null); // want complete control
			
			java.awt.Container pane = this.getContentPane();
			
			// add the controls
			JComponent f;
			ComboBoxModel cbm;
			JComboBox cbt;
			JButton b;
			// movie selection
			{
				f = new JLabel("Select movie:");
				f.setBounds(10,10,100,14);
				pane.add(f);
			
				//movie = new JTextField("<path>");
				//movie = new JTextField("C:\\SEDS\\Lokaverkefni\\temp\\transcoding_test\\in.txt");
				movie = new JTextField(INITIAL_FILE);
				movie.setBounds(100,10,300,20);
				pane.add(movie);
				
				b = new JButton("Browse");
				b.setBounds(450,10,100,20);
				b.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt){btnMovieBrowse(evt);}
					}
				);
				pane.add(b);
			}
			// options - video
			{
				f = new JLabel("Video");
				f.setBounds(10,50,100,14);
				pane.add(f);
				
				f = new JLabel("Codec");
				f.setBounds(15,75,100,14);
				pane.add(f);
				
				cbm = new DefaultComboBoxModel(new String[]{"mpeg1", "mpeg2", "mpeg4", "divx1", "divx2", "divx3"});
				video_codec = new JComboBox( cbm );
				video_codec.setBounds(60, 75, 100, 20);
				video_codec.setSelectedIndex(1);
				pane.add( video_codec );
				
				f = new JLabel("Bitrate");
				f.setBounds(15,100,100,14);
				pane.add(f);
				
				cbm = new DefaultComboBoxModel(new String[]{"128","256","512","1024","2048"});
				video_bitrate = new JComboBox( cbm );
				video_bitrate.setBounds(60, 100, 100, 20);
				video_bitrate.setSelectedIndex(3);
				pane.add( video_bitrate );
			}
			
			// options - audio
			{
				f = new JLabel("Audio");
				f.setBounds(175,50,100,14);
				pane.add(f);
				
				f = new JLabel("Codec");
				f.setBounds(180,75,100,14);
				pane.add(f);
				
				cbm = new DefaultComboBoxModel(new String[]{"mpeg"});
				audio_codec = new JComboBox( cbm );
				audio_codec.setBounds(230, 75, 100, 20);
				audio_codec.setSelectedIndex(0);
				pane.add( audio_codec );
				
				f = new JLabel("Bitrate");
				f.setBounds(180,100,100,14);
				pane.add(f);
				
				cbm = new DefaultComboBoxModel(new String[]{"16","32","64","128","256"});
				audio_bitrate = new JComboBox( cbm );
				audio_bitrate.setBounds(230, 100, 100, 20);
				audio_bitrate.setSelectedIndex(3);
				pane.add( audio_bitrate );
			}
			
			// options - other
			{
				f = new JLabel("Other options");
				f.setBounds(350,50,100,14);
				pane.add(f);
				
				f = new JLabel("Scale");
				f.setBounds(355,75,100,14);
				pane.add(f);
				
				cbm = new DefaultComboBoxModel(new String[]{"0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0"});
				scale = new JComboBox( cbm );
				scale.setBounds(400, 75, 100, 20);
				scale.setSelectedIndex(8);
				pane.add( scale );
				
				f = new JLabel("Parts");
				f.setBounds(355,100,100,14);
				pane.add(f);
				
				parts = new JTextField( String.valueOf(PART_COUNT) );
				parts.setBounds(400,100,30,20);
				pane.add(parts);
			}
			
			// submit button line
			{
				tasks = new JLabel("Tasks:");
				tasks.setBounds(10,140,100,14);
				tasks.setVisible(false);
				pane.add(tasks);
				
				play = new JButton("Play");
				play.setEnabled(false);
				play.setBounds(230,130,100,20);
				play.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt){btnPlay(evt);}
					}
				);
				pane.add(play);
				
				delete = new JButton("Delete");
				delete.setEnabled(false);
				delete.setBounds(340,130,100,20);
				delete.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt){btnDelete(evt);}
					}
				);
				pane.add(delete);
				
				submit = new JButton("Submit");
				submit.setBounds(450,130,100,20);
				submit.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt){btnSubmit(evt);}
					}
				);
				pane.add(submit);
			}
			
			// TaskCanvas
			{
				canvas = new TaskCanvas( taskDisplays );
				canvas.setBounds(10,160,540,30);
				pane.add( canvas );
			}
			
			// display
			{
				JScrollPane scrollableOutput = new JScrollPane();
				scrollableOutput.setBounds(10,200,540,250);
				
				JList output = new JList();
				scrollableOutput.setViewportView(output);
				//output.setBounds(10,240,540,225);
				output.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0,0,0), 1, false));
				output.setModel( logMessages );
				
				pane.add( scrollableOutput );
			}
			
			// advanced settings
			{
				submitterName = new JLabel(SUBMITTER_TEXT);
				submitterName.setBounds(10,455,250,14);
				pane.add(submitterName);
				
				advanced = new JCheckBox("Advanced");
				advanced.setSelected(true);
				advanced.setBounds(470, 452, 100, 15);
				advanced.addActionListener( new ActionListener(){
						public void actionPerformed(ActionEvent e){ cbAdvancedClicked(e); }
					}
				);
				pane.add( advanced );
				
				// class selection
				f = new JLabel("Select task:");
				f.setBounds(10,480,100,14);
				pane.add(f);
				
				classloc = new JTextField(INITIAL_TASK_CLASS);
				classloc.setBounds(100,480,300,20);
				pane.add(classloc);
				
				b = new JButton("Browse");
				b.setBounds(450,480,100,20);
				b.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt){btnClassBrowse(evt);}
					}
				);
				pane.add(b);
				
				// relative path
				f = new JLabel("NFS relative:");
				f.setBounds(10,500,100,14);
				pane.add(f);
				
				f = new JLabel("(NFS.base: " + System.getProperty("yacs.nfs.base")+")");
				f.setBounds(310,500,100,14);
				pane.add(f);
				
				nfsRelative = new JTextField(NFS_RELATIVE_PATH);
				nfsRelative.setBounds(100,500,200,20);
				nfsRelative.addKeyListener(this);
				pane.add(nfsRelative);
				
				// command file for file-directed-tasks
				f = new JLabel("Command file:");
				f.setBounds(10,525,100,14);
				pane.add(f);
				
				directedTaskCommandfile = new JTextField(DT_COMMAND_FILE);
				directedTaskCommandfile.setBounds(100,520,300,20);
				pane.add(directedTaskCommandfile);
				
				// transcoding script
				f = new JLabel("Transc. script:");
				f.setBounds(10,555,100,14);
				pane.add(f);
				
				gmovieTranscodingScript = new JTextField(VLC_TRANSCODING_COMMAND_AND_LOC);
				gmovieTranscodingScript.setBounds(100,550,300,20);
				pane.add(gmovieTranscodingScript);
				
				f = new JLabel("(.[bat|sh])");
				f.setBounds(402,555,100,14);
				pane.add(f);

				// play script
				f = new JLabel("Play script:");
				f.setBounds(10,575,100,14);
				pane.add(f);
				
				gmoviePlayScript = new JTextField(VLC_PLAY_COMMAND_AND_LOC);
				gmoviePlayScript.setBounds(100,570,300,20);
				pane.add(gmoviePlayScript);
			}
			
			pack();
			
			// props of frame itself
			setTitle("gMovie on YACS on DCMS on Niche on DKS");
			setSize(570, 655);
			setResizable(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// gui event handler functions
	private void btnSubmit( ActionEvent evt ){
		submit.setEnabled(false);
		
		String msg = "M:"+this.movie.getText()+", settings: " 
			+ this.video_codec.getSelectedItem() + ", "
			+ this.video_bitrate.getSelectedItem() + ", "
			+ this.audio_codec.getSelectedItem() + ", "
			+ this.audio_bitrate.getSelectedItem() + ", "
			+ this.scale.getSelectedItem() + ", "
			+ this.parts.getText();
		
		logMessages.addValue( msg, msg );
		
		// Create a separate thread to handle the job. "Required" by Swing threading model
		try{
			submitter = getSubmitterInstance();
			this.submitterName.setText( SUBMITTER_TEXT );
			SwingUtilities.invokeLater( submitter );
		}
		catch( Exception e ){
			logMessages.addValue( e.getMessage() );
			e.printStackTrace();
		}
	}
	private void btnMovieBrowse( ActionEvent evt ){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory( new File(TRANSCODING_DIR) );
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	this.movie.setText( chooser.getSelectedFile().getAbsolutePath() );
	    }
	}
	private void btnDelete( ActionEvent evt ){
		delete.setEnabled(false);
		play.setEnabled(false);
		
		if( YACSSettings.TEST__BUSY_UNTIL_DELETED ){
			boolean deleted = this.frontend.deleteJob( job );
			
			logMessages.addValue( "Job deleted: " + deleted );
			
			this.taskDisplays.clear();
			this.taskDisplaysHash.clear();
			this.forceTaskRepaint();
		}
		else
			logMessages.addValue( "Already deleted!" );
		
		submit.setEnabled(true);
	}
	private void btnPlay( ActionEvent evt ){
		logMessages.addValue("Playing transcoded movie...");
		SwingUtilities.invokeLater( new Runnable(){
				public void run(){ runPlayMovie();	}
			}
		);
	}
	private void cbAdvancedClicked( ActionEvent evt ){
		System.out.println("CB: " + evt);
		if( advancedChecked ){
			setSize( 570, 505 );
		}
		else {
			setSize( 570, 655 );
		}
		
		advancedChecked = !advancedChecked;
	}
	private boolean advancedChecked = true;
	private void btnClassBrowse( ActionEvent evt ){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory( new File(CLASS_DIR) );
	    if( chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ) {
	       this.classloc.setText( chooser.getSelectedFile().getAbsolutePath() );
	    }
	}
	
	//KeyListener
	public void 	keyPressed(KeyEvent e){}
	public void 	keyReleased(KeyEvent e){}
	public void 	keyTyped(KeyEvent e){
		String rel = nfsRelative.getText() + e.getKeyChar();
		
		String trans = gmovieTranscodingScript.getText();
		String play = gmoviePlayScript.getText();
		String cmd = directedTaskCommandfile.getText();
	}
	
	// job event interface
	public void jobResult( Job result ){
		jobResult = result;
		
		SwingUtilities.invokeLater( new Runnable(){
				public void run(){ runProcessJobResult();	}
			}
		);
	}
	public void taskChange( TaskContainer task ){
		changes.add( task );
		
		SwingUtilities.invokeLater( new Runnable(){
					public void run(){ runProcessTaskChange();	}
			}
		);
	}
	
	// helpers, Swing runnables
	// Swing "runnable" to handle processing of job result
	private void runProcessJobResult(){
		
		int healings = 0;
		for( TaskDisplay dp : taskDisplays ){
			healings += dp.getRestores();
		}
		
		logMessages.addValue("Job completed in: " + ((System.currentTimeMillis()-submitTime)/1000) +" sek" );
		logMessages.addValue( "\tRemaining: "+jobResult.getRemaining().size() );		
		logMessages.addValue( "\tPending:   "+jobResult.getPending().size() );
		logMessages.addValue( "\tCompleted: "+jobResult.getDone().size() );
		logMessages.addValue( "\tFailed:    "+jobResult.getFailed().size() );
		logMessages.addValue( "\tHealed:    "+healings);
		
		if( submitter != null )
			submitter.splice( jobResult );
	}
	// Swing "runnable" to handle processing of task changes
	private void runProcessTaskChange(){
		
		TaskContainer task = null;
		while( this.changes.size() > 0 ){
			try{
				task = this.changes.remove(0);
			} catch( Exception e ){}
			
			if( task == null )
				continue; // to while... could this perhaps be in the remove catch clause?
			
			String msg = 	"Task change: id:"
								+ task.getTid() + " @"
								+ (task.getWorker()==null?"NULL":task.getWorker().getId().toString()) + ", status: "
								+ task.getStatus();
	
			logMessages.addValue( msg );
			
			String stid = String.valueOf(task.getTid());
			if( !this.taskDisplaysHash.containsKey(stid) )
				logMessages.addValue("Tid: " + stid + " not in hashtable!?");
			else {	// update the progress bar
				TaskDisplay display = this.taskDisplaysHash.get( stid );
				display.setResult( task.getResultCode() );
				display.setState( task.getStatus() );
				display.setWorker( (task.getWorker()==null?null:task.getWorker().getId().toString()) );
			}
			
			try {
				// for submitter specific handling
				if( submitter != null ) {
					submitter.handleTaskChange( task );
				}
			}
			catch( Exception e ){
				logMessages.addValue( e.getMessage() );
			}
		}
			
		this.forceTaskRepaint();
	}
	// Swing "runnable" to handle playing of transcoded movie
	private void runPlayMovie(){
		
		String command = YacsUtils.nfsRelativeToNfsAbsolute(gmoviePlayScript.getText()) + " " + transcodedFileLocation;
		logMessages.addValue(command);
		try {
			Process p = Runtime.getRuntime().exec( command );
			ProcessStreamReader stdout = new ProcessStreamReader( "stdout", p.getInputStream(), null );
			ProcessStreamReader stderr = new ProcessStreamReader( "stderr", p.getErrorStream(), null );
			
			stdout.start();
			stderr.start();
			
			stdout.join();
			stderr.join();
		}
		catch( Exception e ){
			logMessages.addValue( e.getMessage() );
			logMessages.addValue( "Command was: " + command );
		}
		
	}
	
	// helpers
	private void addTaskDisplay( int tid ){
		TaskDisplay display = new TaskDisplay();
		taskDisplays.add( display );
		taskDisplaysHash.put( String.valueOf(tid), display );
	}
	private void forceTaskRepaint(){
		this.tasks.setVisible( taskDisplays.size() > 0 );
		this.canvas.repaint();
		this.repaint();
	}
	
	// submitting logic threads
	abstract class Submitter implements Runnable {
		
		public Submitter(){}
		
		public void run(){
			try {
				// clean the progresss bar
				taskDisplays.clear();
				taskDisplaysHash.clear();
				
				job = new Job("movie"+jobUid+":" + movie.getText());
				jobUid++;
				
				split( job );
				
				logMessages.addValue( "Submitted to master: " + frontend.submit(job) );
				submitTime = System.currentTimeMillis();
				delete.setEnabled(true);
			}
			catch( Exception e ){
				logMessages.addValue( "Exception: "+e.getMessage() );
				e.printStackTrace();
				
				taskDisplays.clear();
				taskDisplaysHash.clear();
				forceTaskRepaint();
				submit.setEnabled(true);
				
			}
			
		}
		
		protected abstract void split( Job job ) throws Exception;
		public abstract void splice( Job result );
		protected void handleTaskChange( TaskContainer task ){}
	}
	
	class PresplitTranscoder extends Submitter {
		private String optionStr=null;
		private String selected_video_codec = null;
		private String selected_video_bitrate = null;
		private String selected_audio_codec = null;
		private String selected_audio_bitrate = null;
		private String selected_scale = null;
		
		
		public PresplitTranscoder(){}
		
		public void split( Job job ) throws Exception{
			
			ArrayList<String> partlist = new ArrayList<String>();
			{
				BufferedReader reader = new BufferedReader( new FileReader(movie.getText()) );
				String line;
				while( (line=reader.readLine()) != null ){
					if( line.length() != 0 && line.charAt(0) != '#' ){
						logMessages.addValue( "Part: " + line );
						partlist.add( line );
					}
				}
				reader.close();
			}
			
			//int partcount = Integer.parseInt(parts.getText());
			
			selected_video_codec 	= translateVideoCodec();
			selected_video_bitrate 	= translateVideoBitrate();
			selected_audio_codec 	= translateAudioCodec();
			selected_audio_bitrate 	= translateAudioBitrate();
			selected_scale 			= translateVideoScale();
			
			optionStr = "vc." + selected_video_codec
						+ "_vb." + selected_video_bitrate
						+ "_ac." + selected_audio_codec
						+ "_ab." + selected_audio_bitrate
						+ "_s." + selected_scale;
			
			Vector<TaskContainer> remaining = job.getRemaining();
			int a=0;
			for( String part : partlist ){
			//for( int a=1; a<=partcount; a++ ){
				a++;
				Serializable[] initParams = new Serializable[]{
						directedTaskCommandfile.getText(),
						gmovieTranscodingScript.getText(),
						selected_video_codec, selected_video_bitrate,
						selected_audio_codec, selected_audio_bitrate,
						selected_scale,
						new Long(0),
						NFS_RELATIVE_PATH+"/"+part,
						NFS_RELATIVE_PATH+"/"+part+".yacs."+a 
				};
				
				remaining.add( TaskContainer.contain(	a,	YACSNames.DEFAULT_REDEPLOYABLE, 
														MovieTranscodingDirectedTask.class.getName(),
														classloc.getText(),
														initParams) );
				
				// add to progress bar;
				addTaskDisplay( a );
			}
			forceTaskRepaint();
		}
		public void splice( Job result ){
			if( result.getRemaining().size() != 0 || result.getPending().size() != 0 || result.getFailed().size() != 0 ){
				logMessages.addValue( "\tMovie transcoding NOT completed!" );
				return;
			}
			
			boolean error = false;
			for( TaskContainer task : result.getDone() ){
				if( task.getResultCode() != YACSNames.RESULT_OK ){
					logMessages.addValue( "Task: " + task.getTid() +" did not return an OK result! Code: " + task.getResultCode() );
					error = true;
				}
			}
			if( error )
				return;
			
			logMessages.addValue( "Merging results..." );
			try {
				String when = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new java.util.Date() );
				long oldTotalSize = 0, totalSize = 0, partSize = 0;
				
				String transcodedFilename = YacsUtils.nfsRelativeToNfsAbsolute(NFS_RELATIVE_PATH+"/sec2__"+optionStr+"_"+when+".transcoded");
				File transcoded = new File( transcodedFilename );
				FileChannel oc = new FileOutputStream(transcoded).getChannel();
										
				java.util.Collections.sort( result.getDone() );
				
				for( TaskContainer task : result.getDone() ){
					
					oldTotalSize += new File( YacsUtils.nfsRelativeToNfsAbsolute((String)task.getInitParams()[8]) ).length();
					String taskOutput = YacsUtils.nfsRelativeToNfsAbsolute((String)task.getInitParams()[9]);
					
					logMessages.addValue( "Current task: " + task.getTid() + "@" + taskOutput );
					
					File to = new File( taskOutput );
					FileChannel ic = new FileInputStream( to ).getChannel();
					
					partSize = ic.size();
					totalSize += partSize;
					ic.transferTo( 0, partSize, oc );
				}
				
				oc.close();
				
				logMessages.addValue("Transcoded file: " + transcodedFilename);
				transcodedFileLocation = transcodedFilename;
				
				logMessages.addValue( "Ratio of prior size: " + Math.round(((double)totalSize/(double)oldTotalSize)*100) + "%"  );
				
				play.setEnabled(true);
			}
			catch( Exception e ){
				logMessages.addValue( e.getMessage() );
			}
		}
		
		private String translateVideoCodec() throws Exception {
			String choice = video_codec.getSelectedItem().toString();
			
			if( 		choice.equals("mpeg1") )	return "mp1v";
			else if( 	choice.equals("mpeg2") )	return "mp2v";
			else if( 	choice.equals("mpeg4") )	return "mp4v";
			else if( 	choice.equals("divx1") )	return "DIV1";
			else if( 	choice.equals("divx2") )	return "DIV2";
			else if( 	choice.equals("divx3") )	return "DIV3";
			else
				throw new Exception("Unsupported video_codec: " + choice);
		}
		private String translateVideoBitrate() throws Exception {
			String choice = video_bitrate.getSelectedItem().toString();
			
			if( 	!choice.equals("128") &&
					!choice.equals("256") &&
					!choice.equals("512") &&
					!choice.equals("1024") &&
					!choice.equals("2048") )
			{
				throw new Exception("Unsupported video_bitrate: " + choice);
			}
			else
				return choice;
		}
		private String translateAudioCodec() throws Exception {
			String choice = audio_codec.getSelectedItem().toString();
			
			if( choice.equals("mpeg") )
				return "mpga";
			else
				throw new Exception("Unsupported audio_codec: " + choice);
		}
		private String translateAudioBitrate() throws Exception {
			String choice = audio_bitrate.getSelectedItem().toString();
			
			if( 	!choice.equals("16") &&
					!choice.equals("32") &&
					!choice.equals("64") &&
					!choice.equals("128") &&
					!choice.equals("256") &&
					!choice.equals("512") )
			{
				throw new Exception("Unsupported audio_bitrate: " + choice);
			}
			else
				return choice;
		}
		private String translateVideoScale() throws Exception {
			return scale.getSelectedItem().toString();
		}
	}
}
