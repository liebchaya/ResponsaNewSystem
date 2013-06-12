package morphology;

import hebmorph.HspellHebrewToken;
import hebmorph.Lemmatizer;
import hebmorph.MorphData;
import hebmorph.datastructures.DictRadix;
import hebmorph.hspell.Loader;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vohmm.corpus.Anal;
import vohmm.corpus.AnalProb;
import vohmm.corpus.CompactCorpusData;
import vohmm.corpus.Token;
import vohmm.corpus.UnknownResolver;
import vohmm.lexicon.BGULexicon;
import vohmm.sw.SimilarWordsMap;
import vohmm.sw.TokenTagsDistributor;
import vohmm.util.MyBufferedReader;

public class MorphLemmatizer {
	
	/**
	 * 
	 * @param taggerDir
	 * @param isMila - true = Mila lemmatizer, false = Hspell lemmatizer
	 * @throws MorphLemmatizerException  
	 */
	public static void initLemmatizer(String taggerDir, Boolean isMila) throws MorphLemmatizerException {
		try {
			m_isMila = isMila;
			if (isMila) {
			String dir = taggerDir; // your tagger-data directory
			vohmm.util.Dir.TAGGER_HOMEDIR = dir + "/";
		    String lexicon = dir + "/lexicon";
		    String knownbitmasks = dir + "/known-bitmasks";
		    String swmap = dir + "/swmap";
		    String ukmodel = dir + "/uk-model";
		    String ukpattern = dir + "/uk-patterns";
		    String compact = dir + "/compact_t";
		    BGULexicon._bHazal = true;
		    m_lex = BGULexicon.fromFile(lexicon);
		    m_lex.setKnownBitmasks(knownbitmasks);
		    UnknownResolver ukResolver = new UnknownResolver(ukmodel,ukpattern,0.01,new CompactCorpusData(new MyBufferedReader(new InputStreamReader(new FileInputStream(compact),"UTF-8"))).getTags());
		    TokenTagsDistributor distributor = new TokenTagsDistributor(new SimilarWordsMap(new MyBufferedReader(new InputStreamReader(new FileInputStream(swmap),"UTF-8"))));
		    m_lex.setDistributor(distributor);
		    //m_lex.setUKResolver(ukResolver);
		    
		    // Other possible ways to call similar tools
//		    AffixFiltering affixFiltering = AffixFiltering.NONE;
//		    m_tagger = new TaggerUtils(vohmm.util.Dir.TAGGER_HOMEDIR, affixFiltering);
//		    m_lex = m_tagger.getLexicon();
//		    
//			m_lex = new MilaMorphAnalyzer2();
			} else {
		    DictRadix<MorphData> dict = Loader.loadDictionaryFromDefaultClasspath(true);
			m_lemmatizer = new Lemmatizer(dict, false);
			}
		} catch (Exception e) {
			throw new MorphLemmatizerException("Problem loading tagger or lemmatizer");
		}
	}
	
	/**
	 * 
	 * @param taggerDir
	 * @throws MorphLemmatizerException  
	 */
	public static void initLemmatizer(String taggerDir) throws MorphLemmatizerException {
		initLemmatizer(taggerDir,true);
	}
	
