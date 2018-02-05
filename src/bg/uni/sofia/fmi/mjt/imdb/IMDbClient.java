package bg.uni.sofia.fmi.mjt.imdb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class IMDbClient implements Runnable {

	private String clientCommand;
	private InetSocketAddress clientAddress;
	private SocketChannel clientSocket;
	

	public IMDbClient(Scanner input) throws IOException {
		System.out.println("Enter client command: ");
		this.clientCommand = input.nextLine();
		
		this.clientAddress = new InetSocketAddress("localhost", 4444);
		this.clientSocket = SocketChannel.open(clientAddress);

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
	
	public void sendServerRequest() {

		byte[] message = new String(clientCommand).getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(message);
		try {
			clientSocket.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		System.out.println("Sent to server: " + clientCommand + " Received from server: " + result);
	}

}

