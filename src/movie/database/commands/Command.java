package movie.database.commands;

import movie.database.exceptions.LocalMDBException;

@FunctionalInterface
public interface Command {
	public String execute(String command) throws LocalMDBException;
}
