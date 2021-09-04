package io.github.mezatsong.ladalja;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.github.mezatsong.ladalja.model.Game;
import io.github.mezatsong.ladalja.model.Role;
import io.github.mezatsong.ladalja.model.User;
import io.github.mezatsong.ladalja.query.QueryListener;

public class ModelRepositoryTest {
    
    @Before
    public void setUp() {
        UtilsForTest.connect();
        DB.register(new QueryListener() {

            @Override
            public void listenQuery(String query) {
                System.out.println("|== SQL ==> " + query);
            }

            @Override
            public void listenResultSet(String query, ResultSet result) {
                
            }

            @Override
            public void listenUpdatedRows(String query, int rows) {
                System.out.println("#== SQL ==> " + query + " |== ROW ==> " + rows);
            }
            
        });
    }

    @After
    public void tearDown() {
        DB.closeConnection();
    }

    @Test 
    public void testInitialization() {
        User user = new User();
        assertEquals(user.getTable(), "users");
        assertEquals(user.getPrimaryKey(), "ID");
    }

    @Test 
    public void testCreation() {
        User user = new User();
        user.setName("User1");
        user.save();
        assertNotNull(user.getId());

        Role role = new Role();
        role.setName("Role1");
        role = Role.create(role);
        assertNotNull(role.getId());

        Game game = new Game();
        game.setName("Game1");
        game.setUserId(user.getId());
        game.save();
        assertNotNull(game.getId());

        user.attach("role_user", "user_id", "role_id", role);
    }

    @Test
    public void testRetreival() {
        User user = ModelRepository.first(User.class);
        User user2 = ModelRepository.find(User.class, user.getId());
        assertEquals(user.getId(), user2.getId());
        assertEquals(user.getName(), user2.getName());
        assertEquals(user.getGames().size(), user2.getGames().size());

        assertTrue(user.getGames().size() > 0);
        assertTrue(user.getRoles().size() > 0);

        List<User> users = ModelRepository.where("name","=","User1").get(User.class);
        assertEquals(users.size(), 1);
        Role role = ModelRepository.where("id", ">", 0).orWhere("name", "Role1").first(Role.class);
        assertNotNull(role);
        role.setUsers(users);
        assertFalse(role.getUsers().isEmpty());

        user.detach("role_user", "user_id", "role_id", role);
        assertFalse(user.getRoles().size() > 0);

        assertTrue(ModelRepository.avg(User.class, "id") <= ModelRepository.sum(User.class, "id"));
        assertTrue(ModelRepository.min(User.class, "id") <= ModelRepository.max(User.class, "id"));
    }

    @Test 
    public void testDelete() {
        List<User> users = User.all(User.class);
        for (User user : users) {
            user.delete();
        }
        
        List<Role> roles = Role.all(Role.class);
        for (Role role : roles) {
            ModelRepository.destroy(Role.class, role.getId());
        }
        
        List<Game> games = Game.all(Game.class);
        for (Game game : games) {
            game.delete();
        }

        assertEquals(ModelRepository.count(User.class), 0);
        assertEquals(ModelRepository.count(Role.class), 0);
        assertEquals(ModelRepository.count(Game.class), 0);
    }

}
