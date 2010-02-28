package se.kth.ict.id2203.uc.components;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.application.ApplicationContinue;
import se.kth.ict.id2203.uc.Application4Init;
import se.kth.ict.id2203.uc.events.UCDecide;
import se.kth.ict.id2203.uc.events.UCPropose;
import se.kth.ict.id2203.uc.ports.UniformConsensus;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

public class Application4 extends ComponentDefinition {
	Positive<UniformConsensus> uc = positive(UniformConsensus.class);
	Positive<Timer> timer = positive(Timer.class);

	private static final Logger logger = LoggerFactory
			.getLogger(Application4.class);

	private String[] commands;
	private int lastCommand;
	private Set<Address> neighborSet;
	private Address self;
	private boolean waitingForOtherProposals = false;
	private int numberOfOtherProposals = 0;
	private long sleepTime;
	private ArrayList<Integer[]> decides = new ArrayList<Integer[]>(); 

	/**
	 * Instantiates a new application0.
	 */
	public Application4() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleContinue, timer);
		subscribe(handleUCDecide, uc);
	}

	Handler<Application4Init> handleInit = new Handler<Application4Init>() {
		public void handle(Application4Init event) {
			commands = event.getCommandScript().split(":");
			lastCommand = -1;
			neighborSet = event.getNeighborSet();
			self = event.getSelf();
			logger.debug("Application :: STARTED!!");
			System.err.println("Application STARTED!!");
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

	Handler<UCDecide> handleUCDecide = new Handler<UCDecide>() {
		public void handle(UCDecide event) {
			numberOfOtherProposals--;
			if (waitingForOtherProposals && (numberOfOtherProposals == 0)) {
				waitingForOtherProposals = false;
				logger.debug("> > Finished waiting for proposals. Sleep: " + sleepTime + "ms");
				doSleep(sleepTime);
			}
			Integer[] vals = {event.getId(), Integer.parseInt(event.getValue())};
			decides.add(vals);
			logger.info("UCDecide. Id: " + event.getId() + " | Val: " + event.getValue());
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
		if (cmd.startsWith("P")) {
			/*
			 * Pi-j This means that the node on which this command is executed
			 * should propose the value j for a consensus instance identified by
			 * i. For simplicity, both i and j should be integers.
			 */
			numberOfOtherProposals++;
			doPropose(cmd.substring(1));
			doNextCommand();
		} else if (cmd.startsWith("D")) {
			/*
			 * Dk This means that the node n on which this command is executed
			 * should wait until it gets a decision to all previous (ongoing)
			 * proposals made by n, then wait for k milliseconds, and then
			 * process any further commands. If there is no previous (ongoing)
			 * proposal made by n, this command should simply wait for k
			 * milliseconds and then process any further commands.
			 */
			waitingForOtherProposals = true;
			sleepTime = Integer.parseInt(cmd.substring(1));
			if (numberOfOtherProposals == 0) {
				doSleep(sleepTime);
			} 
			else {
				logger.debug("> > Waiting for proposals to finish");
			}
		} else if (cmd.startsWith("S")) {
			doSleep(Integer.parseInt(cmd.substring(1)));
		} else if (cmd.startsWith("W")) {
			/*
			 * W The node should sort all the decisions received thus far
			 * according to Paxos instance identifier and print them out. Please
			 * note that you should give enough delay before executing this
			 * command so that all the nodes have decided for all Paxos
			 * instances.
			 */
			doPrintDecidesSorted();
			doNextCommand();
		} else if (cmd.startsWith("X")) {
			doShutdown();
		} else if (cmd.startsWith("R")) {
			doRecover();
		} else if (cmd.equals("help")) {
			doHelp();
			doNextCommand();
		} else {
			logger.info("Bad command: '{}'. Try 'help'", cmd);
			doNextCommand();
		}
	}

	private void doPrintDecidesSorted() {
		Collections.sort(decides, new Comparator<Integer[]>() {
			
			public int compare(Integer[] arg0, Integer[] arg1) {
				// a negative integer, zero, or a positive integer as the first argument 
				//is less than, equal to, or greater than the second.
				return (arg0[0] - arg1[0]);
			}
		});
		logger.info("Decided values::");
		for (Integer[] vals : decides) {
			logger.info("  id: " + vals[0] + "\t | Val: " + vals[1]);
		}
	}

	private void doPropose(String substring) {
		String parts[] = substring.split("-");
		int id = Integer.parseInt(parts[0]);
		int val = Integer.parseInt(parts[1]);
		trigger(new UCPropose(id, val+""), uc);
	}

	private void doRecover() {
		logger.debug("I RECOVERED :-)");
	}

	private final void doHelp() {
		logger.info("Available commands: P<m>, L<m>, S<n>, help, X");
		logger.info("Pm: sends perfect message 'm' to all neighbors");
		logger.info("Lm: sends lossy message 'm' to all neighbors");
		logger.info("Sn: sleeps 'n' milliseconds before the next command");
		logger.info("help: shows this help message");
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
