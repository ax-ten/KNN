package mining;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import data.*;
import example.Example;
import example.ExampleSizeException;
import utility.Keyboard;



public class KNN implements Serializable{
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

    public void salva(String nomeFile) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nomeFile));
        out.writeObject(this);
        out.writeObject(data);
        out.close();
    }

    public static KNN carica(String nomeFile) throws IOException,ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomeFile));
        KNN knn=(KNN)in.readObject();
        knn.data=(Data)in.readObject();
        in.close();
        return knn;
    }

    public String toString(){
        return data.toString();
    }
}
