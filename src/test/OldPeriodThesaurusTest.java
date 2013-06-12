package test;

import iterativeQE.SQLAccess;
import iterativeQE.SQLAccess2;

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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import obj.Pair;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

import ac.biu.nlp.nlp.general.file.FileUtils;

import utils.StringUtils;

public class OldPeriodThesaurusTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws  
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException  {
//		String oldIndex = "C:/ResponsaSys/indexes/responsaNgrams";
		String oldIndex = "/home/ir/liebesc/ResponsaSys/indexes/oldResponsa";
//		BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\expansionsForPer.txt"));
//		SQLAccess sql = new SQLAccess();
//		for(int i=1;i<4;i++){
//			HashMap<Integer, Pair<String, String>> expan = sql.SelectExpansions(i);
//			for(int id:expan.keySet()){
//				writer.write(i + "\t" + expan.get(id).key() + "\t" + expan.get(id).value() + "\n");
//			}
//		}
//		writer.close();
		
		OldPeriodThesaurusTest pt = new OldPeriodThesaurusTest(oldIndex);
		BufferedReader reader = new BufferedReader(new FileReader("/home/ir/liebesc/SO-Index/expansionsForPer.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ir/liebesc/SO-Index/expansionsForPer.period"));
		String line = reader.readLine();
		while(line != null){
			String query = line.split("\t")[2];
			int count = pt.countOldPeriod(query);
			writer.write(line+"\t"+count+"\n");
			line = reader.readLine();
		}
		reader.close();
		writer.close();
		

//		OldPeriodThesaurusTest pt = new OldPeriodThesaurusTest(oldIndex);
//		pt.loadFileToList(new File("C:/filterdWords.txt"));
//		
//		SQLAccess2 sql = new SQLAccess2("annotations5000step1");
//		HashSet<String> termSet = sql.SelectTerms();
//		for(String term:termSet){
//			HashMap<Integer, HashSet<String>> thes = pt.loadFullThesaurus(term);
//			System.out.println(term + "\t" + thes.size());
////			for(int id:thes.keySet())
////				for(String s:thes.get(id)){
////					String cleanNgram = s.replaceAll("\\p{Punct}|\\d","");
////					writer.write(cleanNgram+"\n");
////				}
//			pt.filterThesaurus(term, thes);
//		}
//		
//		System.out.println("===========================");
//		pt.printFullThesaurusDetails();
//		
////		writer.close();
//		
////		File words = new File("/home/ir/liebesc/SO-Index/wordsForPer.txt");
////		File filterd = new File("/home/ir/liebesc/SO-Index/filterdWords.txt");
////		pt.filterWords(words, filterd);
//		
//		
////		String db = "baseline";
////		String db = "annotations5000step0";
////		int counter = 0;
////		System.out.println(db+"generation 0"+"*********");
////		for(String term:termSet){
////			HashMap<Integer, HashSet<String>> thes0 = pt.loadConfThesaurus(term,db,0);
////			System.out.println(term + "\t" + pt.countTermRecall(term,thes));
////		}
//		
////		String db = "annotations5000step1";
//		String db = "baseline";
//		System.out.println(db+"generation 2"+"*********");
//		for(String term:termSet){
//////			if(term.equals("אמונה תפלה")){
////			HashMap<Integer, HashSet<String>> thes0 = pt.loadConfThesaurus(term,db,0);
//////			System.out.println(term + "\t" + pt.countTermRecall(term,thes0));
////			HashMap<Integer, HashSet<String>> thes1 = pt.loadConfThesaurus(term,db,1);
//////			System.out.println(term + "\t" + pt.countTermRecall(term,thes1));
////			HashMap<Integer, HashSet<String>> thes2 = pt.loadConfThesaurus(term,db,2);
//////			System.out.println(term + "\t" + pt.countTermRecall(term,thes2));
////			HashMap<Integer, HashSet<String>> thes3 = pt.loadConfThesaurus(term,db,3);
//////			System.out.println(term + "\t" + pt.countTermRecall(term,thes3));
////			System.out.println(term + "\t" + pt.countTermRecallIncrease(term,thes3,thes1,thes0, thes2));
//////			}
//
//			sql = new SQLAccess2(db);
//			HashMap<Integer, HashSet<String>> thes0 = sql.Select(term);
//			System.out.println(term + "\t" + pt.countTermRecall(term,thes0));
//		}
		
		
	}

	public void loadFileToList(File f) throws IOException{
		m_oldTerms = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line = reader.readLine();
		while(line != null){
			m_oldTerms.add(line);
			line = reader.readLine();
		}
		reader.close();
			
	}
	
	void printFullThesaurusDetails(){
		for(String target:m_thesaurusMap.keySet()){
			System.out.println(target + "\t" + m_thesaurusMap.get(target).size());
			if(target.equals("אמונה תפלה"))
				for(int id:m_thesaurusMap.get(target).keySet())
					System.out.println(id + "\t" + m_thesaurusMap.get(target).get(id) );
		}
	}
	
	OldPeriodThesaurusTest(String oldIndex) throws CorruptIndexException, IOException{
		m_oldReader = IndexReader.open(FSDirectory.open(new File(oldIndex)));
		m_thesaurusMap = new HashMap<String, HashMap<Integer,HashSet<String>>>();
	}
	/**
	 * Period classification by appearances in the old corpus
	 * @param ngram
	 * @return
	 * @throws IOException
	 */
	public int countOldPeriod(String ngram) throws IOException{
		String cleanNgram = ngram.replaceAll("\\p{Punct}|\\d","");
		int docFreq = m_oldReader.docFreq(new Term("TERM_VECTOR",cleanNgram));
		return docFreq;
	}
	
	public HashMap<Integer,HashSet<String>> loadFullThesaurus(String target_term) throws ClassNotFoundException, SQLException{
		HashMap<Integer,HashSet<String>> fullThes = new HashMap<Integer, HashSet<String>>();
		// addIterative
		SQLAccess2 sql1 = new SQLAccess2("annotations5000step1");
		fullThes.putAll(sql1.Select(target_term));
		SQLAccess2 sql2 = new SQLAccess2("baseline");
		HashMap<Integer,HashSet<String>> baselineRes = sql2.Select(target_term);
		for(int id:baselineRes.keySet())
			if(!fullThes.containsKey(id))
				fullThes.put(id, baselineRes.get(id));
		      else
	    	 fullThes.get(id).addAll(baselineRes.get(id));
		return fullThes;
	}
	
	public HashMap<Integer,HashSet<String>> loadConfThesaurus(String target_term, String confDb, int generation) throws ClassNotFoundException, SQLException{
		// addIterative
		SQLAccess2 sql1 = new SQLAccess2(confDb);
//		return sql1.Select(target_term);
		return sql1.SelectGeneration(target_term, generation);
	}
	
	public void filterThesaurus(String target_term,HashMap<Integer,HashSet<String>> fullThesaurus) throws ClassNotFoundException, SQLException, IOException{
		HashMap<Integer,HashSet<String>> thes =  new HashMap<Integer, HashSet<String>>();
		for(int id:fullThesaurus.keySet()){
			int counter = 0;
			for(String s:fullThesaurus.get(id)){
				String cleanNgram = s.replaceAll("\\p{Punct}|\\d","");
				if (m_oldTerms.contains(cleanNgram))
					counter += 1;
			}
			if(counter > 0)
				thes.put(id, fullThesaurus.get(id));
		}	
		m_thesaurusMap.put(target_term, thes);
	}
	
	public int countTermRecall(String target_term,HashMap<Integer,HashSet<String>> confThesaurus) {
		int recallCount = 0;
		for(int id:confThesaurus.keySet())
			if(!m_thesaurusMap.get(target_term).containsKey(id)){
//				System.out.println(m_thesaurusMap.get(target_term).get(id) + "\t" + id);
				recallCount += 1;
			}
		return recallCount;
	}
	
	public int countTermRecallIncrease(String target_term,HashMap<Integer,HashSet<String>> confThesaurus, HashMap<Integer,HashSet<String>> prevThesaurus, HashMap<Integer,HashSet<String>> prevThesaurus2, HashMap<Integer,HashSet<String>> prevThesaurus3) {
		int recallCount = 0;
		for(int id:confThesaurus.keySet())
			if(!m_thesaurusMap.get(target_term).containsKey(id)&& !prevThesaurus.containsKey(id) && !prevThesaurus2.containsKey(id) && !prevThesaurus3.containsKey(id))
				recallCount += 1;
		return recallCount;
	}
	
	public void filterWords(File words, File filterd) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(words));
		BufferedWriter writer = new BufferedWriter(new FileWriter(filterd));
		String word = reader.readLine();
		while (word != null){
			 if (countOldPeriod(word)>0 )
				 writer.write(word + "\n");
			 word = reader.readLine();
		}
		reader.close();
		writer.close();
	}
	
	
	private HashMap<String,HashMap<Integer,HashSet<String>>> m_thesaurusMap = null;
	private HashSet<String> m_oldTerms = null;
	private IndexReader m_oldReader;
}
