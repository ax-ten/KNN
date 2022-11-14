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



public class KNN implements Serializable{
    Data data;

    public KNN(){}

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

    public Double predict(String in) throws ExampleSizeException {
        String args[] = in.split(",");
        int k;
        try{
            k = Integer.parseInt(args[args.length - 1]);
        } catch (NumberFormatException e){throw new ExampleSizeException();}
        Example e = data.parseExample(Arrays.copyOf(args, args.length-1));
        return  data.avgClosest(e,k);
    }

    public Double predict (ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException, ClassCastException, ExampleSizeException {
        System.out.println("Read Example");
        Example e = data.readExample(out,in);
        out.writeObject("Inserisci valore k>=1: ");
        int k=(Integer)(in.readObject());
        return data.avgClosest(e, k);
    }

    public void salva(String nomeFile) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nomeFile));
        out.writeObject(data);
        out.close();
    }

    public KNN carica(String nomeFile) throws IOException,ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomeFile));
        this.data=(Data)in.readObject();
        in.close();
        return this;
    }

    public Data getData(){
        return  data;
    }
}
