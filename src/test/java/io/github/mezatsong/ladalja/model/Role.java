package io.github.mezatsong.ladalja.model;

import java.util.List;

import io.github.mezatsong.ladalja.ModelRepository;

public class Role extends ModelRepository {
    private Long id;
	private String name;
	
	@Override
	public String getTable() {
		return "roles";
	}
	
	public List<User> getUsers() {
		return belongsToMany(User.class, "role_user", "user_id", "role_id"); //see {@link ben.ladalja.Model#belongsToMany(Class, String, String, String)} for details
	}
	
	public void setUsers(List<User> users) {
		for(User user : users)
			attach("role_user", "role_id", "user_id", user); //see {@link ben.ladalja.Model#attach(String, String, String, Model)} for details
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
