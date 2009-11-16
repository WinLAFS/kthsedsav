package yacs.zemulation;

import dks.niche.ids.NicheId;

public class NicheIdEmulator extends NicheId {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	public NicheIdEmulator(){
		this.name = "NID.fake";
	}
	
	public NicheIdEmulator( String name ){
		this.name = name;
	}
	
	@Override
	public NicheId getId(){
		return this;
	}
	
	public String toString(){
		return name;
	}

}
