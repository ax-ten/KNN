package data;
import utility.ExampleSizeException;
import utility.TrainingDataException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Data {
    private List<Example> data;
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
                if (s[0].equals("@desc")) {
                    explanatorySet.add(new DiscreteAttribute(s[1], iAttribute));
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
                    System.out.println("exp:"+explanatorySet.size()+"size"+e.getSize());
                    e.set(s[jColumn], jColumn);
                }

                try {
                    this.data.add(e);
                } catch (ArrayIndexOutOfBoundsException exc){
                    throw new TrainingDataException("Numero di esempi maggiore da quanto indicato");
                }
                try {
                    this.target.add(Double.parseDouble(s[s.length-1]));
                    //this.target[iRow] = new Double(s[s.length - 1]);
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
        }


    private int partition(List<Double> key, int inf, int sup) throws ExampleSizeException {
        int i = inf;
        int j = sup;
        int med = (inf + sup) / 2;
        Double x = key.get(med);
        Collections.swap(data, inf, med);
        Collections.swap(target, inf, med);
        Collections.swap(key, inf, med);
        /*this.data[inf].swap(this.data[med]);
        double temp = this.target[inf];
        this.target[inf] = this.target[med];
        this.target[med] = temp;
        temp = key[inf];
        key[inf] = key[med];
        key[med] = temp;*/

        while(true) {
            while(i > sup || !(key.get(i) <= x)) {
                while(key.get(j) > x) {
                    --j;
                }

                if (i >= j) {
                    Collections.swap(data, inf, j);
                    Collections.swap(target, inf, j);
                    Collections.swap(key, inf, j);
                    /*this.data[inf].swap(this.data[j]);
                    temp = this.target[inf];
                    this.target[inf] = this.target[j];
                    this.target[j] = temp;
                    temp = key[inf];
                    key[inf] = key[j];
                    key[j] = temp;*/
                    return j;
                }

                Collections.swap(data, i, j);
                Collections.swap(target, i, j);
                Collections.swap(key, i, j);
                /*this.data[i].swap(this.data[j]);
                temp = this.target[i];
                this.target[i] = this.target[j];
                this.target[j] = temp;
                temp = key[i];
                key[i] = key[j];
                key[j] = temp;*/
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
    
    int getNumberOfExplanatoryAttributes() {
        return this.explanatorySet.size();
    }

    public double avgClosest(Example e, int k) throws ExampleSizeException {
        List<Double> key = new ArrayList<>();

        int i;
        for(i = 0; i < this.data.size(); ++i) {
            key.set(i, e.distance(this.data.get(i)));
        }

        this.quicksort(key, 0, this.data.size() - 1);

        for(i = 0; i < key.size() && key.get(i) < (double)k; ++i) {
        }

        return this.avgTillPoint(this.target, i - 1);
    }

    private double avgTillPoint(List<Double> array, int point) {
        double sum = 0.0D;

        for(int i = 0; i <= point; ++i) {
            sum += array.get(i);
        }

        return sum / (double)(point + 1);
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
