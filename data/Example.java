package data;

import utility.ExampleSizeException;

public class Example {
    Object[] example;

    public Example(int size) {
        this.example = new Object[size];
    }

    public int getSize(){
        return example.length;
    }

    public void set(Object o, int index) throws IndexOutOfBoundsException {
        this.example[index] = o;
    }

    public Object get(int index) throws IndexOutOfBoundsException {
        return this.example[index];
    }

    public void swap(Example e) throws ExampleSizeException {
        if (e.example.length != this.example.length) {
            throw new ExampleSizeException();
        } else {
            Example temp = new Example(this.example.length);

            for(int i = 0; i < this.example.length; ++i) {
                temp.set(this.get(i), i);
                this.set(e.get(i), i);
                e.set(temp.get(i), i);
            }

        }
    }

    public String toString(){
        StringBuilder output = new StringBuilder();
        for (int i=0;i<this.getSize();i++){
            output.append(String.format("%s,", this.example[i]));
        }
        return output.toString();
    }

    public double distance(Example e) throws ExampleSizeException {
        double d = 0.0D;
        if (e.example.length != this.example.length) {
            throw new ExampleSizeException();
        } else {
            for(int i = 0; i < e.example.length; ++i) {
                if (!this.get(i).equals(e.get(i))) {
                    ++d;
                }
            }
            return d;
        }
    }
}
