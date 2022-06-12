import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import data.Data;
import data.TrainingDataException;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.InsufficientColumnNumberException;
import example.Example;
import example.ExampleSizeException;
import mining.KNN;
import utility.Keyboard;

public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException, ExampleSizeException {
		
		String menu="";
		
		do {
			//load or train knn
			KNN knn=null;
			System.out.println("Caricare KNN da file.txt (1) o da file binario (2) o da database (3)?" );
			int r=0;
			do{
				r=Keyboard.readInt();
			}while(r!=1 && r!=2 && r!=3);
			switch(r) 
			{
				case 1:{
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
				break;
				case 2:
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
				break;
				case 3:
					Data trainingSet=null;
					String table="";
					boolean flag=false;
					do {			
						try {
							System.out.print("Connecting to DB...");
							DbAccess db=new DbAccess();
							System.out.println("done!");
							System.out.println("Nome tabella:");
							table=Keyboard.readString();
							trainingSet= new Data(db,table);
							System.out.println(trainingSet);
							flag=true;
							db.closeConnection();
						}
						catch(InsufficientColumnNumberException | TrainingDataException exc1){System.out.println(exc1.getMessage());}
						catch(DatabaseConnectionException exc2){System.out.println(exc2.getMessage());}
					}
					while(!flag);			
					
					knn=new KNN(trainingSet);
					try{knn.salva(table+"DB.dmp");}
					catch(IOException exc) {System.out.println(exc.getMessage());}
				
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
