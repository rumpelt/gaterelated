/**
 * 
 */
package weka.classifiers;

import java.io.FileWriter;
import java.util.Enumeration;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * @author ashwani
 *	
 */
public class J48Classifier extends J48{
	
	public J48Classifier() {
		super();
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -3658635829523403046L;
	private Instances trainingSet;
	/**
	 * @return the trainingSet
	 */
	public Instances getTrainingSet() {
		return trainingSet;
	}
	/**
	 * @param trainingSet the trainingSet to set
	 */
	public void setTrainingSet(Instances trainingSet) {
		this.trainingSet = trainingSet;
	}
	private Instances testingSet;
	/**
	 * @return the testingSet
	 */
	public Instances getTestingSet() {
		return testingSet;
	}
	/**
	 * @param testingSet the testingSet to set
	 */
	public void setTestingSet(Instances testingSet) {
		this.testingSet = testingSet;
	}
	
	/**
	 * Returns a copy of original instances after removing a set of attributes
	 * @param instances : The input instances
	 * @param indicesToDelete : The index of attributes to be removed
	 * @return
	 */
	public Instances removeAttributes(Instances instances ,int[] indicesToDelete) {
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
	public Instances trainOnInstances(Instances instances, 
			int[] indicesToDelete, String[] options) throws Exception {
		Remove rm =new Remove();
		Instances newInstances = null;
	//String[] options = {"-U","-O","-B"};
		if (options != null)
			this.setOptions(options);
		
		if (indicesToDelete != null) {
			rm.setAttributeIndicesArray(indicesToDelete);
			rm.setInputFormat(instances);
			newInstances = Filter.useFilter(instances, rm);
		}
		else 
			newInstances = instances;
		
		if (newInstances.classIndex() >= 0) {
			this.buildClassifier(newInstances);
			return newInstances;
		}
		else
			return null;
	}
	
	public void evaluate(Instances train, Instances test) 
		throws Exception{
		Evaluation eval = new Evaluation(train);
		
		System.out.println("Training info : num instances "
				+train.numInstances());

		System.out.println("test info : num instances "
				+test.numInstances());
		
		eval.evaluateModel(this, test);
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
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
	
	public  Instances testInstances(Instances instances, int indexOfId,
			int[] indicesToDelete,int[] indicesToDump, boolean dumpMissClasses,
			String dumpfile) 
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
			double result = this.classifyInstance(instance);
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
				
				if (dumpMissClasses)
					dump[dumpind++] = instances.get(count).stringValue(instances.classAttribute());
				dump[dumpind] = instance.classAttribute().value((int)result);
				if (dumpMissClasses && !instances.get(count).stringValue(instances.classAttribute()).equals(dump[dumpind]))
					csvWriter.writeNext(dump);
				else if (!dumpMissClasses)
					csvWriter.writeNext(dump);
			}
		}
		if (csvWriter != null)
			csvWriter.close();
		return newInstances;
	}
}
