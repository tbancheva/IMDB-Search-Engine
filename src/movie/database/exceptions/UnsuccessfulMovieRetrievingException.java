package movie.database.exceptions;

public class UnsuccessfulMovieRetrievingException extends LocalMDBException{

	private static final long serialVersionUID = 1L;

	public UnsuccessfulMovieRetrievingException() {
		super("Information about the movie was not retrieved");
	}
	
	public UnsuccessfulMovieRetrievingException(String message) {
		super(message);
	}
	
	public UnsuccessfulMovieRetrievingException(String message, Throwable t) {
		super(message, t);
	}
	
	public UnsuccessfulMovieRetrievingException(Throwable t) {
		super(t);
	}
}
