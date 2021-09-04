/**
 * 
 */
package io.github.mezatsong.ladalja.query;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.mezatsong.ladalja.DB;
import io.github.mezatsong.ladalja.LadaljaException;

/**
 * 
 * Ladalja's database query builder provides a convenient, fluent interface to creating and running database queries.<br>
 * It can be used to perform most database operations in your application and works on all supported database systems.<br>
 * <p>
 * The Ladalja query builder uses prepared query to protect your application against SQL injection attacks. <br>
 * There is no need to clean strings being passed as bindings.
 * <p>
 * There are some using examples : <br>
 * <br>
 * <pre>
 * ResultSet result = DB.table("users").get(); //select * from users
 * 
 * ResultSet result = DB.table("users").where("email","example@example.com").first(); //select * from users where email=? limit 1;
 * if(result.next()){
 *     result.getString("password");
 * ...
 * 
 *
 * Map&lt;String,Object&gt; map = new HashMap&lt;&gt;()
 * map.put("email","example@example.com");
 * map.put("nom","Example");
 * int tabAge[] = {19,20,23};
 * 
 * DB.table("users").insert(map); //insert into users (email,nom) values(?,?);
 * DB.table("users").whereIn("age",tabAge).update(map); //update users set email=?, nom=? where age in (?,?,?); 
 * ...
 * </pre>
 * <p>
 * And more complicated query.
 * 
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class QueryBuilder<T extends QueryBuilder> {

	
	protected String table;
	
	private String selection;
	
	private String join;
	
	private String clauses;
	
	private String groupBy;
	
	private String having;
	
	private String orderBy;
	
	private String limit;
	
	private String offset;
	
	private String lock;
	
	private QueryBuilder union;

	private List<Object> params;
	
	
	/* ----- CONSTRUCTOR -------- */
	
	public QueryBuilder(String table) {
		this.table = table;
		selection = "*";
		join = "";
		clauses = "";
		groupBy = "";
		having = "";
		orderBy = "";
		limit = "";
		offset = "";
		lock = "";
		union = null;
		params = new ArrayList<Object>();
	}
	
	
	
	/* ------------ FINISHING METHODS -------------- */
	
	/**
	 * Build query and return a java.sql.ResultSet containing the results of builded of query 
	 * you may access each column's value by accessing the column as a property of the row
	 * @return ResultSet of result a java.sql.ResultSet containing the results of builded of query. 
	 */
	public ResultSet get()
	{
		String query = "select "+selection+" from `"+table+"` "+join+" "+clauses+" "+groupBy+" "+having+" "+orderBy+" "+limit+" "+offset+" "+lock;
		query = sloveAmbiguousColumn(query);
		if(union != null){
			String otherQuery = "select "+union.selection+" from "+union.table+" "+union.join+" "+union.clauses+" "+union.groupBy+" "
									+union.having+" "+union.orderBy+" "+union.limit+" "+union.offset+" "+union.lock;
			otherQuery = sloveAmbiguousColumn(otherQuery);
			query = "( "+query+" ) union ( "+otherQuery+" )";
		}
		
		return DB.select(query.trim(), params.toArray());
	}

	
	
	
	/**
	 * Build query and return result of builded of query into List<Map<String, Object>> 
	 * where keys are column name and value of each key is its value
	 * @return list of map where each map corresponding to one row of query result
	 */
	public List<Map<String, Object>> getMap()
	{
		try {
			ResultSet resultSet = get();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			while(resultSet.next())
			{
				Map<String, Object> map = new HashMap<String, Object>();
				ResultSetMetaData metaData = resultSet.getMetaData();
				for(int i=1, length = metaData.getColumnCount(); i <= length; i++)
				{
					map.put(metaData.getColumnName(i), resultSet.getObject(i));
				}
				list.add(map);
			}
			
			return list;
			
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	
	
	/**
	 * Retrieve a single row of query
	 * @return result in a ResultSet
	 */
	public ResultSet first()
	{
		String query = "select "+selection+" from `"+table+"` "+join+" "+clauses+" "+groupBy+" "+having+" "+orderBy+" limit 1 "+offset+" "+lock;
		query = sloveAmbiguousColumn(query);
		if(union != null){
			String otherQuery = "select "+union.selection+" from "+union.table+" "+union.join+" "+union.clauses+" "+union.groupBy+" "
									+union.having+" "+union.orderBy+" "+union.limit+" "+union.offset+" "+union.lock;
			otherQuery = sloveAmbiguousColumn(otherQuery);
			query = "( "+query+" ) union ( "+otherQuery+" ) limit 1";
		}
		
		return DB.select(query.trim(), params.toArray());
	}
	
	
	/**
	 * Retrieve a single row of query
	 * @return result in a Map<String,Object> object, where keys are column name
	 */
	public Map<String,Object> firstMap()
	{
		try{
			ResultSet resultSet = first();
			Map<String, Object> map = new HashMap<String, Object>();
			if(resultSet.next())
			{
				ResultSetMetaData metaData = resultSet.getMetaData();
				for(int i=1, length = metaData.getColumnCount(); i <= length; i++)
				{
					map.put(metaData.getColumnName(i), resultSet.getObject(i));
				}
			}
			
			return map;
			
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	/**
	 * Retrieve the values of a single column
	 * @param column the name of column
	 * @return ResultSet of collection containing the values of a single column
	 */
	public ResultSet pluck(String column)
	{
		String query = "select `"+column+"` from `"+table+"` "+join+" "+clauses+" "+groupBy+" "+having+" "+orderBy+" "+limit +" "+ offset+" "+lock;
		query = sloveAmbiguousColumn(query);
		if(union != null){
			String otherQuery = "select "+union.selection+" from "+union.table+" "+union.join+" "+union.clauses+" "+union.groupBy+" "
									+union.having+" "+union.orderBy+" "+union.limit+" "+union.offset+" "+union.lock;
			otherQuery = sloveAmbiguousColumn(otherQuery);
			query = "( "+query+" ) union ( "+otherQuery+" ) limit 1";
		}
		return DB.select(query.trim(), params.toArray());
	}
	
	
	/**
	 * Retrieve the values of a single column
	 * @param column the name of column
	 * @return java.util.List of collection containing the values of a single column
	 */
	public List<Object> pluckList(String column)
	{
		try {
			ResultSet resultSet = pluck(column);
			List<Object> list = new ArrayList<Object>();
			while(resultSet.next())
			{
				list.add(resultSet.getObject(column));
			}
			
			return list;
			
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	
	/**
	 * If you don't even need an entire row, you may extract a single value from a record of single row using the value method.
	 * This method will return the value of the column directly
	 * @param column
	 * @return the value of the column
	 */
	public Object value(String column)
	{	
		selection = column;
		ResultSet resultSet = first();
		try {
			if(resultSet.next()){
				return resultSet.getObject(column);
			}else{
				return null;
			}
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	
	/**
	 * This method count rows of a record
	 * @return number of rows
	 */
	public long count()
	{
		selection = "count(*) as aggregate";
		ResultSet resultSet = get();
		try {
			if(resultSet.next())
				return resultSet.getLong("aggregate");
			else
				return 0;
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
		
	}
	
	
	/**
	 * This method calculate the maximal value of a column of a record,
	 * or null there is nothing to calculate on, a null record for example
	 * @param column must be numeric type
	 * @return maximal value of rows
	 */
	public Double max(String column)
	{
		selection = "max(`"+column+"`) as aggregate";
		ResultSet resultSet = get();
		try {
			if(resultSet.next())
				return resultSet.getDouble("aggregate");
			else
				return null;
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	
	/**
	 * This method calculate the minimal value of a column of a record,
	 * or null there is nothing to calculate on, a null record for example
	 * @param column must be numeric type
	 * @return minimal of rows
	 */
	public Double min(String column)
	{
		selection = "min(`"+column+"`) as aggregate";
		ResultSet resultSet = get();
		try {
			if(resultSet.next())
				return resultSet.getDouble("aggregate");
			else
				return null;
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	
	/**
	 * This method calculate the average value of a column of a record,
	 * or null there is nothing to calculate on, a null record for example
	 * @param column must be numeric type
	 * @return average of rows
	 */
	public Double avg(String column)
	{
		selection = "avg(`"+column+"`) as aggregate";
		ResultSet resultSet = get();
		try {
			if(resultSet.next())
				return resultSet.getDouble("aggregate");
			else
				return null;
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	
	/**
	 * This method calculate the sum of a column of a record,
	 * if the record is null, it will return 0
	 * @param column must be numeric type
	 * @return sum of rows
	 */
	public double sum(String column)
	{
		selection = "sum(`"+column+"`) as aggregate";
		ResultSet resultSet = get();
		try {
			if(resultSet.next())
				return resultSet.getDouble("aggregate");
			else
				return 0;
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}
	
	
	/**
	 * Method for inserting records into the database table. 
	 * The insert method accepts a map of column names and values
	 * @param arg map of column names and values
	 */
	public void insert(Map<String,Object> arg)
	{
		if(arg == null){
			throw new LadaljaException("Null argument list");
		}
		
		params = new ArrayList<Object>();
		String columns = "(";
		String values = "(";
		for(String key : arg.keySet()){
			columns += "`"+ key +"`,";
			values += "?,";
			params.add( arg.get(key) );
		}
		columns = columns.substring(0, columns.length()-1) + ")";
		values = values.substring(0, values.length()-1) + ")";
		String query = "insert into `" +table+ "` "+columns+" values "+values+" ;";
		query = sloveAmbiguousColumn(query);
		
		DB.insert(query.trim(), params.toArray());
		
	}
	
	
	/**
	 * If the table has an auto-incrementing id, 
	 * use the insertGetId method to insert a record and then retrieve the ID
	 * @param arg map of column names and values
	 * @return The ID of new inserted row
	 */
	public Object insertGetId(Map<String,Object> arg)
	{
		if(arg == null){
			throw new LadaljaException("Null argument list");
		}
		
		String columns = "(";
		String values = "(";
		for(String key : arg.keySet()){
			columns += "`"+ key +"`,";
			if( (arg.get(key) != null) ){
				if( (arg.get(key) instanceof Boolean) )
					arg.put(key, (Boolean)arg.get(key) ? 1:0 );
				else
					arg.put(key, arg.get(key).toString());
			}
			
			if( (arg.get(key) instanceof String) ){
				arg.put(key, "'"+arg.get(key)+"'");
			}

			values += arg.get(key)+",";
		}
		columns = columns.substring(0, columns.length()-1) + ")";
		values = values.substring(0, values.length()-1) + ")";
		String query = "insert into `" +table+ "` "+columns+" values "+values+" ;";
		query = sloveAmbiguousColumn(query);
		
		return DB.insertGetId(query.trim());
	}
	
	
	/**
	 *  Update existing records using the update method. 
	 *  The update method, like the insert method, accepts an array of column and value pairs containing the columns to be updated. 
	 *  You may constrain the  update query using where clauses
	 * @see io.github.mezatsong.ladalja.query.QueryBuilder#insert(Map)
	 * @param arg the Map containing data
	 */
	public void update(Map<String,Object> arg)
	{
		if(arg == null){
			throw new LadaljaException("Null argument list");
		}
		
		List<Object> params2 = new ArrayList<Object>();
		String columnsValues = "";
		for(String key : arg.keySet()){
			columnsValues += " `"+ key +"` = ?,";
			params2.add( arg.get(key) );
		}
		for(Object param : params){
			params2.add(param);
		}
		columnsValues = columnsValues.substring(0, columnsValues.length()-1);
		String query = "update `" +table+ "` set "+columnsValues+ " "+clauses;
		query = sloveAmbiguousColumn(query);
		
		DB.update(query.trim(), params2.toArray());
		
	}
	
	
	/**
	 * Delete records from the table via the delete method. 
	 * You may constrain delete statements by adding where clauses before calling the delete method
	 */
	public void delete()
	{
		String query = "delete from `" +table+ "` "+clauses;
		query = sloveAmbiguousColumn(query);
		
		DB.delete(query.trim(), params.toArray());
		
	}

	
	/**
	 * Method for incrementing the value of a given column. 
	 * This is simply a shortcut, providing a more expressive and terse interface compared to manually writing the update statement.
	 *
	 * This methods accept only one argument: the column to modify. 
	 * the amount by which the column will be incremented is 1
	 * @param column
	 */
	public void increment(String column)
	{
		increment(column,1);
	}
	
	
	/**
	 * Method for incrementing the value of a given column. 
	 * This is simply a shortcut, providing a more expressive and terse interface compared to manually writing the update statement.
	 *
	 * This methods accept at least one argument: the column to modify. 
	 * A second argument passed to control the amount by which the column should be incremented
	 * @param column
	 * @param supplement the amount by which the column should be incremented
	 */
	public void increment(String column, int supplement)
	{
		String query = "update `" +table+ "` set `"+column+"` = `"+column+"` + "+supplement+" "+clauses;
		query = sloveAmbiguousColumn(query);
		
		DB.update(query.trim(), params.toArray());
	}
	
	
	/**
	 * Method for decrementing the value of a given column. 
	 * This is simply a shortcut, providing a more expressive and terse interface compared to manually writing the update statement.
	 *
	 * This methods accept one argument: the column to modify. 
 	 * The amount by which the column will be decremented is 1
	 * @param column
	 */
	public void decrement(String column)
	{
		decrement(column,1);
	}
	
	
	/**
	 * Method for decrementing the value of a given column. 
	 * This is simply a shortcut, providing a more expressive and terse interface compared to manually writing the update statement.
	 *
	 * This methods accept two arguments, one: the column to modify. 
	 * A second argument passed to control the amount by which the column should be decremented
	 * @param column
	 * @param reduction the amount by which the column should be decremented
	 */
	public void decrement(String column, int reduction)
	{
		String query = "update `" +table+ "` set `"+column+"` = `"+column+"` - "+reduction+" "+clauses;
		query = sloveAmbiguousColumn(query);
		
		DB.update(query.trim(), params.toArray());
	}
	
	
	/**
	 * Use this method to truncate the entire table, which will remove all rows and reset the auto-incrementing ID to zero
	 */
	public void truncate()
	{
		DB.statement("truncate "+table);
	}
	
	
	
	/* ------------- SELECTION METHODS -------------- */
	
	/**
	 * Using the select method, you can specify a custom select clause for the query
	 * @param firstColumn
	 * @param otherColumns (optional)
	 * @return this object with new changes
	 */
	public T select(String firstColumn, String... otherColumns)
	{
		if(selection.contains("distinct"))
			selection = "distinct ";
		else
			selection = "";
		selection += "`"+firstColumn+"`";
		if(otherColumns != null)
		{
			for(String column : otherColumns){
				selection += ", `"+column+"`";
			}
		}
		return (T) this;
	}
	
	/**
	 * If you already have a query builder instance and you wish to add a column to its existing select clause, 
	 * you may use the addSelect method
	 * @param column
	 * @return this object with new changes
	 */
	public T addSelect(String column)
	{
		selection += ", `"+column+"`";
		return (T) this;
	}
	
	
	/**
	 * The distinct method allows you to force the query to return distinct results
	 * @return this object with new changes
	 */
	public T distinct()
	{
		selection = "distinct "+selection;
		return (T) this;
	}
	
	
	
	/* ------------- CLAUSALE METHODS --------------- */

	/**
	 * You may use the where method on a query builder instance to add where clauses to the query. 
	 * The most basic call to where requires three arguments. The first argument is the name of the column. 
	 * The second argument is an operator, which can be any of the database's supported operators. 
	 * Finally, the third argument is the value to evaluate against the column.
	 * @param column
	 * @param operator
	 * @param value
	 * @return this object with new changes
	 */
	public T where(String column, String operator, Object value)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " `"+column+"` "+operator+" ? ";
		params.add(value);
		return (T) this;
	}
	
	
	/**
	 * Aliases for where(column, "=" ,value)
	 * @param column
	 * @param value
	 * @return this object with new changes
	 */
	public T where(String column, Object value)
	{
		return where(column, "=" ,value);
	}
	
	
	/**
	 * Aliases for where("id",value)
	 * @param id the value
	 * @return this object with new changes
	 */
	public T whereId(long id)
	{
		return where("id",id);
	}
	
	/**
	 * Aliases for where(column, "like" ,value)
	 * @param column
	 * @param value
	 * @return this object with new changes
	 */
	public T whereLike(String column, String value)
	{
		return where(column, "like" ,value);
	}
	

	/**
	 * Aliases for orWhere(column, "=" ,value)
	 * @param column
	 * @param value
	 * @return this object with new changes
	 */
	public T orWhere(String column, Object value)
	{
		return orWhere(column,"=",value);
	}
	
	
	
	/**
	 * You may chain where constraints together as well as add or clauses to the query. 
	 * The orWhere method accepts the same arguments as the where method
	 * @param column
	 * @param value
	 * @return this object with new changes
	 */
	public T orWhere(String column, String operator, Object value)
	{
		clauses += " or `"+column+"` "+operator+" ? ";
		params.add(value);
		return (T) this;
	}
	
	
	/**
	 * The whereBetween method verifies that a column's value is between two values, min and max
	 * @param column
	 * @param min
	 * @param max
	 * @return this object with new changes
	 */
	public T whereBetween(String column, int min, int max)
	{
		return whereBetween(column, (double)min, (double)max);
	}
	
	/**
	 * The whereBetween method verifies that a column's value is between two values, min and max
	 * @param column
	 * @param min
	 * @param max
	 * @return this object with new changes
	 */
	public T whereBetween(String column, long min, long max)
	{
		return whereBetween(column, (double)min, (double)max);
	}
	
	
	/**
	 * The whereBetween method verifies that a column's value is between two values, min and max
	 * @param column
	 * @param min
	 * @param max
	 * @return this object with new changes
	 */
	public T whereBetween(String column, double min, double max)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " `"+column+"` between "+min+" and "+max+" ";
		return (T) this;
	}
	
	
	/**
	 * The whereNotBetween method verifies that a column's value lies outside of two values, min and max
	 * @param column
	 * @param min
	 * @param max
	 * @return this object with new changes
	 */
	public T whereNotBetween(String column, int min, int max)
	{
		return whereNotBetween(column, (double)min, (double)max);
	}
	
	
	/**
	 * The whereNotBetween method verifies that a column's value lies outside of two values, min and max
	 * @param column
	 * @param min
	 * @param max
	 * @return this object with new changes
	 */
	public T whereNotBetween(String column, long min, long max)
	{
		return whereNotBetween(column, (double)min, (double)max);
	}
	
	
	
	/**
	 * The whereNotBetween method verifies that a column's value lies outside of two values, min and max
	 * @param column
	 * @param min
	 * @param max
	 * @return this object with new changes
	 */
	public T whereNotBetween(String column, double min, double max)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " `"+column+"` not between "+min+" and "+max+" ";
		return (T) this;
	}

	
	/**
	 * The whereIn method verifies that a given column's value is contained within the given array
	 * @param column
	 * @param values
	 * @return this object with new changes
	 */
	public T whereIn(String column, Object[] values)
	{
		if(values == null || values.length < 1)
		{
			if( !clauses.isEmpty() ){
				clauses += " and";
			}else{
				clauses = " where ";
			}
			clauses += " 0 = 1 ";
			return (T) this;
		}
		
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		String intervale = "()";
		if(values != null && values.length > 0){
			intervale = "('"+values[0]+"'";
			for(int i=1; i<values.length; i++){
				intervale += ",'"+values[i]+"'";
			}
			intervale += ")";
		}
			
		clauses += " `"+column+"` in "+ intervale +" ";
		return (T) this;
	}

	
	
	/**
	 * The whereNotIn method verifies that the given column's value is not contained in the given array
	 * @param column
	 * @param values
	 * @return this object with new changes
	 */
	public T whereNotIn(String column, Object[] values)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		String intervale = "()";
		if(values != null && values.length > 0){
			intervale = "('"+values[0]+"'";
			for(int i=1; i<values.length; i++){
				intervale += ",'"+values[i]+"'";
			}
			intervale += ")";
		}
			
		clauses += " `"+column+"` not in "+ intervale +" ";
		return (T) this;
	}

	

	/**
	 * The whereNull method verifies that the value of the given column is NULL
	 * @param column
	 * @return this object with new changes
	 */
	public T whereNull(String column)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " `"+column+"` is null ";
		return (T) this;
	}
	
	
	/**
	 * The whereNoteNull method verifies that the value of the given column is not NULL
	 * @param column
	 * @return this object with new changes
	 */
	public T whereNotNull(String column)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " `"+column+"` is not null ";
		return (T) this;
	}
	
	
	/**
	 * Aliases for whereDate(column,"=",date)
	 * @param column
	 * @param date
	 * @return this object with new changes
	 */
	public T whereDate(String column, Date date)
	{
		return whereDate(column, "=", date);
	}
	
	
	/**
	 * The whereDate method may be used to compare a column's value against a date
	 * @param column
	 * @param operator
	 * @param date
	 * @return this object with new changes
	 */
	public T whereDate(String column, String operator, Date date)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " date(`"+column+"`) "+operator+"  ? ";
		params.add(date.toString());
		return (T) this;
	}

	
	/**
	 * Aliases for whereYear(column, "=" ,year)
	 * @param column
	 * @param year
	 * @return this object with new changes
	 */
	public T whereYear(String column, int year)
	{
		return whereYear(column, "=" ,year);
	}

	
	/**
	 * The whereMonth method may be used to compare a column's value against a specific month of a year
	 * @param column
	 * @param operator
	 * @param year
	 * @return this object with new changes
	 */
	public T whereYear(String column, String operator, int year)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " year(`"+column+"`) "+operator+"  "+year;
		return (T) this;
	}


	/**
	 * Aliases for whereMonth(column, "=" ,month)
	 * @param column
	 * @param month
	 * @return this object with new changes
	 */
	public T whereMonth(String column, int month)
	{
		return whereMonth(column, "=" ,month);
	}
	
	
	/**
	 * The whereMonth method may be used to compare a column's value against a specific month of a year
	 * @param column
	 * @param operator
	 * @param month
	 * @return this object with new changes
	 */
	public T whereMonth(String column, String operator, int month)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " month(`"+column+"`) "+operator+"  "+month;
		return (T) this;
	}
	
	/**
	 * Aliases for whereDay(column, "=", day)
	 * @param column
	 * @param day
	 * @return this object with new changes
	 */
	public T whereDay(String column, int day)
	{
		return whereDay(column, "=", day);
	}
	
	/**
	 * The whereDay method may be used to compare a column's value against a specific day of a month
	 * @param column
	 * @param operator
	 * @param day
	 * @return this object with new changes
	 */
	public T whereDay(String column, String operator, int day)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " day(`"+column+"`) "+operator+"  "+day;
		return (T) this;
	}

	
	/**
	 * The whereColumn method may be used to verify that two columns are equal
	 * @param column1
	 * @param column2
	 * @return this object with new changes
	 */
	public T whereColumn(String column1, String column2)
	{
		return whereColumn(column1,"=",column2);
	}

	
	/**
	 * The whereColumn method may be used to compare that two columns
	 * You have to pass a comparison operator to the method
	 * @param column1
	 * @param operator
	 * @param column2
	 * @return this object with new changes
	 */
	public T whereColumn(String column1, String operator, String column2)
	{
		if( !clauses.isEmpty() ){
			clauses += " and";
		}else{
			clauses = " where ";
		}
		clauses += " `"+column1+"` "+operator+"  `"+column2+"` ";
		return (T) this;
	}
	
	/**
	 * Aliases for whereHas(column,1)
	 * @param column
	 * @return this object with new changes
	 */
	public T whereHas(String column)
	{
		return where(column,">=",1);
	}
	
	/**
	 * Aliases for where(column,">=",number)
	 * @param column
	 * @param number
	 * @return this object with new changes
	 */
	public T whereHas(String column, double number)
	{
		return where(column,number);
	}
	
	/**
	 * Aliases for where(column,">=",number)
	 * @param column
	 * @param number
	 * @return this object with new changes
	 */
	public T whereHas(String column, int number)
	{
		return whereHas(column, (double) number);
	}
	
	/**
	 * Aliases for where(column,">=",number)
	 * @param column
	 * @param number
	 * @return this object with new changes
	 */
	public T whereHas(String column, long number)
	{
		return whereHas(column, (double) number);
	}
	
	/**
	 * Aliases for 
	 * @param column
	 * @return this object with new changes
	 */
	public T whereDoesntHave(String column)
	{
		return whereHas(column, 0);
	}
	
	/* -------------POST CLAUSALE METHODS --------------- */
	
	/**
	 * The groupBy method may be used to group the query result
	 * @param firstColumn
	 * @param otherColumns
	 * @return this object with new changes
	 */
	public T groupBy(String firstColumn, String... otherColumns)
	{
		String columns = "`"+firstColumn+"`";
		if(otherColumns != null && otherColumns.length > 0){
			for(int i=0; i<otherColumns.length; i++){
				columns += ",`"+otherColumns[i]+"`";
			}
		}
		
		groupBy = "group by "+ columns +" ";
		return (T) this;
	}
	
	/**
	 * Aliases for orderBy(column,"asc")
	 * @param column
	 * @return this object with new changes
	 */
	public T orderBy(String column)
	{
		return (T) orderBy(column,"asc");
	}
	
	
	/**
	 * The orderBy method allows you to sort the result of the query by a given column. 
	 * The first argument to the orderBy method should be the column you wish to sort by, 
	 * while the second argument controls the direction of the sort and may be either asc or desc
	 * @param column
	 * @return this object with new changes
	 */
	public T orderBy(String column, String order)
	{
		String cls = " order by ";
		if(column.contains("()")){
			cls += column;
		}else{
			cls += "`"+column+"` ";
		}
		
		orderBy += cls + " "+order;
		return (T) this;
	}
	
	
	/**
	 * Sort the result of the query by a given column in descending order. 
	 * @param column
	 * @return this object with new changes
	 */
	public T orderByDesc(String column)
	{
		return latest(column);
	}
	
	/**
	 * The latest methods allow you to easily order results by date column. 
	 * You have to pass the column name that you wish to sort by, latest before
	 * @param column
	 * @return this object with new changes
	 */
	public T latest(String column)
	{
		return orderBy(column,"desc");
	}
	
	/**
	 * The latest methods allow you to easily order results by date column. 
	 * You have to pass the column name that you wish to sort by, oldest before
	 * @param column
	 * @return this object with new changes
	 */
	public T oldest(String column)
	{
		return orderBy(column,"asc");
	}
	
	/**
	 * The inRandomOrder method may be used to sort the query results randomly.
	 * @return this object with new changes
	 */
	public T inRandomOrder()
	{
		return orderBy("RAND()");
	}
	
	/**
	 * The having methods may be used to group the query result
	 *  The having method's signature is similar to that of the where method
	 * @see io.github.mezatsong.ladalja.query.QueryBuilder#where(java.lang.String,java.lang.String,java.lang.Object) method
	 * @param column
	 * @param operator
	 * @param value
	 * @return this object with new changes
	 */
	public T having(String column, String operator, String value)
	{
		if(having.isEmpty()){
			having = "having";
		}else{
			having += " and";
		}
		having += " `"+column+"` "+operator+ " ? ";
		params.add(value);
		return (T) this;
	}
	
	
	/**
	 * The havingRaw method may be used to set a raw string as the value of the having clause.
	 * @param havingClause
	 * @return this object with new changes
	 */
	public T havingRaw(String havingClause)
	{
		having = havingClause;
		return (T) this;
	}
	
	
	/**
	 * Skip a given number of results in the query.
	 * @param arg
	 * @return this object with new changes
	 */
	public T skip(int arg)
	{
		return offset(arg);
	}
	
	
	/**
	 * To limit the number of results returned from the query.
	 * @param arg
	 * @return this object with new changes
	 */
	public T take(int arg)
	{
		return limit(arg);
	}
	
	/**
	 * Alternative for skip method
	 * @see io.github.mezatsong.ladalja.query.QueryBuilder#skip(int)
	 * @param arg this object with new changes
	 * @return this object with new changes
	 */
	public T offset(int arg)
	{
		offset = " offset "+arg;
		return (T) this;
	}
	
	/**
	 * Alternative for take method
	 * @param arg
	 * @return this object with new changes
	 */
	public T limit(int arg)
	{
		limit = " limit "+arg;
		return (T) this;
	}
	
	
	/**
	 * The query builder also includes a few functions to help you do "pessimistic locking" on your select statements. 
	 * To run the statement with a "shared lock", you may use the sharedLock method on a query. 
	 * A shared lock prevents the selected rows from being modified until your transaction commits
	 * @return this object with new changes
	 */
	public T sharedLock()
	{
		lock = " lock in share mode ";
		return (T) this;
	}
	
	/**
	 * sharedLock alternative for using the "for update" lock. 
	 * A "for update" lock prevents the rows from being modified or from being selected with another shared lock
	 * @return this object with new changes
	 */
	public T lockForUpdate()
	{
		lock = " for update ";
		return (T) this;
	}
	
	
	
	/* ------------------ JOINS AND UNIONS METHODS ------------------- */
	
	/**
	 * The query builder may also be used to write join statements. 
	 * To perform a basic "inner join", you may use the join method on a query builder instance. 
	 * The first argument passed to the join method is the name of the table you need to join to, 
	 * while the remaining arguments specify the column constraints for the join. 
	 * Of course, you can join at most two tables in a single query
	 * @param joinTable
	 * @param column
	 * @param operator
	 * @param joinColumn
	 * @return this object with new changes
	 */
	public T join(String joinTable, String column, String operator, String joinColumn)
	{
		join = " inner join `"+joinTable+"` on `"+column+"` "+operator+" `"+joinColumn+"` ";
		return (T) this;
	}
	
	/**
	 * If you would like to perform a "left join" instead of an "inner join", use the leftJoin method. 
	 * The  leftJoin method has the same signature as the join method
	 * @param joinTable
	 * @param column
	 * @param operator
	 * @param joinColumn
	 * @return this object with new changes
	 */
	public T leftJoin(String joinTable, String column, String operator, String joinColumn)
	{
		join = " left join `"+joinTable+"` on `"+column+"` "+operator+" `"+joinColumn+"` ";
		return (T) this;
	}
	
	/**
	 * To perform a "cross join" use the crossJoin method with the name of the table you wish to cross join to. 
	 * Cross joins generate a cartesian product between the first table and the joined table
	 * @param joinTable
	 * @return this object with new changes
	 */
	public T crossJoin(String joinTable)
	{
		join = " cross join `"+joinTable+"` ";
		return (T) this;
	}
	
	
	/**
	 * The query builder also provides a quick way to "union" two queries together. 
	 * For example, you may create an initial query and use the union method to union it with a second query
	 * @param anOtherQuery
	 * @return this object with new changes
	 */
	public T union(T anOtherQuery)
	{
		union = anOtherQuery;
		return (T) this;
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Take a query and re-organize it, for exemple, it will make all `user.name` like `user`.`name`
	 * @param query
	 * @return unambiguous query string 
	 */
	protected String sloveAmbiguousColumn(String query)
	{
		char tab[] = query.trim().toCharArray();
		char solved[] = new char[ tab.length*3 ];
		int pos = -1;
		int j=0;
		for(int i=0; i<tab.length; i++)
		{
			if (tab[i] == '`')
			{
				solved[j++] = tab[i];
				if(pos >= 0){
					pos = -1;
				}else{
					pos = i;
				}
			}else if(tab[i] == '.' && pos >= 0){
				solved[j++] = '`';
				solved[j++] = '.';
				solved[j++] = '`';
			}else{
				solved[j++] = tab[i];
			}
		}
		
		return String.valueOf(solved).trim().replaceAll("\\s+", " ");
	}
	
}
