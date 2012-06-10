/**
 * 
 */
package specialstruct;

import cchs.CounterComparison;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.IntCounter;

/**
 * @author ashwani
 *
 */
public class StringCounter extends IntCounter<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3524015720426609382L;

	public boolean equals(Object counter) {
		return CounterComparison.areEqual(this,
				(Counter<String>) counter);
	}
}
