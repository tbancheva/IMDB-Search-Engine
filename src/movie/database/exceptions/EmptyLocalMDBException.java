package movie.database.exceptions;

public class EmptyLocalMDBException extends LocalMDBException{
	private static final long serialVersionUID = 1L;

	public EmptyLocalMDBException() {
		super("The local movie database is still empty");
	}
	
	public EmptyLocalMDBException(String message) {
		super(message);
	}
	
	public EmptyLocalMDBException(String message, Throwable t) {
		super(message, t);
	}
	
	public EmptyLocalMDBException(Throwable t) {
		super(t);
	}
}
