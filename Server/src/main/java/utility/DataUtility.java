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

/**
 * wrapper di funzioni statiche su elementi Data
 * @author Giannotte Giampiero
 */
public class DataUtility {
    static final String LOCALPATH = "src/main/Testfile/";
    static final String TXTEXT = ".dat";
    static final String BINEXT = ".dmp";


    /**
     * @param filename nome del file da caricare
     * @return il trainingset salvato sul file
     * @throws IOException se non è possibile accedere al file
     * @throws ClassNotFoundException se il file non corrisponde ad un KNN salvato precedentemente
     */
    public static Data getTrainingSetFromDmp(String filename) throws IOException, ClassNotFoundException {
        return new KNN().carica(LOCALPATH + addMissingExtention(filename,BINEXT)).getData();
    }

    /**
     * @param filename nome del file da caricare
     * @return il trainingset salvato sul file
     * @throws TrainingDataException se il file è mancante o non è convertibile in trainingset
     */
    public static Data getTrainingSetFromDat(String filename) throws TrainingDataException {
        return new Data(LOCALPATH+addMissingExtention(filename,TXTEXT));
    }

    /**
     * @param tablename nome della tabella del DB da cui ottenere il dataset
     * @return trainingset ottenuto dalla tabella
     * @throws DatabaseConnectionException se non è possibile connettersi al database
     * @throws NoValueException se manca un valore nella tabella
     * @throws SQLException se ci sono errori SQL generici
     * @throws TrainingDataException se non è possibile ricavare il trainingset
     * @throws InsufficientColumnNumberException se ci sono meno di due colonne
     */
    public static Data getTrainingSetFromDB(String tablename)
            throws DatabaseConnectionException, NoValueException, SQLException, TrainingDataException, InsufficientColumnNumberException {
         DbAccess db = new DbAccess();
         Data ts = new Data(db,tablename);
         db.closeConnection();
        return ts;
    }

    /**
     * @param filename nome del file al quale aggiungere l'estensione
     * @param fileExt estensione da aggiungere al file
     * @return filename con solo un'estensione
     */
    public static String addMissingExtention(String filename, String fileExt){
        Pattern p = Pattern.compile(fileExt + "$");
        filename = p.matcher(filename).find() ? filename : filename + fileExt;
        return filename;
    }
}
