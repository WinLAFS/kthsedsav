package yacs.zemulation;

import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;

public class ComponentIdEmulator extends ComponentId implements IdentifierInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private NicheIdEmulator nid = null;
	
	public ComponentIdEmulator( String nid ){
		this.nid = new NicheIdEmulator(nid);
	}
	
	public ComponentIdEmulator(){
		nid = new NicheIdEmulator("NID.fake");
	}
	
	public NicheId getId(){
		return nid;
	}
	
	public String toString(){
		if( nid != null )
			return nid.toString();
		else
			return this.getClass().getName()+"@"+String.valueOf(this.hashCode());
	}

}
