package utility;

import data.Data;
import data.TrainingDataException;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.InsufficientColumnNumberException;
import database.NoValueException;
import mining.KNN;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class DataUtility {
    static final String LOCALPATH = "src/main/Testfile/";
    static final String TXTEXT = ".dat";
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

    public static Data getTrainingSetFromDat(String filename) {
        Data ts = null;
        try {
            ts =  new Data(LOCALPATH+addMissingExtention(filename,TXTEXT));
        } catch (TrainingDataException e) {
            e.printStackTrace();
        }
        return ts;
    }

    public static Data getTrainingSetFromDB(String tablename){
        Data ts =null;
        try {
            DbAccess db = new DbAccess();
            ts = new Data(db,tablename);
            db.closeConnection();
        }
        catch(SQLException | TrainingDataException | DatabaseConnectionException |
                NoValueException | InsufficientColumnNumberException exc){
            System.out.println(exc.getMessage());
        }
        return ts;
    }

    public static String addMissingExtention(String filename, String fileExt){
        Pattern p = Pattern.compile(fileExt + "$");
        filename = p.matcher(filename).find() ? filename : filename + fileExt;
        return filename;
    }
}
