package io.github.mezatsong.ladalja;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.mezatsong.ladalja.anotations.Column;
import io.github.mezatsong.ladalja.anotations.Ignore;
import io.github.mezatsong.ladalja.query.QueryBuilder;
import io.github.mezatsong.ladalja.query.QueryBuilderORM;
import io.github.mezatsong.ladalja.tools.Utils;



/**
 * Ladalja provides a beautiful, simple ActiveRecord implementation for working with your database.<br> 
 * Each database table has a corresponding "Model" which is used to interact with that table. <br>
 * {@link ModelRepository.ladalja.Model} allow you to query for data in your tables, as well as insert new records into the table.<br>
 * <p>
 * To create a model, your model class must extends {@link ModelRepository.ladalja.Model}, and 
 * All field must be classes, thus use Integer instead of int
 * Do not use Character class, use String instead
 * You can use {@link ben.ladalja.Column} and {@link ben.ladalja.Ignore} annotations
 * To handle relationships, do not declare a model class into other, declare only method to set and get, in those methods you will use Ladalja relationships 
 * Redefine {@link ModelRepository.ladalja.Model#getTable()} to return the name of table that this model is being represented
 * Eventually override {@link ModelRepository.ladalja.Model#getPrimaryKey()} to return primary key column name, the default implementation return "id"
 * <p>
 * For example, let consider three classe, Game,User and Role<br>
 * User and Role have many to many relationship, thus User have and belongs to many Roles and Role have and belongs to many Users<br>
 * User have many Game<br>
 * Game belong to one User<br>
 * <ul>
 * 	<li>The Game table is "games" (id,name,user_id)</li>
 * 	<li>The Role table is "roles" (id,name)</li>
 * 	<li>The User table is "users" (ID,name)</li>
 * 	<li>The User-Role relationship table is "role_user" (user_id,role_id)</li>
 * </ul>
 * <br>
 * An example of implementations of this situation can be like that
 * <p>
 * <pre>
 * public class Game extends Model {
 *	
 *		private Long id;
 *		<code>@Column</code>("user_id") 
 *		private Long userId;
 *		private String name;
 *	
 *	
 *		<code>@Override</code>
 *		protected String getTable() {
 *			return "games";
 *		}
 *	
 *		public User getUser() {
 *			return belongsTo(User.class, "user_id"); //see {@link ModelRepository.ladalja.Model#belongsTo(Class, String)} for details
 *		}
 * 
 *		public Long getId() { //public void long getId()... will not work use class like you are seeing
 *			return id;
 *		}
 *  
 *
 *		public void setId(Long id) { //public void setId(long id)... will not work use class like you are seeing
 *			this.id = id;
 *		}
 *	
 *		//Other getter and setter
 *
 *	}
 *	</pre>
 *  <p>
 *  <pre>
 *  public class Role extends Model {
 *
 *		private Long id;
 *		private String name;
 *		
 *		<code>@Override</code>
 *		protected String getTable() {
 *			return "roles";
 *		}
 *		
 *		public List&lt;User&gt; getUsers() {
 *			return belongsToMany(User.class, "role_user", "user_id", "role_id"); //see {@link ModelRepository.ladalja.Model#belongsToMany(Class, String, String, String)} for details
 *		}
 *		
 *		public void setUsers(List&lt;User&gt; users) {
 *			for(User user : users)
 *				attach("user_roles", "role_id", "user_id", user); //see {@link ModelRepository.ladalja.Model#attach(String, String, String, ModelRepository)} for details
 *		}
 *
 *		//Other getters and setters
 *		
 *	}
 *  </pre>
 *	<p>
 *  <pre>
 *  public class User extends Model {
 *
 *		private Long id;
 *		private String name;
 *		
 *		<code>@Override</code>
 *		protected String getTable() {
 *			return "roles";
 *		}
 *
 *		<code>@Override</code>
 *		protected String getPrimaryKey(){	//primary key is not "id", so we have to indicate it
 *			return "ID";
 *		}
 *		
 *		public List&lt;Game&gt; getGames() {
 *			return this.hasMany(Game.class, "user_id"); //see {@link ModelRepository.ladalja.Model#hasMany(Class, String)} for details
 *		}
 *
 *		public List&lt;Role&gt; getRoles() {
 *			return this.belongsToMany(Role.class, "role_user", "role_id", "user_id");
 *		}
 *		
 *		public void setGames(List&lt;Game&gt; games) {
 *			for(Game game : games)
 *				game = associate(game,"user_id"); //see {@link ModelRepository.ladalja.Model#associate(ModelRepository, String)} for details
 *		}
 *
 *		//Other getters and setters
 *		
 *	}
 *	</pre>
 *	<p>
 *  An Example of using is<br>
 *  <br>
 *  User user = User.find(User.class, 1); <br>
 *	List&lt;Game&gt; games = user.getGames();<br>
 *	<br>
 *	List&lt;User&gt; users = User.where("name","<>","Admin").get(User.class);<br>
 *	Role role = Role.where("name", "user").first(Role.class);<br>
 *	role.setUsers(users);<br>
 *  <p>
 *  You can have detail in used method in example here : 
 *  <ul>
 *  	<li>{@link ModelRepository.ladalja.Model#find(Class, Object)}</li>
 *  	<li>{@link ModelRepository.ladalja.Model#where(String, String, Object)}</li>
 *  	<li>{@link ModelRepository.ladalja.Model#where(String, Object)}</li>
 *  	<li>{@link ModelRepository.ladalja.Model#first(Class)}</li>
 *  </ul>
 *  
 * @author MEZATSONG TSAFACK Carrel, meztsacar@gmail.com
 * 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class ModelRepository {

	//protected static String table;
	//protected static Class<? extends Model> model;
	
	/* ====================== INSTANCE METHODS ====================*/
	
	/**
	 * Override this to specified the model database table.
	 * @return name of database table that this model is being represented.
	 */
	public String getTable() {
		return Utils.makePlural(this.getClass().getSimpleName().toLowerCase());
	}
	
	
	/**
	 * Override this method to specified the primary key column name in database.
	 * Multiple rows as primary key is not supported.
	 * The default implementation return "id".
	 * @return the name of column representing the primary key
	 */
	protected String getPrimaryKey() {
		return "id";
	}
	
	
	/**
	 * To create a new record in the database, create a new model instance, set attributes on the model, then call the save method.
	 * The save method may also be used to update models that already exist in the database. 
	 * To update a model, you should retrieve it, set any attributes you wish to update, and then call the save method
	 */
	public void save() {
		Map<String,Object> data = mapping(this); 
		if(!data.containsKey(getPrimaryKey())) {
			throw new LadaljaException("Primary key is not present : "+getPrimaryKey()+" is not present");
		}
		
		Object primaryKey = data.get(getPrimaryKey());
		boolean exist = primaryKey != null && newSuperQuery(getClass()).where(getPrimaryKey(),primaryKey).count() > 0;
		
		if( !exist ){ 
			ModelRepository newCreatedInstance = create(this);
			set(getPrimaryField().getName(), newCreatedInstance.get(getPrimaryField().getName()) );
		}else{
			data.remove( getPrimaryKey() );
			newSuperQuery(getClass()).where(getPrimaryKey(),primaryKey).update(data);
		}
	}

	
	/**
	 * Call this method on a model instance to delete a model.
	 */
	public void delete()
	{
		Map<String,Object> data = mapping(this); 
		if(!data.containsKey(getPrimaryKey())){
			throw new LadaljaException("Primary key is not present : "+getPrimaryKey()+" is not present");
		}
		
		Object primaryKey = data.remove( getPrimaryKey() );
		
		if( primaryKey == null){
			throw new LadaljaException("The value of primary key is null : "+getPrimaryKey()+" is null");
		}
		
		newSuperQuery(getClass()).where(getPrimaryKey(),primaryKey).delete();
	}

	
	
	/* ==================== PRIVATE METHOD ================== */
	
	private static String getTable(Class<? extends ModelRepository> model)
	{
		try {
			return model.getDeclaredConstructor().newInstance().getTable();
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
	}
	

	protected Field getPrimaryField() {
		try {
			return getClass().getDeclaredField(getPrimaryKey());
		} catch (NoSuchFieldException e) {
			for (Field pf: getClass().getDeclaredFields()) {
				if (pf.isAnnotationPresent(Column.class)) {
					if (pf.getAnnotation(Column.class).value().equals(getPrimaryKey())) {
						return pf;
					}
				}
			}
		}

		throw new LadaljaException("Can't find field linked with primary key column: " + getPrimaryKey());
	}
	

	private static QueryBuilder newSuperQuery(Class<? extends ModelRepository> model)
	{
		return DB.table(getTable(model));
	}
	
	private static QueryBuilderORM newQuery(Class<? extends ModelRepository> model)
	{
		return new QueryBuilderORM(getTable(model));
	}
	
	private static QueryBuilderORM newQuery()
	{
		return new QueryBuilderORM();
	}
	
	
	protected Object get(String fieldName)
	{
		String getter = "get" +	Utils.toCaptitalize(fieldName);
		try {
			Method method = getClass().getDeclaredMethod(getter);
			return method.invoke(this);
		} catch (NoSuchMethodException e) {
			throw new LadaljaException("Can't find getter (" + getter + ") for field : "+fieldName, e);
		} catch (Exception e) {
			throw new LadaljaException("Error with getter : "+getter, e);
		}
	}
	
	
	protected void set(String fieldName, Object value)
	{
		if (value == null) {
			return;
		}

		String setter = "set" +	Utils.toCaptitalize(fieldName);
		try {
			Field field = getClass().getDeclaredField(fieldName);
			Method method = getClass().getDeclaredMethod(setter, field.getType());
			if (field.getType().getSuperclass() == Number.class || field.getType() == Number.class) {

				if (field.getType() == Long.class) {
					method.invoke(this, Long.valueOf(value.toString()));

				} else if (field.getType() == Integer.class) {
					method.invoke(this, Integer.valueOf(value.toString()));

				} else if (field.getType() == Double.class) {
					method.invoke(this, Double.valueOf(value.toString()));

				} else if (field.getType() == Float.class) {
					method.invoke(this, Float.valueOf(value.toString()));

				} else if (field.getType() == Short.class) {
					method.invoke(this, Short.valueOf(value.toString()));

				} else if (field.getType() == BigInteger.class) {
					method.invoke(this, BigInteger.valueOf(Long.valueOf(value.toString())));

				} else if (field.getType() == BigDecimal.class) {
					method.invoke(this, BigDecimal.valueOf(Double.valueOf(value.toString())));

				} else {
					method.invoke(this, field.getType().cast( (Number) value));
				}


			} else if (field.getType() == String.class) {
				method.invoke(this, value.toString());

			} else if (field.getType() == Date.class) {
				if (value instanceof Number) {
					method.invoke(this, new Date( Long.valueOf(value.toString()) ));
				} else if (value instanceof Timestamp) {
					method.invoke(this, new Date(((Timestamp) value).getTime()));
				} else if (value instanceof Date) {
					method.invoke(this, value);
				} else {
					method.invoke(this, Date.valueOf(value.toString()) );
				}
			} else if (field.getType() == Timestamp.class) {
				if (value instanceof Number) {
					method.invoke(this, new Timestamp( Long.valueOf(value.toString()) ));
				} else if (value instanceof Date) {
					method.invoke(this, new Timestamp(((Date) value).getTime()));
				} else if (value instanceof Timestamp) {
					method.invoke(this, value);
				} else {
					method.invoke(this, Timestamp.valueOf(value.toString()) );
				}
			} else {
				method.invoke(this, field.getType().cast(value));
			}
		} catch (Exception e) {
			throw new LadaljaException("error with : "+fieldName, e);
		} 
	}
	

	public static <T extends ModelRepository> T mapping(ResultSet resultSet, Class<? extends ModelRepository> model)
	{
		T instance = null;
		
		try {
			instance = (T) model.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
		
		
		Field fields[] = model.getDeclaredFields();
		for(Field field : fields)
		{	
			Ignore ignore = field.getDeclaredAnnotation(Ignore.class);
			if(ignore != null)
				continue;
			
			String databaseColumnName = field.getName();
			Column annotation = field.getDeclaredAnnotation(Column.class);
			if(annotation != null){
				databaseColumnName = annotation.value();
			}
			
			try {
				instance.set(field.getName(), resultSet.getObject(databaseColumnName));
			} catch (SQLException e) {
				throw new LadaljaException(e);
			}
			
		}
		
		return instance;
	}
	
	static <T extends ModelRepository> Map<String,Object> mapping(T instance)
	{
		Map<String,Object> map = new HashMap<String,Object>();
		Class<? extends ModelRepository> model = instance.getClass();
		Field fields[] = model.getDeclaredFields();
		for(Field field : fields)
		{	
			Ignore ignore = field.getDeclaredAnnotation(Ignore.class);
			if(ignore != null)
				continue;
			
			String databaseColumnName = field.getName();
			Column annotation = field.getDeclaredAnnotation(Column.class);
			if(annotation != null){
				databaseColumnName = annotation.value();
			}
			
			map.put(databaseColumnName, instance.get(field.getName()));
		}
		
		return map;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	/* =============== QUERY STATIC METHOD ======================= */
	
	/**
	 * Retrieve all row of the model
	 * @param model the class which the result will be mapped into
	 * @return the list of model object
	 */
	public static <T extends ModelRepository> List<T> all(Class<? extends ModelRepository> model)
	{
		return newQuery(model).get(model);
	}
	
	
	/**
	 * Retrieve a model by its primary key.
	 * @param model the class which the result will be mapped into
	 * @param id its primary key
	 * @return the model or null if nothing found
	 */
	public static <T extends ModelRepository> T find(Class<? extends ModelRepository> model, Object id)
	{
		try {
			String primaryKey = model.getDeclaredConstructor().newInstance().getPrimaryKey();
			return where(primaryKey,id).first(model);
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
	}
	
	/**
	 * You can call the find method with an array of primary keys, which will return a collection of the matching records
	 * @param model the class which the result will be mapped into
	 * @param ids list of primary key
	 * @return return a list of the matching records
	 */
	public static <T extends ModelRepository> List<T> find(Class<? extends ModelRepository> model, Object... ids)
	{
		List<T> list = new ArrayList<T>();
		if(ids != null){
			for(Object id : ids)
			{
				T e = find(model,id);
				list.add( e );
			}
		}
		return list;
	}
	
	
	/**
	 * Retrieve the first result of the query; 
	 * If no result is found, a  LadaljaException will be thrown
	 * @param model the class which the result will be mapped into
	 * @param id the primary key
	 * @return first result of the query
	 * @throws LadaljaException if there no matching result
	 */
	public static <T extends ModelRepository> T findOrFail(Class<? extends ModelRepository> model, Object id)
	{
		T e = find(model, id);
		if(e == null){
			throw new LadaljaException("There is no row in table "+getTable(model)+" with "+id+" as id.");
		}
		return e;
	}
	
	
	/**
	 * get a collection of the matching records with list of given primary key
	 * @param model the class which the result will be mapped into
	 * @param ids list of primary key
	 * @return return a list of the matching records
	 * @throws LadaljaException if there primary key in ids which has no result
	 */
	public static <T extends ModelRepository> List<T> findOrFail(Class<? extends ModelRepository> model, Object... ids)
	{
		List<T> list = new ArrayList<T>();
		if(ids != null){
			for(Object id : ids)
			{
				T e = findOrFail(model,id);
				list.add( e );
			}
		}
		return list;
	}
	
	
	/**
	 * Insert a new record in the database and return it. 
	 * @param instance
	 * @return return the new saved instance, with generated keys if there are
	 */
	public static <T extends ModelRepository> T create(T instance)
	{
		Class<? extends ModelRepository> model = instance.getClass();
		
		Map<String,Object> data = mapping(instance); 

		Object primaryKey = data.get(instance.getPrimaryKey());

		if (primaryKey == null && DB.isInsertGetIdSupported()) {
			Object id = newSuperQuery(model).insertGetId(data);
			return findOrFail(model, id);
		} else  {
			newSuperQuery(model).insert(data);
			QueryBuilderORM query = newQuery(model);
			if (primaryKey != null) {
				query.where(instance.getPrimaryKey(), primaryKey);
			} else {
				data.remove(instance.getPrimaryKey());
				for (String key : data.keySet()) {
					query = query.where(key, data.get(key));
				}
				query.orderBy(instance.getPrimaryKey(), "DESC");
			}

			return query.first(model);
		}
	}
	
	
	
	
	/**
	 * Update an existing model or create a new model if none exist
	 * @param instance you want to create or update
	 * @return return the saved instance
	 */
	public static <T extends ModelRepository> T updateOrCreate(T instance)
	{
		instance.save();
		return instance;
	}
	
	
	
	/**
	 * Delete a model without retrieving it
	 * @param model the class of model in which object will be deleted
	 * @param primaryKeyValues list of primary key of objects you want to delete
	 */
	public static void destroy(Class<? extends ModelRepository> model, Object... primaryKeyValues)
	{
		try {
			String primaryKey = model.getDeclaredConstructor().newInstance().getPrimaryKey();
			if(primaryKeyValues != null)
			{
				for(Object primaryKeyValue : primaryKeyValues)
					newSuperQuery(model).where(primaryKey, primaryKeyValue).delete();
			}
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
	}


	/* ===================== QUERY BUILDER METHODS ==================== */
		
	
	/**
	 * @see ben.ladalja.QueryBuilder#get()
	 */
	public static <T extends ModelRepository> List<T> get(Class<? extends ModelRepository> model) {
		return newQuery(model).get(model);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#first()
	 */
	public static <T extends ModelRepository> T first(Class<? extends ModelRepository> model) {
		return newQuery(model).first(model);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#count()
	 */
	public static long count(Class<? extends ModelRepository> model) {
		return newQuery(model).count();
	}


	/**
	 * @see ben.ladalja.QueryBuilder#max(java.lang.String)
	 */
	public static Double max(Class<? extends ModelRepository> model, String column) {
		return newQuery(model).max(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#min(java.lang.String)
	 */
	public static Double min(Class<? extends ModelRepository> model, String column) {
		return newQuery(model).min(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#avg(java.lang.String)
	 */
	public static Double avg(Class<? extends ModelRepository> model, String column) {
		return newQuery(model).avg(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#sum(java.lang.String)
	 */
	public static double sum(Class<? extends ModelRepository> model, String column) {
		return newQuery(model).sum(column);
	}


	

	/**
	 * @see ben.ladalja.QueryBuilder#distinct()
	 */
	public static QueryBuilderORM distinct() {
		return newQuery().distinct(); 
	}


	/**
	 * @see ben.ladalja.QueryBuilder#where(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public static QueryBuilderORM where(String column, String operator, Object value) {
		return  newQuery().where(column, operator, value);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#where(java.lang.String, java.lang.Object)
	 */
	public static QueryBuilderORM where(String column, Object value) {
		return  newQuery().where(column, value);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereId(long)
	 */
	public static QueryBuilderORM whereId(long id) {
		return  newQuery().whereId(id);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereLike(java.lang.String, java.lang.String)
	 */
	public static QueryBuilderORM whereLike(String column, String value) {
		return  newQuery().whereLike(column, value);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#orWhere(java.lang.String, java.lang.Object)
	 */
	public static QueryBuilderORM orWhere(String column, Object value) {
		return  newQuery().orWhere(column, value);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#orWhere(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public static QueryBuilderORM orWhere(String column, String operator, Object value) {
		return  newQuery().orWhere(column, operator, value);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereBetween(java.lang.String, int, int)
	 */
	public static QueryBuilderORM whereBetween(String column, int min, int max) {
		return  newQuery().whereBetween(column, min, max);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereBetween(java.lang.String, long, long)
	 */
	public static QueryBuilderORM whereBetween(String column, long min, long max) {
		return  newQuery().whereBetween(column, min, max);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereBetween(java.lang.String, double, double)
	 */
	public static QueryBuilderORM whereBetween(String column, double min, double max) {
		return  newQuery().whereBetween(column, min, max);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereNotBetween(java.lang.String, int, int)
	 */
	public static QueryBuilderORM whereNotBetween(String column, int min, int max) {
		return  newQuery().whereNotBetween(column, min, max);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereNotBetween(java.lang.String, long, long)
	 */
	public static QueryBuilderORM whereNotBetween(String column, long min, long max) {
		return  newQuery().whereNotBetween(column, min, max);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereNotBetween(java.lang.String, double, double)
	 */
	public static QueryBuilderORM whereNotBetween(String column, double min, double max) {
		return  newQuery().whereNotBetween(column, min, max);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereIn(java.lang.String, java.lang.Object[])
	 */
	public static QueryBuilderORM whereIn(String column, Object[] values) {
		return  newQuery().whereIn(column, values);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereNotIn(java.lang.String, java.lang.Object[])
	 */
	public static QueryBuilderORM whereNotIn(String column, Object[] values) {
		return  newQuery().whereNotIn(column, values);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereNull(java.lang.String)
	 */
	public static QueryBuilderORM whereNull(String column) {
		return  newQuery().whereNull(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereNotNull(java.lang.String)
	 */
	public static QueryBuilderORM whereNotNull(String column) {
		return  newQuery().whereNotNull(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereDate(java.lang.String, java.sql.Date)
	 */
	public static QueryBuilderORM whereDate(String column, Date date) {
		return  newQuery().whereDate(column, date);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereDate(java.lang.String, java.lang.String, java.sql.Date)
	 */
	public static QueryBuilderORM whereDate(String column, String operator, Date date) {
		return  newQuery().whereDate(column, operator, date);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereYear(java.lang.String, int)
	 */
	public static QueryBuilderORM whereYear(String column, int year) {
		return  newQuery().whereYear(column, year);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereYear(java.lang.String, java.lang.String, int)
	 */
	public static QueryBuilderORM whereYear(String column, String operator, int year) {
		return  newQuery().whereYear(column, operator, year);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereMonth(java.lang.String, int)
	 */
	public static QueryBuilderORM whereMonth(String column, int month) {
		return  newQuery().whereMonth(column, month);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereMonth(java.lang.String, java.lang.String, int)
	 */
	public static QueryBuilderORM whereMonth(String column, String operator, int month) {
		return  newQuery().whereMonth(column, operator, month);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereDay(java.lang.String, int)
	 */
	public static QueryBuilderORM whereDay(String column, int day) {
		return  newQuery().whereDay(column, day);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereDay(java.lang.String, java.lang.String, int)
	 */
	public static QueryBuilderORM whereDay(String column, String operator, int day) {
		return  newQuery().whereDay(column, operator, day);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereColumn(java.lang.String, java.lang.String)
	 */
	public static QueryBuilderORM whereColumn(String column1, String column2) {
		return  newQuery().whereColumn(column1, column2);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#whereColumn(java.lang.String, java.lang.String, java.lang.String)
	 */
	public static QueryBuilderORM whereColumn(String column1, String operator, String column2) {
		return  newQuery().whereColumn(column1, operator, column2);
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String)
	 */
	public static QueryBuilderORM whereHas(String column)
	{
		return newQuery().whereHas(column);
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String,double)
	 */
	public static QueryBuilderORM whereHas(String column, double number)
	{
		return  newQuery().whereHas(column,number);
	}
	
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String)
	 */
	public static QueryBuilderORM has(String column)
	{
		return newQuery().whereHas(column);
	}
	
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String,int)
	 */
	public static QueryBuilderORM has(String column, int number)
	{
		return  newQuery().whereHas(column, number);
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String,long)
	 */
	public static QueryBuilderORM has(String column, long number)
	{
		return  newQuery().whereHas(column, number);
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String,double)
	 */
	public static QueryBuilderORM has(String column, double number)
	{
		return  newQuery().whereHas(column,number);
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String,int)
	 */
	public static QueryBuilderORM whereHas(String column, int number)
	{
		return  newQuery().whereHas(column, number);
	}
	
	/**
	 * @see ben.ladalja.QueryBuilder#whereHas(java.lang.String,long)
	 */
	public static QueryBuilderORM whereHas(String column, long number)
	{
		return  newQuery().whereHas(column, number);
	}

	/**
	 * @see ben.ladalja.QueryBuilder#whereDoesntHave(java.lang.String)
	 */
	public static QueryBuilderORM whereDoesntHave(String column)
	{
		return newQuery().whereDoesntHave(column);
	}

	/**
	 * @see ben.ladalja.QueryBuilder#groupBy(java.lang.String, java.lang.String[])
	 */
	public static QueryBuilderORM groupBy(String firstColumn, String... otherColumns) {
		return  newQuery().groupBy(firstColumn, otherColumns);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#orderBy(java.lang.String)
	 */
	public static QueryBuilderORM orderBy(String column) {
		return  newQuery().orderBy(column);
	}
	
	
	/**
	 * @see ben.ladalja.QueryBuilder#orderByDesc(java.lang.String)
	 */
	public static QueryBuilderORM orderByDesc(String column) {
		return  newQuery().orderByDesc(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#orderBy(java.lang.String, java.lang.String)
	 */
	public static QueryBuilderORM orderBy(String column, String order) {
		return  newQuery().orderBy(column, order);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#latest(java.lang.String)
	 */
	public static QueryBuilderORM latest(String column) {
		return  newQuery().latest(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#oldest(java.lang.String)
	 */
	public static QueryBuilderORM oldest(String column) {
		return  newQuery().oldest(column);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#inRandomOrder()
	 */
	public static QueryBuilderORM inRandomOrder() {
		return  newQuery().inRandomOrder();
	}


	/**
	 * @see ben.ladalja.QueryBuilder#having(java.lang.String, java.lang.String, java.lang.String)
	 */
	public static QueryBuilderORM having(String column, String operator, String value) {
		return  newQuery().having(column, operator, value);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#havingRaw(java.lang.String)
	 */
	public static QueryBuilderORM havingRaw(String havingClause) {
		return  newQuery().havingRaw(havingClause);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#skip(int)
	 */
	public static QueryBuilderORM skip(int arg) {
		return  newQuery().skip(arg);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#take(int)
	 */
	public static QueryBuilderORM take(int arg) {
		return  newQuery().take(arg);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#offset(int)
	 */
	public static QueryBuilderORM offset(int arg) {
		return  newQuery().offset(arg);
	}


	/**
	 * @see ben.ladalja.QueryBuilder#limit(int)
	 */
	public static QueryBuilderORM limit(int arg) {
		return  newQuery().limit(arg);
	}
	
	
	
	
	/* =============== QUERY STATIC METHOD FOR RELATIONSHIP ======================= */
	
	/* --------- ONE TO ONE ---------- */
	
	/**
	 * A "one-to-one" relationship is a very basic relation
	 * The first argument passed to the hasOne method is the class of the related model
	 * We determines the foreign key of the relationship with the foreignKey argument
	 * We assumes that the foreign key should have a value matching the primary key (retrieved with getPrimaryKey() method) of the parent
	 * Otherwise null will be returned (in case of there is no matching)
	 * @param relatedModel the class of the related model
	 * @param foreignKey the foreign key of the relationship
	 * @return a new instance of relatedModel class or null
	 */
	public <T extends ModelRepository> T hasOne(Class<? extends ModelRepository> relatedModel, String foreignKey)
	{
		try {
			String relatedModelPrimaryKey = relatedModel.getDeclaredConstructor().newInstance().getPrimaryKey();
			String foreignKeyFieldName = foreignKey;
			boolean found = false;
			for(Field field : getClass().getDeclaredFields())
			{
				if(field.getDeclaredAnnotation(Ignore.class) != null){
					continue;
				}
				if(field.getName().equals(foreignKey) || field.getDeclaredAnnotation(Column.class) != null && 
						field.getDeclaredAnnotation(Column.class).value().equals(foreignKey)){
					foreignKeyFieldName = field.getName();
					found = true;
				}
			}
			if(!found){
				throw new LadaljaException(foreignKey+" column or field not found in the attributes list");
			}
			return newQuery(relatedModel).where(relatedModelPrimaryKey, get(foreignKeyFieldName)).first(relatedModel);
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
		
	}
	
	
	/**
	 * The inverse of one-to-one relationship, this is inverse of hosOne method
	 * The first argument passed to the hasOne method is the class of the related model
	 * We determines the foreign key of the relationship with the foreignKey argument
	 * We assumes that the primary key of parent (retrieved with getPrimaryKey() method of parent) should have a value matching the foreign key of this object
	 * Otherwise null will be returned (in case of there is no matching)
	 * @see ModelRepository.ladalja.Model#hasOne(Class, String)
	 * @param relatedModel the class of the related model
	 * @param foreignKey the foreign key of the relationship
	 * @return a new instance of relatedModel class or null
	 */
	public <T extends ModelRepository> T belongsTo(Class<? extends ModelRepository> relatedModel, String foreignKey)
	{
		try {
			String relatedModelPrimaryKey = relatedModel.getDeclaredConstructor().newInstance().getPrimaryKey();
			return newQuery(relatedModel).where(relatedModelPrimaryKey, get(foreignKey)).first(relatedModel);
		} catch (Exception e) {
			throw new LadaljaException(e);
		}
	}
	
	

	/* --------- ONE TO MANY ---------- */
	
	/**
	 * A "one-to-many" relationship which is used to define relationships where a single model owns any amount of other models
	 * In that case a single model is this object.
	 * @param relatedModel the model class that this object owns any amount 
	 * @param foreignKey the foreign key of the relationship
	 * @return list of matching objects
	 */
	public <T extends ModelRepository> List<T> hasMany(Class<? extends ModelRepository> relatedModel, String foreignKey)
	{
		return newQuery(relatedModel).where(foreignKey, get(getPrimaryField().getName())).get(relatedModel);
	}
	
	
	
	//belongsTo method belong to one-to-one and one-to-many relationship and has already been written in one-to-one relationships
	
	
	/* --------- MANY TO MANY ---------- */
	
	/**
	 * A "many-to-many" relationship which is used when a model A owns any amount of other model B and model B owns also any amount model A 
	 * To define this relationship, three database tables are needed: model A table, model B table and joining table.
	 * The first parameter is the model class which represent B model, ie the model class that we wants to get from this object. 
	 * The second parameter is the name of that joining table.
	 * In the joining table there are two fields, A primary key and B primary key, the names of those columns are respectively the third and last parameters
	 * Thus third parameter is the name of column representing the primary key of this object
	 * while the last parameter is which one represent the primary key of relatedModel class primary key.
	 * @param relatedModel the model class that this object owns any amount 
	 * @param relationshipJoiningTable the relationship's joining table name
	 * @param foreignKey the name of primary key representing column of this object is relationship's joining table
	 * @param joiningForeignKey the name of primary key representing column of relatedModel class object is relationship's joining table
	 * @return list of relatedModel object or empty list if there are no matching.
	 */
	public <T extends ModelRepository> List<T> belongsToMany(Class<? extends ModelRepository> relatedModel, 
											String relationshipJoiningTable,
											String foreignKey, 
											String joiningForeignKey)
	{
		try{
			List<Object> joiningPrimaryKeys = new ArrayList<Object>();
			ResultSet resultSet = DB.table(relationshipJoiningTable).where(foreignKey, get(getPrimaryField().getName())).pluck(joiningForeignKey);
			while(resultSet.next())
			{
				joiningPrimaryKeys.add( resultSet.getObject(joiningForeignKey) );
			}
			
			String joiningPrimaryKey = relatedModel.getDeclaredConstructor().newInstance().getPrimaryKey();
			return newQuery(relatedModel).whereIn(joiningPrimaryKey, joiningPrimaryKeys.toArray()).get(relatedModel);
		}catch(Exception e){
			throw new LadaljaException(e);
		}
	}
	
	
	
	/*  -------- MUTATOR FOR RELATION RELATIONSHIP -------------- */
	
	
	/**
	 * Convenient method for adding new model to relationship<p> 
	 * For example, perhaps you need to insert a new Comment for a Post model.<p> 
	 * Instead of manually setting the post_id attribute on the Comment id, 
	 * you may insert the Comment directly from the relationship's with method
	 * @param instance the instance of T model class you to associate to this object 
	 * @param foreignKey of the relationship
	 * @return an updated instance
	 */
	public <T extends ModelRepository> T associate(T instance, String foreignKey)
	{
		if(instance.getClass().equals(getClass())){
			throw new LadaljaException("Can't make relatonship with your self");
		}
		
		boolean found = false;
		String foreignKeyFieldName = foreignKey;
		for(Field field : instance.getClass().getDeclaredFields())
		{
			if(field.getDeclaredAnnotation(Ignore.class) != null){
				continue;
			}
			
			if(field.getName().equals(foreignKey) || field.getDeclaredAnnotation(Column.class) != null && 
					field.getDeclaredAnnotation(Column.class).value().equals(foreignKey)){
				foreignKeyFieldName = field.getName();
				found = true;
			}
		}
		if(!found){
			throw new LadaljaException(foreignKey+" column or field not found in the attributes list");
		}
		instance.set(foreignKeyFieldName, get(getPrimaryField().getName()));
		instance.save();
		return instance;
	}
	
	
	
	/**
	 * To attach a model A to a model B by inserting a record in the intermediate table that joins the models
	 * @see ModelRepository.ladalja.Model#belongsToMany(Class, String, String, String)
	 * @param relationshipJoiningTable the name of relationship joining table
	 * @param foreignKey the name of foreignKey in relationshipJoiningTable for this object
	 * @param joiningForeignKey the name of foreignKey in relationshipJoiningTable for instance
	 * @param instance the related model instance
	 */
	public <T extends ModelRepository> void attach(
		String relationshipJoiningTable,
		String foreignKey, 
		String joiningForeignKey,
		T  instance
	) 
	{
		if(instance == null){
			return;
		}
		
		if(instance.getClass().equals(getClass())){
			throw new LadaljaException("Can't attach to your self");
		}
		
		List<T> existings = belongsToMany(instance.getClass(),relationshipJoiningTable, foreignKey, joiningForeignKey);
		
		if(!existings.contains(instance)){
			Map<String,Object> map = new HashMap<String,Object>();
			map.put(foreignKey, get(getPrimaryField().getName()));
			map.put(joiningForeignKey, instance.get(instance.getPrimaryField().getName()));
			DB.table(relationshipJoiningTable).insert(map);
		}
		
	}
	
	
	
	/**
	 * To detach a model A to a model B by inserting a record in the intermediate table that joins the models
	 * @param relationshipJoiningTable the name of relationship joining table
	 * @param foreignKey the name of foreignKey in relationshipJoiningTable for this object
	 * @param joiningForeignKey the name of foreignKey in relationshipJoiningTable for instances model
	 * @param instances the related model instances
	 */
	public <T extends ModelRepository> void detach(String relationshipJoiningTable,
										String foreignKey, 
										String joiningForeignKey,
										T... instances)
	{
		if(instances == null || instances.length == 0){
			return;
		}
		
		if(instances[0].getClass().equals(getClass())){
			throw new LadaljaException("Can't have relationship with your self");
		}
		
		for(T instance : instances)
		{
			DB.table(relationshipJoiningTable)
				.where(foreignKey, get(getPrimaryField().getName()))
					.where(joiningForeignKey, instance.get(instance.getPrimaryField().getName()))
						.delete();
		}
	}
	
	
	
	/**
	 * Used to access a specific attribute in intermediate table between two model in many-to-many relationship
	 * @param relationshipJoiningTable the name of relationship joining table
	 * @param foreignKey the name of foreignKey in relationshipJoiningTable for this object
	 * @param joiningForeignKey the name of foreignKey in relationshipJoiningTable for instance model 
	 * @param instance the related model instance
	 * @param column column name of attribute
	 * @return the value of column in the corresponding row in table
	 */
	public <T extends ModelRepository> Object pivot(String relationshipJoiningTable,
											String foreignKey, 
											String joiningForeignKey,
											T instance, 
											String column)
	{
		if(instance.getClass().equals(getClass())){
			throw new LadaljaException("Can't have relationship with your self");
		}
		
		return DB.table(relationshipJoiningTable)
					.where(foreignKey, get(getPrimaryField().getName()))
						.where(joiningForeignKey, instance.get(instance.getPrimaryField().getName()))
							.value(column);
	}
	
	
	/**
	 * Used to access the intermediate table between two model in many-to-many relationship
	 * @param relationshipJoiningTable the name of relationship joining table
	 * @param foreignKey the name of foreignKey in relationshipJoiningTable for this object
	 * @param joiningForeignKey the name of foreignKey in relationshipJoiningTable for instance
	 * @param instance the related model instance
	 * @return Map<String,Object> containing the values with column name as key of each values
	 */
	public <T extends ModelRepository> Map<String,Object> pivot(String relationshipJoiningTable,
														String foreignKey, 
														String joiningForeignKey,
														T instance)
	{
		if(instance.getClass().equals(getClass())){
			throw new LadaljaException("Can't have relationship with your self");
		}
		
		 return DB.table(relationshipJoiningTable)
					.where(foreignKey, get(getPrimaryField().getName()))
						.where(joiningForeignKey, instance.get(instance.getPrimaryField().getName()))
							.firstMap();
	}
	
}
