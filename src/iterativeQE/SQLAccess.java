package iterativeQE;

import java.io.BufferedReader;
import java.io.FileReader;
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

import ac.biu.nlp.nlp.general.file.FileUtils;

import obj.Pair;
import utils.StringUtils;

public class SQLAccess {
	
	/**
	 * Initialize the connection to the database
	 * @param databaseName
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public SQLAccess(String databaseName) throws ClassNotFoundException, SQLException{
		// This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.jdbc.Driver");
	      m_databaseName = databaseName;
	      // Setup the connection with the DB
	      m_connection = DriverManager
	          .getConnection("jdbc:mysql://localhost/"+m_databaseName+"?characterEncoding=UTF-8&"
	              + "user=root&password=");
	}
	
	public void Insert(String query, String result,String lemma, String target_term, int generation, int position, int judgement) throws SQLException{
	      // PreparedStatements can use variables and are more efficient
	      PreparedStatement preparedStatement = m_connection
	          .prepareStatement("insert into  baseline.annotations values (default, ?, ?, ?, ?, ? , ?, ?)");
	      // Parameters start with 1
	      preparedStatement.setString(1, query);
	      preparedStatement.setString(2, result);
	      preparedStatement.setString(3, lemma);
	      preparedStatement.setString(4, target_term);
	      preparedStatement.setInt(5, generation);
	      preparedStatement.setInt(6, position);
	      preparedStatement.setInt(7, judgement);
	      preparedStatement.executeUpdate();
	}
	
	public void InsertExpansions(String query, String result, String target_term, int generation) throws SQLException{
	      // PreparedStatements can use variables and are more efficient
	      PreparedStatement preparedStatement = m_connection
	          .prepareStatement("insert into  annotations5000step1.expansions values (default, ?, ?, ?, ?)");
	      // Parameters start with 1
	      preparedStatement.setString(1, query);
	      preparedStatement.setString(2, result);
	      preparedStatement.setString(3, target_term);
	      preparedStatement.setInt(4, generation);
	      preparedStatement.executeUpdate();
	}
	
	public HashMap<String, Integer> Select(String target_term) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select * from annotations5000step1.annotations where target_term= ? ; ");
	      preparedStatement.setString(1, target_term);
	      ResultSet rs = preparedStatement.executeQuery();
	      HashMap<String, Integer> lemmaSet = saveResultSet(rs);
	      rs.close();
	      return lemmaSet;
	}
	
	public LinkedList<String> SelectGroups(String target_term) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select * from annotations5000step1.annotations where target_term= ? and judgement>0 ; ");
	      preparedStatement.setString(1, target_term);
	      ResultSet rs = preparedStatement.executeQuery();
	      LinkedList<String> lemmaSet = saveGroupsResultSet(rs);
	      rs.close();
	      return lemmaSet;
	}
	
	public HashMap<Integer,Pair<String, String>> SelectExpansions(int generation) throws SQLException{
		// Select
	      PreparedStatement preparedStatement = m_connection
	      .prepareStatement("select * from annotations5000step1.expansions where generation= ? order by id; ");
	      preparedStatement.setInt(1, generation);
	      ResultSet rs = preparedStatement.executeQuery();
	      HashMap<Integer,Pair<String, String>> pairMap = saveExpansions(rs);
	      rs.close();
	      return pairMap;
	}
	
	private HashMap<String, Integer> saveResultSet(ResultSet resultSet) throws SQLException {
		HashMap<String, Integer> lemmaSet = new HashMap<String, Integer>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
//	      String result = resultSet.getString("myuser");
	      String lemma = resultSet.getString("lemma");
	      int judgement = resultSet.getInt("judgement");
	      
	      HashSet<String> lemmaInput = StringUtils.convertStringToSet(lemma);
	      for(String l:lemmaInput)
	    	  lemmaSet.put(l,judgement);
	    }
	    return lemmaSet;
	  }
	
	private LinkedList<String> saveGroupsResultSet(ResultSet resultSet) throws SQLException {
		LinkedList<String> lemmaSet = new LinkedList<String>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
//	      String result = resultSet.getString("myuser");
	      String lemma = resultSet.getString("lemma");
	      int judgement = resultSet.getInt("judgement");
	      String s = lemma+"\t"+judgement;
	      lemmaSet.add(s);
	    }
	    return lemmaSet;
	  }
	private HashMap<Integer,Pair<String,String>> saveExpansions(ResultSet resultSet) throws SQLException {
		HashMap<Integer,Pair<String,String>> pairMap = new HashMap<Integer,Pair<String,String>>();
	    // ResultSet is initially before the first data set
	    while (resultSet.next()) {
	      // It is possible to get the columns via name
	      // also possible to get the columns via the column number
	      // which starts at 1
	      // e.g. resultSet.getSTring(2);
	      String result = resultSet.getString("result");
	      String target_term = resultSet.getString("target_term");
	      int id = resultSet.getInt("id");
	      pairMap.put(id,new Pair<String, String>(target_term,result));
	    }
	    return pairMap;
	  }
	
	
	
	
	 private Connection m_connection = null;
	 private String m_databaseName;
	 

}
