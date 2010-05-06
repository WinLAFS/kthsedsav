package se.sics.kompics.p2p.simulator;

import se.sics.kompics.PortType;
import se.sics.kompics.p2p.experiment.dsl.events.TerminateExperiment;

public class SimulatorPort extends PortType {{
	positive(BTPeerJoin.class);
	positive(BTPeerFail.class);	
	positive(TrackerJoin.class);
	
	negative(TerminateExperiment.class);
}}
