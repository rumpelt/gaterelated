/**
 * 
 */
package topicmodel;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;


import cchs.CategoryVectors;
import stats.PositionIntCounter;
import stats.Sampling;

import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Distribution;
import edu.stanford.nlp.stats.IntCounter;
/**
 * 
 * @author ashwani Based on the paper
 */
public class MultinomialDocumentModel {
	private final String modelName;

	private String tempDocId;
	private PositionIntCounter tempCounterForTempDoc;
	private String topicOfTempDoc;

	public void storeAndRemoveDoc(String docId) {
		this.tempDocId = new String(docId);
		this.tempCounterForTempDoc = this.docCounters.get(docId);
		this.topicOfTempDoc = this.docTopicMap.get(docId);
		this.removeDocFromModel(docId);
	}

	public void restoreDocAndCounters(String docId) {
		if (docId.equals(this.tempDocId)) {
			if (this.tempCounterForTempDoc != null)
				this.docCounters.put(docId, this.tempCounterForTempDoc);
			if (this.topicOfTempDoc != null)
				this.docTopicMap.put(docId, this.topicOfTempDoc);
		}

	}
	
	public  IntCounter<String> createGlobalCounter() {
		IntCounter<String> global = new IntCounter<String>();
		for (Counter<String> eachcounter : this.docCounters.values()) {
			global.addAll(eachcounter);
		}
		return global;
	}
        
       
	
