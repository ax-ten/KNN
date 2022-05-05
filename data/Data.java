package data;
import utility.ExampleSizeException;
import utility.TrainingDataException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

public class Data {
    Example[] data;
    Double[] target;
    int numberOfExamples;
    Attribute[] explanatorySet;
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

            Attribute[] explanatorySet = new Attribute[new Integer(s[1])];
            short iAttribute = 0;

            for(line = sc.nextLine(); !line.contains("@data"); line = sc.nextLine()) {
                s = line.split(" ");
                if (s[0].equals("@desc")) {
                    explanatorySet[iAttribute] = new DiscreteAttribute(s[1], iAttribute);
                } else if (s[0].equals("@target")) {
                    this.classAttribute = new ContinuousAttribute(s[1], iAttribute);
                }

                ++iAttribute;
            }

            this.numberOfExamples = new Integer(line.split(" ")[1]);
            if (numberOfExamples == 0){
                throw new TrainingDataException("Training set vuoto");
            }

            this.data = new Example[this.numberOfExamples];
            this.target = new Double[this.numberOfExamples];
            int righecontate = 0;

            for (short iRow = 0; sc.hasNextLine(); ++iRow) {
                righecontate = iRow;
                Example e = new Example(explanatorySet.length);
                line = sc.nextLine();
                s = line.split(",");

                for (short jColumn = 0; jColumn < s.length - 1; ++jColumn) {
                    e.set(s[jColumn], jColumn);
                }

                try {
                    this.data[iRow] = e;
                } catch (ArrayIndexOutOfBoundsException exc){
                    throw new TrainingDataException("Numero di esempi maggiore da quanto indicato");
                }
                try {
                    this.target[iRow] = new Double(s[s.length - 1]);
                } catch (Exception exc) {
                    throw new TrainingDataException(
                            String.format("Training set privo di variabile target numerica in riga %d", iRow + 1));
                }

                if(iRow ==0 && !sc.hasNextLine()){
                    throw new TrainingDataException("Training set vuoto");
                }
            }

            if (righecontate < numberOfExamples){
                throw new TrainingDataException ("Numero di esempi minore da quanto indicato");
            }

            sc.close();
        }


    private int partition(double[] key, int inf, int sup) throws ExampleSizeException {
        int i = inf;
        int j = sup;
        int med = (inf + sup) / 2;
        Double x = key[med];
        this.data[inf].swap(this.data[med]);
        double temp = this.target[inf];
        this.target[inf] = this.target[med];
        this.target[med] = temp;
        temp = key[inf];
        key[inf] = key[med];
        key[med] = temp;

        while(true) {
            while(i > sup || !(key[i] <= x)) {
                while(key[j] > x) {
                    --j;
                }

                if (i >= j) {
                    this.data[inf].swap(this.data[j]);
                    temp = this.target[inf];
                    this.target[inf] = this.target[j];
                    this.target[j] = temp;
                    temp = key[inf];
                    key[inf] = key[j];
                    key[j] = temp;
                    return j;
                }

                this.data[i].swap(this.data[j]);
                temp = this.target[i];
                this.target[i] = this.target[j];
                this.target[j] = temp;
                temp = key[i];
                key[i] = key[j];
                key[j] = temp;
            }

            ++i;
        }
    }

    private void quicksort(double[] key, int inf, int sup) throws ExampleSizeException {
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
        return data[0].getSize();
    }
    
    int getNumberOfExplanatoryAttributes() {
        return this.explanatorySet.length;
    }

    public double avgClosest(Example e, int k) throws ExampleSizeException {
        double[] key = new double[this.data.length];

        int i;
        for(i = 0; i < this.data.length; ++i) {
            key[i] = e.distance(this.data[i]);
        }

        this.quicksort(key, 0, this.data.length - 1);

        for(i = 0; i < key.length && key[i] < (double)k; ++i) {
        }

        return this.avgTillPoint(this.target, i - 1);
    }

    private double avgTillPoint(Double[] array, int point) {
        double sum = 0.0D;

        for(int i = 0; i <= point; ++i) {
            sum += array[i];
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
                    "[%d]%s    %s%.1f\n", i, spaziopzionale, data[i].toString(), target[i]));
        }
        return output.toString();
    }
}
