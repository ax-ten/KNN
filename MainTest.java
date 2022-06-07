import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import data.Data;
import mining.KNN;
import utility.Keyboard;
import utility.ExampleSizeException;
import utility.TrainingDataException;

public class MainTest {

	
	/**
	 * @param args
	 */
	 public static void main(String[] args) throws FileNotFoundException, TrainingDataException {

		 String MENU="";
		 final String LOCALPATH="Test file/";
		 final String FILEEXTENTION=".dat";
		 boolean flag=false;
		 KNN knn = null;
		 String filename="";
		 Data trainingSet;

		do{
		 System.out.println("Scegliere uno tra i seguenti file:");
		 DirectoryStream<Path> stream = null;
		 try {
			 stream = Files.newDirectoryStream(Paths.get(LOCALPATH), "*.dat");
		 } catch (IOException e) {e.printStackTrace();}

		 for (Path filepath : stream) {
			 System.out.println(filepath.toFile().getName());
		 }

		 do {

			 filename = Keyboard.readString().split(FILEEXTENTION)[0];
			 try{
				 //il file è binario
				 knn=KNN.carica(LOCALPATH+filename+FILEEXTENTION);

			 } catch (IOException e) {
				 // il file è dat
				 trainingSet = new Data(LOCALPATH + filename + FILEEXTENTION);
				 knn=new KNN(trainingSet);

			 } catch (ClassNotFoundException e) {
				 //il file non è presente
				 e.printStackTrace();
			 }

			 System.out.println(knn);
		 } while (!flag);

			// predict
			try{
				String c;
				do {
					// read example withKeyboard
					System.out.println(knn.predict());
					System.out.println("Vuoi ripetere? Y/N");
					c=Keyboard.readString();

				}while (c.toLowerCase().equals("y"));

				System.out.println("Vuoi ripetere una nuova esecuzione con un nuovo oggetto KNN? (Y/N)");
				MENU=Keyboard.readString();

				System.out.println("Vuoi salvare l'attuale oggetto KNN? (Y/N)");
				String save = Keyboard.readString();
				if(save.toLowerCase().equals("y")){
					System.out.println("Nome con cui salvare l'oggetto KNN");
					String file = Keyboard.readString();
					try{knn.salva(LOCALPATH+file+FILEEXTENTION);}
					catch(IOException exc) {System.out.println(exc.getMessage());}
				}
			}catch (ExampleSizeException e){System.out.println(e.getMessage());}
		}
		while(MENU.equalsIgnoreCase("y"));
	}
}
