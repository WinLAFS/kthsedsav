/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.router.events;

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.messages.Message;

/**
 * The <code>GetResponsible</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: GetResponsible.java 294 2006-05-05 17:14:14Z roberto $
 */
public class GetResponsibleResponse extends Message {

	private static final long serialVersionUID = 942110961115830576L;

	private DKSRef responsible;

	private BigInteger id;

	public GetResponsibleResponse(BigInteger lookedupId, DKSRef responsible) {

		this.id = lookedupId;

		this.responsible = responsible;
	}

	public BigInteger getLookedUpId() {
		return id;
	}

	public DKSRef getResponsible() {
		return responsible;
	}

}
