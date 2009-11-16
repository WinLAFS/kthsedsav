package yacs.utils.monitoring;

import java.net.*;
import java.io.*;

import yacs.interfaces.YACSSettings;

public class MProducer {
	private int port;
	private String monitorHost;
	private DatagramSocket connection;
	
	public MProducer(){
		this( YACSSettings.MONITORING_HOST, YACSSettings.MONITORING_PORT );
	}
	
	public MProducer( String monitorHost, int port ){
		this.port = port;
		this.monitorHost = monitorHost;
		
	}
	
	public void send( Serializable message ){
		
		if( !YACSSettings.MONITORING_ACTIVATED ){
			log( "Monitoring not active" );
			return;
		}
		
		try
		{
			if( connection == null )
				connection = new DatagramSocket();
			
			InetSocketAddress forwarder = new InetSocketAddress( monitorHost, port );
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream ois = new ObjectOutputStream(baos);
			ois.writeObject(message);

			DatagramPacket dp = new DatagramPacket(baos.toByteArray(), baos.size(),	forwarder);
			connection.send(dp);
			
			log("MProducer.sent: " + message);
		}
		catch( Exception e ){
			log( "MProducer - unable to send message: " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	protected void log( String message ){
		// TODO: real logging
		System.err.println( message );
	}
}
