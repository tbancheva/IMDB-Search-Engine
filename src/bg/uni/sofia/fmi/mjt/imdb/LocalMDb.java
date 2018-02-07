package bg.uni.sofia.fmi.mjt.imdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import bg.uni.sofia.fmi.mjt.imdb.exceptions.MissingFieldException;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.MissingMovieTitleException;


public class LocalMDb {
	private List<HashMap<String, Object>> localMDb;

	public LocalMDb() {
		localMDb = new ArrayList<HashMap<String, Object>>();
	}

	public synchronized void add(HashMap<String, Object> movie) {
		localMDb.add(movie);
	}

	public boolean isEmpty() {
		return localMDb.isEmpty();
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
	
	public String filter(List<String> words, String filterBy){
		return 	localMDb.stream()
				.filter( s -> allAreContained(words, s.get(filterBy).toString()))
				.map(s -> s.get("Title").toString().concat(" "))
				.reduce("", String::concat);
	}
	
	public boolean allAreContained(List<String> words, String field) {
		return  words.stream().allMatch(field::contains);
	}
	
	public String getByActorsAndGenres(List<String> actors, List<String> genres){
		
		return 	localMDb.stream()
				.filter( s -> allAreContained(actors, s.get("Actors").toString()))
				.filter( s -> allAreContained(genres, s.get("Genre").toString()))
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
	
	public String getField(String field, String title) throws MissingMovieTitleException, MissingFieldException{
		for (HashMap<String, Object> m : localMDb) {
			if(m.get("Title").equals(title) && isValidField(field)) {
				return  m.get(field).toString();
			}
		}
		throw new MissingMovieTitleException("The movie is not in the databse");
	}
	
	public boolean isValidField(String field) throws MissingFieldException {
		if(localMDb.get(0).keySet().contains(field)) {
			return true;
		}

		throw new MissingFieldException("The filed is either missing or invalid");
	}
}
