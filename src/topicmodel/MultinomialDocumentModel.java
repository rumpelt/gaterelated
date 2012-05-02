/**
 * 
 */
package topicmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Distribution;
import edu.stanford.nlp.stats.IntCounter;

/**
 * 
 * @author ashwani
 * Based on the paper 
 */
public class MultinomialDocumentModel {
	private final String modelName;
	
	private String tempDocId;
	private Counter<String> tempCounterForTempDoc;
	private String topicOfTempDoc;
	
	public void storeAndRemoveDoc(String docId) {
		this.tempDocId = docId;
		this.tempCounterForTempDoc = this.docCounters.get(docId);
		this.topicOfTempDoc = this.docTopicMap.get(docId);
		this.removeDocFromModel(docId);
	}
	
	public void restoreDocaAndCounters(String docId) {
		if (docId.equals(this.tempDocId)) {
			this.docCounters.put(docId, this.tempCounterForTempDoc) ;
			if (this.topicOfTempDoc != null)
				this.docTopicMap.put(docId, this.topicOfTempDoc);
		}
		
	}
	/**
	 * @return the modelName
	 */
	public String getModelName() {
		return modelName;
	}

	public MultinomialDocumentModel(String modelName) {
		this.modelName = modelName;
	}
	/**
	 * Counter for each of the documents
	 */
	private HashMap<String, Counter<String>> docCounters = new HashMap<String,
	Counter<String>>();
	
	/**
	 * Mapping from each of the document to actual topic it was assigned
	 */
	private HashMap<String, String> docTopicMap = new HashMap<String, String>();
	
	public List<String> getTopics() {
		ArrayList<String> topics = new ArrayList<String>();
		for (String topic : this.docTopicMap.values()) {
			if (!topics.contains(topic))
				topics.add(topic);
		}
		Collections.sort(topics);
		return topics;	
	}
	public void addDocModel(String docId, List<String> input, String topicName) {
		Counter<String> counter = new IntCounter<String>();
		
		for (String term : input) {
			counter.incrementCount(term);
		}
		
		if (counter.totalCount() > 0) {
			this.docCounters.put(docId, counter);
			if (topicName != null)
				this.docTopicMap.put(docId, topicName);
		}
			
	}
	
	public void addDocTopic(String docId, String topicName) {
		if (topicName != null)
			this.docTopicMap.put(docId, topicName);
	}
	
	public void addTopicOfDocument(String docid, String topic) {
		this.docTopicMap.put(docid, topic);
	}
	
	public void addCounter(String docid, Counter<String> counter) {
		
		this.docCounters.put(docid, counter);
	}
	
	public void removeDocFromModel(String docId) {
		this.docCounters.remove(docId);
		this.docTopicMap.remove(docId);
	}
	
	public void addCounter(String docid, List<String> input) {
		Counter<String> counter = new IntCounter<String>();
		for (String string : input) {
			counter.incrementCount(string);
		}
		if (counter.keySet().size() > 0)
			this.docCounters.put(docid, counter);
	}
	
	public boolean checkEqualityOfCounter(Counter<String> cnt1,
			Counter<String> cnt2) {
		
		boolean result = true;		
		if (cnt1.keySet().size() != cnt2.keySet().size())
			return false;
		
		for (String key : cnt1.keySet() ) {
			if (cnt1.getCount(key) != cnt2.getCount(key)) {
				result = false;
				break;
			}				
		}		
		return result;
	}
	
	/**
	 * 
	 * P(Wt | Cj)
	 * Following refrences
	 * learning to classify from text from labeled and unlabeled data (kamal nigam
	 * , andrew mccallum)
	 * or 
	 * Comparison of event model for naive bayes text classification (mccallum) 
	 * @param word
	 * @param topic
	 * @return
	 */
	public double getProbabilityOfWordGivenClass(String word, String topic,
			List<String> vocab) {
		
		double wordCount = 1; // initialized to 1 for smoothing
		
		for (String docId : this.docCounters.keySet()) {
			if (topic.equals(this.docTopicMap.get(docId)))
				wordCount = wordCount + this.docCounters.get(docId).getCount(word);
		}
		double totalWordCount = vocab.size(); // initailized to vocab size for smoothing
		for (String term : vocab) {
			for (String docId : this.docCounters.keySet()) {
				if (topic.equals(this.docTopicMap.get(docId)))
					totalWordCount = totalWordCount + 
				this.docCounters.get(docId).getCount(term);
			}
		}
		return wordCount / totalWordCount;
	}
	
	
	public double probabilityOfDocLenght(String docId) {
		Counter<Integer> counter = new IntCounter<Integer>();
		for (String doc: this.docCounters.keySet()) {
			counter.incrementCount((int)this.docCounters.get(doc).totalCount());
		}
		return Distribution.getDistribution(counter).probabilityOf((int)this.docCounters.get(docId).totalCount());
	}
	
