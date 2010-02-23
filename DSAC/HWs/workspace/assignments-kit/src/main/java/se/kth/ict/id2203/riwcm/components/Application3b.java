package se.kth.ict.id2203.riwcm.components;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.application.ApplicationContinue;
import se.kth.ict.id2203.application.Flp2pMessage;
import se.kth.ict.id2203.application.Pp2pMessage;
import se.kth.ict.id2203.riwcm.Application3bInit;
import se.kth.ict.id2203.riwcm.events.ReadRequest;
import se.kth.ict.id2203.riwcm.events.ReadResponse;
import se.kth.ict.id2203.riwcm.events.WriteRequest;
import se.kth.ict.id2203.riwcm.events.WriteResponse;
import se.kth.ict.id2203.riwcm.ports.AtomicRegister;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class Application3b extends ComponentDefinition {
	Positive<Timer> timer = positive(Timer.class);
	Positive<AtomicRegister> reg = positive(AtomicRegister.class);
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(Application3b.class);

	private String[] commands;
	private int lastCommand;
	private Set<Address> neighborSet;
	private Address self;
	private int numberOfRegister;

	/**
	 * Instantiates a new application0.
	 */
	public Application3b() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleContinue, timer);
		subscribe(handleRegisterWriteResponse, reg);
		subscribe(handleRegisterReadResponse, reg);
	}

	Handler<WriteResponse> handleRegisterWriteResponse = new Handler<WriteResponse>() {
		public void handle(WriteResponse arg0) {
			logger.info("Write response: R: " + arg0.getRegister());
			doNextCommand();
		}
	};
	
	Handler<ReadResponse> handleRegisterReadResponse = new Handler<ReadResponse>() {
		public void handle(ReadResponse arg0) {
			logger.info("Read response: R: " + arg0.getRegister() + " Val: " + arg0.getValue());
			doNextCommand();
		}
	};
	
	Handler<Application3bInit> handleInit = new Handler<Application3bInit>() {
		public void handle(Application3bInit event) {
			commands = event.getCommandScript().split(":");
			lastCommand = -1;
			neighborSet = event.getNeighborSet();
			self = event.getSelf();
			numberOfRegister = event.getNumberOfRegister();
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
			doNextCommand();
		}
	};

	Handler<ApplicationContinue> handleContinue = new Handler<ApplicationContinue>() {
		public void handle(ApplicationContinue event) {
			doNextCommand();
		}
	};

	Handler<Pp2pMessage> handlePp2pMessage = new Handler<Pp2pMessage>() {
		public void handle(Pp2pMessage event) {
			logger.info("Received perfect message {}", event.getMessage());
		}
	};

	Handler<Flp2pMessage> handleFlp2pMessage = new Handler<Flp2pMessage>() {
		public void handle(Flp2pMessage event) {
			logger.info("Received lossy message {}", event.getMessage());
		}
	};

	private final void doNextCommand() {
		lastCommand++;

		if (lastCommand > commands.length) {
			return;
		}
		if (lastCommand == commands.length) {
			logger.info("DONE ALL OPERATIONS");
			Thread applicationThread = new Thread("ApplicationThread") {
				public void run() {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(System.in));
					while (true) {
						try {
							String line = in.readLine();
							doCommand(line);
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			};
			applicationThread.start();
			return;
		}
		String op = commands[lastCommand];
		doCommand(op);
	}

	private void doCommand(String cmd) {
//		if (cmd.startsWith("P")) {
////			doPerfect(cmd.substring(1));
//			doNextCommand();
//		} else if (cmd.startsWith("L")) {
////			doLossy(cmd.substring(1));
//			doNextCommand();
//		} else if (cmd.startsWith("S")) {
		if (cmd.startsWith("S")) {
			doSleep(Integer.parseInt(cmd.substring(1)));
		} else if (cmd.startsWith("W")) {
			doRegisterWrite(cmd.substring(1));
//			doNextCommand();
		} else if (cmd.startsWith("R")) {
			doRegisterRead(cmd.substring(1));
//			doNextCommand();
		} else if (cmd.startsWith("X")) {
			doShutdown();
//		} else if (cmd.startsWith("R")) {
//			doRecover();
		} else if (cmd.equals("help")) {
			doHelp();
			doNextCommand();
		} else {
			logger.info("Bad command: '{}'. Try 'help'", cmd);
			doNextCommand();
		}
	}

	private void doRegisterWrite(String substring) {
		int register = 0;
		logger.debug("Write: R: " + register  + "\tV:" + substring);
		trigger(new WriteRequest(register, substring), reg);
	}

	private void doRegisterRead(String substring) {
		int register = substring.equals("") ? 0 : Integer.parseInt(substring);
		logger.debug("Read: R: " + register);
		trigger(new ReadRequest(register), reg);
	}


	private final void doHelp() {
		logger.info("Available commands: P<m>, L<m>, S<n>, help, X");
		logger.info("Pm: sends perfect message 'm' to all neighbors");
		logger.info("Lm: sends lossy message 'm' to all neighbors");
		logger.info("Sn: sleeps 'n' milliseconds before the next command");
		logger.info("X: terminates this process");
	}

	private void doSleep(long delay) {
		logger.info("Sleeping {} milliseconds...", delay);

		ScheduleTimeout st = new ScheduleTimeout(delay);
		st.setTimeoutEvent(new ApplicationContinue(st));
		trigger(st, timer);
	}

	private void doShutdown() {
		System.out.close();
		System.err.close();
		System.exit(0);
	}
}
