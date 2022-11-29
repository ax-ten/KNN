package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Objects;

import data.Data;
import data.TrainingDataException;
import database.DatabaseConnectionException;
import database.InsufficientColumnNumberException;
import database.NoValueException;
import example.ExampleSizeException;
import mining.KNN;
import utility.DataUtility;

import static utility.DataUtility.addMissingExtention;

/**
 * Modella Server basato su Thread
 * @author Losito Nicola Dario
 */
public class ServeOneClient extends Thread{
    
    private final String LOCALPATH = "src/main/Testfile/";
    private final String TXTEXT = ".dat";
    private final String BINEXT = ".dmp";

    private String clientaddress;
    private Socket socket;
    private ObjectInputStream in; 
    private ObjectOutputStream out;

    public ServeOneClient(Socket s) throws IOException{
        try{
        socket = s;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        clientaddress = socket.getInetAddress().toString();
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
        start();

    }

    public void run() {
        while (true) {
            String filename ="", clientChoice="";
            Data trainingSet = null;
            KNN knn;
            // attendi la scelta dell'utente
            try {
                clientChoice = in.readObject().toString();
            } catch (IOException  | ClassNotFoundException e){
                System.err.println("Client" + clientaddress + ": Disconnesso improvvisamente, socket chiusa.");
                return;
            }
            try {
                while (trainingSet == null) {
                    filename = in.readObject().toString();
                    trainingSet = getTrainingset(clientChoice, filename);
                }
                //manda il trainingset al client per stamparlo a schermo
                out.writeObject(trainingSet.toString());

                //crea e salva il knn in un file .dmp
                knn = new KNN(trainingSet);
                knn.salva(LOCALPATH + filename + BINEXT);

                //calcola la predizione
                String doPredict = in.readObject().toString();
                do {
                    System.out.println("Client" + clientaddress + ": Formula una predizione");
                    //invia e ricevi ciascun elemento ed effettua la predizione su questo
                    Double prediction = knn.predict(out, in);
                    //inviala all'utente
                    out.writeObject(prediction);
                    System.out.println("Client" + clientaddress + ": Predizione - "+prediction);
                    doPredict = in.readObject().toString();}
                while (doPredict.equals("@PREDICTION"));
            }  catch (IOException | ClassNotFoundException e) {
                System.err.println("Client" + clientaddress + ": Errore di comunicazione, chiusura thread");
                return;
            } catch (ExampleSizeException e) {
                System.err.println("Client" + clientaddress + ": Errore di lettura Example");
            }
        }
    }
    private Data getTrainingset(String clientChoice, String filename){
        Data trainingSet =null;
        String errormsg = "";
        try {
            //ricevi dal client il tipo di richiesta dell'utente
            switch (clientChoice) {
                case "1": {
                    filename = addMissingExtention(filename, TXTEXT);
                    System.out.println("Client" + clientaddress + ": ha richiesto il caricamento di KNN da file: "+filename);
                    trainingSet = DataUtility.getTrainingSetFromDat(filename);
                    break;
                }
                case "2": {
                    filename = addMissingExtention(filename, BINEXT);
                    System.out.println("Client" + clientaddress + ": ha richiesto il caricamento di KNN da file: "+filename);
                    trainingSet = DataUtility.getTrainingSetFromDmp(filename);
                    break;
                }
                case "3": {
                    System.out.println("Client" + clientaddress + ":  ha richiesto il caricamento di KNN da tabella di DB: "+filename);
                    trainingSet = DataUtility.getTrainingSetFromDB(filename);
                    break;
                }
            }
        } catch (FileNotFoundException | TrainingDataException   e) {
            System.err.println("Client" + clientaddress + ": Richiede file non esistente.");
            errormsg = "@ERROR:FileNotFound";
        } catch (DatabaseConnectionException | SQLException e){
            System.err.println("Client" + clientaddress + ": connessione al DB fallita");
            errormsg="@ERROR:DatabaseConnection";
        } catch (InsufficientColumnNumberException | NoValueException e) {
            System.err.println("Client" + clientaddress + ": DB non valido");
            errormsg="@ERROR:DatabaseInvalid";
        } catch (IOException | ClassNotFoundException  e) {
            System.err.println("Client" + clientaddress + ": Errore di comunicazione");
            throw new RuntimeException(e);
        }
        if (!errormsg.equals("")){
            try {
                out.writeObject(errormsg);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }

        return trainingSet;
    }
}