	/**
	 * Gets all possible lemmas from Hspell lemmatizer
	 * @param term
	 * @return
	 */
	private static Set<String> getAllPossibleLemmasHspell(String term) {
		HashSet<String> lemmas = new HashSet<String>();
		List<HspellHebrewToken> lemmaTokens = m_lemmatizer.lemmatize(term);
		if(lemmaTokens != null) {
			for(HspellHebrewToken l:lemmaTokens)
				lemmas.add(l.getLemma());
		}
		return lemmas;
	}
	
	
	public static Set<String> getAllPossibleLemmas(String term) throws MorphLemmatizerException {
		if(!m_isMila)
			return getAllPossibleLemmasHspell(term);
		HashSet<String> lemmas = new HashSet<String>();
		try { 
			//Token token = m_lex.getTokenAnalysis(term,false,true);	// parameters: token, unknown word analysis, distribution of analyses
			Token token = m_lex.getTokenAnalysis(term);	// parameters: token, unknown word analysis, distribution of analyses
			if ((token.isUnknown() && term.startsWith("ד")) || (token.getAnals().isEmpty() && term.startsWith("ד")))
				token = m_lex.getTokenAnalysis(term.substring(1));
//			if(token.getAnals().isEmpty())
//				token = m_lex.getTokenAnalysis(term);
	        for (AnalProb analProb : token.getAnals()) {
	            Anal anal = analProb.getAnal();
	            String lemma = anal.getLemma().getBaseformStr();
	            if(!lemma.equals("unspecified"))
	            	lemmas.add(lemma);
	        }
		} catch(Exception e) {
			throw new MorphLemmatizerException("Problem getting all possible lemmas for " + term);
		}
        return lemmas;
	}
	
	
	public static Set<String> getAllPossibleLemmasAndRoots(String term) throws MorphLemmatizerException {
		if(!m_isMila)
			return getAllPossibleLemmasHspell(term);
		HashSet<String> lemmas = new HashSet<String>();
		try { 
			//Token token = m_lex.getTokenAnalysis(term,false,true);	// parameters: token, unknown word analysis, distribution of analyses
			Token token = m_lex.getTokenAnalysis(term);	// parameters: token, unknown word analysis, distribution of analyses
			if ((token.isUnknown() && term.startsWith("ד")) || (token.getAnals().isEmpty() && term.startsWith("ד")))
				token = m_lex.getTokenAnalysis(term.substring(1));
//			if(token.getAnals().isEmpty())
//				token = m_lex.getTokenAnalysis(term);
	        for (AnalProb analProb : token.getAnals()) {
	            Anal anal = analProb.getAnal();
	            String lemma = anal.getLemma().getBaseformStr();
	            if(!lemma.equals("unspecified"))
	            	lemmas.add(lemma);
	            String root = anal.getLemma().getRoot();
	            if(!lemma.equals("unspecified"))
	            	lemmas.add(lemma);
	        }
		} catch(Exception e) {
			throw new MorphLemmatizerException("Problem getting all possible lemmas for " + term);
		}
        return lemmas;
	}
	
	/**
	 * Supports only Mila lemmatizer
	 * @param term
	 * @return
	 * @throws MorphLemmatizerException
	 */
	public static Set<String> getMostProbableLemma(String term) throws MorphLemmatizerException {
		HashSet<String> lemmas = new HashSet<String>();
		Token token;
		Anal anal;
		try { 
			if (term.split(" ").length > 1){ 
				String multLemma = "";
				for (String t:term.split(" ")) {
					token = m_lex.getTokenAnalysis(t,false,true);	// parameters: token, unknown word analysis, distribution of analyses
					anal = token.getMostProbableAnal();
					if(anal != null){
				        String lemma = anal.getLemma().getBaseformStr();
				        if(!lemma.equals("unspecified"))
				        	multLemma = multLemma + " " + lemma;	
				        else
				        	multLemma = multLemma + " " + t;
					}
					else
						multLemma = multLemma + " " + t;
				}
				lemmas.add(multLemma.trim());
			}
			else {
				token = m_lex.getTokenAnalysis(term,false,true);	// parameters: token, unknown word analysis, distribution of analyses
	//			Token token = m_lex.getTokenAnalysis(term);	// parameters: token, unknown word analysis, distribution of analyses
	//			if ((token.isUnknown() && term.startsWith("ד")) || (token.getAnals().isEmpty() && term.startsWith("ד")))
	//				token = m_lex.getTokenAnalysis(term.substring(1));
		//		if(token.getAnals().isEmpty())
		//			token = m_lex.getTokenAnalysis(term);
				anal = token.getMostProbableAnal();
				if(anal != null){
			        String lemma = anal.getLemma().getBaseformStr();
			        if(!lemma.equals("unspecified"))
			            	lemmas.add(lemma);
				}
			}
		} catch(Exception e) {
			throw new MorphLemmatizerException("Problem getting all possible lemmas for " + term);
		}
        return lemmas;
	}
	
