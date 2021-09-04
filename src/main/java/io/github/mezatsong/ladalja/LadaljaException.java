/**
 * 
 */
package io.github.mezatsong.ladalja;


/**
 * Custom error class for Ladalja
 * If you want to catch a {@link io.github.mezatsong.ladalja.LadaljaException} , catch RuntimeException instead
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 *
 */
 public class LadaljaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LadaljaException() {}

	
	public LadaljaException(String arg0) {
		super(arg0);
	}

	
	public LadaljaException(Throwable arg0) {
		super(arg0);
	}

	
	public LadaljaException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}


}
