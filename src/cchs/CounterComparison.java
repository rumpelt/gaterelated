/**
 * 
 */
package cchs;
import edu.stanford.nlp.stats.Counter;
/**
 * @author ashwani
 *
 */
public final class CounterComparison {
	public static boolean areEqual(Counter<String> c1, Counter<String> c2) {
		if (c1.keySet().size() != c2.keySet().size())
			return false;
		for (String key : c1.keySet()) {
			if (c1.getCount(key) != c2.getCount(key))
				return false;
		}
		return true;
	}
}
