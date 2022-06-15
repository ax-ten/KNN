package Server.data;
import Server.database.*;
import Server.example.Example;
import Server.example.ExampleSizeException;
import Server.utility.Keyboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

public class Data implements Serializable{
    private List<Example> data;
    private List<Example> dataScaled;
    private List<Double> target;
    int numberOfExamples;
    private List<Attribute> explanatorySet;
    ContinuousAttribute classAttribute;

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
                } else if (s[0].equals("@target")) {
                    this.classAttribute = new ContinuousAttribute(s[1], iAttribute);
                }
                ++iAttribute;
            }

            this.numberOfExamples = Integer.valueOf(line.split(" ")[1]);
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

    public Data(DbAccess db, String table) throws TrainingDataException, InsufficientColumnNumberException, SQLException, NoValueException {
        TableSchema ts = new TableSchema(table, db);
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

    private void scaleData(){
        this.dataScaled = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++){
            this.dataScaled.add(scaledExample(data.get(i)));
        }
    }

    private int partition(List<Double> key, int inf, int sup) throws ExampleSizeException {
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

    private void quicksort(List<Double> key, int inf, int sup) throws ExampleSizeException {
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

    public int getExampleSize(){
        return data.get(0).getSize();
    }
    
    public int getNumberOfExplanatoryAttributes() {
        return this.explanatorySet.size();
    }

    public List<Attribute> getExplanatorySet(){
        return explanatorySet;
    }

    public double avgClosest(Example e, int k) throws ExampleSizeException {
        List<Double> key = new ArrayList<>();
        Example scaledExample = scaledExample(e);
        int i;

        for(i = 0; i < this.dataScaled.size(); ++i) {
            key.add(scaledExample.distance(this.dataScaled.get(i)));
        }

        quicksort(key, 0, this.dataScaled.size() - 1);

        for(i = 0; i < key.size() && key.get(i) < (double)k; ++i) {}
        
        return this.avgTillPoint(this.target, i - 1);
    }

    private double avgTillPoint(List<Double> array, int point) {
        double sum = 0.0D;
        for(int i = 0; i <= point; ++i) {
            sum += array.get(i);
        }
        return sum / (double)(point + 1);
    }

    //Restituisce nuova istanza di Example con valori discreti inalterati e valori continui scalati tra 0 e 1
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

    public Example readExample() {
        Example e = new Example(numberOfExamples);
        int i=0;
        double x;
        for(Attribute a:explanatorySet)    {
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
    
    public Example readExample(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException, ClassCastException {
        Example e = new Example(numberOfExamples);
        int i=0;
        double x;
        for(Attribute a:explanatorySet)    {
            if(a instanceof DiscreteAttribute) {
                out.writeObject("@READSTRING");
                out.writeObject("Inserisci valore discreto X["+i+"]: ");
                e.set(i, (String) in.readObject());
            } else {
                do {
                    out.writeObject("@READDOUBLE");
                    out.writeObject("Inserisci valore continuo X["+i+"]: ");
                    x=(Double)in.readObject();
                } while(Double.valueOf(x).equals(Double.NaN));
                e.set(i,x);
            }
            i++;
        }
        out.writeObject("@ENDEXAMPLE");
        return e;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        String space = " ";
        for (int i=0; i<numberOfExamples; i++){
            if (i>9){
                space = "";
            }
            sb.append(String.format(
                    Locale.ENGLISH,
                    "[%d]%s    %s%.1f\n", i, space, data.get(i).toString(), target.get(i)));
        }
        return sb.toString();
    }
}
