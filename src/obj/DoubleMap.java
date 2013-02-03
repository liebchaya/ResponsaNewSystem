package obj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DoubleMap<K> extends HashMap<K,DoubleContainer> {

	private static final long serialVersionUID = 1L;

	public DoubleMap() {
		super();
	}
	
	public void inc(K k, double n) {
		
		if(!containsKey(k))
			put(k,new DoubleContainer(n));
		else
		{
			DoubleContainer d = get(k);
			d.add(n);
		}
		m_total+=n;
	}
	
	public double getValue(K k) {
		
		DoubleContainer d = get(k);
		if(d==null)
			return 0;
		return d.value();
	}
	
	public double getTotal() {
		return m_total;
	}
	
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		Iterator<K> iter = keySet().iterator();
		while(iter.hasNext()) {
			K key = iter.next();
			double value = get(key).value();
			sb.append(key.toString()+"\t"+value+"\n");
		}
		return sb.toString();
	}
	
	/**
	 * sampling in linear time
	 * @return a key sampled from the distribution of objects
	 */
	public K sample() {
	
		Random rand = new Random();
		int randNum = rand.nextInt(new Double(m_total).intValue());
		int sum = 0;
		Iterator<K> keyIter = this.keySet().iterator();
		while(keyIter.hasNext()) {
			
			K key = keyIter.next();
			sum+=this.get(key).value();
			if(sum>randNum)
				return key;
		}
		//should not be reached
		return null;
	}
	
	public List<K> getMostFrequent(int n) {
		
		List<K> result = new LinkedList<K>();
		
		List<K> keys = new ArrayList<K>(keySet());
		List<DoubleContainer> counters = new ArrayList<DoubleContainer>(values());
		
		for(int i = 0; i < n; i++) {
			
			int maxInd = counters.indexOf(Collections.max(counters));
			result.add(keys.get(maxInd));
			keys.remove(maxInd);
			counters.remove(maxInd);
			
			if(keys.size()==0)
				break;
		}
	
		return result;
		
	}
	
	private double m_total;
}

