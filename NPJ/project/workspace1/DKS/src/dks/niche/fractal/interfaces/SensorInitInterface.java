/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.fractal.interfaces;

import dks.niche.ids.ComponentId;

/**
 * The <code>SensorInitInterface</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: SensorInitInterface.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public interface SensorInitInterface extends InitInterface {
	public void initComponentId(ComponentId cid);
}
