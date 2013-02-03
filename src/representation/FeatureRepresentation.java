package representation;

public class FeatureRepresentation {
	/**
	 * Possible feature representations
	 */
	public enum FeatureType{
		Surface,
		Best,
		All
	}
	
	/**
	 * 
	 * @author Chaya Liebeskind
	 * This class is responsible for choosing the right index for feature representation
	 *
	 */
	public FeatureRepresentation(String responsaMainDir,FeatureType featureType){
		m_indexDir = responsaMainDir+"/indexes/";
		m_type = featureType;
	}
	
	public FeatureRepresentation(FeatureType featureType){
		m_type = featureType;
	}
	
	/**
	 * Get the index for feature extraction
	 * Set indication whether to remove features marked with "$"
	 * @param type
	 * @return
	 */
	public String getIndexNameByRepresentation() {
		String indexName = null;
		if (m_type == FeatureType.Surface) {
			indexName = "unigIndex";
			m_removeMarkedFeatures = false;
		}
		else if (m_type == FeatureType.Best) {
			indexName = "indexTagger";
			m_removeMarkedFeatures = true;
		}
		else if (m_type == FeatureType.All) {
			indexName = "index0";
			m_removeMarkedFeatures = true;
		}
		return m_indexDir+indexName;		
	}
	

	public boolean isRemoveMarkedFeatures() {
		return m_removeMarkedFeatures;
	}
	
	public FeatureType getFeatureType(){
		return m_type;
	}

	protected boolean m_removeMarkedFeatures = false;
	protected String m_indexDir = "C:\\ResponsaNew\\indexes\\";
	protected FeatureType m_type = FeatureType.Surface;

}
