package movie.database.exceptions;


public class MissingCommandArgumentsException extends CommandException{

	private static final long serialVersionUID = 1L;

	public MissingCommandArgumentsException() {
		super("There are not enough arguments for this command");
	}
	
	public MissingCommandArgumentsException(String message) {
		super(message);
	}
	
	public MissingCommandArgumentsException(String message, Throwable t) {
		super(message, t);
	}
	
	public MissingCommandArgumentsException(Throwable t) {
		super(t);
	}
}
