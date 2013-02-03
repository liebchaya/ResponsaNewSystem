package obj;

import java.util.Comparator;



	public class PairComparator implements Comparator<Pair<Integer, Double>> {
		@Override
		public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
			return Double.compare(o1.value(), o2.value());
		}		
	}
