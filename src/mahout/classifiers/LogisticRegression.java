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
					 input.size());
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
	/**
         * Do not use this routine. Broken and bad.
	 * remove some of the indices
	 * @param input
	 */
        @Deprecated
	public static void doLeaveOneOutCrossValidation(List<Vector> input,
			String csvfile){
		List<Vector> actual  = null;
		CSVWriter csvwriter = null;
		String[] todump = null;
		
		try {
			csvwriter = new CSVWriter(new FileWriter(csvfile), ',');
			todump = new String[6];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (csvfile != null)  {
			actual = new ArrayList<Vector>();
			for (Vector v : input)
				actual.add(v.clone());
			
		}
		CommonRoutines.removeIndices(input, new int []{0,1});
		
		int missclassified = 0;
		for (int count = 0; count < input.size(); count++) {
			List<Vector> trainingSet = CommonRoutines.leaveOneOutTrainingSet(input,
					count);
			OnlineLogisticRegression ol = new OnlineLogisticRegression(3,
					input.get(0).size() -2
					, new L1());
			//ol.lambda(1.0);
			for (Vector v : trainingSet) {
				System.out.println((int)v.get(v.size() -1) -1);
				System.out.println(v.viewPart(2, v.size() -2));
				ol.train((int)v.get(v.size() -1) -1 , v.viewPart(2, v.size() -2));
			}
			Vector test = input.get(count);
			
		//	Vector result = ol.classify(test.viewPart(0, test.size() -1));
			int currclass = (int)(test.get(test.size()-1) -1 );
			int predictedClass = currclass;
			double maxLikelyhood = ol.logLikelihood(currclass, 
					test.viewPart(0, test.size() -1));
			
			//System.out.println("Corunet class "+(int)(test.get(test.size()-1) -1 ));
			for (int i=0; i < 3; i++) {
				double currLikelyhood = ol.logLikelihood(i, 
						test.viewPart(0, test.size() -1));
				if (currLikelyhood > maxLikelyhood) {
					predictedClass = i;
					maxLikelyhood = currLikelyhood;
				}
				/*
				System.out.print(" log lik for "+ i +" "+ol.logLikelihood(i, 
						test.viewPart(0, test.size() -1)));
						*/
			}
			
			if (todump != null) {
				todump[0] =new String(""+ actual.get(count).get(0));
				todump[1] = new String(""+actual.get(count).get(1));
			}
			if (Double.isNaN(maxLikelyhood) || currclass != predictedClass) {
				missclassified++;
				if (todump != null) {
					todump[2] = "0";
					todump[3] = new String(""+currclass);
					todump[4] = new String(""+predictedClass);
					todump[5] = new String(""+maxLikelyhood);
				}
				
			}
			else if (todump != null) {
				todump[2] = "1";
				todump[3] = new String(""+currclass);
				todump[4] = new String(""+predictedClass);
				todump[5] = new String(""+maxLikelyhood);
			}
			if (todump!=null)
				csvwriter.writeNext(todump);
				
			//System.out.println("");
			/*
			System.out.println(ol.getLambda() + " "+(int)(test.get(test.size()-1) -1 ) + " "+result +" "
					+ol.logLikelihood((int)(test.get(test.size()-1) -1 ), 
					test.viewPart(0, test.size() -1)));
					*/ 
		}
		
		System.out.println("% accuracy := "+ ((input.size() - missclassified)/(double)input.size()) * 100);
		if (todump != null)
			try {
				csvwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	
}
