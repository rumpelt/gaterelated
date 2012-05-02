/**
 * 
 */
package stats;

import java.util.ArrayList;
import java.util.List;

import cern.jet.random.engine.RandomEngine;
import cern.jet.random.sampling.RandomSampler;

/**
 * @author ashwani
 *	Some sampling routines using the colt library
 */
public class Sampling {
	
	public static List<Object> sampleWithoutReplacement(List<Object> population,
			 int count) {
		if (count > population.size())
			return null;
		long[] values = new long[count];
		RandomSampler.sample((long)count,(long) count, count,(long) 0, values, 0,
				RandomEngine.makeDefault());
		List<Object> sample = new ArrayList<Object>();
		for(long index : values) {
			sample.add(population.get((int)index));
		}
		return sample;
	}
}
