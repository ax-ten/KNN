import java.io.FileNotFoundException;
import java.io.IOException;

import data.Data;
import data.Example;
import mining.KNN;
import utility.Keyboard;
import utility.ExampleSizeException;
import utility.TrainingDataException;

public class MainTest {

	
	/**
	 * @param args
	 */
	 public static void main(String[] args) throws FileNotFoundException{
		
		String menu="";
		String testFolder="Test file/";
		String testExt=".dat";

		do {
			//load or train knn
			KNN knn=null;
			System.out.println("Caricare KNN da file.txt (1) o da file binario (2)?" );
			int r=0;
			do{
				r=Keyboard.readInt();
			}while(r!=1 && r!=2);
			if(r==1) {
				boolean flag=false;
				Data trainingSet=null;
				String file="";
				do {			
					try {
						System.out.println("Nome file contenente un training set valido:");
						file=Keyboard.readString();
						trainingSet=new Data(testFolder+file+testExt);
						System.out.println(trainingSet);
						flag=true;
					}
					catch(TrainingDataException exc){System.out.println(exc.getMessage());}
				}
				while(!flag);			
				knn=new KNN(trainingSet);
			}
			else
			{
				boolean flag=false;		
				do {			
					try {
						System.out.println("Nome file contenente una serializzazione dell'oggetto KNN:");
						String file=Keyboard.readString();
						knn=KNN.carica(testFolder+file+testExt);
						System.out.println(knn);
						flag=true;
					}
					catch(IOException | ClassNotFoundException exc){System.out.println(exc.getMessage());}
				}
				while(!flag);		
			}
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
				menu=Keyboard.readString();

				System.out.println("Vuoi salvare l'attuale oggetto KNN? (Y/N)");
				String save = Keyboard.readString();
				if(save.toLowerCase().equals("y")){
					System.out.println("Nome con cui salvare l'oggetto KNN");
					String file = Keyboard.readString();
					try{knn.salva(testFolder+file+testExt);}
					catch(IOException exc) {System.out.println(exc.getMessage());}
				}
			}catch (ExampleSizeException e){System.out.println(e.getMessage());}
		}
		while(menu.toLowerCase().equals("y"));
	}
}
/*	Main test prima di serializzazione
public static void main(String[] args) throws FileNotFoundException{
	try {
		Data trainingSet= new Data("Test file/provaC.dat");
		System.out.println(trainingSet);
		KNN knn=new KNN(trainingSet);
		String r;
		do {
			// read example withKeyboard
			System.out.println("Prediction:"+knn.predict());
			System.out.println("Vuoi ripetere? Y/N");
			r=Keyboard.readString();
		}while (r.toLowerCase().equals("y"));
	}	catch(TrainingDataException exc){System.out.println(exc.getMessage());}	
		catch(ExampleSizeException e){System.out.println(e.getMessage());}
	}
}*/
