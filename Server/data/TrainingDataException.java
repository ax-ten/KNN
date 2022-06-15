package Server.data;

public class TrainingDataException extends Exception{
    //per gestire il caso di acquisizione errata
    //del Training set (per esempio, file inesistente, schema mancante, training set vuoto training set privo
    //di variabile target numerica).
    public TrainingDataException() {}
    public TrainingDataException(String msg){super(msg);}
}
