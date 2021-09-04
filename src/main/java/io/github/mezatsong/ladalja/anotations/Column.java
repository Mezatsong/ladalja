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
 * This annotation is used to indicate the name of column 
 * when the field do not have the same name with database column.<p>
 * For example, if you have a column named created_at, and you want your field to have createdAt name,
 * you will have to declare it like that :  <br>
 * <code>@Column</code>("created_at") java.sql.Timestamp createdAt; <br>
 * instead of<br> 
 * private java.sql.Timestamp createdAt; <br>
 * The value if required.
 * 
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 *
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Column {
	String value();
}
