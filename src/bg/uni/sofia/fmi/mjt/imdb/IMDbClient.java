package bg.uni.sofia.fmi.mjt.imdb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class IMDbClient implements Runnable {

	private String movieTitle;
	private InetSocketAddress clientAddress;
	private SocketChannel clientSocket;

	public IMDbClient(Scanner input) throws IOException {
		this.movieTitle = getTitle(input);
		this.clientAddress = new InetSocketAddress("localhost", 4444);
		this.clientSocket = SocketChannel.open(clientAddress);

		//System.out.println("Connecting to Server on port 4444...");
	}
	
	@Override
	public void run() {
		
		sendServerRequest();
		getServerResponse();
		
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	public String getTitle(Scanner input) {
		String command;
		String[] words;
		
		
		System.out.println("Enter client command: ");
		
		do {
			command = input.nextLine();
			words = command.split(" ");
		} while (!words[0].equals("get-movie"));
				
		
				
		StringBuilder sb = new StringBuilder();
		for(int i=1;i<words.length;i++) {
			sb.append(words[i]);
		}
		return sb.toString();
		
		
	}
	
	public void sendServerRequest() {
		
		byte[] message = new String(movieTitle).getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(message);
		try {
			clientSocket.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//System.out.println("Sending to server: " + movieTitle);
		buffer.flip();
		buffer.clear();
		
	}
	
	public void getServerResponse() {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		try {
			clientSocket.read(bb);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = new String(bb.array()).trim();
		System.out.println("Sent to server: " + movieTitle + " Received from server: " + result);
	}



}
