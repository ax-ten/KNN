package telegrambot;

/**
 * Segnala una chiave di autenticazione non valida
 */
public class InvalidBotException extends  Exception{
    //da sollevare quando la chiave del bot non è valida

    public InvalidBotException (){super("Authkey non valida");}
    public InvalidBotException (String msg){super(msg);}
}
