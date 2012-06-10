/**
 * 
 */
package mahout.classifiers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mahout.commonroutines.CommonRoutines;

import org.apache.mahout.classifier.sgd.AbstractOnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.L2;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.PriorFunction;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;

import stats.Sampling;



import au.com.bytecode.opencsv.CSVWriter;
/**
 * @author ashwani
 *
 */
public class LogisticRegression extends OnlineLogisticRegression {
	
	private static PriorFunction priorfunction = new L1();
	
	/**
         * Assumes that class label is at the end of the vector.
         * startindex : specifies the index at which the first input feautrue for training start.
         * endindex: specifies the index at which the  last input feature for training ends.
         * So for example if the first input feature is at index 0 then startindex is 0.
         * If the last inpu feature is at index 7 then endindex will be set to 7.
         */	
	public static AbstractOnlineLogisticRegression trainOnVector(List<Vector> input, int numfeatures , int numcategories, 
			boolean randomSample, int startindex, int endindex, int numIteration) {
		OnlineLogisticRegression ol = new OnlineLogisticRegression(numcategories,
				numfeatures, LogisticRegression.priorfunction);
		
		if (!randomSample)
			numIteration= 1;
		List<?> randomized = input;
		for (int count = 0 ; count < numIteration; count++) {
			if (count > 1)
				randomized= Sampling.sampleWithoutReplacement(input,
					 input.size(), false, false, new ArrayList<Object>() );
			 for (Object v : randomized) {
				 Vector totrain = (Vector)v;
		//
			//	 System.out.println(totrain+" : "+(int)totrain.get(totrain.size() -1 ));
                                 // endindex = endindex + 1 because of the specification of the viewPart call
				 ol.train((int)totrain.get(totrain.size() -1 ), totrain.viewPart(startindex,
						 endindex+1));
			 }
		}
		return ol;
	}
	
        /**
        * Assumes that class label is at the end of the vector.
	* startindex : specifies the index at which the first input feautrue for training start.
	* endindex: specifies the index at which the  last input feature for training ends.
        * So for example if the first input feature is at index 0 then startindex is 0.
        * If the last inpu feature is at index 7 then endindex will be set to 7.
        */
	public static int test(Vector test, boolean categoryPresent,
			       AbstractOnlineLogisticRegression ol, boolean returnClassPrediction, int startindex,
			       int endindex,  double[] classProb) {
		int actual = 0;
		int predicted = 0;
   		//System.out.println("class logisticregression in mahout function : test: "+test);
		if (categoryPresent) {
			actual = (int) test.get(test.size() -1);
			test = test.viewPart(startindex, endindex + 1);
		}
		Vector result = ol.classify(test);
		//Matrix m = ol.getBeta();
		//System.out.println(m);
		double totalsum=0;
		for (int count = 0; count < result.size();count++) {
			totalsum = totalsum + result.get(count);
		}
		double base = 1.0 - totalsum;
		classProb[0] = base;
		if (result.maxValue() > base) {
			predicted = result.maxValueIndex() + 1;
			classProb[0] = result.maxValue();
		}
		
		if (returnClassPrediction)
			return predicted;
		else
			return (actual == predicted) ? 1 : 0;		
	}
	
}
