/**
 * 
 */
package stats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Distribution;

/**
 * @author ashwani
 * Routine to calculate KL divergence using Stanford NLP package.
 */
public class KLDivergence {
	public static double kldivergence(Counter cntr1, Counter cntr2) {
		double divergence = 0.0;
		
		Iterator i1 = cntr1.keySet().iterator();
		Iterator i2 = cntr2.keySet().iterator();
		HashSet allkeys = new HashSet();
		while(i1.hasNext()) {
			allkeys.add(i1.next());
		}
		while(i2.hasNext()) {
			allkeys.add(i2.next());
		}
		Iterator all = allkeys.iterator();
		Distribution d1 = Distribution.laplaceSmoothedDistribution(
				cntr1, cntr1.keySet().size());
		
		Distribution d2 = Distribution.laplaceSmoothedDistribution(
				cntr2, cntr2.keySet().size());
		while (all.hasNext()) {
			Object i = all.next();
			divergence = divergence + d1.probabilityOf(i) * (d1.logProbabilityOf(i)
					- d2.logProbabilityOf(i));
		}
		return divergence;
	}
	
	public static Counter<String> removeSingleCounteTerms(Counter<String>  counter) {
		ArrayList<String> tobeRemoved = new ArrayList<String>();
		for (String s : counter.keySet()) {
			if (counter.getCount(s) == 1)
				tobeRemoved.add(s);
		}
		for (String key : tobeRemoved)
			counter.remove(key);
		return counter;
	}
}
