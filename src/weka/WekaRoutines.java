/**
 * 
 */
package weka;


import java.io.File;

import java.io.IOException;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * Some WekaRelated routines like dumping to arff file.
 * assigning to a cluster etc.
 * @author ashwani
 *
 */
public class WekaRoutines {
	static Logger logger = Logger.getLogger(WekaRoutines.class.getName());
	
	/**
	 * Routine to dump the file in arff format.
	 * @param filename : name of file to dump to
	 * @param instances : Instances represent set of weka instance
	 * @throws IOException
	 */
	public static void dumpArff(String filename, Instances instances) throws IOException {
		ArffSaver arffsaver = new ArffSaver();
		arffsaver.setFile(new File(filename));
		arffsaver.setInstances(instances);
		arffsaver.writeBatch();
	}
	
	
	
	
	public static void assignClusterToEachInstance(Clusterer clusterer, Instances instances) {
		for (Instance instance : instances) {
			
			try {
				int cnum= clusterer.clusterInstance(instance);
				if (cnum < 0) {
					logger.info("\"Instance id : \","+instance.stringValue(0)+ " ,<null>");
				}
				else {
					logger.info("\"Instance id : \","+instance.stringValue(0)+ ",\" classified to cluster: \","+ cnum);
				}
					
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	public static Clusterer kmeanclusterexecution(Instances instances) {
		logger.setLevel(Level.INFO);
		SimpleKMeans kmean = new SimpleKMeans();
		try {
			CosineDistanceMeasure dm = new CosineDistanceMeasure();
			dm.normalizeInstances(instances);
			kmean.setDistanceFunction(dm);
			kmean.setDisplayStdDevs(true);
			//kmean.setMaxIterations(500);
			kmean.setNumClusters(4);
			kmean.setSeed(100);
			kmean.buildClusterer(instances);
			ClusterEvaluation eval = new ClusterEvaluation();
			eval.setClusterer(kmean);
			eval.evaluateClusterer(instances);
			
			String a = eval.clusterResultsToString();
			System.out.println(a);
			logger.info(a);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return kmean;
	}
}
