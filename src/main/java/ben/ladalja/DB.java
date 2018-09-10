/**
 * 
 */
package ben.ladalja;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Ladalja makes interacting with databases extremely simple across a variety of database backends using either raw SQL, the fluent query builder, and the model.<br>
 * DB is which one you will need if you do not use models<br>
 * Thus is you want to write raw SQL or using {@link ben.ladalja.QueryBuilder}.<br>
 * <p>
 * Before do any thing you will need to indicate where Ladalja config file is located, and in that file your config must look like:<br>
 * <p>
 * 	DB_DRIVER= driver_for_JDBC<br>
 *	DB_CONNECTION=database_connection_type<br>
 *	DB_HOST=host_where_is_located_database<br>
 *	DB_PORT=port<br>
 *	DB_DATABASE=database_name<br>
 *	DB_USERNAME=user_name_for_database<br>
 *	DB_PASSWORD=password_for_above_user_name<br>
 * <p>
 * All those properties must be present
 * to indicated, use the static public field of DB class named CONFIG_FILE_URL
 * like<br> 
 * DB.CONFIG_FILE_URL = "url_to_config_file"
 * <p>
 * after that you can use DB for anythings, for example if you want a raw select SQL query do like
 * DB.select("select * from users"); and it will return you a java.sql.ResultSet containing your query result.
 * There is method for any type of Query select,insert,update and SQL like ALTER TABLE for this last use {@link ben.ladalja.DB#statement(String)} method
 * In all request, the transaction process is implemented, but you can if you want do it by yourself using 
 * {@link ben.ladalja.DB#beginTransaction()} , {@link ben.ladalja.DB#commit()} and {@link ben.ladalja.DB#rollBack()} methods
 * <p>
 * If you want to start request using QueryBuilder, you just have to use {@link ben.ladalja.DB#table(String)} method where the String 
 * parameter is the name of the table on which request will proceed, a new instance of {@link ben.ladalja.QueryBuilder} will be returned.
 * <p>
 * The is also something to listen each query Ladalja make through {@link ben.ladalja.DB#register(ben.ladalja.QueryListener)} .
 * 
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 * 
 */
public final class DB {

	private static Connection connect;
	private static List<QueryListener> queryListeners;
	public static String CONFIG_FILE_URL;
	
	private static boolean transactional = true;
	
	private DB(){}
	
	
	/**
	 * Make new instance of {@link ben.ladalja.QueryBuilder}
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
	 * @throws ben.ladalja.LadaljaException if there is error in configuration file while loading
	 */
	public static Connection connection() throws LadaljaException
	{
		if( connect == null)
		{
			
			Properties properties = new Properties();
			String driver;
			String connectionType;
			String host;
			String port;
			String database;
			String username;
			String password;
			boolean isSQLite = false;
			
			try {
				URL urlToConfigFile = new URL(CONFIG_FILE_URL);
				properties.load( urlToConfigFile.openStream() );
				driver = properties.getProperty( "DB_DRIVER" );
				connectionType = properties.getProperty( "DB_CONNECTION" );
				host = properties.getProperty( "DB_HOST" );
				port = properties.getProperty( "DB_PORT" );
				database = properties.getProperty( "DB_DATABASE" );
				username = properties.getProperty( "DB_USERNAME" );
				password = properties.getProperty( "DB_PASSWORD" );
				isSQLite = connectionType.toLowerCase().equals("sqlite");
			} catch (IOException e) {
				throw new LadaljaException("Can't load config file: " + CONFIG_FILE_URL, e );
			}
			
			 String url = "jdbc:"+connectionType+"://"+host+":"+port+"/"+database;
			 
			 if(isSQLite){
				 url = "jdbc:sqlite:"+database;
				 try {
					connect = DriverManager.getConnection(url);
				} catch (SQLException e) {
					throw new LadaljaException( e );
				}
			 }else{
			
				try {
					Class.forName( driver );
					connect = DriverManager.getConnection(url, username, password);
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
	 * Execute a SELECTs SQL query type, using prepared statement.
	 * @param query the query, eventually with parameters
	 * @param params list of query parameters
	 * @return ResultSet the result of query
	 * @throws ben.ladalja.LadaljaException if query is not SELECT type
	 * 
	 */
	public static ResultSet select(String query, Object... params) throws LadaljaException
	{
		if(!query.toLowerCase().contains("select")){
			throw new LadaljaException("using non select sql query type in select method");
		}
		return (ResultSet) execute(query,params);
	}
	
	
	/**
	 * Execute an INSERTs SQL query type.
	 * @param query the query, eventually with parameters
	 * @param params list of query parameters
	 * @return number of affected rows
	 * @throws ben.ladalja.LadaljaException if query is not INSERT type
	 */
	public static int insert(String query, Object... params) throws LadaljaException
	{
		if(!query.toLowerCase().contains("insert")){
			throw new LadaljaException("using non insert sql query type in insert method");
		}
		return (Integer) execute(query,params);
	}
	
	
	/**
	 * Insert a new row an return the ID of the new inserted row through non prepared statement
	 * The column named id must be present in the table
	 * @param query the query, eventually with parameters
	 * @return ID of new inserted row
	 * @throws ben.ladalja.LadaljaException if query is not SELECT type or id column doesn't exist
	 */
	public static long insertGetId(String query) throws LadaljaException
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
			
			listen(query);
			
			int statut = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			
			long result = 0;
			ResultSet rs = statement.getGeneratedKeys();
			if(statut > 0 && rs.next()){
				result = rs.getLong(1);
			}else{
				throw new LadaljaException("Insertion failled, please check your database constraints");
			}
					
			if(began){
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
	 * @throws ben.ladalja.LadaljaException if query is not UPDATE type
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
	 * @throws ben.ladalja.LadaljaException if query is not DELETE type
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
	 * @throws ben.ladalja.LadaljaException if there are error in your query
	 */
	public static void statement(String query) throws LadaljaException
	{
		listen(query);
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
	 * Register a {@link ben.ladalja.QueryListener} to DB for listening each query before execute it.
	 * @param queryListener {@link ben.ladalja.QueryListener}  to register
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
		String updateWord[] = {"DELETE","UPDATE","INSERT"};
		PreparedStatement statement = null;
		try {
			for(String str: updateWord){
				if(query.trim().replaceAll("\\s+", " ").split(" ")[0].toLowerCase().contains(str.toLowerCase())){
					update = true;
				}
			}
			
			statement = connection().prepareStatement(query);
			
			if(params != null){
				for(int i=0; i<params.length; i++){
					statement.setObject(i+1, params[i]);
				}
			}
			
			listen(query);
			
			Object obj = null;
			boolean began = false; 
			
			if(update){
				if(transactional){
					statement.execute("BEGIN;");
					began = true;
				}
				
				obj = new Integer(statement.executeUpdate());
				
				if(began){
					statement.execute("COMMIT;");
				}
			}else{
				obj = statement.executeQuery();
			}
		
			
			return obj;
		} catch (SQLException e) {
			if(statement != null && update)
				try {
					statement.execute("ROLLBACK;");
				} catch (SQLException e1) {}
			throw new LadaljaException(e);
		}

	}
	

	private static void listen(String query)
	{
		if(queryListeners != null)
		{
			for(QueryListener ql : queryListeners){
				ql.listen(query);
			}
		}
	}
	
	
}
