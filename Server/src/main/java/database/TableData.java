package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import example.Example;

/**
 * Gstisce le query di ottenimento dati e metadati
 * @author Losito Nicola Dario
 */
public class TableData {

	private DbAccess db;
	private String table;
	private TableSchema tSchema;
	private List<Example> transSet;
	private List<Double> target;


	/**
	 * @param db gestore di accesso
	 * @param tSchema metadati del db
	 * @throws SQLException se si presentano errori nelle query sql
	 * @throws NoValueException se l'ultimo elemento della riga è discreto
	 */
	public TableData(DbAccess db, TableSchema tSchema) throws SQLException, NoValueException{
		this.db=db;
		this.tSchema=tSchema;
		this.table=tSchema.getTableName();
		transSet = new ArrayList<>();
		target= new ArrayList<>();
		init();
	}

	/**
	 * @param columnName titolo della colonna da reperire
	 * @return colonna del DB omonima a columnName
	 */
	public Column getColumn(String columnName){
		return this.tSchema.getColumn(columnName);
	}

	/**
	 * @throws SQLException se si presentano errori nelle query sql
	 * @throws NoValueException se l'ultimo elemento della riga è discreto
	 */
	private void init() throws SQLException, NoValueException{
		String query="select ";
		int i=0;
		
		for(Column c:tSchema){			
			query += c.getColumnName();
			query+=",";
		}
		query +=tSchema.target().getColumnName();
		query += (" FROM "+table);
		
		Statement statement = db.getConnection().createStatement();
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			Example currentTuple=new Example(tSchema.getNumberOfAttributes());
			i=0;
			for(Column c:tSchema) {
				if(c.isNumber())
					currentTuple.set(i, rs.getDouble(i+1));
				else
					currentTuple.set(i, rs.getString(i+1));
				i++;
			}
			transSet.add(currentTuple);
			
			if(tSchema.target().isNumber())
				target.add(rs.getDouble(tSchema.target().getColumnName()));
			else
				//target.add(rs.getString(tSchema.target().getColumnName()));
				throw new NoValueException("Target value is discrete");
		}
		rs.close();
		statement.close();	
	}

	/** Gli Examples sono ricavati tramite init()
	 * @return lista di tuple-Example presenti nel DB
	 */
	public List<Example> getExamples(){
		return transSet; 
	}

	/**
	 * @param column colonna dal quale ottenere il valore del tipo specificato da aggregate
	 * @param aggregate può essere MIN o MAX
	 * @return il Double risultante dalla query
	 * @throws SQLException se ci sono errori nella query SQL
	 */
	public Object getAggregateColumnValue(Column column, QUERY_TYPE aggregate) throws SQLException {
		Statement statement = db.getConnection().createStatement();
		ResultSet rs = statement.executeQuery("SELECT " +aggregate+ "("+column.getColumnName()+") FROM "+table);
		rs.next();
		return rs.getDouble(1);
	}

	/** I Target Values vengono assegnati tramite init()
	 * @return lista di valori target
	 */
	public List<Double> getTargetValues(){
		return target; 
	}	
}
