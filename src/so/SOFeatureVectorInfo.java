package so;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import representation.FeatureRepresentation.FeatureType;
import representation.SORelatedTermRepresentation.SORelatedTermType;

public class SOFeatureVectorInfo {
	public SOFeatureVectorInfo(FeatureType type, SORelatedTermType relatedTermType, String scorerName, int vectorSize, int splitNum, String vectorsDir) {
		super();
		m_featureType = type;
		m_soRelatedTermType = relatedTermType;
		m_statScorerName = scorerName;
		m_trunactedVectorSize = vectorSize;
		m_splitNum = splitNum;
		m_vectorsDir = vectorsDir;
	}
	
	public SOFeatureVectorInfo(File infoFile) throws IOException {
		super();
		loadFeatureVectorInfoFromFile(infoFile);
	}
	
	/**
	 * Print FeatureVectorInfo to an information file in the vectors folder
	 * @throws FileNotFoundException
	 */
	public void printFeatureVectorInfo2File() throws FileNotFoundException{
		File f = new File(m_vectorsDir+"/featureVector.info");
		PrintWriter writer = new PrintWriter(new FileOutputStream(f));
		writer.println(m_featureType.toString() + "\t#featureType");
		writer.println(m_soRelatedTermType.toString() + "\t#soRelatedTermType");
		writer.println(m_statScorerName + "\t#statScorerName");
		writer.println(m_trunactedVectorSize + "\t#trunactedVectorSize");
		writer.println(m_splitNum + "\t#splitNum");
		writer.println(m_vectorsDir.toString() + "\t#vectorsDir");
		writer.close();
	}
	
	/**
	 * Extract FeatureVectorInfo information from a file
	 * @param infoFile
	 * @throws IOException
	 */
	public void loadFeatureVectorInfoFromFile(File infoFile) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(infoFile));
		String line = reader.readLine();
		m_featureType = FeatureType.valueOf(line.split("\t")[0]);
		line = reader.readLine();
		m_soRelatedTermType = SORelatedTermType.valueOf(line.split("\t")[0]);
		line = reader.readLine();
		m_statScorerName = line.split("\t")[0];
		line = reader.readLine();
		m_trunactedVectorSize = Integer.parseInt(line.split("\t")[0]);
		line = reader.readLine();
		m_splitNum = Integer.parseInt(line.split("\t")[0]);
		line = reader.readLine();
		m_vectorsDir = line.split("\t")[0];
		reader.close();
	}
	
	public FeatureType getFeatureType() {
		return m_featureType;
	}
	
	public SORelatedTermType getSORelatedTermType() {
		return m_soRelatedTermType;
	}
	
	public int getTrunactedVectorSize() {
		return m_trunactedVectorSize;
	}
	
	public String getStatScorerName() {
		return m_statScorerName;
	}
	
	public String getVectorsDir() {
		return m_vectorsDir;
	}
	
	public int getSplitNum() {
		return m_splitNum;
	}




	private FeatureType m_featureType;
	private SORelatedTermType  m_soRelatedTermType;
	private int m_trunactedVectorSize = 5000;
	private String m_statScorerName = null;
	private String m_vectorsDir = null;
	private int m_splitNum = 11;
}
