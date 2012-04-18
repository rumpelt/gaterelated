/**
 * 
 */
package weka;

import indexreader.LuceneIndexReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import specialstruct.MappedVector;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;

/**
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
	
	
	/*
	 * dumps to a file in arff format
	 */
	@Deprecated
	public static void dumpInArffFormat(String fileNameToDump,String datasetname, 
			MappedVector dataInput, List<String> termSpace) {
		File outputFile = new File(fileNameToDump);
		try {
			outputFile.createNewFile();
			FileWriter fw = new FileWriter(outputFile);
			fw.write("% creator : Ashwin \n");
			fw.write("@RELATION "+datasetname+ "\n");
		//	StringBuffer sb= new StringBuffer();
			fw.write("@ATTRIBUTE identifier STRING\n");
		
			for (int count = 0; count < termSpace.size();count++) {
				String term = new String(termSpace.get(count));
				if (term.contains(","))
					term.replaceAll(",", "");
				fw.write("@ATTRIBUTE "+term+ " NUMERIC\n");
			
			}
			fw.write("@DATA\n");
			Instances instances = returnInstances(dataInput, termSpace);
			for (Instance instance: instances) {
				fw.write(instance.toString()+"\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Deprecated
	public static Instances returnInstances(MappedVector datainput ,
			List<String> termSpace ) {
		
		ArrayList<Attribute> attinfo = new ArrayList<Attribute> (datainput.size());
		
		
		List<String> nominalval = new ArrayList<String>();
		for (String ids : datainput.getKeys()) {
			if (!nominalval.contains(ids))
				nominalval.add(ids);
		}
		Attribute identifiers = new Attribute("Identifier", nominalval);
	//	logger.trace("Identiferis attribute: "+ identifiers.toString());
//	/	System.out.println(identifiers.toString());
		for (String term : datainput.getKeys()) {
			int index = identifiers.addStringValue(term);
		    logger.trace("returnInstances: index returned for term: "+term + "  "+ index);
		}
		
		attinfo.add(identifiers);
		for (String term : termSpace) {
			attinfo.add(new Attribute(term));
		}
		Instances result = new Instances("instance",attinfo, datainput.size());
		int index = 0;
		for(Vector v : datainput.getValues()) {
		
			double[] values = new double[v.size()];
			DenseInstance sparseInstance = new DenseInstance(v.size()+ 1);
			//sparseInstance.insertAttributeAt(0);
			sparseInstance.setValue((Attribute)attinfo.get(0), datainput.getKeys().get(index));
			index++;
			for (int count =1; count < v.size() + 1; count++)
				sparseInstance.setValue((Attribute)attinfo.get(count), (new Double(""+v.get(count -1))).doubleValue());
			sparseInstance.setDataset(result);
			result.add(sparseInstance);
		//	System.out.println(sparseInstance);
			//System.out.println(v);
			
		}
		return result;
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
