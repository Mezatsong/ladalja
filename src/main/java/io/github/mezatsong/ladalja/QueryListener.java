/**
 * 
 */
package io.github.mezatsong.ladalja;

/**
 * This interface provide only one method for listening every SQL query through Ladalja.
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 */
public interface QueryListener {
	
	/**
	 * This method will be called before each query execution
	 * @param query which will be executed
	 */
	void listen(String query);
}
