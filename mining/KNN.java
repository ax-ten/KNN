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
        Example e = readExample();
        int k=0;
        do {
            System.out.print("Inserisci valore k>=1:");
            k=Keyboard.readInt();
        }while (k<1);
        return this.data.avgClosest(e, k);
    }

    private Example readExample() {
        Example e =new Example(this.data.getNumberOfExplanatoryAttributes());
        int i=0;
        for(Attribute a:this.data.getExplanatorySet())    {
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
}
