import static org.junit.Assert.*;

import java.io.IOException;


import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;


import bg.uni.sofia.fmi.mjt.imdb.IMDbServer;
import bg.uni.sofia.fmi.mjt.imdb.LocalMDb;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.EmptyLocalMDbException;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.InvalidCommandException;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.MissingFieldException;
import bg.uni.sofia.fmi.mjt.imdb.exceptions.MissingMovieTitleException;


public class InternetMovieDataBaseTest {
	
	private static LocalMDb localMDb = new LocalMDb();
	private static IMDbServer imDbServer;

	@BeforeClass
	public static void setUp() throws IOException {
		imDbServer = new IMDbServer(localMDb);
		
		imDbServer.addToLocalMDb(imDbServer.readFromFile("Titanic"));
		imDbServer.addToLocalMDb(imDbServer.readFromFile("Troy"));
		imDbServer.addToLocalMDb(imDbServer.readFromFile("Inception"));
		imDbServer.addToLocalMDb(imDbServer.readFromFile("TheGreatGatsby"));
		imDbServer.addToLocalMDb(imDbServer.readFromFile("Interstellar"));
	}
		
	@Test
	public void getFieldTest() throws MissingFieldException, InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, IOException, ParseException{
		assertTrue(imDbServer.getResponse("get-movie Titanic fields= Year").equals(" Year: 1997 "));
	}
	
			
	@Test
	public void getResponse() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, MissingFieldException, IOException, ParseException {
		assertTrue(imDbServer.getResponse("get-movie 300").contains("300"));
	}
	
		
	@Test
	public void printTitlesTest() {
		assertTrue(localMDb.printTitles().equals("Titanic Troy Inception The Great Gatsby Interstellar "));
	}
	
	@Test
	public void sortAscendingTest() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, MissingFieldException, IOException, ParseException {
		assertTrue(new Double(localMDb.getField("imdbRating", "Troy")).doubleValue()
				< new Double(localMDb.getField("imdbRating", "Inception")).doubleValue());
		assertTrue(imDbServer.getResponse("get-movies order= asc").equals("Troy The Great Gatsby Titanic Interstellar Inception "));
	}
	
	@Test
	public void sortDecendingTest() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, MissingFieldException, IOException, ParseException {
		assertTrue(new Double(localMDb.getField("imdbRating", "Interstellar")).doubleValue()
				> new Double(localMDb.getField("imdbRating", "Titanic")).doubleValue());
		assertTrue(imDbServer.getResponse("get-movies order= desc").equals("Inception Interstellar Titanic 300 The Great Gatsby Troy "));
	}
	
	@Test
	public void filterByActorsTest() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, MissingFieldException, IOException, ParseException {
		assertTrue(imDbServer.getResponse("get-movies actors= Leonardo DiCaprio").equals("Titanic Inception "));
	}
	
	@Test
	public void filterByGenresTest() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, MissingFieldException, IOException, ParseException {
		assertTrue(imDbServer.getResponse("get-movies genres= Drama, Romance").equals("Titanic Troy The Great Gatsby "));
	}
	
	@Test
	public void filterByActorsAndGenresTest() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, MissingFieldException, IOException, ParseException{
		assertTrue(imDbServer.getResponse("get-movies genres= Action actors= Leonardo DiCaprio").equals("Inception "));
	}
	
	@Test
	public void getTvSeriesTest() throws InvalidCommandException, EmptyLocalMDbException, MissingMovieTitleException, MissingFieldException, IOException, ParseException {
	  String response = imDbServer.getResponse("get-tv-series Supernatural season= 6");
	  assertTrue(response.contains("\"Episode\":\"1\""));
	  assertTrue(response.contains("\"Episode\":\"10\""));
	}
	

		
	
}
