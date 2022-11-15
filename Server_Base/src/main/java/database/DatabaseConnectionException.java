package database;

/**
 * Segnala errore di connessione al database
 */
public class DatabaseConnectionException extends Exception {
	DatabaseConnectionException(String msg){
		super(msg);
	}
}
