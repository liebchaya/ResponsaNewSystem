package clustering;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import morphology.MorphLemmatizer;
import morphology.MorphLemmatizerException;



public class ClusterFilter {
	
	/**
	 * Initiates the lemmatizer
	 * @param taggerDir
	 * @param isMila
	 * @throws MorphLemmatizerException
	 */
//	public ClusterFilter(String taggerDir, Boolean isMila) throws ClusteringLemmatizerException {
//		m_lemmatizer = new ClusteringLemmatizer(taggerDir, isMila);
//	}
	
	public ClusterFilter() {
	}
	
	/**
	 * Creates seeds cluster
	 * @param term
	 * @throws MorphLemmatizerException
	 */
	public void createFilterCluster(String term) throws MorphLemmatizerException{
		HashSet<String> terms = new HashSet<String>();
		HashSet<String> lemmas = new HashSet<String>();
		for(String t:term.split(" ")){
			lemmas.addAll(MorphLemmatizer.getAllPossibleLemmas(t));
			terms.add(t);
		}
		m_seedCluster = new Cluster(lemmas,terms,true);
	}
	
	/**
	 * Filters a collection of clusters from a seed cluster
	 * @param col
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Collection<Cluster> filterSeed(Collection<Cluster> col) throws InstantiationException, IllegalAccessException{
	Class<?> cls = col.getClass();
	Collection<Cluster> result = (Collection<Cluster>) cls.newInstance();
    for (Cluster element: col) {
        if (!element.equals(m_seedCluster)) {
            result.add(element);
        	}
    	}
    return result;
	}
	
	/**
	 * Filters a collection duplicated clusters for annotation
	 * @param col
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public LinkedList<Cluster> filterDuplicates(LinkedList<Cluster> retrieved, HashSet<Cluster> relevant, boolean createDummy) throws InstantiationException, IllegalAccessException{
		HashSet<Cluster> used = new HashSet<Cluster>();
		LinkedList<Cluster> newList = new LinkedList<Cluster>();
		int dupCounter = 0;
	    for (Cluster element: retrieved) {
	    	boolean found = false;
	        for(Cluster cls: relevant) {
	        	if(element.equals(cls)){
	        		found = true;
	        		if(!used.contains(cls)){
	        			newList.add(element);
	        			used.add(cls);
	        		}
	        		else
	        		{
	        			dupCounter ++;
	        			/**
	        			 * Add dummy cluster in order to decrease precision and AP due to time waste
	        			 */
	        			if(createDummy)
	        				newList.add(new Cluster(new HashSet<String>(), new HashSet<String>()));
	        		}
	        		break;
	        	}
	        }
	        if(!found)
	        	newList.add(element);
    	}
	    m_duplicateRate = (double)dupCounter/retrieved.size(); //for Macro Averaging
	    m_duplicateNum = dupCounter;
	    return newList;
	}
	
	/**
	 * Filters a collection duplicated clusters
	 * @param col
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public LinkedList<Cluster> filterDuplicatesClusters(LinkedList<Cluster> retrieved) throws InstantiationException, IllegalAccessException{
		LinkedList<Cluster> newList = new LinkedList<Cluster>();
	    for (Cluster cls: retrieved) {
        	if(!newList.contains(cls)){
        			newList.add(cls);}
	    }
	    return newList;
	}
	
	/**
	 * Filters a collection 
	 * @param col
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public LinkedList<Cluster> filterTop(LinkedList<Cluster> retrieved, int topk) throws InstantiationException, IllegalAccessException{
		LinkedList<Cluster> newList = new LinkedList<Cluster>();
		int counter = 0;
	    for(Cluster element: retrieved) {
	    	counter++;
	    	if(counter<=topk)
    			newList.add(element);
	    	else
        		break;
	    }
	    return newList;
	}
	
	/**
	 * Filters a collection 
	 * @param col
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public HashSet<Cluster> filterGoldStandard(HashSet<Cluster> relevantClusterJudges, HashSet<String> relMem) throws InstantiationException, IllegalAccessException{
		HashSet<Cluster> newList = new HashSet<Cluster>();
	    for(Cluster element: relevantClusterJudges) {
	    	boolean bFound = false;
	    	HashSet<String> terms = new HashSet<String>();
	    	terms.addAll(element.getTerms());
	    	terms.addAll(element.getLemmas());
	    	for(String t:terms){
	    		if(relMem.contains(t))
	    		{
	    			bFound=true;
	    			break;
	    		}
	    		
	    	}
	    	if(bFound)
	    		newList.add(element);
	    	
	    }
	    return newList;
	}
	
	public double getDuplicateRate() {
		return m_duplicateRate*100;
	}
	
	public int getDuplicateNum() {
		return m_duplicateNum;
	}
	
//	private ClusteringLemmatizer m_lemmatizer = null;
	private Cluster m_seedCluster = null;
	private double m_duplicateRate = 0;
	private int m_duplicateNum = 0;
}
