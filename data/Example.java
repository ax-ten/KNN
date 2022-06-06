package data;

import java.util.*;
import utility.ExampleSizeException;

public class Example {
    private List<Object> example;

    public Example(int size) {
        this.example = new ArrayList<>(size);
    }

    public int getSize(){
        return example.size();
    }

    public void set(Object o, int index) throws IndexOutOfBoundsException {
        this.example.set(index, o);
    }

    public Object get(int index) throws IndexOutOfBoundsException {
        return this.example.get(index);
    }

    public void swap(Example e) throws ExampleSizeException {
        if (e.example.size() != this.example.size()) {
            throw new ExampleSizeException();
        } else {
            Example temp = new Example(this.example.size());

            for(int i = 0; i < this.example.size(); ++i) {
                temp.set(this.get(i), i);
                this.set(e.get(i), i);
                e.set(temp.get(i), i);
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
                if (!this.get(i).equals(e.get(i))) {
                    ++d;
                }
            }
            return d;
        }
    }
}
