/**
 * 
 */
package io.github.mezatsong.ladalja.anotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Fields that use this annotation will be ignored in the attributes list.<br>
 * For example, if you have a something like that
 * <p>
 * <pre>
 * public class Example {
 * 		private Long id;
 * 		private String firstName;
 * 		private String lastName;
 * 		<code>@Ignore</code> private String name;
 * }
 * </pre>
 * The attributes list will be id,firstName and lastName and name field will be completely ignored<br> 
 * when loading object from database or inserting object in database.
 * 
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 *
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Ignore {}
