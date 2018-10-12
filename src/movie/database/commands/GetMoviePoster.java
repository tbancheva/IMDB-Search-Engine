package movie.database.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import movie.database.LocalMDB;
import movie.database.exceptions.LocalMDBException;
import movie.database.exceptions.MissingCommandArgumentsException;

public class GetMoviePoster extends GetMovie {

	private static final String JPG_FILE_EXTENSION = ".jpg";
	private static final String POSTER_FIELD = "Poster";
	
	public GetMoviePoster(LocalMDB localMDB) {
		super(localMDB);
	}

	// Command format: get-movie-poster <movie_name> 
	
	@Override
	public String execute(String command) throws LocalMDBException{

		String[] words = command.split(" ");
		String movieTitle = words[1];
		
		if(words.length < 2) {
			throw new MissingCommandArgumentsException();
		}

		if (!localMDB.containsMovie(movieTitle)) {
			saveMovie(movieTitle);
		}
		
		savePoster(movieTitle);

		return Paths.get(movieTitle + JPG_FILE_EXTENSION).toString();
	}

	private void savePoster(String movieTitle) {
		String posterURL = localMDB.getField(POSTER_FIELD, movieTitle);

		try (InputStream in = new URL(posterURL).openStream()) {
			Files.copy(in, Paths.get(movieTitle.replace(" ", "") + JPG_FILE_EXTENSION));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
