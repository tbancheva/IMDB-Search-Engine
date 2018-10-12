package movie.database.commands;

import movie.database.LocalMDB;
import movie.database.exceptions.InvalidMovieFieldException;
import movie.database.exceptions.LocalMDBException;
import movie.database.exceptions.MissingCommandArgumentsException;

public class GetMovieField extends GetMovie {

	public GetMovieField(LocalMDB localMDB) {
		super(localMDB);
	}
	
	// Command format: get-movie-fields <movie_name> fields= [field_1, field_2]

	@Override
	public String execute(String command) throws LocalMDBException {
		
		if (command.split(" ").length < 4) {
			throw new MissingCommandArgumentsException();
		}

		String[] words = command.split("get-movie-fields | fields=");
		String movieTitle = words[1].trim();
		String[] fields = words[2].split(",|\\s"); // splitting the fields by commas and whitespaces 
												   //to get the field names
		
		if (!localMDB.containsMovie(movieTitle)) {
			saveMovie(movieTitle);
		}

		StringBuilder result = new StringBuilder();
		for (String field : fields) {
			if (localMDB.isValidField(field)) {
				result.append(field + ": " + localMDB.getField(field, movieTitle) + " ");
			}else if(!field.isEmpty() && !localMDB.isValidField(field)){
				throw new InvalidMovieFieldException();
			}
		}
		return result.toString();

	}
}
