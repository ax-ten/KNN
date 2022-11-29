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
    private static int port = 2024;

    public Multiserver(int port){
        Multiserver.port = port;
        run();
    }

    /**
     * Esegue istanza di @SimpleBot e @Multiserver
     * @param args [port] - numero di porta della socket di comunicazione col Client
     */
    public static void main(String[] args){
        new SimpleBot();
        if (args.length>0)
            new Multiserver(Integer.parseInt(args[0]));
        else
            new Multiserver(port);
    }

    /**
     * Blocco di codice del thread server
     */
    private void run(){
        while (true) {
            try (ServerSocket s = new ServerSocket(++port)) {
                System.out.println("Porta aperta: "+port);
                // Si blocca finchè non si verifica una connessione:
                Socket socket = s.accept();
                try {
                    new ServeOneClient(socket);
                    System.out.println("Nuovo Client"+socket.getInetAddress().toString()+ ", porta condivisa: "+port);
                } catch (IOException e) {
                    // Se fallisce chiude il socket,
                    // altrimenti il thread la chiuderà:
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }       
}
