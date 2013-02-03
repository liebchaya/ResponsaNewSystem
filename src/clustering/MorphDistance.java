package clustering;

import java.util.Set;

import com.aliasi.util.Distance;
import com.aliasi.util.Proximity;


public class MorphDistance implements Distance<CharSequence>, Proximity<CharSequence>{

	
	public MorphDistance(MorphDistancePreprocessing pre) {
		m_morphPreData = pre;
	}
		
	public double proximity(CharSequence s1, CharSequence s2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double distance(CharSequence s1, CharSequence s2) {
		String str1 = s1.toString();
		String str2 = s2.toString();
		Set<String> list1 = null,list2 = null;
		try {
			list1 = m_morphPreData.getLemmasList(str1);
			list2 = m_morphPreData.getLemmasList(str2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(String lemma:list2)
			if(list1.contains(lemma))
				return 0;
//		if(list1.isEmpty() || list2.isEmpty()){
//			if((str1.contains(str2)|| str2.contains(str1)) && (str1.length()>4) && (str2.length() >4))
//					return 0;
//			if(str1.length()>1 && str2.length()>1) {
//				if((str1.contains(str2.substring(1, str2.length()))|| str2.contains(str1.substring(1, str1.length()))) && (str1.length()>5) && (str2.length() >5))
//					return 0;
//				if((str1.contains(str2.substring(0, str2.length()-1))|| str2.contains(str1.substring(0, str1.length()-1))) && (str1.length()>5) && (str2.length() >5))
//					return 0;
//			}
//	}
		return Double.MAX_VALUE;
//		return 1;
		
	
	}
	
	
	private MorphDistancePreprocessing m_morphPreData = null;
}
