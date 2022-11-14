package database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Gestisce i metedati di una tabella
 */
public class TableSchema implements Iterable<Column>{
	
	private final List<Column> tableSchema=new ArrayList<>();
	private Column target;
	private final String tableName;


	/** Crea uno Schema di una tabella ottenuta tramite nome e gestore DB
	 * @param tableName nome della tabella
	 * @param db gestore accesso DB
	 * @throws SQLException se ci sono errori nelle query SQL
	 * @throws InsufficientColumnNumberException se ci sono meno di due colonne nella tabella
	 */
	public TableSchema(String tableName, DbAccess db) throws SQLException,InsufficientColumnNumberException{
		this.tableName=tableName;
		
		HashMap<String,String> mapSQL_JAVATypes=new HashMap<String, String>();
		//http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/mapping.html
		mapSQL_JAVATypes.put("CHAR","string");
		mapSQL_JAVATypes.put("VARCHAR","string");
		mapSQL_JAVATypes.put("LONGVARCHAR","string");
		mapSQL_JAVATypes.put("BIT","string");
		mapSQL_JAVATypes.put("SHORT","number");
		mapSQL_JAVATypes.put("INT","number");
		mapSQL_JAVATypes.put("LONG","number");
		mapSQL_JAVATypes.put("FLOAT","number");
		mapSQL_JAVATypes.put("DOUBLE","number");
		
		DatabaseMetaData meta = db.getConnection().getMetaData();

		ResultSet res = meta.getColumns(null, null, tableName, null);	     
		   
	    while (res.next()) {
	        if(mapSQL_JAVATypes.containsKey(res.getString("TYPE_NAME"))){
	        	if(res.isLast()) {
	        		target=new Column(
	        				res.getString("COLUMN_NAME"),
	        				mapSQL_JAVATypes.get(res.getString("TYPE_NAME"))
							);
				}
	        	else{
	        		tableSchema.add(new Column(
	        				res.getString("COLUMN_NAME"),
	        				mapSQL_JAVATypes.get(res.getString("TYPE_NAME")))
	        				);
				}
			}
	    }
	    res.close();
	    if(target==null || tableSchema.size()==0) throw
				new InsufficientColumnNumberException("La tabella selezionata contiene meno di due colonne");
		
		}

		public Column getColumn(String columnName) {
			for (Column c : tableSchema){
				if (c.getColumnName().equals(columnName))
					return c;
			}
			return null;
		}

	/**
	* @return colonna target
	*/
	public Column target(){
		return target;
	}

	/**
	* @return numero di colonne nella tabella
	*/
	int getNumberOfAttributes() {
		return tableSchema.size();
	}

	/**
	* @return nome della tabella
	*/
	String getTableName() {
		return tableName;
	}

	/**
	 * @return iteratore tra le colonne
	 */
	@Override
	public Iterator<Column> iterator() {
			return tableSchema.iterator();
		}	
}
	