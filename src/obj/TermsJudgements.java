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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 
 * @author HZ
 * This class manage judgements in directories: judgements, import, nojudgements.
 * The evatuation will be done according to directory current.
 * directory current is a snapshot (manual copy) of directory judgements (at a specific point of time)
 */
public class TermsJudgements {
	private LinkedList<Judgement> m_judgements = new LinkedList<Judgement>();
	private LinkedList<Judgement> m_emptyJudgements = new LinkedList<Judgement>();
//	private LinkedList<Judgement> m_newEmptyJudgements = new LinkedList<Judgement>();
//	private HashSet<Judgement> m_posJudges = new HashSet<Judgement>();
//	private HashSet<Judgement> m_negJudges = new HashSet<Judgement>();
	private String m_query = null;
	private String m_judgementsDir = null;
	private File m_judgeFile = null;
	private File m_judgeXportFile = null;
	private HashSet<String> m_strLstRelJuges = new HashSet<String>();
	private HashSet<String> m_strLstNonRelJuges = new HashSet<String>();
	
	public TermsJudgements(String query, String judgementsDir) throws IOException{
		m_query = query;
		m_judgementsDir = judgementsDir;
		//importJudgements();
		m_judgeFile = new File(m_judgementsDir+"\\current\\"+m_query.replaceAll("\"", "_").replaceAll("\'", "")+".terms");
		m_judgeXportFile = new File(m_judgementsDir+"\\export\\"+m_query.replaceAll("\"", "_").replaceAll("\'", "")+".terms");
		loadJudgements();
	}
	
	
	/**
	 * load judgements that hasJudgement()  
	 */
	private void loadJudgements() throws IOException {
		//LinkedList<Judgement> judgements = new LinkedList<Judgement>();
		if (m_judgeFile.exists()){
//			BufferedReader reader = new BufferedReader(new FileReader(m_judgeFile));
			FileInputStream fis = new FileInputStream(m_judgeFile);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(fis,"cp1255"));
			String line = reader.readLine();//read header
			line=reader.readLine();//read first line
			while(line!=null){
//				if (line.contains("\""))
//					System.out.println(line);
				String[] arr = line.split("\t");
//				System.out.println(m_judgeFile.getAbsolutePath());
				Judgement j = new Judgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]));
				if (j.hasJudgement()){
					String judge = String.valueOf(j.getJudge());
					if (j.getJudge()>0)
						if (!m_strLstRelJuges.contains(judge))
							m_strLstRelJuges.add(judge);
					if(j.getJudge()<=0)
						if (!m_strLstNonRelJuges.contains(judge))
							m_strLstNonRelJuges.add(judge);
					if (!m_judgements.contains(j))
						m_judgements.add(j);
//					if (j.getJudge()>0)
//						if (!m_posJudges.contains(j))
//							m_posJudges.add(j);
//					if(j.getJudge()<0)
//						if (!m_negJudges.contains(j))
//							m_negJudges.add(j);
				}
				else {
					System.out.println("Error : "+j.toString()+" in file : "+m_judgeFile.getCanonicalPath()+" missing judgement");
					if (!m_emptyJudgements.contains(j))
						m_emptyJudgements.add(j);
//					if (!m_strLstNonRelJuges.contains("-99"))
//						m_strLstNonRelJuges.add("-99");
				}
				line = reader.readLine();			
			}
		}
		

		if (m_judgeXportFile.exists()){
			FileInputStream fis = new FileInputStream(m_judgeXportFile);
	        BufferedReader reader = new BufferedReader(new InputStreamReader(fis,"cp1255"));
//			BufferedReader reader = new BufferedReader(new FileReader(m_judgeXportFile));
			String line = reader.readLine();//read header
			line=reader.readLine();//read first line
			while(line!=null){
				String[] arr = line.split("\t");
				Judgement j = new Judgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]));
				if (!j.hasJudgement())
					if (!m_emptyJudgements.contains(j))
						if (!m_judgements.contains(j))
							m_emptyJudgements.add(j);
				line = reader.readLine();
			}
		}
		//return judgements;
	}


