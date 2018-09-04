/**
 * 
 */
package ben.ladalja;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	 
	QueryBuilderORM(String table) {
		super(table);
	}
	
	
	QueryBuilderORM()
	{
		this(null);
	}

	/**
	 * @deprecated don't use it
	 */
	@Deprecated
	public ResultSet get() {
		throw new UnsupportedOperationException("No class specified");
	}
	
	/**
	 * Build query and return a model containing the results of builded of query. 
	 * You may access each column's value by accessing the column as a property of the row
	 * @param model the model class into the result will be casted
	 * @return ResultSet of result a java.sql.ResultSet containing the results of builded of query. 
	 */
	@SuppressWarnings("unchecked")
	public <T extends Model> List<T> get(Class<? extends Model> model)
	{
		try {
			table = model.newInstance().getTable();
			ResultSet resultSet = super.get();
			List<T> list = new ArrayList<T>();
			while(resultSet.next())
			{
				T e = (T) Model.mapping(resultSet,model);
				list.add(e);
			}
			return list;
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			throw new LadaljaException(e);
		}
	}

	/**
	 * @deprecated don't use it
	 */
	@Deprecated
	public ResultSet first() {
		throw new UnsupportedOperationException("No class specified");
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#first()
	 */
	@SuppressWarnings("unchecked")
	public <T extends Model> T first(Class<? extends Model> model)
	{
		try {
			table = model.newInstance().getTable();
			ResultSet resultSet = super.get();
			if(resultSet.next())
			{
				T e = (T) Model.mapping(resultSet,model);
				return e;
			}
			return null;
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			throw new LadaljaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#pluck(java.lang.String)
	 */
	@Deprecated
	public ResultSet pluck(String column) {
		throw new UnsupportedOperationException("Using pluck method on model class is not supported, can't restrict a model");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#insert(java.util.Map)
	 */
	@Deprecated
	public void insert(Map<String, Object> arg) {
		throw new UnsupportedOperationException("Insert method on model class is not supported, use Model.create instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#insertGetId(java.util.Map)
	 */
	@Deprecated
	public Long insertGetId(Map<String, Object> arg) {
		throw new UnsupportedOperationException("Insert method on model class is not supported, use Model.create instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#update(java.util.Map)
	 */
	@Deprecated
	public void update(Map<String, Object> arg) {
		throw new UnsupportedOperationException("Operation not permitted, use Model.update instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#increment(java.lang.String)
	 */
	@Deprecated
	public void increment(String column) {
		throw new UnsupportedOperationException("Operation not permitted, use Model.update instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#increment(java.lang.String, int)
	 */
	@Deprecated
	public void increment(String column, int supplement) {
		throw new UnsupportedOperationException("Operation not permitted, use Model.update instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#decrement(java.lang.String)
	 */
	@Deprecated
	public void decrement(String column) {
		throw new UnsupportedOperationException("Operation not permitted, use Model.update instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#decrement(java.lang.String, int)
	 */
	@Deprecated
	public void decrement(String column, int reduction) {
		throw new UnsupportedOperationException("Operation not permitted, use Model.update instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#truncate()
	 */
	@Deprecated
	public void truncate() {
		throw new UnsupportedOperationException("Operation not permitted, use Model.trucate instead");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#select(java.lang.String, java.lang.String[])
	 */
	@Deprecated
	public QueryBuilderORM select(String firstColumn, String... otherColumns) {
		throw new UnsupportedOperationException("Can't restrict a model");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#addSelect(java.lang.String)
	 */
	@Deprecated
	public QueryBuilderORM addSelect(String column) {
		throw new UnsupportedOperationException("Can't restrict a model");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#join(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Deprecated
	public QueryBuilderORM join(String joinTable, String column, String operator, String joinColumn) {
		throw new UnsupportedOperationException("Can't make a join in a model");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#leftJoin(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Deprecated
	public QueryBuilderORM leftJoin(String joinTable, String column, String operator, String joinColumn) {
		throw new UnsupportedOperationException("Can't make a join in a model");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#crossJoin(java.lang.String)
	 */
	@Deprecated
	public QueryBuilderORM crossJoin(String joinTable) {
		throw new UnsupportedOperationException("Can't make a join in a model");
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#union(ben.ladalja.QueryBuilder)
	 */
	@Deprecated
	public QueryBuilderORM union(QueryBuilderORM anOtherQuery) {
		throw new UnsupportedOperationException("Can't make an union in a model");
	}


	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#count()
	 */
	@Override
	public long count() {
		throw new UnsupportedOperationException("No class specified");
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#count()
	 */
	public long count(Class<? extends Model> model) {
		try {
			table = model.newInstance().getTable();
			return super.count();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new LadaljaException(e);
		}
	}


	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#max(java.lang.String)
	 */
	@Deprecated
	public Double max(String column) {
		throw new UnsupportedOperationException("No class specified");
	}
	
	
	/**
	 * @see ben.ladalja.QueryBuilder#max(java.lang.String)
	 */
	public Double max(Class<? extends Model> model, String column) {
		try {
			table = model.newInstance().getTable();
			return super.max(column);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new LadaljaException(e);
		}
	}


	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#min(java.lang.String)
	 */
	@Deprecated
	public Double min(String column) {
		throw new UnsupportedOperationException("No class specified");
	}

	/**
	 * @see ben.ladalja.QueryBuilder#min(java.lang.String)
	 */
	public Double min(Class<? extends Model> model, String column) {
		try {
			table = model.newInstance().getTable();
			return super.min(column);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new LadaljaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#avg(java.lang.String)
	 */
	@Deprecated
	public Double avg(String column) {
		throw new UnsupportedOperationException("No class specified");
	}

	
	/**
	 * @see ben.ladalja.QueryBuilder#avg(java.lang.String)
	 */
	public Double avg(Class<? extends Model> model, String column) {
		try {
			table = model.newInstance().getTable();
			return super.avg(column);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new LadaljaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see ben.ladalja.QueryBuilder#sum(java.lang.String)
	 */
	@Deprecated
	public double sum(String column) {
		throw new UnsupportedOperationException("No class specified");
	}
	
	
	/**
	 * @see ben.ladalja.QueryBuilder#sum(java.lang.String)
	 */
	public double sum(Class<? extends Model> model, String column) {
		try {
			table = model.newInstance().getTable();
			return super.sum(column);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new LadaljaException(e);
		}
	}
	
	

}
