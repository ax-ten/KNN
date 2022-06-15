package Client.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import Client.utility.Keyboard;

public class Client {
	
	private Socket socket=null;
	private ObjectOutputStream out=null;
	private ObjectInputStream in=null;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	
	Client (String address, int port) throws IOException, ClassNotFoundException{
		
			socket = new Socket(address, port);
			System.out.println(socket);		
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());	; // stream con richieste del client
			talking();
	}
	
	private void talking() throws IOException, ClassNotFoundException {
		
		int decision=0;
		String menu="";
		
		do {	
			do{
				System.out.println("Load KNN from file [1]");
				System.out.println("Load KNN from binary file  [2]");
				System.out.println("Load KNN from database  [3]");
				decision=Keyboard.readInt();
			}while(decision <0 || decision >3);
			
			String risposta="";
			do {
				out.writeObject(decision);
				String tableName="";
				System.out.println("Table name (without estensione):");
				tableName=Keyboard.readString();
				out.writeObject(tableName);
				risposta=(String)in.readObject();
			
			}while(risposta.contains("@ERROR"));
			
			System.out.println("KNN loaded on the server");
			// predict
			String c;
			do {
				out.writeObject(4);
				boolean flag=true; //reading example
				do {
					risposta=(String)(in.readObject());
					if(!risposta.contains("@ENDEXAMPLE")) {
						// sto leggendo l'esempio
						String msg=(String)(in.readObject());
						if(risposta.equals("@READSTRING"))  //leggo una stringa
						{
							System.out.println(msg);
							out.writeObject(Keyboard.readString());
						}
						else //leggo un numero
						{
							double x=0.0;
							do {
								System.out.println(msg);								
								x=Keyboard.readDouble();
							}
							while(Double.valueOf(x).equals(Double.NaN));
							out.writeObject(x);
						}	
					}
					else flag=false;
				}while( flag);
				
				//sto leggendo  k
				risposta=(String)(in.readObject());
				int k=0;
				do {
					System.out.print(risposta);
					k=Keyboard.readInt();
				}while (k<1);
				out.writeObject(k);
				//aspetto la predizione 
				
				System.out.println("Prediction:"+in.readObject());
	
				System.out.println("Vuoi ripetere predizione? Y/N");
				c=Keyboard.readString();
				
			}while (c.toLowerCase().equals("y"));	
			System.out.println("Vuoi ripetere una nuova esecuzione con un nuovo oggetto KNN? (Y/N)");
			menu=Keyboard.readString();
		}
		while(menu.toLowerCase().equals("y"));
	}
	public static void main(String[] args){
		try {
			InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
			System.out.println(e.toString());
			return;
		}
		
		try {
			new Client(args[0], Integer.valueOf(args[1]));
			
		}catch (IOException e) {
			System.out.println(e.toString());
			return;
		}catch (NumberFormatException e) {
			System.out.println(e.toString());
			return;
		}catch (ClassNotFoundException e) {
			System.out.println(e.toString());
			return;
		}
	}
}
