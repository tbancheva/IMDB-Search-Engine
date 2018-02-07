package bg.uni.sofia.fmi.mjt.imdb;


import java.io.IOException;

import java.util.Scanner;


public class Main {

	public static void main(String args[]) throws IOException {

		LocalMDb localMDb = new LocalMDb();
		Thread server = new Thread(new IMDbServer(localMDb));
		Scanner input = new Scanner(System.in);
		
		System.out.println("Enter number of clients: ");
		int numberOfClients = input.nextInt();
		input.nextLine(); // Consuming new-line left-over
		
		Thread[] threads = new Thread[numberOfClients];
		
		for(int i=0; i<numberOfClients;i++) {
			threads[i] = new Thread(new IMDbClient(input));
			threads[i].start();
		}
				
		server.start();
			
		try {
			for(int i=0; i<numberOfClients;i++) {
				threads[i].join();
			}
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		input.close();
		
				
	}

	
}
