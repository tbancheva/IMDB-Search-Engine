package movie.database.commands;

import movie.database.LocalMDB;
import movie.database.exceptions.EmptyLocalMDBException;
import movie.database.exceptions.InvalidMovieFieldException;
import movie.database.exceptions.LocalMDBException;
import movie.database.exceptions.MissingCommandArgumentsException;

public class FilterMovies implements Command {

	private LocalMDB localMDb;

	public FilterMovies(LocalMDB localMDB) {
		this.localMDb = localMDB;
	}

	// Command format: filter-movies <field>= <field_value>
	
	@Override
	public String execute(String command) throws LocalMDBException {

		if (localMDb.isEmpty()) {
			throw new EmptyLocalMDBException();
		}

		String[] words = command.split("(=|\\s)"); // regex to split by "=" and whitespaces
		String filterBy = words[1];
		String field = words[3];
		
		if (words.length < 3) {
			throw new MissingCommandArgumentsException();
		}

		if (!localMDb.isValidField(filterBy)) {
			throw new InvalidMovieFieldException();
		}
		
		return localMDb.getMoviesFiltered(filterBy, field);

	}

}
