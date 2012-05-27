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
	

	/**
	 * Given a list of object, this routine returns a sample of population
	 * of size sample size choosen at random without replacement from the 
	 * orginal population. Uses a random number generator which is seeded on
	 * the current system time.
	 * If sample size is equal to the population size then population is shuffled
	 * and the original population is returned to avoid creation new set of object.
	 * i.e the object in the original population are shuffled.
	 * @param population
	 * @param samplesize
	 * @return
	 */
	public static List<?> sampleWithoutReplacement(List<?> population,
			 int samplesize) {
		
		if (samplesize > population.size())
			return null;
		
		long[] values = new long[samplesize];
		
		RandomSampler.sample((long)samplesize,(long) population.size(), samplesize,(long) 0, values, 0,
				RandomEngine.makeDefault());
	
		List<Object> sample = new ArrayList<Object>();
			
		for(long index : values) {			
			sample.add(population.get((int)index));
		}
		return sample;		
	}
}
