package so.similarity;

import java.io.File;

import so.SOFeatureVectorInfo;

public class SOSimilarityCalculator {
	
	public SOSimilarityCalculator(SOFeatureVectorInfo info){
		m_vectorsDirName = info.getVectorsDir();
		m_maxVectorSize = info.getTrunactedVectorSize();
		m_splitNum = info.getSplitNum();
		m_scorerName = info.getStatScorerName();
	}

	public void calculateSoSmilarity() throws Exception{
		SOSimilarityAlgorithm algo = new SOSimilarityAlgorithm();
		String targetTermsFileName = m_targetTermsFileName.replace("$", Integer.toString(m_maxVectorSize)).replace("@", m_scorerName);
		File targetVectorsFile = new File(m_vectorsDirName+ "/" + targetTermsFileName);
		algo.computeElementNormalizer(targetVectorsFile);
		for(int i=1;i<=m_splitNum;i++){
			String FullVectorFileName = m_vectorsFileName.replace("$", Integer.toString(m_maxVectorSize)).replace("#", Integer.toString(i)).replace("@", m_scorerName);
			algo.computeElementNormalizer(new File(m_vectorsDirName+ "/" + FullVectorFileName));
			algo.computeRuleSimilarityScores(targetVectorsFile,new File(m_vectorsDirName+ "/" + FullVectorFileName));
		}
		algo.mergeRules(new File(m_vectorsDirName),m_maxVectorSize);
	}
	
	
	
	private String m_vectorsDirName = "/home/ir/liebesc/SO-Index/NewStatisticsFolder/Surface_All";
//	private String m_vectorsDirName = "C:\\Documents and Settings\\HZ\\Desktop\\rules";
	private String m_targetTermsFileName = "Trunc$B_@.txt";
	private String m_vectorsFileName = "Trunc$A#_@.txt";
	private int m_splitNum = 11;
	private int m_maxVectorSize = 1000;
	private String m_scorerName = "Dice";
}
