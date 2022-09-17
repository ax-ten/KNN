package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

import data.Data;
import example.ExampleSizeException;
import mining.KNN;
import utility.DataUtility;

import static utility.DataUtility.addMissingExtention;

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
                            new KNN(trainingSet).salva(LOCALPATH+filename+BINEXT); //Save KNN to binary
                        }
                        break;
                    }
				    case "2":{
                        System.out.println("Echoing: Load KNN from binary file");
                        while (!Objects.equals(filename, "")) {
                            filename = addMissingExtention(in.readObject().toString(), BINEXT);
                            knn = DataUtility.loadKNNFromBin(filename);
                            out.writeObject(knn.toString());
                        }
                        break;
                    }
                    case "3":{
                        System.out.println("Echoing: Load KNN from database");
                        while (!Objects.equals(tablename, "")) {
                            tablename = in.readObject().toString();
                            trainingSet = DataUtility.getTrainingSetFromDB(tablename);
                            out.writeObject(trainingSet.toString());
                            new KNN(trainingSet).salva(LOCALPATH+tablename+"DB"+BINEXT); //Save KNN to binary
                        }
                        break;
                    }
                }
                //prediction
                String predict = in.readObject().toString();
                while(predict.equals("4")){
                    System.out.println("Start prediction");
                    Double prediction = knn.predict(out, in);
                    out.writeObject(prediction);
                    predict = in.readObject().toString();
                }
            }
            System.out.println("closing...");
            } catch(IOException | ClassNotFoundException | ExampleSizeException e) {
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

