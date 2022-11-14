package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
 *
 */
public class ServerOneClient extends Thread{
    
    final String LOCALPATH = "Server/Test file/";
    final String TXTEXT = ".dat";
    final String BINEXT = ".dmp";

    private Socket socket;
    private ObjectInputStream in; 
    private ObjectOutputStream out;

    public ServerOneClient(Socket s) throws IOException{
        try{
        socket = s;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
        start();
    }

    public void run(){
        try {
            while (true) {
                String filename = ".", tablename = ".";
                KNN knn=null;
                Data trainingSet;
                String str = in.readObject().toString();
                if (str.equals("")) break;
                switch (str) {
                    case "1":{
                        System.out.println("Echoing: Load KNN from file");
                        while (!Objects.equals(filename, "")) {
                            filename = in.readObject().toString();
                            trainingSet = DataUtility.getTrainingSetFromDat(addMissingExtention(filename,TXTEXT));
                            out.writeObject(trainingSet.toString());
                            knn = new KNN(trainingSet); //Save KNN to binary
                            knn.salva(LOCALPATH+tablename+"DB"+BINEXT);
                        }
                        break;
                    }
				    case "2":{
                        System.out.println("Echoing: Load KNN from binary file");
                        while (!Objects.equals(filename, "")) {
                            filename = addMissingExtention(in.readObject().toString(), BINEXT);
                            knn = DataUtility.loadKNNFromBin(filename);
                            out.writeObject(knn.getData().toString());
                        }
                        break;
                    }
                    case "3":{
                        System.out.println("Echoing: Load KNN from database");
                        while (!Objects.equals(tablename, "")) {
                            tablename = in.readObject().toString();
                            trainingSet = DataUtility.getTrainingSetFromDB(tablename);
                            out.writeObject(trainingSet.toString());
                            knn = new KNN(trainingSet); //Save KNN to binary
                            knn.salva(LOCALPATH+tablename+"DB"+BINEXT);
                        }
                        break;
                    }
                }
                //predict
                String predict = in.readObject().toString();
                while(predict.equals("4")){
                    System.out.println("Start prediction");
                    Double prediction = knn.predict(out, in);
                    out.writeObject(prediction);
                    predict = in.readObject().toString();
                }
            }
            System.out.println("closing...");
        } catch(IOException | ClassNotFoundException | ExampleSizeException | TrainingDataException |
                DatabaseConnectionException | NoValueException | SQLException | InsufficientColumnNumberException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch(IOException e) {
                System.err.println("Socket not closed");
            }
        }
    }

}

