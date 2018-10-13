package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import movie.database.LocalMDB;
import movie.database.commands.Command;
import movie.database.commands.FilterMovies;
import movie.database.commands.GetMovie;
import movie.database.commands.GetMovieField;
import movie.database.commands.GetMoviePoster;
import movie.database.commands.GetMovies;
import movie.database.commands.GetTvSeries;
import movie.database.commands.SortMovies;
import movie.database.exceptions.LocalMDBException;
import movie.database.exceptions.WrongCommandNameException;

public class ClientHandler implements Runnable {
	private static final String ENCODING = "UTF-8";

	private Map<String, Command> commands = new HashMap<>();
	private LocalMDB localMDb;
	
	private Socket clientSocket;

	public ClientHandler(Socket clientSocket, LocalMDB localMDb) throws IOException {
		this.clientSocket = clientSocket;
		this.localMDb = localMDb;
		fill();
	}

	@Override
	public void run() {
		try {
			communicateWithClient();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void communicateWithClient() throws IOException {
		String request, response;

		try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), ENCODING));
				PrintWriter pw = new PrintWriter(clientSocket.getOutputStream())) {

			while (true) {
				pw.println("Enter movie database command or type \"exit\" to close");
				pw.flush();
				request = in.readLine();

				if (request.equals("exit")) {
					break;
				}

				try {
					response = getCommandResponse(request);
				} catch (LocalMDBException e) {
					response = e.getMessage();
				}

				pw.println(response);
				pw.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getCommandResponse(String command) throws LocalMDBException {
		String commandName = command.split(" ")[0];

		if (!commands.containsKey(commandName)) {
			throw new WrongCommandNameException();
		}

		return commands.get(commandName).execute(command);
	}

	private void fill() {
		commands.put("get-movie", new GetMovie(localMDb));
		commands.put("get-movie-fields", new GetMovieField(localMDb));
		commands.put("get-movie-poster", new GetMoviePoster(localMDb));
		commands.put("get-tv-series", new GetTvSeries(localMDb));
		commands.put("get-movies", new GetMovies(localMDb));
		commands.put("filter-movies", new FilterMovies(localMDb));
		commands.put("sort-movies", new SortMovies(localMDb));
	}
}
