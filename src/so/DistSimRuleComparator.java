package so;


import java.util.Comparator;

public class DistSimRuleComparator implements Comparator<DistSimRule> {
	@Override
	public int compare(DistSimRule o1, DistSimRule o2) {
		return Double.compare(o1.getScore(), o2.getScore());
	}		
}