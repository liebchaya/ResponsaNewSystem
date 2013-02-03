/**
 * 
 */
package obj;

import java.util.List;

/**
 * @author Chaya Liebeskind
 * Add lemma details to Judgement
 */
public class LemmaJudgement extends Judgement {
	
	private String m_lemma = null;
	private List<String> m_suggestion = null;
	private int m_group = 0; 
	
	public int getGroup(){
		return m_group;
	}

	public LemmaJudgement(String query, String text){
		super(query,text);
	}

	
	public LemmaJudgement(String query, String text, int judge, String lemma, List<String> suggestion, int group){
		super(query,text,judge);
		m_lemma = lemma;
		m_suggestion = suggestion;
		m_group = group;
	}
	
	public LemmaJudgement(String query,  int judge, String lemma, List<String> suggestion, int group){
		super(query,judge);
		m_lemma = lemma;
		m_suggestion = suggestion;
		m_group = group;
		
	}
	
	public LemmaJudgement(String query){
		super(query);
	}
	
	public String toString(){
		String lemma = m_lemma;
		if (lemma == null)
			lemma = "";
		String suggestions = "";
		if(m_suggestion != null)
			suggestions = m_suggestion.toString();
		return super.toString()+lemma+"\t"+suggestions+"\t"+m_group;
	}

}
