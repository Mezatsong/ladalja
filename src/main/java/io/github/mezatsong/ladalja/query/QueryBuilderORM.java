/**
 * 
 */
package io.github.mezatsong.ladalja.query;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import io.github.mezatsong.ladalja.LadaljaException;
import io.github.mezatsong.ladalja.ModelRepository;

/**
 * This class is just a custom {@link ben.ladalja.QueryBuilder} for model<br>
 * The main difference is that class return models instead of java.sql.ResultSet<br>
 * When you are using this class, don't use the inherited methods, use redefined instead
 * otherwise UnsupportedOperationException will be throw
 * for example instead of use {@link ben.ladalja.QueryBuilderORM#get()}, use {@link ben.ladalja.QueryBuilderORM#get(Class)}
 * where Class is the model class you want to be returned
 * 
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 * @see ben.ladalja.QueryBuilder
 * 
 */
 public class QueryBuilderORM extends QueryBuilder<QueryBuilderORM> {
	 
	public QueryBuilderORM(String table) {
		super(table);
	}
	
	
	public QueryBuilderORM()
	{
		this(null);
	}

	/**
	 * @deprecated don't use it
	 */
	@Deprecated
	public ResultSet get() {
		if (table == null) {
			throw new UnsupportedOperationException("No class specified");
		} else {
			return super.get();
		}
	}
	
	/**
	 * Build query and return a model containing the results of builded of query. 
	 * You may access each column's value by accessing the column as a property of the row
	 * @param model the model class into the result will be casted
	 * @return ResultSet of result a java.sql.ResultSet containing the results of builded of query. 
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModelRepository> List<T> get(Class<? extends ModelRepository> model)
	{
		try {
			table = model.getDeclaredConstructor().newInstance().getTable();
			ResultSet resultSet = super.get();
			List<T> list = new ArrayList<T>();
			while(resultSet.next())
			{
				T e = (T) ModelRepository.mapping(resultSet,model);
				list.add(e);
			}
			return list;
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
	}

	/**
	 * @deprecated don't use it
	 */
	@Deprecated
	public ResultSet first() {
		if (table == null) {
			throw new UnsupportedOperationException("No class specified");
		} else {
			return super.first();
		}
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#first()
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModelRepository> T first(Class<? extends ModelRepository> model)
	{
		try {
			table = model.getDeclaredConstructor().newInstance().getTable();
			ResultSet resultSet = super.get();
			if(resultSet.next())
			{
				T e = (T) ModelRepository.mapping(resultSet,model);
				return e;
			}
			return null;
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
	}

}
