package movie.database.exceptions;

public abstract class LocalMDBException extends Exception{

	private static final long serialVersionUID = 1L;

	public LocalMDBException() {
		super();
	}
	
	public LocalMDBException(String message) {
		super(message);
	}
	
	public LocalMDBException(String message, Throwable t) {
		super(message, t);
	}
	
	public LocalMDBException(Throwable t) {
		super(t);
	}
}
