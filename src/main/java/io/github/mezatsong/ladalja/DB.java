/**
 * 
 */
package io.github.mezatsong.ladalja;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import io.github.mezatsong.ladalja.query.QueryBuilder;
import io.github.mezatsong.ladalja.query.QueryListener;

/**
 * Ladalja makes interacting with databases extremely simple across a variety of database backends using either raw SQL, the fluent query builder, and the model.<br>
 * DB is which one you will need if you do not use models<br>
 * Thus is you want to write raw SQL or using {@link io.github.mezatsong.ladalja.query.QueryBuilder}.<br>
 * <p>
 * Before do any thing you will need to indicate where Ladalja config file is located, and in that file your config must look like:<br>
 * <p>
 * 	LADALJA_DRIVER= driver_for_JDBC<br>
 *	LADALJA_CONNECTION=jdbc_database_connection_type (default is sqlite)<br>
 *	LADALJA_HOST=host_where_is_located_database<br>
 *	LADALJA_PORT=port<br>
 *	LADALJA_DATABASE=database_name<br>
 *	LADALJA_USERNAME=user_name_for_database<br>
 *	LADALJA_PASSWORD=password_for_above_user_name<br>
 * <p>
 * Or just this line
 * <p>
 * LADALJA_JDBC_URL=jdbc_url<br>
 * </p>
 * You can also do without the configuration file by specifying these properties on system properties, using java.lang.System.setProperty method
 * All those properties must be present.<br>
 * In order to indicate these properties, 
 * use the static public field of DB class named CONFIG_FILE like<br> 
 * DB.CONFIG_FILE = "path to config file"
 * We will use ClassLoader.getSystemResourceAsStream(DB.CONFIG_FILE) to load it
 * <p>
 * after that you can use DB for anythings, for example if you want a raw select SQL query do like
 * DB.select("select * from users"); and it will return you a java.sql.ResultSet containing your query result.
 * There is method for any type of Query select,insert,update and SQL like ALTER TABLE for this last use {@link io.github.mezatsong.ladalja.DB#statement(String)} method
 * In all request, the transaction process is implemented, but you can if you want do it by yourself using 
 * {@link io.github.mezatsong.ladalja.DB#beginTransaction()} , {@link io.github.mezatsong.ladalja.DB#commit()} and {@link io.github.mezatsong.ladalja.DB#rollBack()} methods
 * <p>
 * If you want to start request using QueryBuilder, you just have to use {@link io.github.mezatsong.ladalja.DB#table(String)} method where the String 
 * parameter is the name of the table on which request will proceed, a new instance of {@link io.github.mezatsong.ladalja.query.QueryBuilder} will be returned.
 * <p>
 * The is also something to listen each query Ladalja make through {@link io.github.mezatsong.ladalja.DB#register(io.github.mezatsong.ladalja.query.QueryListener)} .
 * 
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 * 
 */
public final class DB {

	private static Connection connect;
	private static List<QueryListener> queryListeners;
	public static String CONFIG_FILE;

	private static boolean isInsertGetIdSupported = true;
	
	private static boolean transactional = true;
	
	private DB(){}
	
	
	/**
	 * Make new instance of {@link io.github.mezatsong.ladalja.query.QueryBuilder}
	 * @param tableName the name of the table on which request will proceed
	 * @return Return new instance of query builder for building query.
	 */
	@SuppressWarnings("rawtypes")
	public static QueryBuilder table(String tableName)
	{
		return new QueryBuilder<QueryBuilder>(tableName);
	}
	
	
	/**
	 * Provide the connection object to database, using the configuration file properties
	 * @return a java.sql.Connection instance which is used for querying, new instance will be loaded only the first time you call it or at the first querying
	 * @throws io.github.mezatsong.ladalja.LadaljaException if there is error in configuration file while loading
	 */
	public static Connection connection() throws LadaljaException
	{
		if(connect == null)
		{
			
			Properties properties = new Properties();
			
			String serverTimezone = Calendar.getInstance().getTimeZone().getID();
			
			try {
				properties.load( ClassLoader.getSystemResourceAsStream(CONFIG_FILE) );
			} catch (NullPointerException e) {
				properties = System.getProperties();
			} catch (IOException e) {
				throw new LadaljaException("Can't load config file: " + CONFIG_FILE, e );
			}

			String connectionType = properties.getProperty( "LADALJA_CONNECTION", "sqlite" );
			String driver = properties.getProperty( "LADALJA_DRIVER" );

			String host = properties.getProperty( "LADALJA_HOST" );
			String port = properties.getProperty( "LADALJA_PORT" );
			String database = properties.getProperty( "LADALJA_DATABASE" );
			String username = properties.getProperty( "LADALJA_USERNAME" );
			String password = properties.getProperty( "LADALJA_PASSWORD" );
			String jdbcUrl = properties.getProperty( "LADALJA_JDBC_URL", 
				"jdbc:" + connectionType + "://" + host + ":" + port + "/" + database + "?serverTimezone=" + serverTimezone 
			);
			boolean isSQLite = connectionType.toLowerCase().equals("sqlite");
			
			 
			if (isSQLite) {
				disableTransaction(); // SQLite does not support transaction
				isInsertGetIdSupported = false; // SQLite does not support insertGetId
				final String url = (jdbcUrl != null && !jdbcUrl.isEmpty()) ? jdbcUrl : "jdbc:sqlite:"+database;
				try {
					connect = DriverManager.getConnection(url);
				} catch (SQLException e) {
					throw new LadaljaException( e );
				}
			} else {
			
				try {
					Class.forName( driver );
					connect = DriverManager.getConnection(jdbcUrl, username, password);
				} catch ( ClassNotFoundException e ) {
					throw new LadaljaException("Can't find driver: " + driver, e );
				}
				 catch (SQLException e) {
					throw new LadaljaException(e);
				}
			}
		}
		
		return connect;
	}


