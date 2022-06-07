package data;

import java.util.*;
import utility.ExampleSizeException;

public class Example {
    private List<Object> example;
    int size;

    public Example(int size) {
        this.example = new ArrayList<>(size);
        this.size = size;
    }

    public int getSize(){
        return example.size();
    }

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

    public Object get(int index) throws IndexOutOfBoundsException {
        return this.example.get(index);
    }

    public void add(Object o) {
        this.example.add(o);
    }

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

    public String toString(){
        StringBuilder output = new StringBuilder();
        for (int i=0;i<this.getSize();i++){
            output.append(String.format("%s,", this.example.get(i)));
        }
        return output.toString();
    }

    public double distance(Example e) throws ExampleSizeException {
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
                } else throw new IllegalArgumentException("Data type mismatch at row "+i);
            }
            return d;
        }
    }
}
