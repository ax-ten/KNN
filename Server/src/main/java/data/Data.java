package data;
import database.*;
import example.*;
import utility.Keyboard;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Modella il training set
 * @author Damato Luigi Lele
 */
public class Data implements Serializable{
    private List<Example> data;
    private List<Example> dataScaled;
    private List<Double> target;
    private int numberOfExamples;
    private List<Attribute> explanatorySet;

    /**
     * crea un trainingset modellato sul file passato come parametro
     * @param fileName path del file .dat da caricare (e.g. src/main/Testfile/provac.dat)
     * @throws TrainingDataException se il file non è valido: se è vuoto, c'è un errore nello schema
     *                se il tipo di attributo non è specificato, se il numero di esempi è diverso da quanto
     *                indicato.
     */
    public Data(String fileName) throws TrainingDataException {
        File inFile = new File(fileName);
        String line;
        String[] s;

        try (Scanner sc = new Scanner(inFile)){
            line = sc.nextLine();
            if (!line.contains("@schema")) {
                sc.close();
                throw new TrainingDataException("Errore nello schema");
            }

            explanatorySet = new ArrayList<>();
            short iAttribute = 0;

            for(line = sc.nextLine(); !line.contains("@data"); line = sc.nextLine()) {
                s = line.split(" ");
                if (s[0].equals("@desc")){
                    if(s[2].equals("discrete")) {
                        explanatorySet.add(new DiscreteAttribute(s[1], iAttribute));
                    } else if (s[2].equals("continuous")) {
                        explanatorySet.add(new ContinuousAttribute(s[1], iAttribute));
                    } else {
                        sc.close();
                        throw new TrainingDataException("Attributo di tipo non specificato");
                    }
                }
                ++iAttribute;
            }

            this.numberOfExamples = Integer.parseInt(line.split(" ")[1]);
            this.data = new ArrayList<>(this.numberOfExamples);
            this.target = new ArrayList<>(this.numberOfExamples);

            if (numberOfExamples == 0){
                throw new TrainingDataException("Training set vuoto");
            }

            for (short iRow = 1; sc.hasNextLine(); ++iRow) {
                Example e = new Example(explanatorySet.size());
                line = sc.nextLine();
                s = line.split(",");

                for (short jColumn = 0; jColumn < s.length - 1; ++jColumn) {
                    if(explanatorySet.get(jColumn) instanceof DiscreteAttribute){
                    e.set(jColumn, s[jColumn]);
                    } else {
                        Double value = Double.parseDouble(s[jColumn]);
                        e.set(jColumn, value);
                        ((ContinuousAttribute) explanatorySet.get(jColumn)).setMin(value);
                        ((ContinuousAttribute) explanatorySet.get(jColumn)).setMax(value);
                    }
                }

                try { this.data.add(e);
                } catch (ArrayIndexOutOfBoundsException exc){
                    throw new TrainingDataException("Numero di esempi maggiore da quanto indicato");
                }
                try { this.target.add(Double.parseDouble(s[s.length-1]));
                } catch (Exception exc) {
                    throw new TrainingDataException("Training set privo di variabile target numerica in riga "+ iRow + 1);
                }
                if(iRow ==0 && !sc.hasNextLine()){
                    throw new TrainingDataException("Training set vuoto");
                }
                if(!sc.hasNextLine() && iRow<numberOfExamples){
                    throw new TrainingDataException ("Numero di esempi minore da quanto indicato");
                }
            }
            sc.close();
            scaleData();
        } catch (FileNotFoundException exc){
            throw new TrainingDataException("File di training inesistente");
        }
    }

    /**
     * crea un trainingset modellato su database e tabella passati come parametro
     * @param db istanza del gestore di database
     * @param tableName nome della tabella sul quale è salvata la lista di Example
     * @throws TrainingDataException se la tabella non è traslabile in trainingSet
     * @throws InsufficientColumnNumberException se il database è vuoto
     * @throws SQLException qualsiasi errore di compilazione sql
     * @throws NoValueException se il database è vuoto
     */
    public Data(DbAccess db, String tableName)
            throws TrainingDataException, InsufficientColumnNumberException, SQLException, NoValueException {
        TableSchema ts = new TableSchema(tableName, db);
        TableData td = new TableData(db, ts);
        data = td.getExamples();
        target = td.getTargetValues();
        this.numberOfExamples = data.size();
        explanatorySet = new ArrayList<>();
        int i = 0;
        for (Column c:ts){
            if (c.isNumber()){
                explanatorySet.add(new ContinuousAttribute(c.getColumnName(), (short) i));
                ((ContinuousAttribute) explanatorySet.get(i)).setMin(
                    (Double) td.getAggregateColumnValue(c,QUERY_TYPE.MIN));
                ((ContinuousAttribute) explanatorySet.get(i)).setMax(
                    (Double) td.getAggregateColumnValue(c,QUERY_TYPE.MAX));
            }else explanatorySet.add(new DiscreteAttribute(c.getColumnName(), (short) i));
            i++;
        }
        scaleData();
    }

