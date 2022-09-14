package main.data;

public class ContinuousAttribute extends Attribute {

    private double min;
    private double max;

    public ContinuousAttribute(String name, short index) {
        super(name, index);
        this.min = 0;
        this.max = 0;
    }
    //Aggiorna min in base al valore v passato come parametro
    void setMin (Double v){
        if (v < min) min = v;
    }
    //Aggiorna max in base al valore v passato come parametro
    void setMax (Double v){
        if (v > max) max = v;
    }

    double scale (Double value){
        double scale = (value-min)/(max-min);
        return scale;
    }
}