	public String printGlobalCounter(boolean ascending) {
		IntCounter<String> global = this.createGlobalCounter();
		StringBuilder sb = new StringBuilder();
		
		for (int count=0; count < global.keySet().size(); count++) {
			String key;
			if (!ascending)
			    key = global.argmax();
			else
				key = global.argmin();			
			sb.append(key + ","+ global.getCount(key)+"\n");			
			global.remove(key);
		}
		return sb.toString();
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
	private HashMap<String, PositionIntCounter> docCounters = new HashMap<String, PositionIntCounter>();

	/**
	 * Mapping from each of the document to actual topic it was assigned
	 */
	private HashMap<String, String> docTopicMap = new HashMap<String, String>();

        public void addModel(MultinomialDocumentModel md) {
            for (String doc : md.getdocCounters().keySet()) {
		Counter<String> counter = this.getdocCounters().get(doc);
		if (counter == null)
		    this.getdocCounters().put(doc, md.getdocCounters().get(doc));
		else
		    counter.addAll(md.getdocCounters().get(doc));
	    }

	    for (String doc : md.getdocTopicMap().keySet()){
		if (this.getdocTopicMap().get(doc) == null){
		    this.getdocTopicMap().put(doc, md.getdocTopicMap().get(doc));
		}
            }
                   	    
        }
        
        public HashMap<String, PositionIntCounter> getdocCounters() {
	    return this.docCounters;
        }

        public HashMap<String, String> getdocTopicMap() {
	    return this.docTopicMap;
        }
	/**
	 * String representaion of various terms and probabiliites etc. Useful for
	 * debug purpose. Created this to debug the trigram model.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("model name " + this.modelName + "\n");
		Counter<String> topics = this.getTopicCounter();
		sb.append("topic frequency and distribution" + "\n");
		for (String key : topics.keySet()) {
			sb.append(" key : " + key + " " + topics.getCount(key));
		}
		sb.append("\n");
		sb.append("topic probabilites" + "\n");
		for (String classes : this.getTopics()) {
			sb.append(" " + classes + " "
					+ this.getClassPriorProbability(classes) + "\n");
		}

		sb.append("\n");
		sb.append("term frequency in various topics" + "\n");
		for (String topic : this.docCounters.keySet()) {
			sb.append("docid :" + topic + "\n");
			Counter<String> counter = this.docCounters.get(topic);
			for (String key : counter.keySet()) {
				sb.append("term : " + key + " count:" + counter.getCount(key)
						+ "\n");
			}
		}
		sb.append("Various distribution parameter. " + "\n");
		for (String docid : this.docCounters.keySet()) {
			// this.storeAndRemoveDoc(docid);
			sb.append("doc id : " + docid + " doc likelyhood "
					+ this.documentLikelyhood(docid) + " ");
			for (String classes : this.getTopics()) {
				sb.append(" prob of class give doc " + classes + " "
					  + this.getProbOfClassGivenDocument(docid, classes, false)
						+ " prob of doc given class "
					  + this.getProbabilityOfDocGivenClass(docid, classes, false));
			}
			sb.append("\n");
			// this.restoreDocAndCounters(docid);
		}
		return sb.toString();
	}

	public Counter<String> getTopicCounter() {
		Counter<String> counter = new IntCounter<String>();
		for (String val : this.docTopicMap.values())
			counter.incrementCount(val);
		return counter;
	}

	public List<String> getTopics() {
		ArrayList<String> topics = new ArrayList<String>();
		for (String topic : this.docTopicMap.values()) {
			if (!topics.contains(topic))
				topics.add(topic);
		}
		if (this.topicOfTempDoc != null
				&& !topics.contains(this.topicOfTempDoc))
			topics.add(this.topicOfTempDoc);
		Collections.sort(topics);
		return topics;
	}

	public void addDocModel(String docId, List<String> input, String topicName) {

		if (input == null || input.size() <= 0)
			return;

		PositionIntCounter counter = new PositionIntCounter();
		counter.addAll(input);
		
		if (counter.totalCount() > 0.0) {
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

	public void addCounter(String docid, PositionIntCounter counter) {
		if (counter != null && counter.size() > 0)
			this.docCounters.put(docid, counter);
	}

	public void removeDocFromModel(String docId) {
		this.docCounters.remove(docId);
		this.docTopicMap.remove(docId);
	}

	public void addCounter(String docid, List<String> input) {
		if (input == null || input.size() <= 0)
			return;
		PositionIntCounter counter = new PositionIntCounter();
        counter.addAll(input);
		
        if (counter.keySet().size() > 0)
			this.docCounters.put(docid, counter);
	}

	public boolean checkEqualityOfCounter(Counter<String> cnt1,
			Counter<String> cnt2) {

		boolean result = true;
		if (cnt1.keySet().size() != cnt2.keySet().size())
			return false;

		for (String key : cnt1.keySet()) {
			if (cnt1.getCount(key) != cnt2.getCount(key)) {
				result = false;
				break;
			}
		}
		return result;
	}

    public double getProbOfCompWordGivenClass(String word, String topic, List<String> vocab,
                                            int prior, boolean useipw) {
	    double cwordcount = prior;
	    HashMap<String, Double> topicProb  = new HashMap<String, Double>();
	    for (String name : this.getTopics()) {
                topicProb.put(name, this.getClassPriorProbability(name));
	    }
	    for (String docId : this.docCounters.keySet()) {
		String localtopic = this.docTopicMap.get(docId);
	        if (!topic.equals(localtopic)) { 
		    if (useipw)
			cwordcount = cwordcount + 
			    (this.docCounters.get(docId).getCount(word) / topicProb.get(localtopic));
		    else
			cwordcount = cwordcount + this.docCounters.get(docId).getCount(word);
	        }
            }
            double totalcompwordcount = vocab.size();
	    for (String docId : this.docCounters.keySet()) {
		String localtopic = this.docTopicMap.get(docId);
		if (!topic.equals(localtopic)) {
		    if (useipw)
                        totalcompwordcount = totalcompwordcount + (this.docCounters.get(docId).totalCount() /
								   topicProb.get(localtopic));
                    else
			totalcompwordcount = totalcompwordcount + this.docCounters.get(docId).totalCount();
		}
	    }
	    return cwordcount / totalcompwordcount;
         }
    
    public List<String>  docTerms(String docId ) {
    	Counter<String> crt = this.docCounters.get(docId);
    	List<String> keys = new ArrayList<String>();
    	for (String key : crt.keySet()) {
    		keys.add(key);
    	}
    	return keys;
    }
	/**
	 * 
	 * P(Wt | Cj) Following refrences learning to classify from text from
	 * labeled and unlabeled data (kamal nigam , andrew mccallum) or Comparison
	 * of event model for naive bayes text classification (mccallum)
	 * 
	 * @param word
	 * @param topic
	 * @return
	 */
	public double getProbabilityOfWordGivenClass(String word, String topic,
			List<String> vocab) {

		double wordCount = 1; // initialized to 1 for smoothing
		
		for (String docId : this.docCounters.keySet()) {
			
			if (topic.equals(this.docTopicMap.get(docId))) {
				if (this.docCounters.get(docId).getCount(word) > 0) {
					wordCount = wordCount
		    				+ this.docCounters.get(docId).getCount(word) ;
				}
			}
		}
		
		double totalWordCount = vocab.size(); // initailized to vocab size for
												// smoothing
		for (String term : vocab) {
			for (String docId : this.docCounters.keySet()) {
				if (topic.equals(this.docTopicMap.get(docId))) {
					if (this.docCounters.get(docId).getCount(term) > 0) {
						totalWordCount = totalWordCount
							+ this.docCounters.get(docId).getCount(term);					}
				}
			}
		}
		return wordCount / totalWordCount;
	}

	public double probabilityOfDocLength(String docId) {
		Counter<Integer> counter = new IntCounter<Integer>();
		for (String doc : this.docCounters.keySet()) {
			counter.incrementCount((int) this.docCounters.get(doc).totalCount());
		}

		return Distribution.getDistribution(counter).probabilityOf(
				(int) this.docCounters.get(docId).totalCount());
	}

	public double probabilityOfDocLength(List<String> input) {
		Counter<Integer> counter = new IntCounter<Integer>();
		for (String doc : this.docCounters.keySet()) {
			counter.incrementCount((int) this.docCounters.get(doc).totalCount());
		}
		return Distribution.getDistribution(counter)
				.probabilityOf(input.size());
	}
	
	/**
	 *Get the vocabulary given a topic or if topic is null then get the whole
	 * vocabulary 
	 * @param topic
	 * @return
	 */
   public List<String> getVocab(String topic) {
	    List<String> vocab = new ArrayList<String>();
	    
		for (String key : this.docCounters.keySet()) {
			if (topic == null || this.docTopicMap.get(key).equals(topic)) {
				for (String term : this.docCounters.get(key).keySet()) {
					if (!vocab.contains(term))
						vocab.add(term);
				}
			}
		}
		return vocab;
	}
   
    public HashMap<String, Double> getDistributionForTopic(String topicName) {
    	HashMap<String, Double> dist = new HashMap<String, Double>();
    	List<String> vocab = this.getVocab(topicName);
    	for  (String v : vocab) {
    		dist.put(v, this.getProbabilityOfWordGivenClass(v, topicName,
    				vocab));
    	}
    	return dist;
    }
   
    public double getProbabilityOfDocGivenClass(String docId, String topicName, boolean uselog) {
	        double prob;
	        if (!uselog)
		    prob = 1.0;
                else
		    prob = 0.0;
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
		    if (!uselog) {
			    termProb = Math.pow(termProb, termCount);
			    termProb = termProb / factorial((int) termCount);
			    prob = prob * termProb;
                        }
            else {
            	
            	if (Double.isNaN(Math.log(termProb)))
             		termProb = 0;
             	else
             		termProb = termCount * Math.log(termProb);
			    termProb = termCount * Math.log(termProb);
			    termProb = termProb - Math.log(factorial((int) termCount));
			    prob = prob + termProb;
            }
		}
                if(!uselog)
		    prob = factorial((int) this.docCounters.get(docId).totalCount()) * prob;
                else
                    prob = Math.log(factorial((int) this.docCounters.get(docId).totalCount())) + prob;
		return prob;
	}