    /**
     * Crea un Esempio della stessa dimensione del primo esempio nel dataset
     * in base all'ordine e al tipo degli Attribute dell'Example viene chiesto all'utente tramite Keyboard
     * di inserire un valore discreto o continuo
     * @return l'esempio creato tramite Keyboard
     */
    public Example readExample() {
        Example e = new Example(getExampleSize());
        int i=0;
        double x;
        for(Attribute a:getExplanatorySet())    {
            if(a instanceof DiscreteAttribute) {
                System.out.print("Inserisci valore discreto X["+i+"]: ");
                e.set(i, Keyboard.readString());
            } else {
                do {
                    System.out.print("Inserisci valore continuo X["+i+"]: ");
                    x=Keyboard.readDouble();
                } while(Double.valueOf(x).equals(Double.NaN));
                e.set(i,x);
            }
            i++;
        }
        return e;
    }

    /**
     * crea un Esempio della stessa dimensione del primo esempio nel dataset
     * in base all'ordine al tipo degli Attribute dell'Example viene chiesto all'utente tramite input stream
     * di inserire valori discreti o continui.
     * @param out terminale sul quale vengono mandati i prompt
     * @param in input del terminale di interfaccia
     * @return l'esempio creato
     * @throws IOException se ci sono errori di Input/Output
     * @throws ClassNotFoundException  se è inserito un valore discreto non valido
     * @throws ClassCastException se è inserito un valore continuo non valido
     */
    public Example readExample(ObjectOutputStream out, ObjectInputStream in)
            throws IOException, ClassNotFoundException, ClassCastException {
        Example e = new Example(getExampleSize());
        int i=0;
        double x;
        for(Attribute a:getExplanatorySet())    {
            if(a instanceof DiscreteAttribute) {
                out.writeObject("@READSTRING");
                out.writeObject("Inserisci valore discreto X["+i+"]: ");
                e.set(i,in.readObject());
            } else {
                out.writeObject("@READDOUBLE");
                out.writeObject("Inserisci valore continuo X["+i+"]: ");
                x=(Double)in.readObject();
                e.set(i,x);
            }
            i++;
        }
        out.writeObject("@ENDEXAMPLE");
        return e;
    }

