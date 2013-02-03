package obj;


public class Term {

	public Term(String m_value, String m_lemma) {
		super();
		this.m_value = m_value;
		this.m_lemma = m_lemma;
	}
	
	public Term(String m_value) {
		super();
		this.m_value = m_value;
	}
	
	public Term(Term iOther) {
		m_value = iOther.m_value;
		m_lemma = iOther.m_lemma;
	}
	
	public String getValue() {
		return m_value;
	}
	public void setValue(String m_value) {
		this.m_value = m_value;
	}
	public String getLemma() {
		return m_lemma;
	}
	public void setLemma(String m_lemma) {
		this.m_lemma = m_lemma;
	}
	
	@Override
	public String toString() {
		return "Term [m_value=" + m_value + ", m_lemma=" + m_lemma + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_lemma == null) ? 0 : m_lemma.hashCode());
		result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (m_lemma == null) {
			if (other.m_lemma != null)
				return false;
		} else if (!m_lemma.equals(other.m_lemma))
			return false;
		if (m_value == null) {
			if (other.m_value != null)
				return false;
		} else if (!m_value.equals(other.m_value))
			return false;
		return true;
	}
	
	protected String m_value = null;
	protected String m_lemma = null;
	
	
}
