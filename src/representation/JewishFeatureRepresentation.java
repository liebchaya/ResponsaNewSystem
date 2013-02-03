package representation;


public class JewishFeatureRepresentation extends FeatureRepresentation{

	public JewishFeatureRepresentation(FeatureType featureType) {
		super(featureType);
	}
	
	public JewishFeatureRepresentation(String responsaMainDir,FeatureType featureType){
		super(responsaMainDir,featureType);
	}
	
	@Override
	public String getIndexNameByRepresentation() {
		String indexName = "modernJewishOnly";
		m_removeMarkedFeatures = false;
		return m_indexDir+indexName;	
	}

}
