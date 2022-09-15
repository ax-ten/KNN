package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

import data.Data;
import data.TrainingDataException;
import database.*;
import example.ExampleSizeException;
import mining.KNN;

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
                KNN knn=null;
                String str = in.readObject().toString();
                if (str.equals("END")) break;
                switch (str) {
                    case "1":{
                        System.out.println("Echoing: Load KNN from file");
                        boolean flag=false;
                        Data trainingSet=null;
                        String file="";
                        do {			
                            try {
                                file=in.readObject().toString();
                                System.out.println("Nome file richiesto: "+LOCALPATH+file+TXTEXT);
                                trainingSet= new Data(LOCALPATH+file+TXTEXT);
                                out.writeObject(trainingSet.toString());
                                System.out.println(trainingSet);
                                flag=true;
                            }
                            catch(TrainingDataException exc){System.out.println(exc.getMessage());}
                        }while(!flag);			
                        knn=new KNN(trainingSet);
                        try{knn.salva(LOCALPATH+file+BINEXT);
                        }catch(IOException exc) {System.out.println(exc.getMessage());}
                        }
                    break;
				    case "2":{
                        System.out.println("Echoing: Load KNN from binary file");
                        boolean flag=false;
                        do {			
                            try {
                                String file=in.readObject().toString();
                                System.out.println("Nome file richiesto: "+LOCALPATH+file+TXTEXT);
                                knn=KNN.carica(LOCALPATH+file+BINEXT);
                                out.writeObject(knn.toString());
                                System.out.println(knn);
                                flag=true;
                            }
                            catch(IOException | ClassNotFoundException exc){System.out.println(exc.getMessage());}
                        }
                        while(!flag);		
                    }
                    break;
                    case "3":{
                        System.out.println("Echoing: Load KNN from database");
                        Data trainingSet=null;
                        String table="";
                        boolean flag=false;
                        do {			
                            try {
                                System.out.print("Connecting to DB...");
                                DbAccess db=new DbAccess();
                                System.out.println("done!");
                                table = in.readObject().toString();
                                System.out.println("Nome tabella: "+ table);							
                                trainingSet=new Data(db,table);
                                out.writeObject(trainingSet.toString());
                                System.out.println(trainingSet);
                                flag=true;
                                db.closeConnection();
                            }
                            catch(InsufficientColumnNumberException | SQLException | TrainingDataException | DatabaseConnectionException | NoValueException exc){
                                System.out.println(exc.getMessage());
                            }
                        }
                        while(!flag);			
                        
                        knn=new KNN(trainingSet);
                        try{knn.salva(LOCALPATH+table+"DB"+BINEXT);}
                        catch(IOException exc) {System.out.println(exc.getMessage());}
                    }
                    break;
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
                } catch(IOException e) {
                    e.printStackTrace();
                    System.err.println("IO Exception");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (ExampleSizeException e) {
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

