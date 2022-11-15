package database;

/**
 * Segnala la mancanza di un valore in una tabella
 */
public class NoValueException extends Exception {
	public NoValueException(String msg) {
		super(msg);
	}
}
