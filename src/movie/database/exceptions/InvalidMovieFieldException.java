package movie.database.exceptions;

public class InvalidMovieFieldException extends CommandException{

	private static final long serialVersionUID = 1L;

	public InvalidMovieFieldException() {
		super("There is an invalid movie field");
	}
	
	public InvalidMovieFieldException(String message) {
		super(message);
	}
	
	public InvalidMovieFieldException(String message, Throwable t) {
		super(message, t);
	}
	
	public InvalidMovieFieldException(Throwable t) {
		super(t);
	}
}
