package bg.uni.sofia.fmi.mjt.imdb;

import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String args[]) throws IOException {

		LocalMDb localMDb = new LocalMDb();
		Thread server = new Thread(new IMDbServer(localMDb));
		Scanner input = new Scanner(System.in);
		
		Thread client1 = new Thread(new IMDbClient(input));
		//Thread client2 = new Thread(new IMDbClient(input));
		//Thread client3 = new Thread(new IMDbClient(input));
		//Thread client4 = new Thread(new IMDbClient(input));
		client1.start();
		//client2.start();
		//client3.start();
		//client4.start();
		server.start();
	
		
		//try {
		//	client1.join();
		//	client2.join();
			//client3.join();
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//}
					
			
		input.close();
	
		
	}
	
}
