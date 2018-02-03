package bg.uni.sofia.fmi.mjt.imdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class LocalMDb {
	private List<HashMap<String, Object>> localMDb;

	public LocalMDb() {
		localMDb = new ArrayList<HashMap<String, Object>>();
	}

	public void add(HashMap<String, Object> movie) {
		localMDb.add(movie);
	}

	public int getSize() {
		return localMDb.size();
	}

	public void printTitles() {
		for (HashMap<String, Object> m : localMDb) {
			System.out.println(m.get("Title"));
		}
	}

	//order ascending
	public void sortByRatingAsc() {
		Collections.sort(localMDb, new Comparator<HashMap<String, Object>>() {
			public int compare(HashMap<String, Object> one, HashMap<String, Object> two) {
				return ((String) one.get("imdbRating")).compareTo(two.get("imdbRating").toString());
			}
		});
	}
	
	//order descending
	public void sortByRatingDesc() {
		Collections.sort(localMDb, new Comparator<HashMap<String, Object>>() {
			public int compare(HashMap<String, Object> one, HashMap<String, Object> two) {
				return ((String) one.get("imdbRating")).compareTo(two.get("imdbRating").toString())*-1;
			}
		});
	}
	
	public List<String> getByGenre(String genre){
		List<String> filteredByGenre = new ArrayList<>();
		filteredByGenre = localMDb.stream()
					  .filter( s -> s.get("Genre").toString().contains(genre))
					  .map(s -> s.get("Title").toString())
					  .collect(Collectors.toList());
		return filteredByGenre;
	}
	
	public List<String> getByActors(String actors){
		List<String> filteredByActors = new ArrayList<>();
		filteredByActors = localMDb.stream()
					   .filter( s -> s.get("Actors").toString().contains(actors))
					   .map(s -> s.get("Title").toString())
					   .collect(Collectors.toList());
		return filteredByActors;
	}
	
	public String getPosterUrl(String title) {
		String poster = null;
		for (HashMap<String, Object> m : localMDb) {
			if(m.get("Title").equals(title)) {
				poster = m.get("Poster").toString();
			}
		}
		
		return poster;
	}
}
