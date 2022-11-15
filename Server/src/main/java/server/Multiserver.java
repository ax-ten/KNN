package server;

import telegrambot.InvalidBotException;
import telegrambot.SimpleBot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Gestore di un server thread
 * @author Losito Nicola Dario
 */
public class Multiserver {
    private static int PORT = 2025;

    public Multiserver(int port){
        PORT = port;
        run();
    }

    /**
     * Esegue istanza di @SimpleBot e @Multiserver
     * @param args [port] - numero di porta della socket di comunicazione col Client
     */
    public static void main(String[] args){
        new SimpleBot();
        if (args.length>0)
            new Multiserver(Integer.valueOf(args[0]));
        else
            new Multiserver(PORT);
    }

    /**
     * Blocco di codice del thread server
     */
    private void run(){
        try (ServerSocket s = new ServerSocket(PORT)){
            System.out.println("Server Started");
            System.out.println("addr: "+s.getLocalSocketAddress()+" port: "+s.getLocalPort());
            while(true) {
            // Si blocca finchè non si verifica una connessione:
                Socket socket = s.accept();
                try {
                    new ServeOneClient(socket);
                }catch(IOException e) {
                    // Se fallisce chiude il socket,
                    // altrimenti il thread la chiuderà:
                    socket.close();
                    }                    
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }       
}
