package yacs.resources.data;

import java.io.Serializable;

/**
 * Information about load and availability issues of the system.
 * @author LTDATH
 */
public class LoadInformation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private LOAD load;
	
	public LoadInformation(LOAD load){
		this.load = load;
	}
	
	
	public LOAD getLoad() {
		return load;
	}
	public void setLoad(LOAD load) {
		this.load = load;
	}

	//
	public enum LOAD {
		LOW,
		HIGH
	}

}
