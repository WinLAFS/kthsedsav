package dks.utils.serialization;

public class Args {
    public int port = 3331;
    public String host = "localhost";
    public int iterations = 1;
    public int links = 1;
    public int mina_bufsize = -1;
    public int io_bufsize = -1;
    public int msgsize = 1024;
    public int pipeline = 1;
    public int threads = 1;
    public double load = 0.0;
    public int cycles = 0;
    public int delay = 0;
    public boolean tcp_nodelay = false;   /* reset (i.e. do the Nagel's algorithm) by default */
    public boolean wait_write = false;
    public boolean short_ack = false;
    // public boolean wait_write_single_thread = false;
    public boolean verbose = false;
    public boolean newmsg = false;
    public boolean dryrun = false;
    public boolean log = false;
    final int idle_time = 100;

	public Args(String argv[]) {
		int arg = 0;
		int args = argv.length;
		while (arg < args) {
			if (argv[arg].equals("-h") || argv[arg].equals("-help")) {
				System.err.println("Arguments:");
				System.err.println("\"-h\" or \"-help\"");
				System.err.println("\"-v\" or \"-verbose\"");
				System.err.println("\"-port <number>\" for the first port in the range, " + port + " if not specified");
				System.err.println("\"-host <number>\", " + host + " if not specified");
				System.err.println("\"-iterations <number>\" or \"-i <number>\" specifies the number of round-trip messages");
				System.err.println("\"-links <number>\" or \"-l <number>\" specifies the number of TCP/IP links (" + links + ")");
				System.err.println("    Iterations are split between links. Links use the port range starting from -port");
				System.err.println("\"-msgsize <number>\" sets the size of message payload in characters (" + msgsize + ")");
				System.err.println("\"-pipeline <number>\" defines the number of messages sent before a response (" + pipeline + ")");
				System.err.println("\"-threads <number>\" that together simulate local computation (" + threads + ")");
				System.err.println("\"-load <float>\" percent of cycles to simulate local computation (" + load + ")");
				System.err.println("\"-cycles <number>\" of empty loop(s) that consume 100% of the computer (" + cycles + ")");
				System.err.println("\"-delay <number>\" milliseconds between sending messages (" + delay + ")");
				System.err.println("\"-mina-bufsize <number>\", the MINA read/write buffer sizes in bytes");
				System.err.println("\"-io-bufsize <number>\", the socket send/receive buffer sizes in bytes");
				System.err.println("    The default MINA settings are not changed if the last two options are not present");
				System.err.println("\"-tcp-nodelay\" switches off the Nagel's algorithm");
				System.err.println("\"-wait-write\" or \"-ww\" turns on checking for write status");
				System.err.println("\"-short-ack\" requires 1 char ACK message instead of original one");
				System.err.println("\"-newmsg\" switches on the generation of fresh message for every iteration");
				System.err.println("\"-dryrun\" produces a run without any messages sent");
				System.err.println("    All channels, new messages and waiting threads, whenever requested, are created.");
				System.err.println("\"-log turns logging on.");
				System.err.println("    Requires 'org.apache.log4j.config.file' and 'niche.log.file' properties.");
				// System.err.println("\"-ww-single-thread\" or \"-ww-st\" instructs to use a single thread for waiting for write status");
				System.exit(0);
			}

			if (argv[arg].equals("-port")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-port\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					port = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-port\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-host")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-host\" must be followed by a string");
        			System.exit(1);
        		}
				host = argv[arg];
			}

