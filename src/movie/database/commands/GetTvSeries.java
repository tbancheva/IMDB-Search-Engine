package movie.database.commands;

import movie.database.LocalMDB;
import movie.database.exceptions.LocalMDBException;
import movie.database.exceptions.MissingCommandArgumentsException;


public class GetTvSeries extends GetMovie {

	public GetTvSeries(LocalMDB localMDB) {
		super(localMDB);
	}
	
	//Command format: get-tv-series <tv_series_name> season= <number_of_season>
	
	@Override
	public String execute(String command) throws LocalMDBException {
		String[] words = command.split("get-tv-series| season=");
		
		if(words.length < 3 || !command.contains("season=")) {
			throw new MissingCommandArgumentsException();
		}
		
		String normalTitle = words[1].trim();
		String queryTitle = normalTitle.replace(' ', '+') + "&Season=" + words[2].trim();
		
				
		if(!localMDB.containsMovie(normalTitle)) {
			saveMovie(normalTitle);
		}
		
		return getJSON(queryTitle).toJSONString();
		
	}
	
 }
