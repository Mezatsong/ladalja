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
 class LadaljaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	LadaljaException() {}

	
	LadaljaException(String arg0) {
		super(arg0);
	}

	
	LadaljaException(Throwable arg0) {
		super(arg0);
	}

	
	LadaljaException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}


}
