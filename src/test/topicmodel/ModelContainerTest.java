/**
 * 
 */
package test.topicmodel;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import topicmodel.ModelContainer;

import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Distribution;
import edu.stanford.nlp.stats.IntCounter;

/**
 * @author ashwani
 *	Test routine for model container
 */
public class ModelContainerTest {
	
	
	private ModelContainer models;
	
	/**
	 * Mapping from topic to their respective counter of same type.
	 * 
	 */		
	@org.junit.Before
	public void  buildContainer() {
		String containerName;
		
		HashMap<String, Counter<String>> topicCounters = new HashMap<String,
		Counter<String>>();
		
		HashMap<String, Integer>topicCounts = new HashMap<String, Integer>();
		
		Counter<String> firstCounter = new IntCounter<String>();
		Counter<String> secondCounter = new IntCounter<String>();
		Counter<String> thirdCounter = new IntCounter<String>();
		
		String[] breastNbottle = {"breast", "and" ,"bottle","using", "enfamil",
				"breast", "bottle", "using","enfamil","lipil"};
		String[] bottle = {"bottle","using","enfamil","lipil"
				,"bottle","using","carnation","good","start"};
		String[] breast = {"breast", 
				         "breast", "using","bottle","milk"};
		
		for (String s : breastNbottle)
			firstCounter.incrementCount(s);
		for (String s : bottle)
			secondCounter.incrementCount(s);
		for (String s : breast)
			thirdCounter.incrementCount(s);
		
		containerName = "unigram";
		topicCounters.put("breast&bottle", firstCounter);
		topicCounters.put("bottle", secondCounter);
		topicCounters.put("breast", thirdCounter);
		
		topicCounts.put("breast&bottle", 2);
		topicCounts.put("bottle", 2);
		topicCounts.put("breast", 2);
		
		this.models = new ModelContainer(containerName);
		this.models.setTopicCounters(topicCounters);
		this.models.setTopicCounts(topicCounts);
	}
	
	@org.junit.Test
	/**
	 * look at the buildContainer call.
	 * If anything changes in that  class then it can change whole lot of
	 * things in here
	 */
	public void test_getProbabilityOfClass() {
		if (this.models.isUseLaplaceSmoothing()) {
			assertTrue(this.models.getProbabilityOfClass("breast&bottle") == (3.0/9.0));
			assertTrue(this.models.getProbabilityOfClass("bottle") == (3.0/9.0));
		
		}
		else {
			assertTrue(this.models.getProbabilityOfClass("breast&bottle") == (2.0/6.0));
			assertTrue(this.models.getProbabilityOfClass("bottle") == (2.0/6.0));
			assertTrue(this.models.getProbabilityOfClass("breast") == (2.0/6.0));
		}
	}
	
	@org.junit.Test
	public void test_getProbabilityOfx() {
		assertTrue(this.models.getProbabilityOfx("breast") == (5.0/34.0));
		if(this.models.isUseLaplaceSmoothing())
			assertTrue(this.models.getProbabilityOfx("#") == (1.0/34.0));
		else
			assertTrue(this.models.getProbabilityOfx("#") == 0.0);
	}
	
	@org.junit.Test
	public void test_getProbOfxGivenTopic() {
		if (this.models.isUseLaplaceSmoothing()) {
			assertTrue(this.models.getProbOfxGivenTopic("breast", "breast") 
					== (3.0/ (7 * this.models.getProbabilityOfClass("breast"))));
			assertTrue(this.models.getProbOfxGivenTopic("&", "breast") 
					== (1.0/ (3 * this.models.getProbabilityOfClass("breast"))));
		}
		else {
			assertTrue(this.models.getProbOfxGivenTopic("breast", "breast") 
				== (1/(2.0 * this.models.getProbabilityOfClass("breast"))));
			assertTrue(this.models.getProbOfxGivenTopic("#", "breast") == 0.0);
		}

	}
}
