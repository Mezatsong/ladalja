package io.github.mezatsong.ladalja;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;

import org.junit.Test;

public class BDTest {
    
    @Test
    public void testConnexion() {
        try {
            System.setProperty("LADALJA_CONNECTION", "mysql");
            System.setProperty("LADALJA_DRIVER", "com.mysql.cj.jdbc.Driver");
            System.setProperty("LADALJA_HOST", "localhost");
            System.setProperty("LADALJA_PORT", "3306");
            System.setProperty("LADALJA_DATABASE", "tonton-anicet_cwadix");
            System.setProperty("LADALJA_USERNAME", "root");
            System.setProperty("LADALJA_PASSWORD", "root");
            Connection conn = DB.connection();
            assertNotNull(conn);
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(e instanceof LadaljaException);
        }
    }

}
