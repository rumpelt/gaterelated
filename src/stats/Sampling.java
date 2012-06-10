/**
 * 
 */
package stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;



/**
 * @author ashwani
 *	Some sampling routines using the colt library
 */
public class Sampling {
	
	private static RandomDataImpl randomdataimpl =  new RandomDataImpl();
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
	 * @result : the list to which population will be returned
	 * @return
	 */
	public static List<?> sampleWithoutReplacement(List<?> population,
			 int samplesize , boolean usenewgenerator, boolean reseed , 
			 List<Object> result) {
		
		if (samplesize > population.size())
			return null;
		RandomDataImpl randomData;
		if (usenewgenerator)
			randomData = new RandomDataImpl();
		else
			randomData = randomdataimpl;
			
		if (reseed)
			randomData.reSeed();
		
		Object[] sample = new Object[samplesize];
		sample = randomData.nextSample(population, samplesize);	
	    
		for(Object ob : sample)
			result.add(ob);
		return result;		
	}
	
	
	public static List<String> generatePopulation(HashMap<String, Double> words,
			int popsize , int numscatter) {
		
		ArrayList<String> generatedList = new ArrayList<String>();
		for (String k : words.keySet()) {
			double prob = words.get(k);
			prob = prob * popsize;
			int numtimes = BigDecimal.valueOf(prob).setScale(0, RoundingMode.HALF_UP).intValue();
			for (int count =0 ; count < numtimes ;count++)
				generatedList.add(k);
		}
		if (generatedList.size() ==0 ) {
			for (String k : words.keySet()) {
				generatedList.add(k);
			}
		}
		for (int count=  0 ; count < numscatter ; count++)
			generatedList = (ArrayList<String>) Sampling.sampleWithoutReplacement(generatedList, 
					generatedList.size(), false,false ,new ArrayList<Object>() );
		
		return generatedList;
	}
}
