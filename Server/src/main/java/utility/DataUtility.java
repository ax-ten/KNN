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


    public static KNN loadKNNFromBin(String filename) throws IOException, ClassNotFoundException {
        return new KNN().carica(LOCALPATH + addMissingExtention(filename,BINEXT));
    }

    public static Data getTrainingSetFromDat(String filename) throws TrainingDataException {
        return new Data(LOCALPATH+addMissingExtention(filename,TXTEXT));
    }

    public static Data getTrainingSetFromDB(String tablename)
            throws DatabaseConnectionException, NoValueException, SQLException, TrainingDataException, InsufficientColumnNumberException {
         DbAccess db = new DbAccess();
         Data ts = new Data(db,tablename);
         db.closeConnection();
        return ts;
    }

    public static String addMissingExtention(String filename, String fileExt){
        Pattern p = Pattern.compile(fileExt + "$");
        filename = p.matcher(filename).find() ? filename : filename + fileExt;
        return filename;
    }
}
