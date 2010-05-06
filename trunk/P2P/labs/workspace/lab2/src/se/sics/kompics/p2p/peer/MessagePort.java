package se.sics.kompics.p2p.peer;

import se.sics.kompics.PortType;

public class MessagePort extends PortType {{
	negative(BTMessage.class);
	positive(BTMessage.class);
}}