			if (argv[arg].equals("-iterations") || argv[arg].equals("-i")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-iterations\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					iterations = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-iterations\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-links") || argv[arg].equals("-l")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-links\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					links = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-links\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-msgsize")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-msgsize\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					msgsize = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-msgsize\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-pipeline")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-pipeline\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					pipeline = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-pipeline\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-threads")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-threads\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					threads = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-threads\" argument must be an integer");
        			System.exit(1);
        		}
				if (threads <= 0) {
					System.err.println("\"-threads\" argument must be a positive integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-load")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-load\" must be followed by a double");
        			System.exit(1);
        		}
				try {
					load = Double.parseDouble(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-load\" argument must be a double");
        			System.exit(1);
        		}
				if (load < 0.0 || load > 1.0) {
					System.err.println("\"-load\" argument must be within the [0.0 1.0] range");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-cycles")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-cycles\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					cycles = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-cycles\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-delay")) {
				arg++;
				if (arg >= args) {
					System.err.println("\"-delay\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					delay = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-delay\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-mina-bufsize")) {
				arg++;
				if (arg >= args) {
					System.err.println("-\"mina-bufsize\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					mina_bufsize = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-mina-bufsize\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-io-bufsize")) {
				arg++;
				if (arg >= args) {
					System.err.println("-\"io-bufsize\" must be followed by an integer");
        			System.exit(1);
        		}
				try {
					io_bufsize = Integer.parseInt(argv[arg]);
				} catch (NumberFormatException e) {
					System.err.println("\"-io-bufsize\" argument must be an integer");
        			System.exit(1);
        		}
			}

			if (argv[arg].equals("-tcp-nodelay")) {
				tcp_nodelay = true;
			}

			if (argv[arg].equals("-wait-write") || argv[arg].equals("-ww")) {
				wait_write = true;
			}

			if (argv[arg].equals("-short-ack")) {
				short_ack = true;
			}

			if (argv[arg].equals("-newmsg")) {
				newmsg = true;
			}

			if (argv[arg].equals("-dryrun")) {
				dryrun = true;
			}

			if (argv[arg].equals("-log")) {
				log = true;
			}

			// if (argv[arg].equals("-ww-single-thread") || argv[arg].equals("-ww-st")) {
			// 	wait_write_single_thread = true;
			// }

			if (argv[arg].equals("-v") || argv[arg].equals("-verbose") ) {
				verbose = true;
			}

			arg++;
		}

		if (verbose) {
			System.out.println("-port " + port);
			System.out.println("-host " + host);
			System.out.println("-iterations " + iterations);
			System.out.println("-links " + links);
			System.out.println("-pipeline " + pipeline);
			System.out.println("-threads " + threads);
			System.out.println("-load " + load);
			System.out.println("-cycles " + cycles);
			System.out.println("-delay " + delay);
			System.out.println("-msgsize " + msgsize);
			System.out.println("-mina-bufsize " + mina_bufsize);
			System.out.println("-io-bufsize " + io_bufsize);
			System.out.println("-tcp-nodelay " + tcp_nodelay);
			System.out.println("-wait-write " + wait_write);
			System.out.println("-short-ack " + short_ack);
			System.out.println("-newmsg " + newmsg);
			System.out.println("-dryrun " + dryrun);
			System.out.println("-log " + log);
			// System.out.println("-ww-single-thread=" + wait_write_single_thread);
		}
	}

    public Args(Args argsIn) {
    	port = argsIn.port;
    	host = argsIn.host;
    	iterations = argsIn.iterations;
    	links = argsIn.links;
    	mina_bufsize = argsIn.mina_bufsize;
    	io_bufsize = argsIn.io_bufsize;
    	msgsize = argsIn.msgsize;
    	pipeline = argsIn.pipeline;
    	threads = argsIn.threads;
    	load = argsIn.load;
    	cycles = argsIn.cycles;
    	delay = argsIn.delay;
    	tcp_nodelay = argsIn.tcp_nodelay;
    	wait_write = argsIn.wait_write;
    	short_ack = argsIn.short_ack;
    	newmsg = argsIn.newmsg;
    	dryrun = argsIn.dryrun;
    	log = argsIn.log;
    	// wait_write_single_thread = argsIn.wait_write_single_thread;
    	verbose = argsIn.verbose;
    }
 };
