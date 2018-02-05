package bg.uni.sofia.fmi.mjt.imdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class IMDbServer implements Runnable {

	private Selector selector;
	private ServerSocketChannel serverSocket;
	private InetSocketAddress address;
	private String response;
	private LocalMDb localMDb;

	public IMDbServer(LocalMDb localMDb) throws IOException {
		this.selector = selector.open();
		this.serverSocket = ServerSocketChannel.open();
		this.address = new InetSocketAddress("localhost", 4444);
		this.localMDb = localMDb;

		serverSocket.bind(address);
		serverSocket.configureBlocking(false);
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);

	}

	public void run() {
		try {
			runServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void runServer() throws IOException {
		while (true) {

			selector.select();

			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> it = keys.iterator();

			while (it.hasNext()) {
				SelectionKey myKey = it.next();
				synchronized (myKey) {
					if (myKey.isAcceptable()) {
						SocketChannel clientSocket = serverSocket.accept();
						clientSocket.configureBlocking(false);
						clientSocket.register(selector, SelectionKey.OP_READ);
					} else if (myKey.isReadable()) {
						SocketChannel clientSocket = (SocketChannel) myKey.channel();
						ByteBuffer bb = ByteBuffer.allocate(256);
						clientSocket.read(bb);
						String result = new String(bb.array()).trim();

						response = getCommand(result);

						bb.flip();
						bb.clear();

						clientSocket.register(selector, SelectionKey.OP_WRITE);
					} else if (myKey.isWritable()) {
						SocketChannel clientSocket = (SocketChannel) myKey.channel();
						ByteBuffer bb = ByteBuffer.allocate(1024);

						bb = ByteBuffer.wrap(response.getBytes());
						clientSocket.write(bb);

						bb.flip();
						bb.clear();
						clientSocket.close();
					}
				}
			}
			it.remove();
		}

	}

	public String handleRequest(String title) {
		String movieTitle = title.replace("+", "");
		
		if (doesFileAlreadyExists(movieTitle + ".txt")) {
			addToLocalMDb(readFromFile(movieTitle));
			return readFromFile(movieTitle);
		}

		try {
			JSONObject json = getJSON(title);
			writeToFile(movieTitle, json);
			addToLocalMDb(json.toJSONString());
			return json.toJSONString();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void addToLocalMDb(String json) {
		HashMap<String, Object> map = new Gson().fromJson(json, new TypeToken<HashMap<String, Object>>() {
		}.getType());
		localMDb.add(map);
	}

	public static void writeToFile(String title, JSONObject obj) {
		try (FileWriter file = new FileWriter(title + ".txt")) {
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

		try (BufferedReader br = new BufferedReader(new FileReader(title + ".txt"))) {
			while (br.ready()) {
				sb.append(br.readLine());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sb.toString();

	}

	public JSONObject getJSON(String title) throws IOException, ParseException {

		URL url = new URL("http://www.omdbapi.com/?apikey=619a884e&t=" + title);
		InputStream inputStream = url.openStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));

		return jsonObject;
	}
	
	public String getCommand(String command) {
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
		} else {
			System.out.println("inavalid command");
			// throw new Invalid Command Exception - to do
		}
		return null;
	}
	
	public String getTvSeries(String command) {
		String[] words;
		words = command.split("get-tv-series| season=");
		String movieTitle = words[1].trim();
		String queryTitle = movieTitle.replace(' ', '+') + "&Season=" + words[2].trim();
		
		handleRequest(queryTitle);
		
		return localMDb.getField("Episodes", movieTitle);
	}
	
	public String getMoviePoster(String command) {
		String[] words;
		words = command.split("get-movie-poster");
		String normalTitle = words[1].trim();
		String queryTitle = words[1].trim().replace(' ', '+');
					
		handleRequest(queryTitle); // adding the movie and its file to the local movie database
				
		String posterURL = localMDb.getPosterUrl(normalTitle);
		try(InputStream in = new URL(posterURL).openStream()){   // downloading the poster
		    Files.copy(in, Paths.get(normalTitle.replace(" ", "") + ".jpg"));
		}catch(IOException e) {
			e.printStackTrace();
		}
		return Paths.get(normalTitle + ".jpg").toString();
	}
	
	public String getMovie(String command){
		String[] words;
		words = command.split(",|\\s"); // split command on commas and spaces
		
		String movieTitle;
									
		if(!command.contains("fields")) {    //if there are no fields listed we just check for the movie
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < words.length; i++) {
				sb.append(words[i]);
				sb.append('+');  // + is added between the words of the title because that is how 
			}                    // the query to the imdb api must look
			int last = sb.lastIndexOf("+");
			sb.deleteCharAt(last);
			movieTitle = sb.toString();
			
			return handleRequest(movieTitle);
		}else {         // if there are fields listed first we check for the movie then we acquire the fields
			StringBuilder sb = new StringBuilder();
			List<String> fields = new ArrayList<>();
			int fieldsBeginIndex = 0;
			
			for (int i = 1; !words[i].equals("fields="); i++) {  //acquiring the title
				sb.append(words[i]);
				sb.append('+');
				fieldsBeginIndex = i;
			}
			fieldsBeginIndex+=2;
			int last = sb.lastIndexOf("+");
			sb.deleteCharAt(last);
			movieTitle = sb.toString();
			handleRequest(movieTitle);  // adding the movie and its file to the local movie database
			
			for (int i = fieldsBeginIndex; i<words.length; i++) {  //acquiring the fields
				if(!words[i].isEmpty()) {
					fields.add(words[i]);
				}
			}
			
			String normalTitle = movieTitle.replace('+', ' ');
			StringBuilder result = new StringBuilder();
			for(String w: fields) {     //accumulating the fields with their values
				result.append(w + ": " + localMDb.getField(w, normalTitle) + " ");
			}	
			return result.toString();
		}
		
	}

	public String getMovies(String command) {
		
		if (command.contains("order") && command.contains("asc")) {
			localMDb.sortByRating(1);
		} else if (command.contains("order") && command.contains("desc")) {
			localMDb.sortByRating(-1);
		}
		
		if (command.contains("genres") && command.contains("actors")) {
			String[] words = command.split("actors=| genres=");
			 return localMDb.getByActorsAndGenres(words[1].trim(), words[2].trim());
		}else if (command.contains("actors")) {
			String[] words = command.split("actors=");
			return localMDb.filter(words[1].trim(), "Actors");
		}else if(command.contains("genres")) {
			String[] words = command.split("genres=");
			return localMDb.filter(words[1].trim(), "Genre");
		}
		
		return localMDb.printTitles();
		
	}

}
