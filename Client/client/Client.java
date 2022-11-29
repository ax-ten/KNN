package Client.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;

import Client.utility.Keyboard;

/**
 * Modella il client
 * @author Losito Nicola Dario
 */
public class Client {
	
	private Socket socket=null;
	private ObjectOutputStream out=null;
	private ObjectInputStream in=null;
	/**
	 * @param address indirizzo ipv4 della socket di comunicazione
	 * @param port porta della socket di comunicazione
	 */
	public Client (String address, int port) {
		try {
			socket = new Socket(address, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream()); // stream con richieste del client
			talking();
		} catch (ClassNotFoundException | ConnectException e) {
			System.out.println("Impossibile connettersi al Server, assicurarsi che sia in esecuzione");
		}catch (SocketException e){
			System.out.println("Connessione al server interrotta. Il programma verrà chiuso.");
		} catch (NumberFormatException e) {
			System.out.println("Input non valido, riprova");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @throws IOException se si presentano errori di lettura/scrittura
	 * @throws ClassNotFoundException Se si presentano errori di lettura socket
	 */
	private void talking() throws IOException, ClassNotFoundException {
		
		int answLoadFrom, k;
		String serverAnsw, answFilename, answContinue;
		
		do {	
			do{
				System.out.println("[1] Carica training set da file .dat\n"+
								   "[2] Carica training set da file binario .dmp\n" +
								   "[3] Carica training set dal database ");
				answLoadFrom=Keyboard.readInt();
				if (answLoadFrom <1 || answLoadFrom >3)
					System.out.println("Valore non valido! inserirne uno tra quelli elencati. ");
				else
					//invia al server la scelta presa
					out.writeObject(answLoadFrom);
			}while(answLoadFrom <0 || answLoadFrom >3);

			do {
				//chiedi nome del file all'utente
				System.out.println("Inserire il nome del file:");
				answFilename=Keyboard.readString();

				//manda la stringa col nome del file e attendi il trainingset
				out.writeObject(answFilename);
				serverAnsw=(String)in.readObject();

				// se anziché il trainingset arriva un messaggio di errore,
				// mostralo a schermo e chiedi nuovamente il nome del file
				switch (serverAnsw){
					case ("@ERROR:FileNotFound"): {
						System.out.println("File non trovato, inserire un altro nome (non è necessario specificare l'estensione di file)");
						break;
					}
					case ("@ERROR:DatabaseConnection"): {
						System.out.println("Impossibile connettersi al DB, assicurati che sia avviato");
						break;
					}
					case ("@ERROR:DatabaseInvalid"): {
						System.out.println("La tabella specificata non è valida, specificarne una nuova");
						break;
					}
					default:
						// altrimenti mostra a schermo il trainingset
						System.out.println("KNN caricato sul server:\n" + serverAnsw);
				}
			} while(serverAnsw.contains("@ERROR"));

			// predict
			do {
				// segnala al server che sta cominciando la prediction
				out.writeObject("@PREDICTION");
				do {
					double x;
					// Ricava il tipo di dato del prossimo elemento dell'Explanatory set
					serverAnsw=(String)(in.readObject());

					// Ricava il messaggio da mostrare all'utente
					String serverPrompt = (String) (in.readObject());
					System.out.println(serverPrompt);

					switch (serverAnsw) {
						// Se è un valore discreto
						case "@READSTRING":
							out.writeObject(Keyboard.readString());
							break;
						// Se è un valore continuo
						case "@READDOUBLE":
							do x = Keyboard.readDouble();
							while (Double.valueOf(x).equals(Double.NaN));
							out.writeObject(x);
							break;
					}
				// Continua finché non è finito l'explanatoryset
				}while(!serverAnsw.contains("@ENDEXAMPLE"));

				// Il server richiederà a questo punto K
				serverAnsw=(String)(in.readObject()); // @READINT
				do k = Keyboard.readInt();
				while (Integer.valueOf(k).equals(Integer.MIN_VALUE) || k < 1);
				out.writeObject(k);

				// aspetto la predizione
				System.out.println("Predizione: "+in.readObject());

				// Chiedi se effettuare una nuova prediction
				System.out.println("Vuoi effettuare un'altra predizione sullo stesso dataset? (S/N)");
				answContinue = forceAnswerbetween(new String[]{"s","n"});

			}while (answContinue.equalsIgnoreCase("s"));
			out.writeObject(answContinue);
			System.out.println("Vuoi aprire un altro trainingset? (S/N)");

			answContinue = forceAnswerbetween(new String[]{"s","n"});
		} while(answContinue.equalsIgnoreCase("s"));
		System.out.println("Chiusura del client.");
		return;
	}
	private String forceAnswerbetween(String[] answers){
		String prompt = "Inserire un valore tra: ",
				answ;
		boolean loop;
		int i =0;
		for (String s: answers){
			prompt = prompt + String.format("'%s'",s);
			i++;
			if (answers.length != i )
				prompt = prompt+",";
		}
		do {
			answ = Keyboard.readString().toLowerCase();
			loop = !Arrays.asList(answers).contains(answ);
			if(loop)
				System.out.println(prompt);
		} while (loop);
		return answ;
	}

	/**
	 * Crea un Client di comunicazione col Server
	 * @param args [address] indirizzo di connessione
	 *             [port] porta di connessione
	 */
	public static void main(String[] args){
		String ip, port;
		boolean loop=false;
		do {
			try {
				ip = args[0];
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Inserire Ip:");
				ip = Keyboard.readString();
			}

			try {
				port = args[1];
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Inserire Porta:");
				port = Keyboard.readString();
			}

			try {
				InetAddress.getAllByName(ip);
			} catch (UnknownHostException e) {
				System.err.println("Impossibile connettersi al server per l'ip specificato");
				loop = true;
			}
		} while (loop);
		new Client(ip, Integer.parseInt(port));
	}
}
