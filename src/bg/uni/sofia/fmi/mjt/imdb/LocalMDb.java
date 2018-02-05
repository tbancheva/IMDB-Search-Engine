package bg.uni.sofia.fmi.mjt.imdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class LocalMDb {
	private List<HashMap<String, Object>> localMDb;

	public LocalMDb() {
		localMDb = new ArrayList<HashMap<String, Object>>();
	}

	public synchronized void add(HashMap<String, Object> movie) {
		localMDb.add(movie);
	}

	public int getSize() {
		return localMDb.size();
	}

	public String printTitles() {
		return 	localMDb.stream()
				 .map(s -> s.get("Title").toString().concat(" "))
		                .reduce("", String::concat);
	}
	
	public synchronized void sortByRating(int descOrAsc) {
		Collections.sort(localMDb, new Comparator<HashMap<String, Object>>() {
			public int compare(HashMap<String, Object> one, HashMap<String, Object> two) {
				return ((String) one.get("imdbRating")).compareTo(two.get("imdbRating").toString())*descOrAsc;
			}
		});
	}
	
	public String filter(String actors, String filterBy){
		return 	localMDb.stream()
				.filter( s -> s.get(filterBy).toString().contains(actors))
				.map(s -> s.get("Title").toString().concat(" "))
				.reduce("", String::concat);
	}
	
	public String getByActorsAndGenres(String actors, String genres){
		
		return 	localMDb.stream()
				.filter( s -> s.get("Actors").toString().contains(actors))
				.filter( s -> s.get("Genre").toString().contains(genres))
				.map(s -> s.get("Title").toString().concat(" "))
				.reduce("", String::concat);
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
	
	public String getField(String field, String title) {
		for (HashMap<String, Object> m : localMDb) {
			if(m.get("Title").equals(title) && isValidField(field)) {
				return  m.get(field).toString();
			}
		}
		return null;
	}
	
	public boolean isValidField(String field) {
		if(localMDb.get(0).keySet().contains(field) || field.equals("Episodes")) {
			return true;
		}
		return false;
	}
}
