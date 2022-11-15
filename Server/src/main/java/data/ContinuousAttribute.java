package data;

/**
 * Modella un attributo continuo
 */
public class ContinuousAttribute extends Attribute {

    private double min;
    private double max;

    /**
     * Invoca il costruttore della super-classe
     * @param name nome simbolico dell'attributo
     * @param index identificativo numerico dell'attributo
     */
    public ContinuousAttribute(String name, short index) {
        super(name, index);
        this.min = 0;
        this.max = 0;
    }

    /**
     * Aggiorna min in base al valore v passato come parametro
     * @param v min sarà pari a questo valore
     */
    void setMin (Double v){
        if (v < min) min = v;
    }

    /**
     * Aggiorna max in base al valore v passato come parametro
     * @param v max sarà pari a questo valore
     */
    void setMax (Double v){
        if (v > max) max = v;
    }

    /**
     * @param value valore per cui scale = (value-min)/(max-min)
     * @return scala su cui basare attributi continui dello stesso tipo
     */
    double scale (Double value){
        double scale = (value-min)/(max-min);
        return scale;
    }
}