	public double getProbabilityOfDocGivenClass(List<String> input,
						    String topicName, boolean uselog) {
		if (input == null || input.size() == 0)
			return 0.0;
		double prob = 0;
        if (!uselog)
		   prob = 1.0;
		List<String> vocab = new ArrayList<String>();
		
		for (String key : this.docCounters.keySet()) {
			for (String term : this.docCounters.get(key).keySet())
				if (!vocab.contains(term))
					vocab.add(term);
		}
		Counter<String> counter = new IntCounter<String>();
		for (String text : input) {
			counter.incrementCount(text);
		}
		
		for (String term : vocab) {
			double termCount = counter.getCount(term);
			if (termCount == 0.0)
				continue;
			double termProb = this.getProbabilityOfWordGivenClass(term,
									      topicName, vocab);
			if (!uselog) {
			    termProb = Math.pow(termProb, termCount);
		        termProb = termProb / factorial((int) termCount);
			    prob = prob * termProb;
			}
            else {
            	if (Double.isNaN(Math.log(termProb)))
            		termProb = 0;
            	else
            		termProb = termCount * Math.log(termProb);
			   termProb = termProb - Math.log(factorial((int) termCount));
			   prob = prob + termProb;
            }
		}
        if (!uselog)
		    prob = factorial((int) counter.totalCount()) * prob;
		else
		    prob = Math.log(factorial((int) counter.totalCount())) + prob;
		return prob;
	}
    /**
       false argument passed to getProbabilityOfDocGivenClass method. 
       look at the paper by mccallum and nigam to calculate the log likelihood.
   
     */
    public double documentLikelyhood(List<String> input) {
		ArrayList<String> classes = new ArrayList<String>();
		for (String key : this.docTopicMap.values()) {
			if (!classes.contains(key))
				classes.add(key);
		}
		double result = 0.0;
		for (String topic : classes) {
			double classPrior = this.getClassPriorProbability(topic);
			double docProb = this.getProbabilityOfDocGivenClass(input, topic, false);
			result = result + (classPrior * docProb);
		}
		return result;
	}
         /**
         false argument passed to getProbabilityOfDocGivenClass method. 
         look at the paper by mccallum and nigam to calculate the log likelihood.
         
        */
	public double documentLikelyhood(String docId) {
		ArrayList<String> classes = new ArrayList<String>();
		for (String key : this.docTopicMap.values()) {
			if (!classes.contains(key))
				classes.add(key);
		}
		double result = 0.0;
		for (String topic : classes) {
			double classPrior = this.getClassPriorProbability(topic);
			double docProb = this.getProbabilityOfDocGivenClass(docId, topic, false);
			result = result + (classPrior * docProb);
		}
		return result;
	}

