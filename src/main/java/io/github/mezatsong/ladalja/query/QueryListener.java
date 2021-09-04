/**
 * 
 */
package io.github.mezatsong.ladalja.query;

import java.sql.ResultSet;

/**
 * This interface provide only one method for listening every SQL query through Ladalja.
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 */
public interface QueryListener {
	
	/**
	 * This method will be called before each query execution
	 * @param query which will be executed
	 */
	void listenQuery(String query);
	
	/**
	 * This method will be called after each select query execution
	 * @param query which will be executed
	 */
	void listenResultSet(String query, ResultSet result);
	
	/**
	 * This method will be called after each insert, update and delete query execution
	 * @param query which will be executed
	 */
	void listenUpdatedRows(String query, int rows);
}
