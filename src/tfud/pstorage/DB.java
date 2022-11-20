/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tfud.pstorage;

import java.sql.ResultSet;

/**
 *
 * @author brian
 */
public interface DB {

    void connect(String hostname, String dbname, String username, String password);

    void open();

    void query(String q);

    ResultSet getResultSet();

    void close();

    void update(String sql);
}
