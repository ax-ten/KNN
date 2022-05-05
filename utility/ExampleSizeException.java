package utility;

public class ExampleSizeException extends  Exception {
    //da sollevare nei metodi swap e
    //distance di Example qualora i due esempi coinvolti dall’operazione non abbiano la stessa dimensione.

    public ExampleSizeException (){super("Esempio da predire non ha la stessa dimensione del training set");}
    public ExampleSizeException (String msg){super(msg);}
}
