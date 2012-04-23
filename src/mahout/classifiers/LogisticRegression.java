/**
 * 
 */
package mahout.classifiers;

import java.util.List;

import mahout.commonroutines.CommonRoutines;

import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.Vector;


/**
 * @author ashwani
 *
 */
public class LogisticRegression extends OnlineLogisticRegression {
	
	public static void doLeaveOneOutCrossValidation(List<Vector> input){
		OnlineLogisticRegression ol = new OnlineLogisticRegression(3,
				input.get(0).size() -1
				, new L1());
		
		for (int count = 0; count < input.size(); count++) {
			List<Vector> trainingSet = CommonRoutines.leaveOneOutTrainingSet(input, count);
			
			for (Vector v : trainingSet) {
				ol.train((int)v.get(v.size() -1) -1 , v.viewPart(0, v.size() -1));
			}
			Vector test = input.get(count);
			Vector result = ol.classify(test.viewPart(0, test.size() -1));
			
			System.out.println(ol.getLambda() + " "+(int)(test.get(test.size()-1) -1 ) + " "+result +" "
					+ol.logLikelihood((int)(test.get(test.size()-1) -1 ), 
					test.viewPart(0, test.size() -1))); 
		}
	}
}
