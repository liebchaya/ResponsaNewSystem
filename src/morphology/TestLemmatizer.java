package morphology;

public class TestLemmatizer {

	/**
	 * @param args
	 * @throws MorphLemmatizerException 
	 */
	public static void main(String[] args) throws MorphLemmatizerException {
		String taggerDir = "C:\\projects_ws\\Tagger\\";
		Boolean isMila = true;
		MorphLemmatizer.initLemmatizer(taggerDir, isMila);
		System.out.println(MorphLemmatizer.getMostProbableLemma("קנין"));
		System.out.println(MorphLemmatizer.getMostProbableLemma("מקנה"));
		System.out.println(MorphLemmatizer.getMostProbableLemma("קני"));
		System.out.println(MorphLemmatizer.getMostProbableLemma("שהקנה"));
		System.out.println(MorphLemmatizer.getMostProbableLemma("קונין"));

	}

}
