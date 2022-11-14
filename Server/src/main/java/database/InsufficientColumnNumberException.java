package database;

/**
 * Segnala un numero insufficente di colonne in una tabella
 */
public class InsufficientColumnNumberException extends Exception {
	public InsufficientColumnNumberException(String msg) {super(msg);}
}
