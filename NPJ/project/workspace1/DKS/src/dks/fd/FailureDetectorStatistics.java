/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.fd;

import static dks.fd.FailureDetectorConstants.FD_MINIMUM_RTO;

import org.apache.log4j.Logger;

/**
 * The <code>ConnectionDetails</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id:FailureDetectorStatistics.java 98 2006-11-14 15:13:32Z cosmin $
 */
public class FailureDetectorStatistics {

	/*#%*/ private static Logger log = Logger.getLogger(FailureDetectorStatistics.class);

	/* Metric statistics */

	private double avgRTT;

	// private long count;

	private double varRTT;

	// private long diff;

	private double RTO;

	private double showedRTO;

	private double alpha = 0.125;

	private double beta = 0.25;

	private long K = 4;

	/**
	 * FailureDetectorStatistics Constructor
	 */
	public FailureDetectorStatistics() {
		avgRTT = 0;
		// count = 0;
		varRTT = 0.0;
		// diff = 0;
		RTO = -1;
	}

	/**
	 * Updates the average RTO, we use a TCP-style calculation of the RTO
	 * 
	 * @param rtt
	 *            The RTT of the packet
	 */
	public void updateRTO(long rtt) {

		if (RTO == -1) {
			// Set RTO to RTT if it's the first time it's updated
			// this.count = 1;

			/*
			 * SRTT <- R, RTTVAR <- R/2, RTO <- SRTT + max (G, K*RTTVAR)
			 * 
			 */
			this.avgRTT = rtt;
			this.varRTT = rtt / 2.0;

			this.RTO = avgRTT + K * varRTT;

			/*#%*/ log.debug("Initial RTO " + RTO);
		} else {

			// log.debug("Changing RTO " + RTO);
			// log.debug("VAR " + varRTT);
			// log.debug("AVG " + avgRTT);
			// log.debug("Beta "+beta);
			// log.debug("Alpha "+alpha);

			// RTTVAR <- (1 - beta) * RTTVAR + beta * |SRTT - R'|
			this.varRTT = (1 - beta) * varRTT + beta * Math.abs((avgRTT - rtt));

			// log.debug("Variance " + varRTT);
			// SRTT <- (1 - alpha) * SRTT + alpha * R'
			this.avgRTT = (1 - alpha) * avgRTT + alpha * rtt;

			// log.debug("Average " + avgRTT);

			// RTO = AVG + K x VAR;
			this.RTO = avgRTT + K * varRTT;

			// log.debug("Result RTO " + RTO);

			// // AVG = (((AVG * CNT) + RTT) / (CNT + 1));
			// this.avgRTT = (((avgRTT * count) + RTT) / (count + 1));

			// log.debug("Average RTT " + avgRTT);
			//
			// // DIFF = (AVG - RTT)^2;
			// this.diff = pow((avgRTT - RTT), 2);
			//
			// log.debug(" DIFF " + diff);
			//			
			// log.debug("Var RTT before "+ varRTT);
			//
			// // VAR = (((VAR * CNT) + DIFF) / (CNT + 1)); // variance of RTT
			// this.varRTT = (((varRTT * count) + diff) / (count + 1));
			//
			// log.debug("Variance " + varRTT);
			// // CNT++;
			// this.count++;
			//
			// // RTO = AVG + 4 x VAR;
			// this.RTO = avgRTT + K * varRTT;

		}
		// log.debug("RTO before check if between max and min value " + RTO);
		if (this.RTO < FD_MINIMUM_RTO) {
			this.showedRTO = FD_MINIMUM_RTO;

			// maximum does not make sense
			// } else if (this.RTO > FD_MAXIMUM_RTO) {
			// this.showedRTO = FD_MAXIMUM_RTO;
		} else {
			this.showedRTO = RTO;
		}
	}

	/**
	 * Gets the current RTO of the connection
	 * 
	 * @return the current estimated round trip time for this connection
	 */

	public long getRTO() {
		return (long) showedRTO;
	}

}
