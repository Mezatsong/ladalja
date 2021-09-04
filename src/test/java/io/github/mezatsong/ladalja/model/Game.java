package io.github.mezatsong.ladalja.model;

import io.github.mezatsong.ladalja.ModelRepository;
import io.github.mezatsong.ladalja.anotations.Column;

public class Game extends ModelRepository {

	private Long id;
	@Column("user_id") 
	private Long userId;
	private String name;

	public User getUser() {
		return belongsTo(User.class, "user_id"); //see {@link ben.ladalja.Model#belongsTo(Class, String)} for details
	}

	public Long getId() { //public void long getId()... will not work use class like you are seeing
		return id;
	}
 
	public void setId(Long id) { //public void setId(long id)... will not work use class like you are seeing
		this.id = id;
	}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	
}
