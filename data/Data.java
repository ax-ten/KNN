package data;
import database.DbAccess;
import example.Example;
import example.ExampleSizeException;
import utility.Keyboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
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
        Scanner sc;
        try {
            sc = new Scanner(inFile);
        } catch (FileNotFoundException exc){
            throw new TrainingDataException("File di training inesistente");
        }
        String line = sc.nextLine();

        if (!line.contains("@schema")) {
            throw new TrainingDataException("Errore nello schema");
        }
        String[] s = line.split(" ");

        explanatorySet = new ArrayList<>(new Integer(s[1]));
        short iAttribute = 0;

        for(line = sc.nextLine(); !line.contains("@data"); line = sc.nextLine()) {
            s = line.split(" ");
            if (s[0].equals("@desc")){
                if(s[2].equals("discrete")) {
                explanatorySet.add(new DiscreteAttribute(s[1], iAttribute));
                } else if (s[2].equals("continuous")) {
                    explanatorySet.add(new ContinuousAttribute(s[1], iAttribute));
                } else throw new TrainingDataException("Attributo di tipo non specificato");
            } else if (s[0].equals("@target")) {
                this.classAttribute = new ContinuousAttribute(s[1], iAttribute);
            }
            ++iAttribute;
        }

        this.numberOfExamples = new Integer(line.split(" ")[1]);
        if (numberOfExamples == 0){
            throw new TrainingDataException("Training set vuoto");
        }

        this.data = new ArrayList<>(this.numberOfExamples);
        this.target = new ArrayList<>(this.numberOfExamples);

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

            try {
                this.data.add(e);
            } catch (ArrayIndexOutOfBoundsException exc){
                throw new TrainingDataException("Numero di esempi maggiore da quanto indicato");
            }
            try {
                this.target.add(Double.parseDouble(s[s.length-1]));
            } catch (Exception exc) {
                throw new TrainingDataException(
                        String.format("Training set privo di variabile target numerica in riga %d", iRow + 1));
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
    }

    public Data(DbAccess db, String table) {
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

        this.quicksort(key, 0, this.dataScaled.size() - 1);

        System.out.println(key);

        for(i = 0; i < key.size() && key.get(i) < (double)k; ++i) {
        }
        
        return this.avgTillPoint(this.target, i - 1);
    }

    private double avgTillPoint(List<Double> array, int point) {
        double sum = 0.0D;
        for(int i = 0; i <= point; ++i) {
            sum += array.get(i);
            System.out.println("Sum "+sum);
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
        Example e =new Example(numberOfExamples);
        int i=0;
        for(Attribute a:explanatorySet)    {
            if(a instanceof DiscreteAttribute) {
                System.out.print("Inserisci valore discreto X["+i+"]:");
                e.set(i, Keyboard.readString());
            } else {
                double x=0.0;
                //TODO sostituire con while?
                do {
                    System.out.print("Inserisci valore continuo X["+i+"]:");
                    x=Keyboard.readDouble();
                } while(new Double(x).equals(Double.NaN));
                e.set(i,x);
            }
            i++;
        }
        return e;
    }

    public String toString(){
        StringBuilder output = new StringBuilder();
        String spaziopzionale = " ";
        for (int i=0; i<numberOfExamples; i++){
            if (i>9){
                spaziopzionale = "";
            }
            output.append(String.format(
                    Locale.ENGLISH,
                    "[%d]%s    %s%.1f\n", i, spaziopzionale, data.get(i).toString(), target.get(i)));
        }
        return output.toString();
    }
}
