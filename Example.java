public class Example {
    Object[] example ; // Array di Object che contiene un valore per ciascun attributo indipendente

    // costruisce l’array example come array di dimensione size
    public Example(int size) {
        example = new Object[size];
    }

    // memorizza o nella posizione index di example
    public void set(Object o, int index) {
        //TODO
        // SE index < size allora set
    }

    // %index  :  posizione in example da cui estrarre valore
    // %return :  elemento in posizione index
    public Object get(int index){
        //TODO
        // SE index < size allora get
        return example[index];
    }

    // scambia i valori contenuti nel campo example dell’ oggetto corrente con i valori contenuti nel campo
    //      example del parametro e
    public void swap(Example e) {
        //TODO
    }

    // calcola e restituisce la distanza di Hamming calcolata tra l’istanza di Example passata come
    //      parametro e quella corrente
    public double distance(Example e){
        //TODO
        return 0d;
    }
}
