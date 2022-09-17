package utility;

import mining.KNN;

import java.io.IOException;

import static utility.DataUtility.addMissingExtention;

public class KNNUtility {
    static final String LOCALPATH = "src/main/Testfile/";
    static final String BINEXT = ".dmp";


    public static KNN loadKNNFromBin(String filename)  {
        KNN knn=new KNN();
        try {
            System.out.println(LOCALPATH+addMissingExtention(filename,BINEXT));
            knn.carica(LOCALPATH + addMissingExtention(filename,BINEXT));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return knn;
    }
}
