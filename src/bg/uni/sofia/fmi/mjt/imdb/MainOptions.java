package bg.uni.sofia.fmi.mjt.imdb;


import java.io.IOException;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String args[]) throws IOException {

		LocalMDb localMDb = new LocalMDb();
		Thread server = new Thread(new IMDbServer(localMDb));
		Scanner input = new Scanner(System.in);
		
		//System.out.println("Enter number of clients: ");
		//int numberOfClients = input.nextInt();
		int numberOfClients = 3;
		Thread[] threads = new Thread[numberOfClients+1];
		
		for(int i=0; i<numberOfClients;i++) {
			threads[i] = new Thread(new IMDbClient(input));
			threads[i].start();
		}
		
			
		/*ExecutorService executor = Executors.newFixedThreadPool(numberOfClients);
		for(int i = 0; i<numberOfClients;i++) {
			Runnable client = new IMDbClient(input);
			executor.execute(client);
		}*/
		
			
		
		/*Thread client1 = new Thread(new IMDbClient(input));
		Thread client2 = new Thread(new IMDbClient(input));
		Thread client3 = new Thread(new IMDbClient(input));
		Thread client4 = new Thread(new IMDbClient(input));
		client1.start();
		client2.start();
		client3.start();
		client4.start();*/
		
		server.start();
		
		
		/*executor.shutdown();
		while (!executor.isTerminated()) { }*/
		
		try {
			for(int i=0; i<numberOfClients;i++) {
				threads[i].join();
			}
			
			/*client1.join();
			client2.join();
			client3.join();
			client4.join();*/
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
					
			
		input.close();
		
				
	}

	
}
