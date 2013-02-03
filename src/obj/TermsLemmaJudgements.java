package obj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import utils.FileUtils;


/**
 * 
 * @author Chaya Liebeskind
 * This class manage judgments in directories: judgements, import, nojudgements.
 * The evaluation will be done according to directory "current".
 * directory current is a snapshot (manual copy) of directory judgements (at a specific point of time)
 */
public class TermsLemmaJudgements {
	private LinkedList<LemmaJudgement> m_judgements = new LinkedList<LemmaJudgement>();
	private LinkedList<LemmaJudgement> m_emptyJudgements = new LinkedList<LemmaJudgement>();
	private String m_query = null;
	private String m_judgementsDir = null;
	private File m_judgeFile = null;
	private File m_judgeXportFile = null;
	
	private HashMap<String,HashMap<String,Integer>> m_groupLstRelJuges = new HashMap<String, HashMap<String,Integer>>();
	private HashSet<String> m_groupLstNonRelJuges = new HashSet<String>();
	private HashSet<String> m_termsLstRelJuges = new HashSet<String>();
	private HashSet<String> m_termsLstNonRelJuges = new HashSet<String>();
//	private LinkedList<String> m_groupLstAppended = new LinkedList<String>();
	private boolean m_hasMissingTerms = false;
	private HashMap<Integer,String> m_groupsLemmasMap = null;
	
	public TermsLemmaJudgements(String query, String judgementsDir) throws IOException{
		m_query = query;
		m_judgementsDir = judgementsDir;
		m_judgeFile = new File(m_judgementsDir+"\\current\\"+m_query.replaceAll("\"", "_").replaceAll("\'", "")+".terms");
		m_judgeXportFile = new File(m_judgementsDir+"\\export\\"+m_query.replaceAll("\"", "_").replaceAll("\'", "")+".terms");
		System.out.println("Loading judgements for " +query);
		loadClustersDescriptions();
		loadJudgements();
	}
	
