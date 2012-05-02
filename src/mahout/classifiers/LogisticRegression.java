/**
 * 
 */
package mahout.classifiers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mahout.commonroutines.CommonRoutines;

import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.L2;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.UniformPrior;
import org.apache.mahout.math.Vector;

import au.com.bytecode.opencsv.CSVWriter;


/**
 * @author ashwani
 *
 */
public class LogisticRegression extends OnlineLogisticRegression {
	/**
	 * remove some of the indices
	 * @param input
	 */
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
