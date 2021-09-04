package io.github.mezatsong.ladalja;

import java.sql.Connection;

class UtilsForTest {

    static Connection connect() {
        System.setProperty(
            "LADALJA_JDBC_URL", 
            "jdbc:sqlite:" + ClassLoader.getSystemResource("test.db").getPath()
        );
        
        return DB.connection();
    }

}
