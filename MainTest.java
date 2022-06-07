import java.io.FileNotFoundException;
import java.io.IOException;

import data.Data;
import data.Example;
import data.ExampleSizeException;
import data.TrainingDataException;
import mining.KNN;
import utility.Keyboard;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException{
		
		String menu="";
		
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
						trainingSet= new Data(file);
						System.out.println(trainingSet);
						flag=true;
					}
					catch(TrainingDataException exc){System.out.println(exc.getMessage());}
				}
				while(!flag);			
				knn=new KNN(trainingSet);
				try{knn.salva(file+".dmp");}
				catch(IOException exc) {System.out.println(exc.getMessage());}
			}
			else
			{
				boolean flag=false;		
				do {			
					try {
						System.out.println("Nome file contenente una serializzazione dell'oggetto KNN:");
						String file=Keyboard.readString();
						knn=KNN.carica(file);
						flag=true;
					}
					catch(IOException | ClassNotFoundException exc){System.out.println(exc.getMessage());}
				}
				while(!flag);		
			}
			
			// predict
			String c;
			do {
				// read example withKeyboard
				System.out.println(knn.predict());
				System.out.println("Vuoi ripetere? Y/N");
				c=Keyboard.readString();
				
			}while (c.toLowerCase().equals("y"));	
					
			System.out.println("Vuoi ripetere una nuova esecuzione con un nuovo oggetto KNN? (Y/N)");
			menu=Keyboard.readString();
		}
		while(menu.toLowerCase().equals("y"));
	}
}
