package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	private static final String HOST = "localhost";
	private static final int PORT = 4444;
	private static final String ENCODING = "UTF-8";

	private Socket clientSocket;

	public Client() {
		try {
			this.clientSocket = new Socket(HOST, PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendToServer() {
		try (Scanner input = new Scanner(System.in);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), ENCODING));
				PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());) {

			while (true) {
				System.out.println(in.readLine());
				String request = input.nextLine();
				pw.println(request);
				pw.flush();

				if (request.equals("exit")) {
					break;
				}

				String received = in.readLine();
				System.out.println(received);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException {
		Client client = new Client();
		client.sendToServer();
	}

}
