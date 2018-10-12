package movie.database.commands;

import java.util.ArrayList;
import java.util.List;

import movie.database.LocalMDB;
import movie.database.exceptions.EmptyLocalMDBException;
import movie.database.exceptions.InvalidMovieFieldException;
import movie.database.exceptions.LocalMDBException;
import movie.database.exceptions.MissingCommandArgumentsException;

public class SortMovies implements Command {
	private LocalMDB localMDb;

	public SortMovies(LocalMDB localMDB) {
		this.localMDb = localMDB;
	}

	// Command format: sort-movies <field> [desc]
	
	@Override
	public String execute(String command) throws LocalMDBException{
		
		String[] words = command.split(" ");
		
		
		if(words.length < 2) {
			throw new MissingCommandArgumentsException();
		}
		
		String field = words[1];

		if (localMDb.isEmpty()) {
			throw new EmptyLocalMDBException();
		}
		
		if(!localMDb.isValidField(field)) {
			throw new InvalidMovieFieldException();
		}
		
		return getSortedMovies(command, field);

	}
	
	private String getSortedMovies(String command, String field) {

		List<String> sortedMovies = new ArrayList<>();

		if (command.contains("desc")) {
			sortedMovies = localMDb.sortByField(field, -1);
		} else {
			sortedMovies = localMDb.sortByField(field, 1);
		}

		return sortedMovies.stream().reduce("", String::concat);
	}

}