	/**
	 * Close the connection object to database if exist
	 * @throws io.github.mezatsong.ladalja.LadaljaException if there is error while closing the connection
	 */
	public static void closeConnection() throws LadaljaException
	{
		try {
			if (connect != null && connect.isClosed()) {
				connect.close();
			}
		} catch (SQLException e) {
			throw new LadaljaException(e);
		}
	}

	
	/**
	 * Execute a SELECTs SQL query type, using prepared statement.
	 * @param query the query, eventually with parameters
	 * @param params list of query parameters
	 * @return ResultSet the result of query
	 * @throws io.github.mezatsong.ladalja.LadaljaException if query is not SELECT type
	 * 
	 */
	public static ResultSet select(String query, Object... params) throws LadaljaException
	{
		if(!query.toLowerCase().contains("select")){
			throw new LadaljaException("using non select sql query type in select method");
		}
		return (ResultSet) execute(query, params);
	}
	
	
	/**
	 * Execute an INSERTs SQL query type.
	 * @param query the query, eventually with parameters
	 * @param params list of query parameters
	 * @return number of affected rows
	 * @throws io.github.mezatsong.ladalja.LadaljaException if query is not INSERT type
	 */
	public static int insert(String query, Object... params) throws LadaljaException
	{
		if(!query.toLowerCase().contains("insert")){
			throw new LadaljaException("using non insert sql query type in insert method");
		}
		return (Integer) execute(query, params);
	}
	
	
	/**
	 * Insert a new row an return the ID of the new inserted row through non prepared statement
	 * The column named id must be present in the table
	 * @param query the query, eventually with parameters
	 * @return ID of new inserted row
	 * @throws io.github.mezatsong.ladalja.LadaljaException if query is not SELECT type or id column doesn't exist
	 */
	public static Object insertGetId(String query) throws LadaljaException
	{
		if(!query.toLowerCase().contains("insert")){
			throw new LadaljaException("using non insert sql query type in insertGetId method");
		}
		Statement statement = null;
		try{
			statement = connection().createStatement();
			boolean began = false;
			if(transactional){
				statement.execute("BEGIN;");
				began = true;
			}
			
			listenQuery(query);
			
			int statut = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

			listenUpdatedRows(query, statut);
			
			Object result = 0;
			ResultSet rs = statement.getGeneratedKeys();
			listenResultSet(query, rs);

			if (statut > 0 && rs.next()) {
				result = rs.getObject(1);
			} else {
				throw new LadaljaException("Insertion failled, please check your database constraints");
			}
					
			if (began) {
				statement.execute("COMMIT;");
			}
			
			return result;
			
		}catch(SQLException e){
			if(statement != null)
				try{
					statement.execute("ROLLBACK;");
				}catch(SQLException e1){}
			throw new LadaljaException(e);
		}
		
	}
	
	
	/**
	 * Execute an UPDATEs SQL query type.
	 * @param query the query, eventually with parameters
	 * @param params list of query parameters
	 * @return number of affected rows
	 * @throws io.github.mezatsong.ladalja.LadaljaException if query is not UPDATE type
	 */
	public static int update(String query, Object... params) throws LadaljaException
	{
		if(!query.toLowerCase().contains("update")){
			throw new LadaljaException("using non update sql query type in update method");
		}
		return (Integer) execute(query,params);
	}
	
	
	/**
	 * Execute a DELETEs SQL query type.
	 * @param query the query, eventually with parameters
	 * @param params list of query parameters
	 * @return number of affected rows
	 * @throws io.github.mezatsong.ladalja.LadaljaException if query is not DELETE type
	 */
	public static int delete(String query, Object... params) throws LadaljaException
	{
		if(!query.toLowerCase().contains("delete")){
			throw new LadaljaException("using non delete sql query type in delete method");
		}
		return (Integer) execute(query,params);
	}
	