    public double getProbOfClassGivenDocument(String docId, String topicName, boolean uselog) {
    	double classPrior = this.getClassPriorProbability(topicName);
		double docCondProb = this.getProbabilityOfDocGivenClass(docId,
									topicName, uselog);
		if (docCondProb == 0.0)
			return 0.0;
		
		double docLikelyHood = this.documentLikelyhood(docId);
		
        if (!uselog)
		   return (classPrior / docLikelyHood) * docCondProb;
        else {
		   double logdoclikely = 0.0;
		   if (!Double.isNaN(Math.log(docLikelyHood)))
		        logdoclikely = Math.log(docLikelyHood);	
		   logdoclikely = Math.log(docLikelyHood);	
		   return Math.log(classPrior) + docCondProb - logdoclikely;
		}
	}
    public double averageSentenceLenght() {
    	double totalcount = 0;
    	for (String  k: this.docCounters.keySet()) {
    		totalcount = totalcount + this.docCounters.get(k).totalCount();
    	}
    	return totalcount / this.docCounters.size();
    }
    
    public List<String> generateWords(List<String> input ,
    		 String generationTopic, int popsize , int scatter) {
    	
    	ArrayList<String> generatedWord =new ArrayList<String>();
    	double wordcount =
    		BigDecimal.valueOf(this.averageSentenceLenght()).setScale(0, RoundingMode.HALF_UP).intValue();
    	
    	int count = 0;
        HashMap<String, Double> worddist = null;
		worddist = 
			this.getDistributionForTopic(generationTopic);
		List<String> words = Sampling.generatePopulation(worddist,
    			popsize, scatter);
		
    	for ( ; count < wordcount ; 	count++) {    		
    		Object choosenword = Sampling.sampleWithoutReplacement(words,
    				1, false,false , new ArrayList<Object> ()).get(0);
    		generatedWord.add((String)choosenword);
    	}
    	return generatedWord;
    }
    
