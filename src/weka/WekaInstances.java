/**
 * 
 */
package weka;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.SparseInstance;

/**
 * @author ashwani
 *  A set of functions to ease the creation of instances.
 *  Some tips are as following:
 *  To create a numeric attribute use : new Attribute("nameOfAtrribute")
 *  TO create a string attribute first create a dummy List<String> initialized to
 *  null and then invoke new Attribute("nameOfAttribute",dummyListOfString). 
 *  The dummyListOfString must be set to null
 *  To create a nominal attribute use the same constructor as above but supply 
 *  the list of possible values i.e. new Attribute("nameOfAttribute",List<String>);
 *  An attribute can be inserted at a given index using insertAttributeAt call of
 *  the super class.
 *  While creating nominal attributes for training instance and testing instance
 *  ensure that order of the values of nominal attribute is same.
 */
public class WekaInstances extends Instances {

	private static final long serialVersionUID = 1708795221477220771L;
	/**
	 * A temporary instance given for ease of use.
	 * Few routines also give on this instance for ease.
	 * Before using this instance You must call initWorkingInstance() or
	 * initWorkingInstance(boolean sparseInstance).
	 * To flush the old values; 
	 */
	private Instance workingInstance;
	/**
	 * @return the workingInstance
	 */
	public Instance getWorkingInstance() {
		return workingInstance;
	}
	/**
	 * Add a string attribute at a particular index
	 * @param attributeName
	 * @param index
	 */
	public void addStringAttribute(String attributeName, int index) {
		List<String> values = null;
		this.insertAttributeAt(new Attribute(attributeName, values), index);
	}
	/**
	 * If the working instance is dense or sparse instance
	 */
	private boolean sparseInstance;

	/**
	 * @return the sparseInstance
	 */
	public boolean isSparseInstance() {
		return sparseInstance;
	}
	/**
	 * @param sparseInstance the sparseInstance to set
	 */
	public void setSparseInstance(boolean sparseInstance) {
		this.sparseInstance = sparseInstance;
	}
	/**
	 * @param dataset
	 */
	public WekaInstances(String datasetName, ArrayList<Attribute> attributes,
			int capacity) {
		super(datasetName,attributes, capacity);
		this.workingInstance = null;
		this.sparseInstance = false;
		// TODO Auto-generated constructor stub
	}
	
	public void clearWorkingInstance() {
		this.workingInstance = null;
	}
	
	
	public void addClassAttribute(String attributeName, List<String> possibleValues) {
		this.insertAttributeAt(new Attribute(attributeName,possibleValues), this.numAttributes());
		this.setClassIndex(this.numAttributes() -1);
	}
	/**
	 * initalizes/reinitializes the temporary instance workingInstance of this
	 * class. 
	 * @param sparseInstance : if the Instance workingInstance of this class is
	 * sparse or dense.
	 */
	public void initWorkingInstance(boolean sparseInstance) {
		if (sparseInstance)
			this.workingInstance = new  SparseInstance(super.numAttributes());
		else
			this.workingInstance = new DenseInstance(super.numAttributes());
	}
	
	public void setClassMissingForEachInstance() {
		Attribute classattribute = this.classAttribute();
		for(Instance inst : this) {
//			inst.setMissing(classattribute);
			inst.setValue(classattribute, "UNKNOWN");
//			System.out.println(inst.stringValue(classattribute));
		}
	}
	
	/**
	 * go over each attribute of each instance and set the values of numberic
	 * attributes to value
	 * @param forceInsert : If set then sets value of the all the numeric attribute
	 * to value. else operate only on missing.
	 * @param val
	 */
	public void setValueForEachInstance(boolean forceInsert , double value) {
		//this.dump();
		for (Instance inst : this) {
			for (int index=0; index < this.numAttributes();index++) {
				Attribute att = this.attribute(index);
				if (forceInsert && att.isNumeric()) {
					inst.setValue(att, value);
				}
				else if (!forceInsert && inst.isMissing(att) && att.isNumeric())
					inst.setValue(att, value);
			}
		}
	}
	/**
	 * initalizes/reinitializes the temporary instance workingInstance of this
	 * class. 
	 * The Instance workingInstance of this class is  sparse or dense based on
	 * boolean flag sparseInstance of this class. By default this flag is set to
	 * true.
	 */
	public void initWorkingInstance() {
		if (this.sparseInstance)
			this.workingInstance = new  SparseInstance(super.numAttributes());
		else
			this.workingInstance = new DenseInstance(super.numAttributes());
	}
	
