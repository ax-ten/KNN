package main.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Multiserver {
    private int PORT = 2025;

    public Multiserver(int port){
        this.PORT = port;
        run();
    }

    public Multiserver(){
        new Multiserver(PORT);
    }

    public static void main(String[] args){
        new Multiserver(Integer.valueOf(args[0]));
    }

    private void run(){
        try (ServerSocket s = new ServerSocket(PORT)){
            System.out.println("Server Started");
            System.out.println("addr: "+s.getLocalSocketAddress()+" port: "+s.getLocalPort());
            while(true) {
            // Si blocca finchè non si verifica una connessione:
                Socket socket = s.accept();
                try {
                    new ServerOneClient(socket);
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
