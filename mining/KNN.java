package mining;
import data.*;
import utility.ExampleSizeException;
import utility.Keyboard;



public class KNN {
    Data data;

    public KNN(Data trainingSet) {
        this.data = trainingSet;
    }


    public Double predict(Example e, int k) throws ExampleSizeException {
        return this.data.avgClosest(e, k);
    }

    public Double predict() {
        int size = data.getExampleSize();
        Example e = new Example(size);

        for (int i=0; i<size; i++){
            System.out.printf("Inserisci valore X[%d]:%n",i);
            e.set(i,Keyboard.readWord());
        }

        System.out.println("Inserisci valore k>=1:");
        int k = Keyboard.readInt();

        Double result = null;

        try {
           result = this.data.avgClosest(e, k);
        } catch (ExampleSizeException ex) {
            ex.printStackTrace();
        }

        return result;
    }

}