    /**
     * istanza dataScaled di questa classe, creando una copia dei valori, che se continui vengono scalati in
     * valori che vanno da 0 a 1, in base al valore originale
     */
    private void scaleData(){
        this.dataScaled = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++){
            this.dataScaled.add(scaledExample(data.get(i)));
        }
    }

    /**
     * ordina e ripartisce gli elementi di dataScaled, target e key in base ai parametri inf e sup
     * in accordo ai valori contenuti in key
     *
     * @param key lista di ordinamento
     * @param inf indice inferiore
     * @param sup indice superiore
     * @return partizione di key
     */
    private int partition(List<Double> key, int inf, int sup)  {
        int i = inf;
        int j = sup;
        int med = (inf + sup) / 2;
        Double x = key.get(med);
        Collections.swap(dataScaled, inf, med);
        Collections.swap(target, inf, med);
        Collections.swap(key, inf, med);

        while(true) {
            while(i > sup || !(key.get(i) <= x)) {
                while(key.get(j) > x) {
                    --j;
                }

                if (i >= j) {
                    Collections.swap(dataScaled, inf, j);
                    Collections.swap(target, inf, j);
                    Collections.swap(key, inf, j);
                    return j;
                }

                Collections.swap(dataScaled, i, j);
                Collections.swap(target, i, j);
                Collections.swap(key, i, j);
            }

            ++i;
        }
    }

    /**
     * ordina data, target e key in accordo ai valori contenuti in key.
     * Se sup<inf non ha alcun effetto.
     * @param key lista di valori che stabiliscono l'ordine di dataScaled e target
     * @param inf indice di posizione inferiore
     * @param sup indice di posizione superiore
     */
    private void quicksort(List<Double> key, int inf, int sup)  {
        if (sup >= inf) {
            int pos = this.partition(key, inf, sup);
            if (pos - inf < sup - pos + 1) {
                this.quicksort(key, inf, pos - 1);
                this.quicksort(key, pos + 1, sup);
            } else {
                this.quicksort(key, pos + 1, sup);
                this.quicksort(key, inf, pos - 1);
            }
        }
    }

    /**
     * @return dimensione del primo Example in data
     */
    public int getExampleSize(){
        return data.get(0).getSize();
    }

    /**
     * @return la dimensione dell'explanatorySet
     */
    public int getNumberOfExplanatoryAttributes() {
        return this.explanatorySet.size();
    }

    /**
     * @return explanatorySet
     */
    public List<Attribute> getExplanatorySet(){
        return explanatorySet;
    }

    /** Restituisce una stringa ad alto linguaggio, (e.g.: [D],[D],[C],[K]") sulla quale l'utente si basa
     * per chiedere le predizioni
     * @return stringa rappresentativa dell'explanatorySet
     */
    public String explanatorySetStringBuilder(){
        String out ="";
        int eSize = getNumberOfExplanatoryAttributes();
        for (int i=0; i < eSize; i++){
            if(explanatorySet.get(i) instanceof DiscreteAttribute){
                out= out + "[D],";
            } else if (explanatorySet.get(i) instanceof ContinuousAttribute){
                out= out + "[C],";
            }
        }
        return out+"[K]";
    }

    /**
     * Avvalora key con le distanze calcolate tra ciascuna istanza di Example memorizzata in data ed e
     * @param e example di riferimento
     * @param k distanza massima tra e ed il vettore target
     * @return la media dei valori precedenti al k-esimo dopo aver ordinato target
     * @throws ExampleSizeException se dataScaled ed e sono di dimensioni diverse
     */
    public double avgClosest(Example e, int k) throws ExampleSizeException {
        double d;
        List<Double> key = new ArrayList<>();
        Example scaledExample = scaledExample(e);
        int i;

        for(i = 0; i < this.dataScaled.size(); ++i) {
            d=scaledExample.distance(this.dataScaled.get(i));
            key.add(d);
        }

        quicksort(key, 0, this.dataScaled.size() - 1);

        // il body di for è vuoto perché la sua unica funzione è di aumentare i finché non rispetta la condizione
        for(i = 0; i < key.size() && key.get(i) < (double)k; ++i) {}
        
        return this.avgTillPoint(this.target, i - 1);
    }

    /**
     * @param l lista dal quale ottenere la media
     * @param point indice (incluso) dell'elemento ultimo da cui ottenere la media aritmentica
     * @return la media aritmetica di solo i primi elementi di una lista
     */
    private double avgTillPoint(List<Double> l, int point) {
        double sum = 0.0D;
        for(int i = 0; i <= point; ++i) {
            sum += l.get(i);
        }
        return sum / (double)(point + 1);
    }

    /**
     * Restituisce nuova istanza di Example con valori discreti inalterati e valori continui
     *   scalati tra 0 e 1 in base alla funzione scale
     * @param e Example da copiare e scalare
     * @return Example scalato
     */
    Example scaledExample (Example e) {
        int eSize = e.getSize();
        Example example = new Example(eSize);
        for (int i=0; i < eSize; i++){
            if(explanatorySet.get(i) instanceof DiscreteAttribute){
                example.set(i, e.get(i));
            } else if (explanatorySet.get(i) instanceof ContinuousAttribute){
                example.set(i,((ContinuousAttribute)explanatorySet.get(i)).scale((Double)e.get(i)));
            }
        }
        return example;
    }

    /**
     * builder di Stringa ad alto linguaggio che descrive ogni riga del dataset (e.g.: [1]   3.0,A,2)
     * @return stringa di dataset formattato e leggibile
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();
        String space = "  ";
        for (int i=0; i<numberOfExamples; i++){
            if (i>9){
                space = " ";
            } else if (i >99){
                space = "";
            }
            sb.append(String.format(
                    Locale.ENGLISH,
                    "[%d]%s    %s%.1f\n", i, space, data.get(i).toString(), target.get(i)));
        }
        return sb.toString();
    }

    /**
     * Builder di Stringa ad alto linguaggio che descrive ogni riga del dataset (e.g.: [1]   3.0,A,2).
     * Separa ogni @max caratteri, per permettere di inviare messaggi con capacità limitata
     * @return lista di stringhe del dataset formattato e leggibile
     */
    public LinkedList<String> toTgMessage(){
        final int max = 1000;
        String backt = "```",
                appendable,
                space = "  ";
        StringBuilder sb = new StringBuilder(backt);
        LinkedList<String> messages = new LinkedList<>();
        for(int i=0; i<numberOfExamples; i++){
            if (i > 9)  space = " ";
            if (i >99)  space = "";
            appendable = String.format(
                    Locale.ENGLISH,
                    " [%d]%s    %s%.1f\n", i, space, data.get(i).toString(), target.get(i));
            if (sb.length() + appendable.length() < max)
                sb.append(appendable);
            else {
                messages.add(sb.toString() + backt);
                sb = new StringBuilder(backt);
                sb.append(appendable);
            }
        }
        sb.append(backt);
        messages.add(sb.toString());
        return messages;
    }

}
