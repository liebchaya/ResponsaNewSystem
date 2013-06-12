package morphology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vohmm.application.SimpleTagger3;
import vohmm.corpus.AffixFiltering;
import vohmm.corpus.Sentence;
import vohmm.corpus.Token;
import vohmm.corpus.TokenExt;
import vohmm.lexicon.BGULexicon;
import vohmm.sw.PluralForms;
import vohmm.util.Logger;

public class Tagger {
	private static SimpleTagger3 m_tagger;
	

	public static void init(String taggerHomdir) throws Exception{  
		m_tagger = new SimpleTagger3(taggerHomdir,vohmm.application.Defs.TAGGER_OUTPUT_FORMAT_BASIC,false,false,false,false,null,AffixFiltering.NONE);
		BGULexicon._bHazal = true;
	}
	
	
	private static List<Sentence> getTaggedSentences(String str) throws Exception  {
		return m_tagger.getTaggedSentences(str);
	}
	
	public static Set<String>  getTaggerLemmas(String str) throws Exception{
		HashSet<String> lemmas = new HashSet<String>();
		String lemma = "";
		for (Sentence sentence : getTaggedSentences(str)) {
			for (TokenExt token : sentence.getTokens()) {
				lemma = lemma + " " + token._token.getSelectedAnal().getLemma().getBaseformStr();
		 }
		}
		if (!lemma.equals(""))
			lemmas.add(lemma);
		return lemmas;
	}

}
