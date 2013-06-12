package representation;


public class JewishFeatureRepresentation extends FeatureRepresentation{

	public JewishFeatureRepresentation(FeatureType featureType, String modernJewishIndex) {
		super(featureType);
		m_modernJewishIndexName = modernJewishIndex;
	}
	
	
	@Override
	public String getIndexNameByRepresentation() {
		m_removeMarkedFeatures = false;
		return m_modernJewishIndexName;	
	}

	private String m_modernJewishIndexName;
}
