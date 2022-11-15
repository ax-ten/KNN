package example;

import data.Attribute;
import data.ContinuousAttribute;

import java.io.Serializable;
import java.util.*;

/**
 * Modella un Esempio
 * @author Giannotte Giampiero
 */
public class Example implements Serializable{
    private List<Object> example;
    private int size;

    /**
     * @param size dimensione dell'Example
     */
    public Example(int size) {
        this.example = new ArrayList<>(size);
        this.size = size;
    }



    /**
     * @param attributes lista di stringhe, ognuna corrispondente ad un attributo
     * @throws NumberFormatException se il parsing del double non è possibile
     */
    public Example (String[] attributes) throws  NumberFormatException {
        this(attributes.length);
        int i=0;
        for (Object a:this.example){
            if(a instanceof ContinuousAttribute) {
                this.set(i, Double.parseDouble(attributes[i]));
            } else {
                this.set(i, attributes[i]);
            }
            i++;
        }
    }

    /**
     * @return dimensione dell'Example
     */
    public int getSize(){
        return example.size();
    }

    /**
     * @param index posizione nel quale inserire o
     * @param o oggetto da salvare in posizione index
     * @throws IndexOutOfBoundsException se index non rispetta la dimensione di Example
     */
    public void set(int index, Object o) throws IndexOutOfBoundsException {
        if (index > size) throw new IndexOutOfBoundsException();
        else {
            try {
                this.example.set(index, o);
            } catch (IndexOutOfBoundsException e){
                this.example.add(o);
            }
        }
    }

    /**
     * @param index posizione nella lista dell'oggetto chiamante
     * @return oggetto salvato in posizione index nella lista dell'oggetto chiamante
     * @throws IndexOutOfBoundsException se index non rispetta la dimensione di Example
     */
    public Object get(int index) throws IndexOutOfBoundsException {
        return this.example.get(index);
    }

    /**
     * @param o aggiungi un elemento all'esempio
     */
    public void add(Object o) {
        this.example.add(o);
        this.size++;
    }

    /** Sostituisci gli elementi in @e con quelli all'interno dell'oggetto chiamante
     * @param e Example coi valori da swappare
     * @throws ExampleSizeException se l'oggetto chiamante ed e hanno dimensioni diverse
     */
    public void swap(Example e) throws ExampleSizeException {
        if (e.example.size() != this.example.size()) {
            throw new ExampleSizeException();
        } else {
            Example temp = new Example(this.example.size());

            for(int i = 0; i < this.example.size(); ++i) {
                temp.set(i,this.get(i));
                this.set(i,e.get(i));
                e.set(i,temp.get(i));
            }
        }
    }

    /** Builder della stringa di linguaggio ad alto livello che indica ad ogni posizione nella lista l'elemento corrispondente
     * @return contenuto di example formattato e leggibile
     */
    public String toString(){
        StringBuilder output = new StringBuilder();
        for (int i=0;i<this.getSize();i++){
            output.append(String.format("%s, ", this.example.get(i)));
        }
        return output.toString();
    }

    /** Calcola la distanza tra l'oggetto chiamante ed e.
     * Se gli attributi sono Discreti e diversi, la distanza aumenta di 1.
     * Se gli attributi sono Continui, la distanza aumenta di un numero pari alla differenza tra gli attributi.
     * @param e Example sul quale calcolare la distanza
     * @return distanza tra oggetto chiamante ed e
     * @throws ExampleSizeException se oggetto chiamante ed e hanno dimensioni diverse
     * @throws IllegalArgumentException se due attributi alla stessa posizione non sono dello stesso tipo
     */
    public double distance(Example e) throws ExampleSizeException, IllegalArgumentException {
        double d = 0.0D;
        if (e.example.size() != this.example.size()) {
            throw new ExampleSizeException();
        } else {
            for(int i = 0; i < e.example.size(); ++i) {
                if (this.get(i) instanceof String && e.get(i) instanceof String){
                    if (!this.get(i).equals(e.get(i))) {
                        d = d+1;
                    }
                } else if (this.get(i) instanceof Double && e.get(i) instanceof Double){
                    d=d+Math.abs((Double)this.get(i)-(Double)e.get(i));
                } else
                    throw new IllegalArgumentException("Data type mismatch at row "+i);
            }
            return d;
        }
    }
}