//	/** 
//	 * (1)creates 3 list of judgements: importjudgements,judgements, emptyjudgements
//	 * (2)updates judgements and emptyjudgements according to importjudgements
//	 **/
//	private void importJudgements1() throws IOException{
//		LinkedList<Judgement> import_judgements = new LinkedList<Judgement>();
//		File importFile = new File(m_judgementsDir+"\\import\\"+m_query+".terms");
//		import_judgements = loadJudgements1(importFile);
//		
//		LinkedList<Judgement> judgements = new LinkedList<Judgement>();
//		File judgementsFile = new File(m_judgementsDir+"\\judgements\\"+m_query+".terms");
//		judgements = loadJudgements1(judgementsFile);
//		
//		LinkedList<Judgement> empty_judgements = new LinkedList<Judgement>();
//		File noJudgementsFile = new File(m_judgementsDir+"\\nojudgements\\"+m_query+".terms");
//		empty_judgements = loadEmptyJudgements1(noJudgementsFile);
//
//		for (Judgement j :import_judgements){
//			if (!judgements.contains(j))
//				judgements.add(j);
//		}
////		System.out.println("*** judgements list ***");
////		for (Judgement  j: judgements){
////			System.out.println(j.toString());
////		}
////		System.out.println("***  ***");
////		System.out.println("*** empty judgements list ***");
////		for (Judgement  j: empty_judgements){
////			System.out.println(j.toString());
////		}		
////		System.out.println("***  ***");
//		
//		Iterator<Judgement> iter = empty_judgements.iterator();
//		while (iter.hasNext()){
//			Judgement j = iter.next();
//			if (judgements.contains(j))
//					iter.remove();
//		}
//		m_judgements = judgements;
//		m_emptyJudgements = empty_judgements;
//		
//	}
//	/**
//	 * load judgements that hasJudgement()  
//	 */
//	private LinkedList<Judgement> loadJudgements1(File f) throws IOException {
//		LinkedList<Judgement> judgements = new LinkedList<Judgement>();
//		if (f.exists()){
//			BufferedReader reader = new BufferedReader(new FileReader(f));
//			String line = reader.readLine();//read header
//			line=reader.readLine();//read first line
//			while(line!=null){
////				if (line.contains("\""))
////					System.out.println(line);
//				String[] arr = line.split("\t");
//				Judgement j = new Judgement(arr[0],fix(arr[1]),Integer.parseInt(arr[2]));
//				if (j.hasJudgement()){
//				   if (!judgements.contains(j)){
//					   //System.out.println(j.toString());
//					   judgements.add(j);
//				   }
//				}
//				else EL.info("Error : "+j.toString()+" in file : "+f.getCanonicalPath()+" missing judgement");
//				line = reader.readLine();			
//			}
//		}
//		return judgements;
//	}
//
//	/**
//	 * load judgements that  !hasJudgement()  (judge=-99)
//	 */ 
//	private LinkedList<Judgement> loadEmptyJudgements1(File f) throws IOException {
//		LinkedList<Judgement> emptyJudgements = new LinkedList<Judgement>();
//		if (f.exists()){
//			BufferedReader reader = new BufferedReader(new FileReader(f));
//			String line = reader.readLine();//read header
//			line=reader.readLine();//read first line
//			while(line!=null){
////				if (line.contains("\""))
////					System.out.println(line);
//				String[] arr = line.split("\t");
//				Judgement j = new Judgement(arr[0],arr[1],Integer.parseInt(arr[2]));
//				if (!j.hasJudgement()){
//					if (!emptyJudgements.contains(j)){
//						//System.out.println(j.toString());
//						emptyJudgements.add(j);
//					}
//				}
//				else EL.info("Error : "+j.toString()+" in file : "+f.getCanonicalPath()+" has judgement");
//				line = reader.readLine();			
//			}
//		}
//		return emptyJudgements;
//	}
	
