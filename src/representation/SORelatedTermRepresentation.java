package representation;

public class SORelatedTermRepresentation {
	/**
	 * Possible feature representations
	 */
	public enum SORelatedTermType{
		Surface,
		Best,
		All
	}
	
	/**
	 * 
	 * @author Chaya Liebeskind
	 * This class is responsible for choosing the right index for related term representation
	 *
	 */
	public SORelatedTermRepresentation(String responsaMainDir,SORelatedTermType soRelatedType){
		m_indexDir = responsaMainDir+"/indexes/";
		m_type = soRelatedType;
	}
	
	public SORelatedTermRepresentation(SORelatedTermType soRelatedType){
		m_type = soRelatedType;
	}
	
	/**
	 * Get the index for related term enumeration
	 * Set indication whether to remove features marked with "$"
	 * @param type
	 * @return
	 */
	public String getIndexNameByRepresentation() {
		String indexName = null;
		if (m_type == SORelatedTermType.Surface) {
			indexName = "unigIndex";
			m_removeMarkedFeatures = false;
		}
		else if (m_type == SORelatedTermType.Best) {
			indexName = "indexTagger";
			m_removeMarkedFeatures = true;
		}
		else if (m_type == SORelatedTermType.All) {
			indexName = "index0";
			m_removeMarkedFeatures = true;
		}
		return m_indexDir+indexName;		
	}
	

	public boolean isRemoveMarkedFeatures() {
		return m_removeMarkedFeatures;
	}
	
	public SORelatedTermType getSoRelatedTermType(){
		return m_type;
	}

	private boolean m_removeMarkedFeatures = false;
	private String m_indexDir = "C:\\ResponsaNew\\indexes\\";
	private SORelatedTermType m_type = SORelatedTermType.Surface;

}
