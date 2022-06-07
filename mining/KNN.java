package mining;
import data.*;
import utility.ExampleSizeException;
import utility.Keyboard;



public class KNN {
    Data data;

    public KNN(Data trainingSet) {
        this.data = trainingSet;
    }

    public Double predict() throws ExampleSizeException {
        Example e = this.data.readExample();
        int k=0;
        do {
            System.out.print("Inserisci valore k>=1:");
            k=Keyboard.readInt();
        }while (k<1);
        return this.data.avgClosest(e, k);
    }
}
