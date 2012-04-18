/**
 * 
 */
package test.misc;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import misc.MedicalTrainer;

import org.junit.Test;
import static org.junit.Assert.* ;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.IntCounter;

/**
 * @author ashwani
 *
 */
public class MedicalTrainerTest {
	
	@Test
	public void test_addToCounter() {
		String input = "I am god of god and god is I am";
		List<String> tokens = new Vector<String>();
		StringTokenizer st = new StringTokenizer(input);
		while(st.hasMoreElements()) {
			tokens.add(st.nextToken());
		}
		Counter<String> counter =  new IntCounter<String>();
		MedicalTrainer.addToCounter(counter, tokens);
		assertTrue(counter.getCount("I") == 2.0000);
		assertTrue(counter.getCount("am") == 2.0000);
		assertTrue(counter.getCount("god") == 3.0000);
		assertTrue(counter.getCount("of") == 1.0000);
		assertTrue(counter.getCount("and") == 1.0000);
		assertTrue(counter.getCount("is") == 1.0000);
		
	}
	
	@Test
	public void test_returnFreqDist() {
		String input = "I am god of god and god is I am god";
		// following will convert to lower case so be care ful
		Counter<String> counter = MedicalTrainer.returnFreqDist(input, 1, 
				true,true, null);
		assertTrue(counter.getCount("i") == 2.0000);
		assertTrue(counter.getCount("am") == 2.0000);
		assertTrue(counter.getCount("god") == 3.0000);
		assertTrue(counter.getCount("of") == 1.0000);
		assertTrue(counter.getCount("and") == 1.0000);
		assertTrue(counter.getCount("is") == 1.0000);
		
		counter = MedicalTrainer.returnFreqDist(input, 2, 
				true,true, null);
		assertTrue(counter.getCount("i am") == 2.0000);
		assertTrue(counter.getCount("god of") == 1.0000);
		assertTrue(counter.getCount("of the") == 0.0000);
	}
	
}