	public double probabilityOfDocLenght(List<String> input) {
		Counter<Integer> counter = new IntCounter<Integer>();
		for (String doc: this.docCounters.keySet()) {
			counter.incrementCount((int)this.docCounters.get(doc).totalCount());
		}
		return Distribution.getDistribution(counter).probabilityOf(input.size());
	}
	
	
	public double getProbabilityOfDocGivenClass(String docId, String topicName) {
		
		double prob = 1.0;
		List<String> vocab = new ArrayList<String>();
		
		for (String key : this.docCounters.keySet()) {
			for (String term : this.docCounters.get(key).keySet()) 
				if (!vocab.contains(term))
					vocab.add(term);
		}
		for (String term : vocab) {
			double termCount = this.docCounters.get(docId).getCount(term);
			if (termCount == 0.0)
				continue;
			double termProb = this.getProbabilityOfWordGivenClass(term, topicName, vocab);
			termProb = Math.pow(termProb, termCount);
			termProb = termProb/ factorial((int) termCount);
			prob = prob * termProb;
		}
		prob = factorial((int)this.docCounters.get(docId).totalCount()) * prob;
		return this.probabilityOfDocLenght(docId) * prob;
	}
	
public double getProbabilityOfDocGivenClass(List<String> input , String topicName) {
		if (input.size() == 0)
			return 0.0;
		double prob = 1.0;
		List<String> vocab = new ArrayList<String>();
		
		for (String key : this.docCounters.keySet()) {
			for (String term : this.docCounters.get(key).keySet()) 
				if (!vocab.contains(term))
					vocab.add(term);
		}
		Counter<String> counter = new IntCounter<String>();
		for (String text: input) {
			counter.incrementCount(text);
		}
		
		for (String term : vocab) {
			double termCount = counter.getCount(term);
			if (termCount == 0.0)
				continue;
			double termProb = this.getProbabilityOfWordGivenClass(term, topicName, vocab);
			termProb = Math.pow(termProb, termCount);
			termProb = termProb/ factorial((int) termCount);
			prob = prob * termProb;
		}
		prob = factorial((int)counter.totalCount()) * prob;
		return this.probabilityOfDocLenght(input) * prob;
	}

	public double documentLikelyhood(List<String> input) {
		ArrayList<String> classes = new ArrayList<String>();
		for (String key : this.docTopicMap.values()) {
			if (!classes.contains(key))
				classes.add(key);
		}
		double result = 0.0;
		for (String topic : classes) {
			double classPrior = this.getClassPriorProbability(topic);
			double docProb = this.getProbabilityOfDocGivenClass(input, topic);
			result = result + (classPrior * docProb);
		}
		return result;
	}
	
	public double documentLikelyhood(String  docId) {
		ArrayList<String> classes = new ArrayList<String>();
		for (String key : this.docTopicMap.values()) {
			if (!classes.contains(key))
				classes.add(key);
		}
		double result = 0.0;
		for (String topic : classes) {
			double classPrior = this.getClassPriorProbability(topic);
			double docProb = this.getProbabilityOfDocGivenClass(docId, topic);
			result = result + (classPrior * docProb);
		}
		return result;
	}
	
	public double getProbOfClassGivenDocument(String docId, String topicName) {
		double classPrior = this.getClassPriorProbability(topicName);
		double docCondProb = this.getProbabilityOfDocGivenClass(docId, topicName);
		if (docCondProb == 0.0)
			return 0.0;
		
		double docLikelyHood = this.documentLikelyhood(docId);
		return (classPrior/ docLikelyHood) * docCondProb;
	}
	
	public double getProbOfClassGivenDocument(List<String> input, String topicName) {
		double classPrior = this.getClassPriorProbability(topicName);
		double docCondProb = this.getProbabilityOfDocGivenClass(input, topicName);
		if (docCondProb == 0.0)
			return 0.0;
		double docLikelyHood = this.documentLikelyhood(input);
		return (classPrior/ docLikelyHood) * docCondProb;
	}
	/**
	 * learning to classify from text from labeled and unlabeled data (kamal nigam
	 * , andrew mccallum)
	 * or 
	 * Comparison of event model for naive bayes text classification
	 * P(Cj | Theta)
	 * @return
	 */
	public double getClassPriorProbability(String topicName) {
		double numdocs = this.docTopicMap.size();
		int classcount = 0;
		for (String key : this.docTopicMap.keySet()) {
			if (topicName.equals(this.docTopicMap.get(key)))
				classcount++;
		}
		return classcount / numdocs;
	}
	
	public static int factorial( int iNo ) {

        // Make sure that the input argument is positive

	if (iNo < 0) throw
           new IllegalArgumentException("iNo must be >= 0");

        // Use simple look to compute factorial....

        int factorial = 1;
        for(int i = 2; i <= iNo; i++) 
            factorial *= i;               

        return factorial;
    }
}