//	public int append(LinkedList<WeightedTerm> terms) throws CorruptIndexException, IOException{
//		int count = 0;
//		for (WeightedTerm t:terms){
//			Judgement j = new Judgement( m_query, t.normalizedValue());
//			if (!m_judgements.contains(j)){
//				if (!m_emptyJudgements.contains(j))
//					m_emptyJudgements.add(j);
//				count++;
//			}
//		}
//		EL.info("[TermsJudgements:append] "+count+" expansion terms have no judgement");
//		return count;
//	}
	
	public int append(LinkedList<String> terms) throws IOException{
		int count = 0;
		for (String t:terms){
			Judgement j = new Judgement( m_query, t);
			if (!m_judgements.contains(j)){
				if (!m_emptyJudgements.contains(j))
					m_emptyJudgements.add(j);
				count++;
			}
		}
		System.out.println("[TermsJudgements:append] "+count+" expansion terms have no judgement");
		return count;
	}
	
	public void exportJFile() throws IOException{
		if (m_emptyJudgements.size()>0){
//			File expf = new File(m_judgementsDir+"\\export\\"+m_judgeFile.getName());
			FileOutputStream fos = new FileOutputStream(m_judgeXportFile);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "Cp1255"));
			writer.write("query\tterm\tjudgement(1,0,-1)\n");
//			for (Judgement j: m_newEmptyJudgements){
//				writer.append(j.toString()+"\n");
//			}
			for (Judgement j: m_emptyJudgements){
				writer.write(j.toString()+"\n");
			}
			for (Judgement j: m_judgements){
				writer.write(j.toString()+"\n");
			}
			writer.close();
		}
	}
	
//	public void writeFile() throws IOException {
//		//write empty judgements to file
//		File f1 = null;
//		BufferedWriter writter1 = null;
//		boolean first = true;
//		
//		for (Judgement j:m_emptyJudgements){
//			if (first){
//				f1 = new File(m_judgementsDir+"\\nojudgements\\"+m_query+".terms");
//				writter1 = new BufferedWriter(new FileWriter(f1));
//				writter1.write("query\tterm\tjudgement(1,0,-1)\n");
//				first = false;
//			}
//			if (!j.hasJudgement())
//				writter1.write(j.toString()+"\n");
//		}
//		if (writter1!=null)
//			writter1.close();
//				
//		//write full judgements
//		File f2 = null;
//		BufferedWriter writter2 = null;
//		first = true;
//		
//		for (Judgement j:m_judgements){
//			if (first){
//				f2 = new File(m_judgementsDir+"\\judgements\\"+m_query+".terms");
//				writter2 = new BufferedWriter(new FileWriter(f2));
//				writter2.write("query\tterm\tjudgement(1,0,-1)\n");
//				first = false;
//			}
//			if (j.hasJudgement())
//				writter2.write(j.toString()+"\n");
//		}
//		if (writter2!=null)
//			writter2.close();
//	}
	
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
		return newTerm;
			
	}

	public LinkedList<Judgement> getEmptyJudgements(){
		return m_emptyJudgements;
	}
	
	public LinkedList<Judgement> getJudgements(){
		return m_judgements;
	}
	public void clear() {
		m_judgements.clear();
		m_emptyJudgements.clear();
//		m_newEmptyJudgements.clear();
//		m_posJudges.clear();
//		m_negJudges.clear();
		m_query = null;		
	}

	public HashSet<String> getStrLstRelJudeges(){
		return m_strLstRelJuges;
	}
	public HashSet<String> getStrLstNonRelJudeges(){
		return m_strLstNonRelJuges;
	}
	
	public Integer getJudge(String term) {
		for(Judgement j: m_judgements)
			if (j.getText().equals(term))
				return j.getJudge();
		return -99;
	}


	public int append(HashSet<String> terms) {
		int count = 0;
		for (String t:terms){
			Judgement j = new Judgement( m_query, t);
			if (!m_judgements.contains(j)){
				if (!m_emptyJudgements.contains(j))
					m_emptyJudgements.add(j);
				count++;
			}
		}
		System.out.println("[TermsJudgements:append] "+count+" expansion terms have no judgement");
		return count;
	}
	
	
}
