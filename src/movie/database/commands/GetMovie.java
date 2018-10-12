package movie.database.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import movie.database.LocalMDB;
import movie.database.exceptions.LocalMDBException;
import movie.database.exceptions.MissingCommandArgumentsException;
import movie.database.exceptions.UnsuccessfulMovieRetrievingException;

public class GetMovie implements Command{
	
	private static final String TXT_FILE_EXTENSION = ".txt";
	private static final String CHAR_ENCODING = "UTF-8";
	
	protected LocalMDB localMDB;
	
	public GetMovie(LocalMDB localMDB) {
		this.localMDB = localMDB;
	}
	
	// Command format: get-movie <movie_name> 
	
	@Override
	public String execute(String command) throws LocalMDBException {
		
		if(command.split(" ").length < 2) {
			throw new MissingCommandArgumentsException();
		}
		
		String movieTitle = command.split("get-movie")[1].trim().replace(' ', '+');
		
		return saveMovie(movieTitle);
	}
	
	public String saveMovie(String title) throws LocalMDBException  {
		String movieTitle = title.replace("+", "");

		if (doesFileAlreadyExists(movieTitle + TXT_FILE_EXTENSION)) {
			addToLocalMDb(readFromFile(movieTitle));
			return readFromFile(movieTitle);
		}

		JSONObject json = getJSON(title);
		writeToFile(movieTitle, json);
		addToLocalMDb(json.toJSONString());
		return json.toJSONString();
	}
	
	public void addToLocalMDb(String json) {
		HashMap<String, Object> map = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
		}.getType());
		localMDB.add(map);
	}
	
	public boolean doesFileAlreadyExists(String fileName) {
		File file = new File(fileName);
		return (file.exists() && !file.isDirectory());
	}

	public static void writeToFile(String title, JSONObject obj) {
		try (FileWriter file = new FileWriter(title + TXT_FILE_EXTENSION)) {
			file.write(obj.toJSONString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String readFromFile(String title) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(title + TXT_FILE_EXTENSION))) {
			while (br.ready()) {
				sb.append(br.readLine());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return sb.toString();

	}

	public JSONObject getJSON(String title) throws LocalMDBException {
		
		URL url = null;
		
		try {
			url = new URL("http://www.omdbapi.com/?apikey=33eb477&t=" + title);
		} catch (MalformedURLException e) {
			throw new UnsuccessfulMovieRetrievingException(e);
		}
		
		try(InputStream inputStream = url.openStream()){
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, CHAR_ENCODING));
			return jsonObject;
		} catch (ParseException | IOException e) {
			throw new UnsuccessfulMovieRetrievingException(e);
		}
		
	}

}
