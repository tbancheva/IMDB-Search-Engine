package bg.uni.sofia.fmi.mjt.imdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bg.uni.sofia.fmi.mjt.imdb.exceptions.EmptyLocalMDbException;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.InvalidCommandException;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.MissingFieldException;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.MissingMovieTitleException;

public class IMDbServer implements Runnable {

	private static final int BLOCKING_QUEUE_CAPACITY = 100;
	private static final String CHAR_ENCODING = "UTF-8";
	private static final String JPG_FILE_EXTENSION = ".jpg";
	private static final String TXT_FILE_EXTENSION = ".txt";

	private Selector selector;
	private ServerSocketChannel serverSocket;
	private InetSocketAddress address;
	private BlockingQueue<String> requests;

	private LocalMDb localMDb;

	public IMDbServer(LocalMDb localMDb) throws IOException {
		this.selector = selector.open();
		this.serverSocket = ServerSocketChannel.open();
		this.address = new InetSocketAddress("localhost", 4444);
		this.localMDb = localMDb;
		requests = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);

		serverSocket.bind(address);
		serverSocket.configureBlocking(false);
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void run() {
		try {
			runServer();
		} catch (IOException | InvalidCommandException | EmptyLocalMDbException | MissingMovieTitleException
				| ParseException | MissingFieldException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void runServer() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException,
			MissingFieldException, IOException, ParseException, InterruptedException {

		while (true) {

			int readyChannels = selector.select();
			if (readyChannels == 0)
				continue;

			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();

			while (it.hasNext()) {
				SelectionKey myKey = it.next();

				if (myKey.isAcceptable()) {
					SocketChannel clientSocket = serverSocket.accept();
					clientSocket.configureBlocking(false);
					clientSocket.register(selector, SelectionKey.OP_READ);
				} else if (myKey.isReadable()) {
					SocketChannel clientSocket = (SocketChannel) myKey.channel();
					ByteBuffer bb = ByteBuffer.allocate(256);

					clientSocket.read(bb);

					String request = new String(bb.array()).trim();
					System.out.println("Received: " + request);
					requests.put(request);

					clientSocket.register(selector, SelectionKey.OP_WRITE);
				} else if (myKey.isWritable()) {
					SocketChannel clientSocket = (SocketChannel) myKey.channel();
					ByteBuffer bb = ByteBuffer.allocate(2048);

					bb = ByteBuffer.wrap(getResponse(requests.take()).getBytes());
					clientSocket.write(bb);

					clientSocket.close();
				}
			}

			it.remove();
		}

	}

	public String getResponse(String command) throws InvalidCommandException, EmptyLocalMDbException,
			MissingMovieTitleException, MissingFieldException, IOException, ParseException {

		String[] words;
		words = command.split(" ");

		if (words[0].equals("get-movie")) {
			return getMovie(command);
		} else if (words[0].equals("get-movies")) {
			return getMovies(command);
		} else if (words[0].equals("get-tv-series")) {
			return getTvSeries(command);
		} else if (words[0].equals("get-movie-poster")) {
			return getMoviePoster(command);
		}

		throw new InvalidCommandException("The command is not valid");

	}
	
	public String getMovie(String command)
			throws MissingMovieTitleException, MissingFieldException, IOException, ParseException {

		if (!command.contains("fields")) {
			String[] words;
			words = command.split("get-movie");
			return handleRequest(words[1].trim().replace(' ', '+'));
		} else {
			String[] words;
			words = command.split("get-movie | fields=");

			String[] fields = words[2].split(",");

			StringBuilder result = new StringBuilder();
			for (String w : fields) {
				result.append(w + ": " + localMDb.getField(w.trim(), words[1].trim()) + " ");
			}
			return result.toString();
		}

	}

	public String handleRequest(String title) throws MissingMovieTitleException, IOException, ParseException {
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
		localMDb.add(map);
	}

	public static void writeToFile(String title, JSONObject obj) {
		try (FileWriter file = new FileWriter(title + TXT_FILE_EXTENSION)) {
			file.write(obj.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean doesFileAlreadyExists(String fileName) {
		File file = new File(fileName);
		return (file.exists() && !file.isDirectory());
	}

	public String readFromFile(String title) {

		StringBuilder sb = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new FileReader(title + TXT_FILE_EXTENSION))) {
			while (br.ready()) {
				sb.append(br.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();

	}

	public JSONObject getJSON(String title) throws MissingMovieTitleException, ParseException, IOException {

		URL url;
		try {
			url = new URL("http://www.omdbapi.com/?apikey=619a884e&t=" + title);
		} catch (MalformedURLException e) {
			throw new MissingMovieTitleException("There is no movie with that title");
		}
		InputStream inputStream = url.openStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, CHAR_ENCODING));
		inputStream.close();
				
		return jsonObject;
	}

	public String getTvSeries(String command)
			throws MissingMovieTitleException, IOException, ParseException {
		String[] words;
		words = command.split("get-tv-series| season=");
		String queryTitle = words[1].trim().replace(' ', '+') + "&Season=" + words[2].trim();

		return getJSON(queryTitle).toJSONString();
	}

	public String getMoviePoster(String command) throws MissingMovieTitleException, IOException, ParseException {
		String[] words;
		words = command.split("get-movie-poster");
		String normalTitle = words[1].trim();
		String queryTitle = words[1].trim().replace(' ', '+');

		handleRequest(queryTitle);

		String posterURL = localMDb.getPosterUrl(normalTitle);
		try (InputStream in = new URL(posterURL).openStream()) {
			Files.copy(in, Paths.get(normalTitle.replace(" ", "") + JPG_FILE_EXTENSION));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Paths.get(normalTitle + JPG_FILE_EXTENSION).toString();
	}


	public String getMovies(String command) throws EmptyLocalMDbException {

		if (localMDb.isEmpty()) {
			throw new EmptyLocalMDbException("The local movie database is empty.");
		}

		if (command.contains("order") && command.contains("asc")) {
			localMDb.sortByRating(1);
		} else if (command.contains("order") && command.contains("desc")) {
			localMDb.sortByRating(-1);
		}

		if (command.contains("genres") && command.contains("actors")) {
			String[] words = command.split("actors=| genres=");
			String[] genres = words[1].trim().split(",");
			String[] actors = words[2].trim().split(",");

			List<String> actorsTrimmed = new ArrayList<>();
			List<String> genresTrimmed = new ArrayList<>();

			for (String s : genres) {
				genresTrimmed.add(s.trim());
			}

			for (String s : actors) {
				actorsTrimmed.add(s.trim());
			}
			return localMDb.getByActorsAndGenres(actorsTrimmed, genresTrimmed);
		} else if (command.contains("actors")) {
			return getMoviesFiltered(command, "Actors");
		} else if (command.contains("genres")) {
			return getMoviesFiltered(command, "Genre");
		}

		return localMDb.printTitles();

	}

	public String getMoviesFiltered(String command, String filterBy) {
		String splitAt = filterBy.equals("Genre") ? "genres=" : "actors=";

		String[] words = command.split(splitAt);
		String[] searchedFor = words[1].trim().split(",");

		List<String> searchedForTrimmed = new ArrayList<>();

		for (String s : searchedFor) {
			searchedForTrimmed.add(s.trim());
		}

		return localMDb.filter(searchedForTrimmed, filterBy);
	}

}
