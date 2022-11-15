package mining;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import data.*;
import example.*;
import utility.Keyboard;


/**
 * Modella il gestore di predizioni su un trainingset
 * @author Damato Luigi Lele
 */
public class KNN implements Serializable{
    private Data trainingSet;

    /**
     * Crea istanza KNN vuota
     */
    public KNN(){}

    /**
     * @param trainingSet Data - dataset su cui KNN è stato allenato a sviluppare predizioni
     */
    public KNN(Data trainingSet) {
        this.trainingSet = trainingSet;
    }

    /**
     * @return la predizione basata su un Esempio costruito via terminale
     * @throws ExampleSizeException se l'esempio inserito non è valido
     */
    public Double predict() throws ExampleSizeException {
        Example e = trainingSet.readExample();
        int k=0;
        do {
            System.out.print("Inserisci valore k>=1:");
            k=Keyboard.readInt();
        }while (k<1);
        return trainingSet.avgClosest(e, k);
    }

    /**
     * @param in stringa di ingresso sul quale effettuare parsing dei valori sul quale costruire
     *          l'esempio e k, dove k è il numero di #todo.
     *          (e.g: A,2,3. A,2 è l'Example. 3 è la k.)
     * @return la predizione effettuata
     * @throws ExampleSizeException se in non è valida
     */
    public Double predict(String in) throws ExampleSizeException {
        String args[] = in.split(",");
        int k;
        try{
            k = Integer.parseInt(args[args.length - 1]);
        } catch (NumberFormatException e){throw new ExampleSizeException();}
        Example e = new Example(Arrays.copyOf(args, args.length-1));
        return  trainingSet.avgClosest(e,k);
    }

    /**
     * Predice un risultato in base al valore immesso tramite parametro in, calcolato sulla base di trainingSet
     *
     * @param out stream di uscita
     * @param in stream di entrata
     * @return risultato della predizione
     * @throws IOException se uno stream non è valido
     * @throws ClassNotFoundException se il parsing di Example non è valido
     * @throws ClassCastException se il casting di k non è valido
     * @throws ExampleSizeException se l'example immesso non è congruo al trainingSet del KNN
     */
    public Double predict (ObjectOutputStream out, ObjectInputStream in)
            throws IOException, ClassNotFoundException, ClassCastException, ExampleSizeException {
        System.out.println("Reading Example");
        Example e = trainingSet.readExample(out,in);
        int k=0;
        out.writeObject("Inserisci valore k>=1:");
        k=(Integer)(in.readObject());
        return trainingSet.avgClosest(e, k);
    }

    /**
     * Salva il trainingSet del KNN corrente in un file nella directory src/main/Testfile/
     * @param nomeFile nome del path local e del file da salvare. (e.g "src/main/Testfile/provaC.dat")
     * @throws IOException se non è possibile salvare il file
     */
    public void salva(String nomeFile) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nomeFile));
        out.writeObject(trainingSet);
        out.close();
    }

    /**
     * Carica il trainingSet sul KNN corrente, il return è opzionale.
     * @param nomeFile nome del path locale e del file da caricare (e.g "src/main/Testfile/provaC.dmp")
     * @return questo KNN con il trainingSet aggiornato dal file specificato
     * @throws IOException se non è possibile caricare il file
     * @throws ClassNotFoundException se non è possibile fare il casting dell'oggetto salvato
     */
    public KNN carica(String nomeFile) throws IOException,ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomeFile));
        this.trainingSet=(Data)in.readObject();
        in.close();
        return this;
    }

    /**
     * @return restituisce il trainingset
     */
    public Data getData(){
        return  trainingSet;
    }
}
