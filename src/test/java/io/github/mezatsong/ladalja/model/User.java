package io.github.mezatsong.ladalja.model;

import java.util.List;

import io.github.mezatsong.ladalja.ModelRepository;
import io.github.mezatsong.ladalja.anotations.Column;

public class User extends ModelRepository {
    @Column("ID") 
    private Long id;
	private String name;
	
    @Override
	public String getPrimaryKey() {	//primary key is not "id", so we have to indicate it
		return "ID";
	}
	
	public List<Game> getGames() {
		return this.hasMany(Game.class, "user_id"); //see {@link ben.ladalja.Model#hasMany(Class, String)} for details
	}

	public List<Role> getRoles() {
		return this.belongsToMany(Role.class, "role_user", "role_id", "user_id");
	}
	
	public void setGames(List<Game> games) {
		for(Game game : games)
			game = associate(game,"user_id"); //see {@link ben.ladalja.Model#associate(Model, String)} for details
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
