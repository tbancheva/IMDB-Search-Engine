package movie.database.commands;

import movie.database.LocalMDB;
import movie.database.exceptions.EmptyLocalMDBException;
import movie.database.exceptions.LocalMDBException;

public class GetMovies implements Command{
	private LocalMDB localMDb;

	public GetMovies(LocalMDB localMDB) {
		this.localMDb = localMDB;
	}

	// Command format: get-movies
	
	@Override
	public String execute(String command) throws LocalMDBException{

		if (localMDb.isEmpty()) {
			throw new EmptyLocalMDBException();
		}

		return localMDb.printTitles();
	}
}
