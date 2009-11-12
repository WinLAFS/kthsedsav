package se.kth.ict.npj.hw1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The main class of Hangman server. Opens server socket and accepts client requests.
 *
 */
public class HangmanServer {

	/**
	 * Main method of Hangman server
	 * 
	 * @param args 1st argument - port of the server. Default port=9900
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		boolean listen = true;
		int serverPort;
		ServerSocket serverSocket = null;
		
		try {
			serverPort = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Can't parse server port number from command line");
			System.out.println("Using default server port 9900");
			serverPort = 9900;
		}
		
		try{
			serverSocket = new ServerSocket(serverPort);
		} catch (IOException e){
			System.err.println("Can't open server socket on port");
			System.exit(1);
		}
		
		while(listen){
			try{
				Socket clientSocket = serverSocket.accept();
				(new HangmanConHandler(clientSocket)).start();
				System.out.println("New client connected");
			} catch (IOException ex){
				System.err.println("Can't create client socket");
			}
		}
		serverSocket.close();
	}
}
