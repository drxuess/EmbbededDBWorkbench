/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlclient.model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sqlclient.database.DatabaseConnection;

/**
 *
 * @author Morgan
 */
public class SQLObject {
    private List<ArrayList<String>> results = null;
    private List<String> columnHeadings = null;
    private String logMsg = null;
    
    //Constructor for resultset statments
    public SQLObject(List<ArrayList<String>> rs) throws SQLException{
        //Populate the column headings
        columnHeadings = new ArrayList<String>();
        for (String colName : rs.get(0)) {
            columnHeadings.add(colName);
        }
        
        //Remove Column Headings from ArrayList
        rs.remove(0);
        
        //Assign remainder ArrayList of data to the results
        results = rs;

        //Fill Log message
        logMsg = "Returned " + results.size() + " rows";
            
    }
    
    //Constructor for update statements
    public SQLObject(int rows){
        logMsg = "Updated " + rows + " rows";
    }
    
    //Constructor for other statements
    public SQLObject(String cmd){
        logMsg = cmd + " completed";
    }

    public List<ArrayList<String>> getResults() {
        return results;
    }

    public List<String> getColumnHeads() {
        return columnHeadings;
    }

    public String getLogMsg() {
        return logMsg;
    }
    
    
}
