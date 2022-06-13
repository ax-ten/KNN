package database;

import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import example.Example;

public class TableData {

	private DbAccess db;
	private String table;
	private TableSchema tSchema;
	private List<Example> transSet;
	private List target;
	
	
	public TableData(DbAccess db, TableSchema tSchema) throws SQLException,InsufficientColumnNumberException{
		this.db=db;
		this.tSchema=tSchema;
		this.table=tSchema.getTableName();
		transSet = new ArrayList();
		target= new ArrayList();	
		init();
	}

	public Column getColumn(String columnName){
		return this.tSchema.getColumn(columnName);
	}

	private void init() throws SQLException{		
		String query="select ";
		int i=0;
		
		Iterator<Column> it=tSchema.iterator();
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
				target.add(rs.getString(tSchema.target().getColumnName()));
		}
		rs.close();
		statement.close();	
	}
	
	public List<Example> getExamples(){
		return transSet; 
	}

	public Object getAggregateColumnValue(Column column, QUERY_TYPE aggregate) throws SQLException {
		Statement statement = db.getConnection().createStatement();
		ResultSet rs = statement.executeQuery("SELECT " +aggregate+ " ("+column+") FROM "+table);
		return rs.getDouble(0);
	}

	public List getTargetValues(){
		return target; 
	}
	
}
