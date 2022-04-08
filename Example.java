public class Example {
    Object[] example ; // Array di Object che contiene un valore per ciascun attributo indipendente

    // costruisce l’array example come array di dimensione size
    public Example(int size) {
        example = new Object[size];
    }

    // memorizza o nella posizione index di example
    public void set(Object o, int index) {
    }

    // restituisce il valore memorizzato nella posizione index di example
    public Object get(int index){
        return example[index];
    }

    // scambia i valori contenuti nel campo example dell’ oggetto corrente con i valori contenuti nel campo
    //      example del parametro e
    public void swap(Example e) {
    }

    // memorizza o nella posizione index di example
    public void set(String a, int jColumn) {
    }

    // calcola e restituisce la distanza di Hamming calcolata tra l’istanza di Example passata come
    //      parametro e quella corrente
    public double distance(Example e){
        return 0d;
    }
}
