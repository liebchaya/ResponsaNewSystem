package obj;



public class WeightedTerm extends Term{
	public WeightedTerm(Term iTerm, double iWeight)
	{
		super(iTerm);
		m_weight = iWeight;
	}

	public double weight()
	{
		return m_weight;
	}
	public void setPeriodProfile(int[] pp){
		for (int i=0;i<=4;i++)
			m_periodProfile[i]=pp[i];
	}
	
	public int[] getPeriodProfile(){
		return new int[]{m_periodProfile[0],m_periodProfile[1],m_periodProfile[2],m_periodProfile[3],m_periodProfile[4]};
	}
	
	public String getPpString(){
		return m_periodProfile[1]+"\t"+m_periodProfile[2]+"\t"+m_periodProfile[3]+"\t"+m_periodProfile[4];
	}
	
	public String getString (){
		return m_value+"\t"+m_weight;
	}
	public String getFullString(){
		String pp = "";
		if (!(m_periodProfile[1]==-1 && m_periodProfile[2]==-1 
				&& m_periodProfile[3]==-1 && m_periodProfile[4]==-1))
			pp = m_periodProfile[1]+"\t"+m_periodProfile[2]+"\t"+m_periodProfile[3]+"\t"+m_periodProfile[4];
		return m_value+"\t"+m_weight+"\t"+pp;
		       
	}
	
	public boolean hasRelevance(){
		if (m_relevance == 0 || m_relevance == 1)
			return true;
		return false;
	}
	public void setRelevance(int r){
		m_relevance = r;
	}
	public int getRelevance(){
		return m_relevance;
	}
	
	private double m_weight = -1;
	private int[] m_periodProfile=new int[]{-1,-1,-1,-1,-1};
	private int m_relevance = -99;

	
	
}
