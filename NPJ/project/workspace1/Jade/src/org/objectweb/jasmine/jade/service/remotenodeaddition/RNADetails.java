package org.objectweb.jasmine.jade.service.remotenodeaddition;
import java.util.HashMap;

public final class RNADetails {

	private final String VORegistryHost;
	private final int VORegistryPort;
	private final String memberId;
	private final HashMap<String,String> configProperties;
	
	public RNADetails(String VORegistryHost, int VORegistryPort, String memberId, HashMap<String,String> configProperties){
		this.VORegistryHost=VORegistryHost;
		this.VORegistryPort=VORegistryPort;
		this.memberId=memberId;
		this.configProperties=configProperties;
	}

	public String getVORegistryHost() {
		return VORegistryHost;
	}

	public int getVORegistryPort() {
		return VORegistryPort;
	}

	public String getMemberId() {
		return memberId;
	}

	public HashMap<String,String> getConfigProperties() {
		return configProperties;
	}
}
