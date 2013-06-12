package iterativeQE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import obj.Pair;
import utils.StringUtils;

public class SQLAccess2 {
	
	public SQLAccess2(String dbName) throws ClassNotFoundException, SQLException{
		// This will load the MySQL driver, each DB has its own driver
		m_dbName = dbName;
	      Class.forName("com.mysql.jdbc.Driver");
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://localhost/"+dbName+"?characterEncoding=UTF-8&"
	              + "user=root&password=");
	}
	
	
	
	public HashMap<Integer, HashSet<String>> Select(String target_term) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = connect
	      .prepareStatement("select * from " + m_dbName + ".annotations where target_term= ? and judgement>0; ");
	      preparedStatement.setString(1, target_term);
	      ResultSet rs = preparedStatement.executeQuery();
	      HashMap<Integer, HashSet<String>> lemmaSet = saveResultSet(rs);
	      rs.close();
	      return lemmaSet;
	}
	
	public HashSet<String> SelectTerms() throws SQLException{
		// Select
	      PreparedStatement preparedStatement = connect
	      .prepareStatement("SELECT `query` FROM `annotations` where `judgement` > 0 group by `query`");
	      ResultSet rs = preparedStatement.executeQuery();
	      HashSet<String> lemmaSet = saveTermsSet(rs);
	      rs.close();
	      return lemmaSet;
	}
	
	public HashMap<Integer, HashSet<String>> SelectGeneration(String target_term, int generation) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = connect
	      .prepareStatement("select * from " + m_dbName + ".annotations where target_term= ? and judgement>0 and generation= ? ; ");
	      preparedStatement.setString(1, target_term);
	      preparedStatement.setInt(2, generation);
	      ResultSet rs = preparedStatement.executeQuery();
	      HashMap<Integer, HashSet<String>> lemmaSet = saveResultSet(rs);
	      rs.close();
	      return lemmaSet;
	}
	
	
	
	
	private HashMap<Integer, HashSet<String>> saveResultSet(ResultSet resultSet) throws SQLException {
		HashMap<Integer, HashSet<String>> lemmaSet = new HashMap<Integer, HashSet<String>>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
//	      String result = resultSet.getString("myuser");
	      String result = resultSet.getString("result");
	      int judgement = resultSet.getInt("judgement");
	      
	      HashSet<String> resultInput = StringUtils.convertStringToSet(result);
	      if(!lemmaSet.containsKey(judgement))
	    	  lemmaSet.put(judgement, resultInput);
	      else
	    	  lemmaSet.get(judgement).addAll(resultInput);
	    }
	    return lemmaSet;
	  }
	
	private HashSet<String> saveTermsSet(ResultSet resultSet) throws SQLException {
		HashSet<String> lemmaSet = new HashSet<String>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
//	      String result = resultSet.getString("myuser");
	      String query = resultSet.getString("query");
	      lemmaSet.add(query);
	    }
	    return lemmaSet;
	  }
	
	
	
	 private Connection connect = null;
	 private String m_dbName = null;
	 

}
