package example;

/**
 * Segnala dimensioni diverse di due esempi chiamati nei metodi Example.distance() ed Example.swap()
 * @author Damato Luigi Lele
 */
public class ExampleSizeException extends  Exception {
    public ExampleSizeException (){super("Esempio da predire non ha la stessa dimensione del training set");}
    public ExampleSizeException (String msg){super(msg);}
}
