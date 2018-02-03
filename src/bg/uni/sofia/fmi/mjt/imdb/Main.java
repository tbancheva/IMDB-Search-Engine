package bg.uni.sofia.fmi.mjt.imdb;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;




public class Main {

	public static void main(String args[]) throws IOException {

		LocalMDb localMDb = new LocalMDb();
		Thread server = new Thread(new IMDbServer(localMDb));
		Scanner input = new Scanner(System.in);
		
		Thread client1 = new Thread(new IMDbClient(input));
		//Thread client2 = new Thread(new IMDbClient(input));
		//Thread client3 = new Thread(new IMDbClient(input));
		//Thread client2 = new Thread(new IMDbClient(input));
		client1.start();
		//client2.start();
		//client3.start();
		server.start();
	
		
		try {
			client1.join();
			//client2.join();
			//client3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String title = localMDb.getPosterUrl("300");
		try(InputStream in = new URL(title).openStream()){
		    Files.copy(in, Paths.get("300.jpg"));
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		//System.out.println(localMDb.getSize());	
		/*System.out.println("Before sorting");
		localMDb.printTitles();
		localMDb.sortByRatingDesc();
		System.out.println("After sorting");
		localMDb.printTitles();
		
		System.out.println("Genres ------------------");
		List<String> sortedGenre = new ArrayList<>();
		sortedGenre = localMDb.getByGenre("Drama");
		for(int i =0; i < sortedGenre.size();i++) {
			System.out.println(sortedGenre.get(i));
		}
		
		System.out.println("Actors--------------------");
		List<String> sortedActors = new ArrayList<>();
		sortedActors = localMDb.getByActors("Leonardo DiCaprio");
		for(int i =0; i < sortedActors.size();i++) {
			System.out.println(sortedActors.get(i));
		}*/
		
		input.close();
			

	}

	
}