	public double getProbOfClassGivenDocument(List<String> input,
						  String topicName, boolean uselog) {
		
		double classPrior = this.getClassPriorProbability(topicName);
		double docCondProb = this.getProbabilityOfDocGivenClass(input,
									topicName, uselog);
		if (docCondProb == 0.0)
			return 0.0;
		double docLikelyHood = this.documentLikelyhood(input);
		if (!uselog)
		    return (classPrior / docLikelyHood) * docCondProb;
        else {
		    double logdoclikely = 0.0;
		    double lcalssprior = 0.0;
		    if (!Double.isNaN(Math.log(docLikelyHood)))
		        logdoclikely = Math.log(docLikelyHood);
		    if (!Double.isNaN(Math.log(classPrior)))
		    	lcalssprior  = Math.log(classPrior);
		    double val = lcalssprior + docCondProb - logdoclikely;
		    return val;
		}
    }

	/**
	 * learning to classify from text from labeled and unlabeled data (kamal
	 * nigam , andrew mccallum) or Comparison of event model for naive bayes
	 * text classification P(Cj | Theta)
	 * 
	 * @return
	 */
	public double getClassPriorProbability(String topicName) {
		double numdocs = this.docTopicMap.size();
		// class count init to 1 for laplace smooting
		double classcount = 1;
		for (String key : this.docTopicMap.keySet()) {
			if (topicName.equals(this.docTopicMap.get(key))) {
				classcount = classcount + 1;
			}
		}
		return classcount / (numdocs + this.getTopics().size());
	}

	public static long factorial(int iNo) {
		if (iNo < 0)
			throw new IllegalArgumentException("iNo must be >= 0");
		long factorial = 1;
		for (int i = 2; i <= iNo; i++)
			factorial *= i;

		return factorial;
	}
	
	public  Counter<String> getCounterOfTopic(String topic) {
		Counter<String> cnt = new IntCounter<String>();
		for (String doc : this.docCounters.keySet()) {
			if (this.docTopicMap.get(doc).equals(topic))
			    cnt.addAll(this.docCounters.get(doc));
		}
		return cnt;
	}
	
	public void printWordDistForEachTopic(String filename) throws IOException {
		
		HashMap<String, Counter<String>> topicCounter = 
			new HashMap<String, Counter<String>>();
		
		for (String topic : this.getTopics()) {
			topicCounter.put(topic, this.getCounterOfTopic(topic));
		}
		
		FileWriter fw  = new FileWriter(filename);
		CSVWriter csv = new CSVWriter(fw);
		String[] row = new String[3];
		
		for (String topic : this.getTopics()) {
			Counter<String> cnt = this.getCounterOfTopic(topic);
			for (String key : cnt.keySet()) {
				row[0] = topic;
				row[1] = key;
				row[2] = ""+(int)(cnt.getCount(key));
				csv.writeNext(row);
			}
			
		}
		csv.close();
	}
}