	/**
	 * Adds a nominal value for the attribute.
	 * @param attributeName : The name of the attribute for which nominal 
	 * value is to be Added.
	 * @param value : the nominal value
	 * @return : the index of the nominal value or -1 if the attribute is not 
	 * of type nominal or string or some other error.
	 * look at super.addStringValue() for detail.
	 */
	public int addNominalValues(String attributeName, String value) {
		Attribute attribute = super.attribute(attributeName);
		return (attribute.indexOfValue(value) < 0) ?
				attribute.addStringValue(value) : -1;
		
	}
	

	/**
	 * Adds the temporary instance workingInstance of this class to the 
	 * super class which is a Instances object.
	 */
	public void addWorkingInstance() {
		super.add(this.workingInstance);
		this.workingInstance = super.get(super.numInstances() -1);
	//	this.workingInstance = null;		
	}
	
	/**
	 * Set the value of the temporary instance workingInstance. 
	 * @param attributeName : Name of the attribute to for which value is to be set
	 * @param value : the string value to be set
	 */
	public void setValueOfWorkingInstance(String attributeName, String value) {
		Attribute attribute = super.attribute(attributeName);
		this.workingInstance.setValue(attribute, value);
		//this.dump();
	
	}
	

	/**
	 * Set the value of the temporary instance workingInstance. 
	 * @param attributeName : Name of the attribute to for which value is to be set
	 * @param value : the double value to be set
	 */
	public void setValueOfWorkingInstance(String attributeName, double value) {
		Attribute attribute = super.attribute(attributeName);
		this.workingInstance.setValue(attribute, value);
	}
	
	
	/**
	 * Set the value of the temporary instance workingInstance. This is for string
	 * attributes only. Wil
	 * @param attributeName : Name of the attribute to for which value is to be set
	 * @param value : the double value to be set
	 * @param createAttribute: If the attribute is not present in the instances then
	 * create the attribute
	 */
	public void setValueOfWorkingInstance(String attributeName, String value
			, boolean createAttribute) {
		Attribute attribute = super.attribute(attributeName);
		if (attribute == null && createAttribute) {
			ArrayList<String> dummy = null;
			if (super.classAttribute() != null)
				super.insertAttributeAt(new Attribute(attributeName, dummy), 
						super.numAttributes() -1);
			else
				super.insertAttributeAt(new Attribute(attributeName, dummy),
						super.numAttributes() );
		}
		this.workingInstance.setValue(attribute, value);
	}
	
	/**
	 * Set the value of the temporary instance workingInstance. 
	 * @param attributeName : Name of the attribute to for which value is to be set
	 * @param value : the double value to be set
	 * @param createAttribute: If the attribute is not present in the instances then
	 * create the attribute
	 */
	public void setValueOfWorkingInstance(String attributeName, double value
			, boolean createAttribute) {
		Attribute attribute = super.attribute(attributeName);
		if (attribute == null && createAttribute) {
			if (super.classAttribute() != null)
				super.insertAttributeAt(new Attribute(attributeName), 
						super.numAttributes() -1);
			else
				super.insertAttributeAt(new Attribute(attributeName),
						super.numAttributes() );
			
		}
		this.workingInstance.setValue(super.attribute(attributeName), value);
		//this.dump();
	}
	
	/**
	 *  A simple routine to see the values of the each instance on the terminal
	 *  which have been added till the function is called.
	 */
	public void dump( ){
		for (Instance instance : this) {
			SparseInstance s = (SparseInstance)instance;			
			System.out.println(instance);
		}
	}
	
	

}
