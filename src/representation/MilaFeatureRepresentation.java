package representation;


public class MilaFeatureRepresentation extends FeatureRepresentation{

	public MilaFeatureRepresentation(FeatureType featureType) {
		super(featureType);
	}
	
	public MilaFeatureRepresentation(String responsaMainDir,FeatureType featureType){
		super(responsaMainDir,featureType);
	}
	
	@Override
	public String getIndexNameByRepresentation() {
		String indexName = "modernHebrew";
		m_removeMarkedFeatures = false;
		return m_indexDir+indexName;	
	}

}
