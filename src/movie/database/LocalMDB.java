package movie.database;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class LocalMDB {
	private static final String TITLE_FIELD = "Title";
	private Set<HashMap<String, Object>> localMDb;

	public LocalMDB() {
		localMDb = new HashSet<HashMap<String, Object>>();
	}

	public synchronized void add(HashMap<String, Object> movie) {
		localMDb.add(movie);
	}

	public boolean isEmpty() {
		return localMDb.isEmpty();
	}

	public String printTitles() {
		return 	localMDb.stream()
				        .map(movie -> movie.get(TITLE_FIELD).toString().concat(" "))
		                .reduce("", String::concat);
	}
	
	public boolean containsMovie(String movieTitle) {
		return isEmpty() ? false : localMDb.stream().anyMatch(m -> m.get(TITLE_FIELD).equals(movieTitle));
	}
	
	public List<String> sortByField(String field, int descOrAsc) {
		
		List<HashMap<String, Object>> movies = localMDb.stream().collect(Collectors.toList());
		
		movies.sort(new Comparator<HashMap<String, Object>>() {
			public int compare(HashMap<String, Object> m1, HashMap<String, Object> m2) {
				return (m1.get(field).toString().compareTo(m2.get(field).toString()))*descOrAsc;
			}
		});
		
		return movies.stream().map(movie->movie.get(TITLE_FIELD).toString().concat(" ")).collect(Collectors.toList());
	}

	
	public String getMoviesFiltered(String fieldFilteredBy, String searchedForPartOfField) {
		return 	localMDb.stream()
						.filter(movie -> movie.get(fieldFilteredBy).toString().contains(searchedForPartOfField))
						.map(movie -> movie.get(TITLE_FIELD).toString().concat(" "))
						.reduce("", String::concat);
	}
	
	
	public String getField(String field, String title) {
		return localMDb.stream()
					   .filter(m -> m.get(TITLE_FIELD).equals(title))
					   .map(movie -> movie.get(field).toString())
					   .reduce("", String::concat);
	}
	
	public boolean isValidField(String field) {
		return localMDb.stream().anyMatch(movie -> movie.get(field) != null);
	}
}