	/**
	 * Execute a simple query, like ALTER TABLE, but don't use it for CRUDs query type.
	 * @param query the query to be executed
	 * @throws io.github.mezatsong.ladalja.LadaljaException if there are error in your query
	 */
	public static void statement(String query) throws LadaljaException
	{
		listenQuery(query);
		Statement statement = null;
		try{
			statement = connection().createStatement();
			boolean began = false;
			if(transactional){
				statement.execute("BEGIN;");
				began = true;
			}
			
			statement.execute(query);
			
			if(began){
				statement.execute("COMMIT;");
			}
		}catch(SQLException e){
			if(statement != null)
				try{
					statement.execute("ROLLBACK;");
				}catch(SQLException e1){}
			throw new LadaljaException(e);
		}
	}
	
	
	/**
	 * Register a {@link io.github.mezatsong.ladalja.query.QueryListener} to DB for listening each query before execute it.
	 * @param queryListener {@link io.github.mezatsong.ladalja.query.QueryListener}  to register
	 */
	public static void register(QueryListener queryListener)
	{
		if(queryListeners == null){
			queryListeners = new ArrayList<QueryListener>();
		}
		queryListeners.add(queryListener);
	}
	
	
	/**
	 * Begin new transaction manually
	 */
	public static void beginTransaction()
	{
		try{
			Statement statement = connection().createStatement();
			statement.execute("BEGIN;");
		}catch(SQLException e){
			
		}
	}
	
	
	/**
	 * Roll back transaction manually
	 */
	public static void rollBack()
	{
		try{
			Statement statement = connection().createStatement();
			statement.execute("ROLLBACK;");
		}catch(SQLException e){
			
		}
	}
	
	
	/**
	 * Commit transaction manually
	 */
	public static void commit()
	{
		try{
			Statement statement = connection().createStatement();
			statement.execute("COMMIT;");
		}catch(SQLException e){
			
		}
	}
	
	
	/**
	 * Check if transaction mode is enabled
	 * @return true if enabled
	 */
	public static boolean isTransactional()
	{
		return transactional;
	}

	
	/**
	 * Check if InsertGetId is supported
	 * @return true if enabled
	 */
	public static boolean isInsertGetIdSupported()
	{
		return isInsertGetIdSupported;
	}

	/**
	 * Enable transaction mode, it mean all query will be transactional
	 */
	public static void enableTransaction()
	{
		transactional = true;
	}
	
	/**
	 * Disable transaction mode
	 */
	public static void disableTransaction()
	{
		transactional = false;
	}
	
	
	private static Object execute(String query, Object... params)  throws LadaljaException
	{
		boolean update = false;
		String updateWord[] = {"DELETE", "UPDATE", "INSERT"};
		PreparedStatement statement = null;
		try {
			for(String str: updateWord){
				if (query.trim().replaceAll("\\s+", " ").split(" ")[0].toLowerCase().contains(str.toLowerCase())) {
					update = true;
				}
			}
			
			statement = connection().prepareStatement(query);
			
			if(params != null){
				for(int i=0; i<params.length; i++){
					statement.setObject(i+1, params[i]);
				}
			}
			
			listenQuery(query);
			
			Object obj = null;
			boolean began = false; 
			
			if (update) {
				if (transactional) {
					statement.execute("BEGIN;");
					began = true;
				}
				
				int rows = statement.executeUpdate();
				listenUpdatedRows(query, rows);
				obj = Integer.valueOf(rows);
				
				if (began) {
					statement.execute("COMMIT;");
				}
			} else {
				ResultSet result = statement.executeQuery();
				listenResultSet(query, result);
				obj = result;
			}
			
			return obj;
		} catch (SQLException e) {
			if (statement != null && update) {
				try {
					statement.execute("ROLLBACK;");
				} catch (SQLException e1) {}
			}
			throw new LadaljaException(e);
		}
	}
	

	private static void listenQuery(String query)
	{
		if (queryListeners != null) {
			for (QueryListener ql: queryListeners) {
				ql.listenQuery(query);
			}
		}
	}
	
	

	private static void listenResultSet(String query, ResultSet result)
	{
		if (queryListeners != null) {
			for (QueryListener ql: queryListeners) {
				ql.listenResultSet(query, result);
			}
		}
	}
	

	private static void listenUpdatedRows(String query, int rows)
	{
		if (queryListeners != null) {
			for (QueryListener ql: queryListeners) {
				ql.listenUpdatedRows(query, rows);
			}
		}
	}
	
	
}
