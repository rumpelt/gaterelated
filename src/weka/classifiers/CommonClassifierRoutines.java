/**
 * 
 */
package weka.classifiers;

import java.io.FileWriter;
import java.util.Random;

import au.com.bytecode.opencsv.CSVWriter;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * @author ashwani
 *
 */
public final class CommonClassifierRoutines {
	/**
	 * Returns a copy of original instances after removing a set of attributes
	 * @param instances : The input instances
	 * @param indicesToDelete : The index of attributes to be removed
	 * @return
	 */
	public static Instances removeAttributes(Instances instances ,int[] indicesToDelete) {
		Remove rm =new Remove();
		Instances newInstances = null;
	//String[] options = {"-U","-O","-B"};
	//this.setOptions(options);
		if (indicesToDelete != null) {
			rm.setAttributeIndicesArray(indicesToDelete);
			try {
				rm.setInputFormat(instances);
				newInstances = Filter.useFilter(instances, rm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else 
			newInstances = instances;
		return newInstances;
	}
	
	/**
	 * Train on Instances and build the model for this class. The class
	 * extends the J48. Need to add code to clear the previous model before 
	 * training.
	 * @param instances
	 * @param indicesToDelete : Removes some attributes before training.
	 * @return
	 * @throws Exception
	 */
	public static AbstractClassifier trainOnInstances(AbstractClassifier classifier,
			Instances instances, 
			int[] indicesToDelete, String[] options) throws Exception {
		
		Instances newInstances = null;
	//String[] options = {"-U","-O","-B"};
		if (options != null)
			classifier.setOptions(options);
		
		if (indicesToDelete != null) {
			Remove rm =new Remove();
			rm.setAttributeIndicesArray(indicesToDelete);
			rm.setInputFormat(instances);
			newInstances = Filter.useFilter(instances, rm);
		}
		else 
			newInstances = instances;
		
		classifier.buildClassifier(newInstances);
		return classifier;		
	}
	
	public  static boolean testInstances(AbstractClassifier classifier,
			Instance instance, Instances trainSet,	int[] indicesToDelete) 
	throws Exception {
	
		trainSet.add(instance);
		Instances newInstances = trainSet;
		if (indicesToDelete != null) {
			Remove rm = null;
			rm = new Remove();
			rm.setAttributeIndicesArray(indicesToDelete);
			rm.setInputFormat(trainSet);
			newInstances = Filter.useFilter(trainSet, rm);
		}
		instance = newInstances.get(trainSet.size() -1);
		double result = classifier.classifyInstance(instance);
		String classname = instance.classAttribute().value((int)result);
		
		if (classname.equals(instance.stringValue(newInstances.classAttribute())))
			return true;
		else
			return false;
	}
	/**
	 * Test each instance and dump some output on terminal or in file. 
	 * @param instances : Set of instance
	 * @param indexOfId : The index of the attribute which is identifier
	 * @param indicesToDelete : indices to be removed before testing. For example\
	 * the identifier attribute above.
	 * @param indicesToDump : the indexes of the attribute which you want to dump on
	 * terminal.
	 * @param dumpfile : If you want to dump in file then specify the fulie
	 * @return : Returns the instances which we got after removing some of 
	 * attributes
	 * @throws Exception
	 */
	
	public  static Instances testInstances(AbstractClassifier classifier,
			Instances instances, int indexOfId,	int[] indicesToDelete,
			int[] indicesToDump,String dumpfile , boolean dumpEverything) 
	throws Exception {
		Remove rm = null;
		Instances newInstances = instances;
		if (indicesToDelete != null) {
			rm = new Remove();
			rm.setAttributeIndicesArray(indicesToDelete);
			rm.setInputFormat(instances);
			newInstances = Filter.useFilter(instances, rm);
		}
		CSVWriter csvWriter = null;		
		if (dumpfile !=null)
			csvWriter = new CSVWriter(new FileWriter(dumpfile),',');
		
		for (int count = 0; count < newInstances.numInstances();count++) {
			Instance instance = newInstances.get(count);
			double result = classifier.classifyInstance(instance);
			if (csvWriter == null)
				System.out.println(instances.get(count).stringValue(indexOfId) +": "
						+instance.classAttribute().value((int)result));
			else {
				String[] dump = new String[indicesToDump.length+2];
				for (int cnt = 0; cnt < dump.length; cnt++)
					dump[cnt] = "";
				int dumpind = 0;
				for (int ind : indicesToDump) {
					if (instances.get(count).attribute(ind).isNumeric())
						dump[dumpind] = new String(""+instances.get(count).value(ind));
					else	
						dump[dumpind] = instances.get(count).stringValue(ind);
					dumpind++;
				}
				
				if (dumpfile != null)
					dump[dumpind++] = instances.get(count).stringValue(instances.classAttribute());
				dump[dumpind] = instance.classAttribute().value((int)result);
				if (dumpEverything)
					csvWriter.writeNext(dump);
				else if (dumpfile !=null && !instances.get(count).stringValue(instances.classAttribute()).equals(dump[dumpind]))
					csvWriter.writeNext(dump);
				else if (!instances.get(count).stringValue(instances.classAttribute()).equals(dump[dumpind]))
					System.out.println(dump);
				
			}
		}
		if (csvWriter != null)
			csvWriter.close();
		return newInstances;
	}
	

	public static void evaluate(AbstractClassifier classifier, 
			Instances train, Instances test) 
		throws Exception{
		Evaluation eval = new Evaluation(train);
		
		System.out.println("Training info : num instances "
				+train.numInstances());

		System.out.println("test info : num instances "
				+test.numInstances());
		
		eval.evaluateModel(classifier, test);
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
	}
	
	public static Instances returnRestOfInstance(Instances instances, int index) {
		Instances result = new Instances(instances);
		result.remove(index);
		return result;
	}
	

	
	public static double leaveOneOutCrossValidation(AbstractClassifier classifier ,
			Instances instances, int[] indicesTORemove, int [] indicesToPrint,
			String[] options, String dumpfile) throws Exception {
		double missclassified = 0;
		Instances copied = instances;
		if (indicesTORemove != null)
			copied = CommonClassifierRoutines.removeAttributes(instances, indicesTORemove);
		CSVWriter csvWriter = null;		
		if (dumpfile !=null)
			csvWriter = new CSVWriter(new FileWriter(dumpfile),',');
		for (int i=0; i < copied.size(); i++) {
			Instances trainSet = CommonClassifierRoutines.returnRestOfInstance(copied, i);
			
			classifier = CommonClassifierRoutines.trainOnInstances(classifier, trainSet, null, options);
			double result = classifier.classifyInstance(copied.get(i));
			double[] dists = classifier.distributionForInstance(copied.get(i)) ;
			// For the following array initialization 2 is actual class value and
			// predicted class value and one column for if prediction is true or false
			String[] dump = new String[indicesToPrint.length+3+ instances.classAttribute().numValues()];
			for (int cnt = 0; cnt < dump.length; cnt++)
				dump[cnt] = "";
			int dumpind = 0;
			for (int ind : indicesToPrint) {
				if (instances.attribute(ind).isNumeric())
					dump[dumpind] = new String(""+instances.get(i).value(ind));
				else	
					dump[dumpind] = instances.get(i).stringValue(ind);
				dumpind++;
			}
			
			dump[dumpind++] = instances.get(i).stringValue(instances.classAttribute());
			dump[dumpind++] = instances.classAttribute().value((int)result);
			if (dump[dumpind-1].equals(dump[dumpind-2]))
				dump[dumpind++] = "1";
			else {
				dump[dumpind++] = "0";
				missclassified++;
			}
			for (double val : dists)
				dump[dumpind++] = new String(""+val);
			if (csvWriter != null)
				csvWriter.writeNext(dump);			
		}
		if (csvWriter != null)
			csvWriter.close();
		return missclassified;
	}
}
