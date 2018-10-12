import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import org.junit.BeforeClass;
import org.junit.Test;

import movie.database.LocalMDB;

import movie.database.exceptions.LocalMDBException;
import server.ClientHandler;

public class LocalMDBTest {

	private static LocalMDB localMDB = new LocalMDB();
	private static ClientHandler handler;

	@BeforeClass
	public static void setUp() throws LocalMDBException, IOException {
		handler = new ClientHandler(new Socket(),localMDB);

		handler.getCommandResponse("get-movie Titanic");
		handler.getCommandResponse("get-movie Troy");
		handler.getCommandResponse("get-movie Inception");
	}
	
	// Get moviestests
	@Test
	public void printTitlesTest() throws LocalMDBException {
		assertTrue(handler.getCommandResponse("get-movies").equals("Troy Inception Titanic "));
	}

	// Get movie fields tests
	@Test
	public void getMovieFieldsTest() throws LocalMDBException {
		assertTrue(handler.getCommandResponse("get-movie-fields Titanic fields= Year").equals("Year: 1997 "));

		assertTrue(handler.getCommandResponse("get-movie-fields Inception fields= Actors")
				.equals("Actors: Leonardo DiCaprio, Joseph Gordon-Levitt, Ellen Page, Tom Hardy "));

		assertTrue(handler.getCommandResponse("get-movie-fields Troy fields= BoxOffice, Genre")
				.equals("BoxOffice: $132,500,000 Genre: Drama, History, Romance "));
	}

	// Sort movies tests
	@Test
	public void sortAscendingTest() throws LocalMDBException {
		assertTrue(new Double(localMDB.getField("imdbRating", "Troy"))
				.doubleValue() < new Double(localMDB.getField("imdbRating", "Inception")).doubleValue());
		assertTrue(handler.getCommandResponse("sort-movies imdbRating").equals("Troy Titanic Supernatural Inception "));
	}

	@Test
	public void sortDecendingTest() throws LocalMDBException {
		assertTrue(handler.getCommandResponse("sort-movies imdbRating desc").equals("Inception Supernatural Titanic Troy "));
	}

	// Filter movies tests
	@Test
	public void filterByActorsTest() throws LocalMDBException {
		assertTrue(handler.getCommandResponse("filter-movies Actors= Leonardo DiCaprio").equals("Inception Titanic "));
	}

	@Test
	public void filterByGenresTest() throws LocalMDBException {
		assertTrue(handler.getCommandResponse("filter-movies Genre= Drama, Romance").equals("Troy Supernatural Titanic "));
	}

	// Get tv series tests
	@Test
	public void getTvSeriesTest() throws LocalMDBException {
		String response = handler.getCommandResponse("get-tv-series Supernatural season= 5");
		
		assertTrue(response.contains("\"Episode\":\"1\""));
		assertTrue(response.contains("\"Episode\":\"10\""));
	}

	// Get movie poster tests
	@Test
	public void getMoviePosterTest() throws LocalMDBException {
		assertTrue(handler.getCommandResponse("get-movie-poster Troy").equals("Troy.jpg"));
	}

}