		/**
	 * Supports only Mila lemmatizer
	 * @param term
	 * @return
	 * @throws MorphLemmatizerException
	 */
	public static String getProbableLemma(String term) throws MorphLemmatizerException {
		String lemma = term;
		try { 
			Token token = m_lex.getTokenAnalysis(term,false,true);	// parameters: token, unknown word analysis, distribution of analyses
//			Token token = m_lex.getTokenAnalysis(term);	// parameters: token, unknown word analysis, distribution of analyses
//			if ((token.isUnknown() && term.startsWith("ד")) || (token.getAnals().isEmpty() && term.startsWith("ד")))
//				token = m_lex.getTokenAnalysis(term.substring(1));
	//		if(token.getAnals().isEmpty())
	//			token = m_lex.getTokenAnalysis(term);
			Anal anal = token.getMostProbableAnal();
			if(anal != null){
		        String curLemma = anal.getLemma().getBaseformStr();
		        if(!curLemma.equals("unspecified"))
		            	lemma=curLemma;
			}
		} catch(Exception e) {
			throw new MorphLemmatizerException("Problem getting all possible lemmas for " + term);
		}
        return lemma;
	}

	
	/**
	 * Gets all possible lemmas 
	 * Additional treatment for וי suffix
	 * e.g.  מכסה - כיסוי
	 * @param term
	 * @return	Set<String>
	 * @throws MorphDistancePrePException
	 */
	public Set<String> getAllPossibleLemmasAddDerivations(String term) throws MorphLemmatizerException {
		HashSet<String> lemmas = new HashSet<String>();
		boolean found = true;
		try { 
			Token token = m_lex.getTokenAnalysis(term);	// parameters: token, unknown word analysis, distribution of analyses
			if ((token.isUnknown() && term.startsWith("ד")) || (token.getAnals().isEmpty() && term.startsWith("ד")))
				token = m_lex.getTokenAnalysis(term.substring(1));
	        for (AnalProb analProb : token.getAnals()) {
	        	found = true;
	            Anal anal = analProb.getAnal();
	            String lemma = anal.getLemma().getBaseformStr();
	            if(lemma.equals("unspecified"))
	            	continue;
	            String tag = anal.getTag().toString();
	            if(tag.contains("verb")||tag.contains("participle")){
	            	if(lemma.endsWith("ה")){
		            	Token t = m_lex.getTokenAnalysis(lemma.substring(0, lemma.length()-1) + "וי");
		            	if(!t.isUnknown()) {
			    			Anal a = t.getMostProbableAnal();
			    			lemma = a.getLemma().getBaseformStr();
			    			if(!lemma.equals("unspecified"))
			    				lemmas.add(lemma);
		            		else
		            			found = false;
		            		}
		                if(t.isUnknown() || !found){
		                	String maleh = lemma.substring(0, lemma.length()-1) + "וי";
		                	maleh = maleh.substring(0,1) + "י" + maleh.substring(1);
		            		t = m_lex.getTokenAnalysis(maleh);
			            	if(!t.isUnknown()) {
				    			Anal a = t.getMostProbableAnal();
				    			lemma = a.getLemma().getBaseformStr();
				    			if(lemma.equals("unspecified"))
					            	continue;
				    			lemmas.add(lemma);
			            		}
			            	}
	            	}	
	            }
	            	
	            lemmas.add(lemma);
	            
	        }
	        lemmas.add(term);
		} catch(Exception e) {
			throw new MorphLemmatizerException("Problem getting all possible lemmas and derivations for " + term);
		}
        return lemmas;
	}

	private static BGULexicon m_lex;
	//private MilaMorphAnalyzer2 m_lex;
	//private TaggerUtils m_tagger = null;
    private static Lemmatizer m_lemmatizer = null;
    private static boolean m_isMila = true;
}