	/**
	 * Load .groups file
	 * lemma \t groupId
	 * @throws IOException
	 */
	private void loadClustersDescriptions() throws IOException {
		File lemmasFile = new File(m_judgementsDir+"\\current\\"+m_query.replaceAll("\"", "_").replaceAll("\'", "")+".groups");
		if(lemmasFile.exists()){
			String encoding = FileUtils.getFileEncoding(lemmasFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lemmasFile), encoding));
			m_groupsLemmasMap = new HashMap<Integer, String>();
			String line = reader.readLine();//read header
			while (line!= null){
				int id = Integer.parseInt(line.split("\t")[1]);
				if (!m_groupsLemmasMap.containsKey(id))
					m_groupsLemmasMap.put(id,line.split("\t")[0]);
				line = reader.readLine();
			}
		}
		else
			System.out.println("Missing groups for " +m_query);
		
	}


	/**
	 * Load judgements that hasJudgement()  
	 */
	private void loadJudgements() throws IOException {
		if (m_judgeFile.exists()){
			BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(m_judgeFile), "Windows-1255"));
			String line = reader.readLine();//read header
			line=reader.readLine();//read first line
			while(line!=null){
				// skip empty lines
				if(line.equals(""))
					line=reader.readLine();
				String[] arr = line.split("\t");
				LemmaJudgement j ;
				if(arr.length == 6){ //all judgment's data is available 
					LinkedList<String> suggestions = new LinkedList<String>(); 
					String sug = new String(arr[4]);
					for(String s:sug.split(",")){
						suggestions.add(fix(s));
					}
					int group = Integer.parseInt(arr[5]);
					String lemma = "";
					if(m_groupsLemmasMap !=null && m_groupsLemmasMap.containsKey(group))
						lemma = m_groupsLemmasMap.get(group);
					j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),lemma, suggestions,group);
				}
				else if(arr.length == 5){ // groupId wasn't assigned
					LinkedList<String> suggestions = new LinkedList<String>(); 
					String sug = new String(arr[4]);
					 for(String s:sug.split(",")){
						 suggestions.add(fix(s));
					 }
					 j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),arr[3], suggestions,0);
				}
				else if(arr.length == 4) // no lemma suggestions nor groupId
					j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),arr[3], null,0);
				else {
					j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),null, null,0);
					System.out.println("Error : incorrect format for lemma annotation" +" in file : "+m_judgeFile.getCanonicalPath()+" missing judgement");
				}
				if (j.hasJudgement()){
					if (j.getGroup()>0) {
						if(arr[3] != null){
							String key = m_groupsLemmasMap.get(j.getGroup());
							if (m_groupLstRelJuges.containsKey(key))
								m_groupLstRelJuges.get(key).put(fix(arr[1]),j.getJudge());
							else {
								HashMap<String,Integer> set = new HashMap<String,Integer>();
								set.put(fix(arr[1]),j.getJudge());
								m_groupLstRelJuges.put(key,set);
							}
						}
						else 
							System.out.println("Missing lemma assignment for a correct term");
						if (!m_termsLstRelJuges.contains(fix(arr[1])))
							m_termsLstRelJuges.add(fix(arr[1]));
					}
					if(j.getGroup()<=0) {
						if (!m_groupLstNonRelJuges.contains(fix(arr[1])))
							m_groupLstNonRelJuges.add(fix(arr[1]));
						if (!m_termsLstNonRelJuges.contains(fix(arr[1])))
							m_termsLstNonRelJuges.add(fix(arr[1]));
					}
					if (!m_judgements.contains(j))
						m_judgements.add(j);
				}
				else {
					System.out.println("Error : "+j.toString()+" in file : "+m_judgeFile.getCanonicalPath()+" missing judgement");
					if (!m_emptyJudgements.contains(j))
						m_emptyJudgements.add(j);
				}
				line = reader.readLine();			
			}
		}
		

		if (m_judgeXportFile.exists()){
			BufferedReader reader = new BufferedReader( new InputStreamReader(new FileInputStream(m_judgeXportFile), "Windows-1255"));
			String line = reader.readLine();//read header
			line=reader.readLine();//read first line
			while(line!=null){
				String[] arr = line.split("\t");
				LemmaJudgement j ;
				if(arr.length == 6){ 
					LinkedList<String> suggestions = new LinkedList<String>(); 
					String sug = new String(arr[4]);
					for(String s:sug.split(",")){
						suggestions.add(fix(s));
					}
					int group = Integer.parseInt(arr[5]);
					String lemma = "";
					if(m_groupsLemmasMap !=null && m_groupsLemmasMap.containsKey(group))
						lemma = m_groupsLemmasMap.get(group);
					j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),lemma, suggestions,group);
				}
				else if(arr.length == 5) {
					 LinkedList<String> suggestions = new LinkedList<String>();
					 for(String s:arr[4].split(","))
						 suggestions.add(fix(s));
					 j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),arr[3], suggestions,0);
					 }
				else if(arr.length == 4)
					j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),arr[3], null,0);
				else {
					j = new LemmaJudgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]),null, null,0);
					System.out.println("Error : incorrect format for lemma annotation" +" in file : "+m_judgeFile.getCanonicalPath()+" missing judgement");
				}
				if (!j.hasJudgement())
					if (!m_emptyJudgements.contains(j))
						if (!m_judgements.contains(j))
							m_emptyJudgements.add(j);
				line = reader.readLine();
			}
		}
	}



	public int append(LinkedList<String> terms) {
		int count = 0;
		for (String t:terms){
			LemmaJudgement j = new LemmaJudgement( m_query, t);
			if (!m_judgements.contains(j)){
				if (!m_emptyJudgements.contains(j))
					m_emptyJudgements.add(j);
				count++;
				m_hasMissingTerms = true;
			} 
		}
		System.out.println("[TermsJudgements:append] "+count+" expansion terms have no judgement");
		if(m_hasMissingTerms)
			System.out.println("[TermsJudgements:append] "+" there are expansion terms with no judgement");
		return count;
	}
	
	public int append(HashSet<String> terms) {
		int count = 0;
		for (String t:terms){
			LemmaJudgement j = new LemmaJudgement( m_query, t);
			if (!m_judgements.contains(j)){
				if (!m_emptyJudgements.contains(j))
					m_emptyJudgements.add(j);
				m_hasMissingTerms = true;
				count++;
			} /*else {
				if (!m_groupLstAppended.contains(String.valueOf(getJudge(t))))
					m_groupLstAppended.add(String.valueOf(getJudge(t)));
			}*/
		}
		System.out.println("[TermsJudgements:append] "+count+" expansion terms have no judgement");
		if(m_hasMissingTerms)
			System.out.println("[TermsJudgements:append] "+" there are expansion terms with no judgement");
		return count;
	}
	
	public boolean exportJFile() throws IOException{
		if (m_emptyJudgements.size()>0){
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(m_judgeXportFile), "Windows-1255"));
			writer.write("query\tterm\tjudgement(1,0,-1)\tlemma\tsuggestions\tgroup\n");
			for (LemmaJudgement j: m_emptyJudgements){
				writer.write(j.toString()+"\n");
			}
			for (LemmaJudgement j: m_judgements){
				writer.write(j.toString()+"\n");
			}
			writer.close();
			return true;
		}
		return false;
	}
	
	
	/**
	 * manual judge changed lines that contains terms with " 
	 * it adds " before and after the term, and double the original " to ""
	 * this method fixes this problem and remove unnecessary ". 
	 */
	private String fix(String term){
		String newTerm=term;
		if (term.startsWith("\""))
			newTerm=term.substring(1);
		if (term.endsWith("\""))
			newTerm = newTerm.substring(0,newTerm.length()-1);
		newTerm = newTerm.replaceAll("\"\"", "\"");
		if (newTerm.startsWith("["))
			newTerm=newTerm.substring(1);
		if (newTerm.endsWith("]"))
			newTerm = newTerm.substring(0,newTerm.length()-1);
		return newTerm;
			
	}

	public LinkedList<LemmaJudgement> getEmptyJudgements(){
		return m_emptyJudgements;
	}
	
	public LinkedList<LemmaJudgement> getJudgements(){
		return m_judgements;
	}
	
	public void clear() {
		m_judgements.clear();
		m_emptyJudgements.clear();
		m_query = null;		
	}

	public HashSet<String> getGroupLstRelJudeges(){
		return (HashSet<String>) m_groupLstRelJuges.keySet();
	}
	public HashSet<String> getGroupLstNonRelJudeges(){
		return m_groupLstNonRelJuges;
	}
	
	public HashMap<String,HashMap<String,Integer>> getGroupMapRelJudeges(){
		return m_groupLstRelJuges;
	}
	
	public HashSet<String> getTermsLstRelJudeges(){
		return m_termsLstRelJuges;
	}
	
	public HashSet<String> getTermsLstNonRelJudeges(){
		return m_termsLstNonRelJuges;
	}
	
	
	public Integer getJudge(String term) {
		for(Judgement j: m_judgements)
			if (j.getText().equals(term))
				return j.getJudge();
		return -99;
	}


	public boolean hasMissingTerms() {
		return m_hasMissingTerms;
	}
	
	public String get01judgements(LinkedList<String> expTermsList) {
	String str = "";
	for (String s: expTermsList){
		int j= getJudge(s);
		String jStr="";
		if (j>0)
			jStr="1";
		if (j==0)
			jStr="0";
		if (j<0 && j!=-99)
			jStr="-1";
		if (j==-99)
			jStr="-99";
		
		str=str+jStr+"\t";
	}
		return str;
	}



public String getJudgementsPosGroups(LinkedList<String> expTermsList) {

	String str="";
	for (String s :expTermsList){
		int g = getJudge(s);
		if (g>0)
			if (!str.contains(Integer.toString(g)+"\t"))
				str=str+Integer.toString(g)+"\t";
	}
	return str;
}

public String getGjudgements(LinkedList<String> expTermsList) {
String str = "";
for (String s: expTermsList){
	str=str+Integer.toString(getJudge(s))+"\t";
}
	return str;
}

	
}
