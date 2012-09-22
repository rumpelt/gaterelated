/**
 * 
 */
package weka.classifiers;

import java.util.ArrayList;

/**
 * @author ashwani
 * A class which will contain different type of results which can be used for
 * later analysis.
 */
public class ClassifierResult {
    
    /**
     * probabilites of correctl classified instances
     */
    private ArrayList<Double> correctprob = new ArrayList<Double>();
    
    /**
     * @return the correctprob
     */
    public ArrayList<Double> getCorrectprob() {
	return correctprob;
    }
    
    /**
     * @return the wrongprob
     */
    public ArrayList<Double> getWrongprob() {
	return wrongprob;
    }
    
    /**
     * add the probability of a correctly classified instance
     * @param prob
     * @param id : for future use , when you want to map an id with probability
     */
    public void addCorrectResult(double prob, String id) {
	this.correctprob.add(prob);
    }
    
    /**
     * A probability of a wrongly classified instance;
     * @param prob
     * @param id : For future use when we want to map an id with its 
     * probability. Will be useful for debugging purpose.
     */
    public void addWrongResult(double prob, String id) {
	this.wrongprob.add(prob);
    }
    /**
     * proababilietes of wrongly classified instances
     */
    private ArrayList<Double> wrongprob = new ArrayList<Double>();
    
}
