/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlclient.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sqlclient.model.SQLObject;

/**
 *
 * @author Morgan Xu
 */
public class DatabaseConnection {
    private Connection conn = null;
    private String connString = null;
    private String driver = null;
    private String location = null;
    
    public DatabaseConnection(String location){
        this.location = location;
        connString = "jdbc:" + driver + ":" + location + ";create=true";
    }
    
    //Set driver
    public void setDriver(String driver){
        this.driver = driver;
        connString = "jdbc:" + driver + ":" + location + ";create=true";
    }
    
    //Open Connection
    public void openConnection() throws SQLException {
        try {
            conn = DriverManager.getConnection(connString);
            System.out.println("Connected to Database");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
    
    //Close Connection
    public void closeConnection() throws SQLException {
        try {
            if (conn != null) {
                conn.close();
            }
            System.out.println("Disconnected from Database");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseConnection.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
    
    //Execute Queries
    public SQLObject execute(String sql) throws SQLException{
        openConnection();
        Statement stmt = conn.createStatement();
        boolean status = stmt.execute(sql);
        if (status == true) {
            List<ArrayList<String>> rs = processResultSet(stmt.getResultSet());
            closeConnection();
            return new SQLObject(rs);
        } else {
            if (!(stmt.getUpdateCount() == -1)) {
                int rows = stmt.getUpdateCount();
                closeConnection();
                return new SQLObject(rows);
            } else {
                String cmd = sql.split(" ")[0].trim().toUpperCase();
                closeConnection();
                return new SQLObject(cmd);
            }
        }
    }
    
    //Utility
    public List<ArrayList<String>> processResultSet(ResultSet rs) throws SQLException{
        //Initiate results list
        List<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
        
        //Get the column count
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        
        //Build the column headings list
        ArrayList<String> columnHeadings = new ArrayList<String>();
        for (int i = 1; i <= columnCount; i++) {
            columnHeadings.add(rsmd.getColumnLabel(i));
        }
        results.add(columnHeadings);
        
        //Fill Results
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<String>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getString(i));
            }
            results.add(row);
        }
        
        return results;
    }
    
    public SQLObject listAllTables() throws SQLException{
        openConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs;
        if("h2".equals(driver)){
            rs = stmt.executeQuery("SELECT table_schema || '.' || table_name AS \"Tables\"" +
                    " FROM information_schema.tables" +
                    " WHERE table_schema = SCHEMA()" +
                    " AND table_name like 'TEST%'");
        } else {
            rs = stmt.executeQuery("SELECT s.schemaname || '.' || t.tablename AS \"Tables\"" +
                    " FROM sys.systables t, sys.sysschemas s" +
                    " WHERE t.schemaid = s.schemaid" +
                    " AND t.tabletype = 'T'" +
                    " ORDER BY s.schemaname, t.tablename");
        }
        return new SQLObject(processResultSet(rs));
    }
}
