/**
 * 
 */
package weka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.lucene.util.Version;

import edu.stanford.nlp.stats.Counter;

import stats.KLDivergence;
import tokenizers.StopWordList;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.CommonClassifierRoutines;
import weka.classifiers.Evaluation;
import weka.classifiers.J48Classifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instances;

import misc.FeedCategories;
import misc.MedicalTrainer;

/**
 * @author ashwani
 *	Main driver class for Weka Related class experiments performed by me
 */
public final class MainDriver {

	/**
	 * @param args
	 */
	public static void main(String[] argv) {
		// TODO Auto-generated method stub
		String csvfile = argv[0];
		int  identifierCol =Integer.parseInt(argv[1]); 
		int agecol = Integer.parseInt(argv[2]);
		int textcol = Integer.parseInt(argv[3]);
		int labelcol = Integer.parseInt(argv[4]);
		String idName = argv[5];
		String options = null;
		//String options = argv[6];
		boolean skipheader = Boolean.parseBoolean(argv[6]) ;
		classificationExperiments(csvfile, textcol, agecol,identifierCol ,labelcol ,
				(float)0.0, (float)1.0 , skipheader, idName, options);
	}
	
	public static void classificationExperiments(String filename, int textcol,
			int agecol, int identifiercol, int labelcol,float lowage, float upage ,
			 boolean skipheader, String idname ,String machineOptions) {
	
		List<Integer> ngrams = new Vector<Integer>();
		ngrams.add(1);
		ngrams.add(2);
	//	ngrams.add(3);
		
		try {	
			/*
			
			Counter<String> unigramFreqDist = MedicalTrainer.returnFreqDist(
					filename, textcol, agecol, lowage, upage, 2,
					true, StopWordList.getMedicalStopWordList(Version.LUCENE_35), 
					true);
			MedicalTrainer.printToFile("/home/ashwani/xyz/bigrams.csv", unigramFreqDist, 2937);
			HashMap<Integer, Counter<String>> countermap =
				new HashMap<Integer,Counter<String>>();
			countermap.put(0, unigramFreqDist);
			*/
			/*
			WekaInstances trainingSet = MedicalTrainer.returnRelativeProbabilityWeight(
					filename, countermap, identifiercol, idname, textcol, agecol, 
					lowage, upage, labelcol, FeedCategories.returnAllValues(), 
					ngrams, true, StopWordList.getMedicalStopWordList(Version.LUCENE_35),
					true, skipheader,	"relativeprobabiltraining");
					
			*/
			Counter<String> unigramFreqDist = MedicalTrainer.returnFreqDist(
					filename, textcol, agecol, lowage, upage, 1,
					true, true,StopWordList.getMedicalStopWordList(Version.LUCENE_35), 
					true);
			Counter<String> bgramFreqDist = MedicalTrainer.returnFreqDist(
					filename, textcol, agecol, lowage, upage, 2,
					true, true,StopWordList.getMedicalStopWordList(Version.LUCENE_35), 
					true);
			Counter<String> tgramFreqDist = MedicalTrainer.returnFreqDist(
					filename, textcol, agecol, lowage, upage, 3,
					true, true,StopWordList.getMedicalStopWordList(Version.LUCENE_35), 
					true);
			unigramFreqDist = KLDivergence.removeSingleCounteTerms(unigramFreqDist);
			bgramFreqDist = KLDivergence.removeSingleCounteTerms(bgramFreqDist);
			tgramFreqDist = KLDivergence.removeSingleCounteTerms(tgramFreqDist);
			HashSet<String> keys = new HashSet<String>(unigramFreqDist.keySet());
			keys.addAll(bgramFreqDist.keySet());
			keys.addAll(tgramFreqDist.keySet());
			WekaInstances trainingSet = MedicalTrainer.returnIndicatorVectorOfTermSpace(
					filename, keys, identifiercol, idname, textcol, agecol, lowage, 
					upage, labelcol, FeedCategories.returnAllValues(),ngrams , true, 
					StopWordList.getMedicalStopWordList(Version.LUCENE_35), true, 
					skipheader,true,"training");
			
			
			ArrayList<String> options =null;
			if (machineOptions != null) {
				StringTokenizer st = new StringTokenizer(machineOptions);
				options = new ArrayList<String>();
				while(st.hasMoreTokens()) {
					options.add(st.nextToken());
					}
				}
			
			//WekaRoutines.dumpArff("/home/ashwani/disgusst.arff", trainingSet);
			AbstractClassifier classifier = null;
			classifier  = new SimpleLogistic(100, true, false);
			//classifier = new J48Classifier();
			if (options != null && options.size() > 0)
				classifier.setOptions(options.toArray(new String[options.size()]));
			/*
			CommonClassifierRoutines.trainOnInstances(classifier, trainingSet, new int[] {0,1}, null);
			
			WekaInstances testSet = MedicalTrainer.returnIndicatorVectorOfTermSpace(
					filename, unigramFreqDist.keySet(), identifiercol, idname, textcol, agecol, lowage, 
					upage, labelcol, FeedCategories.returnAllValues(),ngrams , false, 
					StopWordList.getMedicalStopWordList(Version.LUCENE_35), false, 
					skipheader,true,"training");
			testSet.setClassMissingForEachInstance();
					
			CommonClassifierRoutines.testInstances(classifier, testSet, 0, 
					 new int[] {0,1},  new int[] {0,1}, "/home/ashwani/xyz/feedcateggory0-1.csv", true);
					 */
			//j48.trainOnInstances(trainingSet, new int[] {0,1}, null);
			//j48.testInstances(trainingSet, 0, new int[] {0,1},
			//		new int[] {0,1}, true, "/home/ashwani/xyz/col5bi.csv");
		//	trainingSet.dump();
			/*
			CommonClassifierRoutines.leaveOneOutCorss(classifier, trainingSet, new int[] {0,1}, 
					new int[] {0,1}, null, "/home/ashwani/xyz/presentation/C4.5UnigramRandomSet3.csv");
		
			*/
			Instances instances = CommonClassifierRoutines.removeAttributes(
					trainingSet, new int[] {0,1});
			Evaluation eval = new Evaluation(instances);
			eval.crossValidateModel(classifier, instances, instances.numInstances(), new Random(1000));
			System.out.println(eval.toSummaryString("\nResults\n======\n", false));
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
