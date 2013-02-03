package obj;

import java.io.Serializable;


public class DoubleContainer implements Serializable, Comparable<DoubleContainer>{
	public DoubleContainer(double iStartingValue)
	{
		m_value = iStartingValue;
	}
	
	
	public DoubleContainer(DoubleContainer iOther)
	{
		m_value = iOther.value();
	}
	
	
	public synchronized double set(double iValue)
	{
		m_value = iValue;
		return m_value;
	}
	
	
	public synchronized double add(double iValue)
	{
		m_value += iValue;
		return m_value;
	}
	
	
	public synchronized double sub(double iValue)
	{
		m_value -= iValue;
		return m_value;
	}
	
	
	public synchronized double value()
	{
		return m_value;
	}
	
	
	public String toString()
	{
		return Double.toString(value());
	}
	
	public int compareTo(DoubleContainer o)
	{
		if(m_value<o.m_value)
			return -1;
		if(m_value>o.m_value)
			return 1;
		return 0;
	}
	
	private double m_value;

	private static final long serialVersionUID = -6444798612862166231L;

	
}

