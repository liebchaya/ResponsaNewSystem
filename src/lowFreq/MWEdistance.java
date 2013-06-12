package lowFreq;

import java.util.HashSet;
import java.util.Set;

import com.aliasi.util.Distance;
import com.aliasi.util.Proximity;


public class MWEdistance implements Distance<CharSequence>, Proximity<CharSequence>{

	
	public double proximity(CharSequence s1, CharSequence s2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double distance(CharSequence s1, CharSequence s2) {
		String str1 = s1.toString();
		String str2 = s2.toString();
		if(str1.indexOf(str2)!=-1||str2.indexOf(str1)!=-1)
			return 0;
		return Double.MAX_VALUE;
//		return 1;
	}
	
	}
	

