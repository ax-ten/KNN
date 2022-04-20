public class KNN {
    Data data; // modella il training set

    // avvalora il training set
    public KNN(Data trainingSet) {
        this.data = trainingSet;
    }

    // predice il valore target dell'esempio passato come parametro
    public Double predict(Example e, int k) {
        return data.avgClosest(e,k);
    }
}
