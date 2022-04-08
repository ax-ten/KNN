import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;



class Data {

	Example[] data;
	Double target[];
	int numberOfExamples;
	Attribute explanatorySet[];
	ContinuousAttribute classAttribute;

	Data(String fileName)throws FileNotFoundException{

		File inFile = new File (fileName);

		Scanner sc = new Scanner (inFile);
		String line = sc.nextLine();
		if(!line.contains("@schema"))
			throw new RuntimeException("Errore nello schema");
		String s[]=line.split(" ");

		//popolare explanatory Set

		Attribute[] explanatorySet = new Attribute[new Integer(s[1])];
		short iAttribute=0;
		line = sc.nextLine();
		while(!line.contains("@data")){
			s=line.split(" ");
			if(s[0].equals("@desc"))
			{ // aggiungo l'attributo allo spazio descrittivo
				//@desc motor discrete

				explanatorySet[iAttribute] = new DiscreteAttribute(s[1],iAttribute);
			}
			else if(s[0].equals("@target"))
				classAttribute=new ContinuousAttribute(s[1], iAttribute);

			iAttribute++;
			line = sc.nextLine();

		}

		//avvalorare numero di esempi
		numberOfExamples = new Integer(line.split(" ")[1]);

		//popolare data e target
		data = new Example[numberOfExamples];
		target = new Double[numberOfExamples];


		short iRow=0;
		while (sc.hasNextLine())
		{
			Example e=new Example(explanatorySet.length);
			line = sc.nextLine();
			// assumo che attributi siano tutti discreti
			s=line.split(","); //E,E,5,4, 0.28125095
			for(short jColumn=0;jColumn<s.length-1;jColumn++)
				e.set(s[jColumn],jColumn);
			data[iRow]=e;
			target[iRow]=new Double(s[s.length-1]);
			iRow++;

		}
		sc.close();
	}



	/*
	 * Partiziona data rispetto all'elemento x di key e restiutisce il punto di separazione
	 */
	private  int partition(double key[], int inf, int sup){
		int i,j;

		i=inf;
		j=sup;
		int	med=(inf+sup)/2;

		Double x=key[med];

		data[inf].swap(data[med]);

		double temp=target[inf];
		target[inf]=target[med];
		target[med]=temp;

		temp=key[inf];
		key[inf]=key[med];
		key[med]=temp;

		while (true)
		{

			while(i<=sup && key[i]<=x){
				i++;

			}

			while(key[j]>x) {
				j--;

			}

			if(i<j) {
				data[i].swap(data[j]);
				temp=target[i];
				target[i]=target[j];
				target[j]=temp;

				temp=key[i];
				key[i]=key[j];
				key[j]=temp;


			}
			else break;
		}
		data[inf].swap(data[j]);

		temp=target[inf];
		target[inf]=target[j];
		target[j]=temp;

		temp=key[inf];
		key[inf]=key[j];
		key[j]=temp;

		return j;

	}

	/*
	 * Algoritmo quicksort per l'ordinamento di data
	 * usando come relazione d'ordine totale "<=" definita su key
	 * @param A
	 */
	private void quicksort(double key[], int inf, int sup){

		if(sup>=inf){

			int pos;

			pos=partition(key, inf, sup);

			if ((pos-inf) < (sup-pos+1)) {
				quicksort(key, inf, pos-1);
				quicksort(key, pos+1,sup);
			}
			else
			{
				quicksort(key, pos+1, sup);
				quicksort(key, inf, pos-1);
			}


		}

	}

	int getNumberOfExplanatoryAttributes(){
		return explanatorySet.length;
	}

	double avgClosest(Example e, int k){
		//TODO
		// 1. 	Avvalora key con le distanze calcolate tra ciascuna istanza di Example memorizzata in data
		//			ed e (usare il metodo distance di Example)
		// 2. 	ordina data, target e key in accordo ai valori contenuti in key (usare quicksort)
		// 3. 	identifica gli esempi di data che sono associati alle k distanze più piccole in key
		// 4. 	calcola e restituisce la media dei valori memorizzati in target in corrispondenza degli esempi
		//			isolati al punto 3
		return 0d;
	}

	public static void main(String args[])throws FileNotFoundException{
		Data trainingSet=new Data("servo.dat");
		System.out.println(trainingSet);
	}

}
