package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import movie.database.LocalMDB;

public class Server {

	private ServerSocket ss;
	private LocalMDB localMDb;

	public Server(LocalMDB localMDB) {
		this.localMDb = localMDB;

		try {
			this.ss = new ServerSocket(4444);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runServer() throws IOException {
		while (true) {
			Socket clientSocket = ss.accept();
			Thread t = new Thread(new ClientHandler(clientSocket, localMDb));
			t.start();
		}
	}

	public static void main(String[] args) {
		LocalMDB localMDB = new LocalMDB();
		Server server = new Server(localMDB);
		try {
			server.runServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
