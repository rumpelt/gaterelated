/**
 * 
 */
package cchs;

import java.util.List;

import org.apache.mahout.common.distance.CosineDistanceMeasure;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;

/**
 * @author ashwani
 *
 */
public final class CategoryVectors {
	
	private static String discrimatoryterms[] = new String[] {"breast","" +
			"breastfeed", "pump",
		"bottle", "enfamil","lipil",
		 "iron","prosobee",
		"similac","isomil",	"advance",
		"enfacare","carnation"};
	private static Normal normalclass = new Normal(0.2, 0.25,
			RandomEngine.makeDefault() );
	private static double[] breastclass = new  double[]{1.0, 1.0,  
			1.0, 0.0, 0.0,	
			0.0, 0.0, 0.0,
			0.0,0.0,0.0,
			0.0, 0.0};
	
	private static double[] bottleclass = new double[] {0.0,0.0,
			0.0, 1.0,1.0,
			1.0, 1.0, 1.0, 
			1.0,1.0, 1.0,
			1.0, 1.0 };
	
	private static double[] breastnbottle = new double[] {1.0,1.0,
			1.0,1.0,1.0,
			1.0,1.0,1.0,
			1.0, 1.0, 1.0,
			1.0, 1.0
		};
	
	public static double  returnPdf(double va) {
		return normalclass.pdf(va);
	}
	public static double returnSimilarityMeasure(List<String> input , String topic ) {
		double [] vector = new double[discrimatoryterms.length];
		for(int count=0; count < discrimatoryterms.length; count++ ) {
			if (input.contains(discrimatoryterms[count]))
				vector[count] = 1.0;
			else
				vector[count] = 0.0;
		}
		if (topic.equals("breast and no information on cereals"))
			return CosineDistanceMeasure.distance(vector, breastclass);
		else if (topic.equals("bottle and no information on cereals"))
			return CosineDistanceMeasure.distance(vector, bottleclass);
		else
			return CosineDistanceMeasure.distance(vector, breastnbottle);
	}
	
	
}
