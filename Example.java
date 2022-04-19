public class Example {
    Object[] example ; // Array di Object che contiene un valore per ciascun attributo indipendente

    // costruisce l’array example come array di dimensione size
    public Example(int size) {
        example = new Object[size];
    }

    // #o  :  oggetto da inserire in example
    // #index  :  indicatore su example dell'oggetto da sostituire
    public void set(Object o, int index) throws IndexOutOfBoundsException {
        example[index] = o;
    }

    // #index  :  posizione in example da cui estrarre valore
    // %return :  elemento in posizione index
    public Object get(int index) throws IndexOutOfBoundsException{
        return example[index];
    }

    // #e
    //
    public void swap(Example e) {
        this.example = new Object[e.example.length];
        for (int i=0; i<e.example.length; i++){
            this.set(e.get(i), i);
        }
    }

    // calcola e restituisce la distanza di Hamming calcolata tra l’istanza di Example passata come
    //      parametro e quella corrente
    public double distance(Example e){
        double d = 0d;
        try {
            for (int i=0; i<e.example.length; i++){
                if (this.get(i) == e.get(i)){
                    d++;
                }
            }
        } catch (Exception exc ){
            System.out.println("Eccezione trovata: " + exc );
        }
        return 0d;
    }
}
