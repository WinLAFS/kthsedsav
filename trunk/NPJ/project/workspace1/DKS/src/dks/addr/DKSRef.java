/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.addr;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * The <code>DKSRef</code> class represents a reference to a DKS d. It
 * contains the d's network address and the DKS identifier
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSRef.java 475 2007-12-06 12:18:08Z roberto $
 */
public class DKSRef implements Comparable<DKSRef> , Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 6916949686399156220L;

	private InetAddress ip;

	private int port;

	private BigInteger id;

	private String dksref;

	private static final Pattern dksRef = Pattern.compile(
			"^ *?dks://(.*?):(\\d*.?)/(\\d*?) *?$", Pattern.CASE_INSENSITIVE);

	/* when adding new fields don't forget to regenerate hashCode() */

	/**
	 * Constructor for DKSRef
	 * 
	 * @param ip
	 * @param port
	 * @param id
	 */
	public DKSRef(InetAddress ip, int port, BigInteger id) {
		super();
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.dksref = "dks://" + ip.getHostAddress() + ":" + port + "/" + id;
	}

	public DKSRef(String dksref) throws MalformedURLException,
			UnknownHostException {
System.out.println(1111111111);
		if (!dksRef.matcher(dksref).matches()) {
			throw new MalformedURLException(
					"Only DKS scheme supported (dks://<IP>:<PORT>/<UNIQUE_GUID>), DKSRef passed:"
							+ dksref);
		}
		
		String colons = dksRef.matcher(dksref).replaceAll("$1:$2:$3");

		String[] sp = colons.split(":");

		this.ip = InetAddress.getByName(sp[0]);
		this.port = Integer.parseInt(sp[1]);
		this.id = new BigInteger(sp[2]);
		this.dksref = "dks://" + ip.getHostAddress() + ":" + port + "/" + id;
	}

	public String getDKSWebURL() {
		return "http://" + ip.getHostAddress() + ":" + (port+1) + "/info/" + id;
	}

	/**
	 * @return Returns the IP address
	 */
	public InetAddress getIp() {
		return ip;
	}

	/**
	 * @return Returns the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return Returns the id.
	 */
	public BigInteger getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((id == null) ? 0 : id.hashCode());
		result = PRIME * result + ((ip == null) ? 0 : ip.hashCode());
		result = PRIME * result + port;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DKSRef o) {
		if (!(o instanceof DKSRef)){
			
			System.out.println("CClass ="+o.getClass());
			throw new ClassCastException();			
		}
		if (((DKSRef) o).id.compareTo(id) < 0)
			return 1;
		if (((DKSRef) o).id.compareTo(id) > 0)
			return -1;
		else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DKSRef other = (DKSRef) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!ip.equals(other.ip)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

	/**
	 * @return Returns the String representation of the DKSRef.
	 */
	@Override
	public String toString() {
		return dksref;
	}
}