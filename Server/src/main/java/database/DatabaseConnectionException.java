package database;

/**
 * Segnala errore di connessione al database
 */
public class DatabaseConnectionException extends Exception {
	public DatabaseConnectionException(String msg){
		super(msg);
	}
}
