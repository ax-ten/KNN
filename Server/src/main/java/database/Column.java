package database;

/**
 * Per istanziare l'elemento colonna di un database
 * @author Losito Nicola Dario
 */
public class Column{
	private final String name;
	private final String type;

	/**
	 * @param name nome simbolico della colonna
	 * @param type tipo di attributo della colonna
	 */
	Column(String name,String type){
		this.name=name;
		this.type=type;
	}

	/**
	 * @return il titolo della colonna
	 */
	public String getColumnName(){
		return name;
	}

	/**
	 * @return vero se il tipo della colonna è numerico
	 */
	public boolean isNumber(){
		return type.equals("number");
	}

	/**
	 * @return stringa: "titolo : tipo della colonna"
	 */
	public String toString(){
		return name+":"+type;
	}
}