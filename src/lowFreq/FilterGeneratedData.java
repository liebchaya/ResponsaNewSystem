package lowFreq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FilterGeneratedData{
	
	
	 public FilterGeneratedData(boolean includeOld, boolean includeModern, boolean addDocumnt2SearchResults, boolean inModernCorpus, double modernJewishThreshold,double oldThreshold, double freqThreshold) {
		m_freqThreshold = freqThreshold;
		m_MWEmodernJewishThreshold = modernJewishThreshold;
		m_MWEoldThreshold = oldThreshold;
		m_bAddDocumnt2SearchResults = addDocumnt2SearchResults;
		m_bIncludeModern = includeModern;
		m_bIncludeOld = includeOld;
		m_bInModernCorpus = inModernCorpus;
		generateFilterName();
	}
	 
	public FilterGeneratedData(boolean includeOld, boolean includeModern, boolean addDocumnt2SearchResults, boolean inModernCorpus) {
			m_bAddDocumnt2SearchResults = addDocumnt2SearchResults;
			m_bIncludeModern = includeModern;
			m_bIncludeOld = includeOld;
			m_bInModernCorpus = inModernCorpus;
			generateFilterName();
	}
	
	public void filterFiles(String outputDir) throws IOException{
		File dir = new File(outputDir+"/Surface_Surface/");
		for (File f:dir.listFiles()) {
			System.out.println("File: " + f.getAbsolutePath());
			if (f.getName().endsWith("_Data.txt")) {
				BufferedReader reader = new BufferedReader(new FileReader(f));
				BufferedWriter writer = new BufferedWriter(new FileWriter(dir.getAbsolutePath() + "/" + f.getName().replace("_Data.txt","_" + m_filterName + ".filter")));
				String line = reader.readLine();
				writer.write(line + "\n");
				line = reader.readLine();
				while (line!=null){
					if (accept(line))
						writer.write(line + "\n");
					line = reader.readLine();
				}
				reader.close();
				writer.close();
			}
		}
	}
	/**
	 * fileLine format: 
	 * term + "\t" + dice + "\t" + freq + "\t" + oldPeriodCount + "\t" + contribPair.key() + "\t" + contribPair.value() + "\t" + modernInter + "\t" + meModernJewishScore + "\t" + meOldScore + "\n");
	 * @param fileLine
	 * @return
	 */
	private boolean accept(String fileLine){
		String tokens[] = fileLine.split("\t");
//		System.out.println(tokens[0]);
		if (!m_bIncludeOld && Integer.parseInt(tokens[3]) > 0)
			return false;
		if (!m_bIncludeModern && Integer.parseInt(tokens[3]) == 0)
			return false;
		if (m_bAddDocumnt2SearchResults && Integer.parseInt(tokens[4]) == 0)
			return false;
		if (m_bInModernCorpus && Integer.parseInt(tokens[6]) == 0)
			return false;
		if (m_MWEmodernJewishThreshold > -1 && Double.parseDouble(tokens[7]) < m_MWEmodernJewishThreshold)
			return false;
		if (m_MWEoldThreshold > -1 && Double.parseDouble(tokens[8]) < m_MWEoldThreshold)
			return false;
		if (m_freqThreshold > -1 && Double.parseDouble(tokens[2]) < m_freqThreshold)
			return false;
		return true;
	}
	
	private void generateFilterName(){
		String name = "";
		if(m_bIncludeOld)
			name = "Old";
		else
			name = "NoOld";
		
		if(m_bIncludeModern)
			name += "Modern";
		else
			name = "NoModern";
		
		if(m_bAddDocumnt2SearchResults)
			name += "AddDocument";
		
		if(m_bInModernCorpus)
			name += "InModernCorpus";
		
		if(m_MWEoldThreshold > -1) {
			String oldThresh = Double.toString(m_MWEoldThreshold);
			name = name + "OldMWE" + oldThresh.replace(".", "_");
		}
		
		if(m_MWEmodernJewishThreshold > -1) {
			String oldThresh = Double.toString(m_MWEmodernJewishThreshold);
			name = name + "ModernJewishMWE" + oldThresh.replace(".", "_");
		}
		
		if(m_freqThreshold > -1) {
			String oldThresh = Double.toString(m_freqThreshold);
			name = name + "Freq" + oldThresh.replace(".", "_");
		}
		m_filterName = name;	
	}
	 
	 
	private boolean m_bIncludeOld = true;
	private boolean m_bIncludeModern = true;
    private boolean m_bAddDocumnt2SearchResults = true;
    private boolean m_bInModernCorpus = true;
	private double m_MWEoldThreshold = -1;
	private double m_MWEmodernJewishThreshold = -1;
	private double m_freqThreshold = -1;
	private String m_filterName;
 
 
}
