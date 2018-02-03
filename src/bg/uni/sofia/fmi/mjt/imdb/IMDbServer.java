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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class IMDbServer implements Runnable{

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
				// synchronized (myKey) {
				if (myKey.isAcceptable()) {
					SocketChannel clientSocket = serverSocket.accept();
					clientSocket.configureBlocking(false);
					clientSocket.register(selector, SelectionKey.OP_READ);
					// System.out.println("Server accepted connection");

				} else if (myKey.isReadable()) {
					SocketChannel clientSocket = (SocketChannel) myKey.channel();
					ByteBuffer bb = ByteBuffer.allocate(256);
					clientSocket.read(bb);
					String result = new String(bb.array()).trim();
					// System.out.println("Server received: " + result);

					response = handleRequest(result);

					// myKey.attach(handleRequest(result));
					bb.flip();
					bb.clear();

					clientSocket.register(selector, SelectionKey.OP_WRITE);
				} else if (myKey.isWritable()) {
					SocketChannel clientSocket = (SocketChannel) myKey.channel();
					ByteBuffer bb = ByteBuffer.allocate(1024);

					bb = ByteBuffer.wrap(response.getBytes());

					// System.out.println("Server sending... ");
					clientSocket.write(bb);

					bb.flip();
					bb.clear();
					clientSocket.close();

				}

			}
			it.remove();
		
		}
		
	}
	
	public void closeServerSocket() throws IOException {
		serverSocket.close();
	}

	public String handleRequest(String title) {
		if (doesFileAlreadyExists(title + ".txt")) {
			addToLocalMDb(readFromFile(title));
			return readFromFile(title);
		}

		try {
			JSONObject json = getJSON(title);
			writeToFile(title, json);
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

}
