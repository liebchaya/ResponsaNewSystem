package obj;

import java.util.Comparator;

public class SortByWeightName implements Comparator<WeightedTerm> {

	public int compare(WeightedTerm arg0, WeightedTerm arg1) {
		
		if (Double.compare(arg1.weight(),arg0.weight())!=0)
			return Double.compare(arg1.weight(),arg0.weight());
		
		if (arg1.getValue().compareTo(arg0.getValue())>0)
			return -1;
		
		if (arg1.getValue().compareTo(arg0.getValue())<0)
			return 1;

		return 0;
	}
}
