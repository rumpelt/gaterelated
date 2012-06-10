/**
 * 
 */
package weka;

import java.util.Enumeration;

import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

/**
 * @author ashwani
 *
 */
public class CosineDistanceMeasure extends EuclideanDistance{
	private Instances data =null;
	private boolean debug = false;
	/* (non-Javadoc)
	 * @see weka.core.OptionHandler#getOptions()
	 */

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see weka.core.OptionHandler#listOptions()
	 */
	public Enumeration listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see weka.core.OptionHandler#setOptions(java.lang.String[])
	 */
	public void setOptions(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void normalizeInstance(Instance instance) {
		
		double product = 0.0;
		for (int count =0 ; count < instance.numAttributes(); count++) {
			Attribute att = instance.attribute(count);
			if(!att.isNumeric() || instance.isMissing(att))
				continue;
			product = product + (instance.value(att) * instance.value(att));
		}
		product = Math.sqrt(product);
		if (product == new Double("0.00000000000000000").doubleValue())
			return;
		for(int count = 0; count < instance.numAttributes(); count++) {
			Attribute att = instance.attribute(count);
			if(!att.isNumeric() || instance.isMissing(att))
				continue;
			
			instance.setValue(att, instance.value(att)/product);
			
		}
	}
	public void normalizeInstances(Instances instances) {
		for (Instance instance : instances) {
			this.normalizeInstance(instance);
		}
		
	}
	/**
	 * returns the cosine distance.
	 * To find the actual angle you need to take the inverse cosine of the 
	 * result
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	public static double distance(double[] vec1, double[] vec2) {
		assert(vec1.length == vec2.length);
		double product = 0;
		double vec1magnitude = 0;
		double vec2magnitude = 0;
		for (int count =0 ; count < vec1.length; count++) {
			product = product + vec1[count] * vec2[count];
			vec1magnitude = vec1magnitude + Math.pow(vec1[count], 2);
			vec2magnitude = vec2magnitude + Math.pow(vec2[count], 2);
		}
		double deno = vec1magnitude * vec2magnitude;
		if (deno == 0.0)
			return Double.NaN;
		else
			return product/ deno;
	}
	/* 
	 * (non-Javadoc)
	 * @see weka.core.DistanceFunction#distance(weka.core.Instance, weka.core.Instance)
	 */
	public double distance(Instance inst1, Instance inst2) {
		// TODO Auto-generated method stub
		assert(inst1.numAttributes() == inst2.numAttributes());
		
		double product = 0;
		double inst1magnitude = 0;
		double inst2magnitude = 0;
		for (int count =0 ; count < inst1.numAttributes(); count++) {
			Attribute att1 = inst1.attribute(count);
			Attribute att2 = inst1.attribute(count);
			if(!att1.isNumeric() || !att2.isNumeric() || inst1.isMissing(att1) 
					|| inst2.isMissing(att2) )
				continue;
			assert(att1.name().equals(att2.name()));
		
			product = product + (inst1.value(att1) * inst2.value(att2));
			
			inst1magnitude = inst1magnitude + (inst1.value(att1) 
					* inst1.value(att1));
			
			inst2magnitude = inst2magnitude + (inst2.value(att2) 
					* inst2.value(att2));
		}
		inst1magnitude = Math.sqrt(inst1magnitude);
		inst2magnitude = Math.sqrt(inst2magnitude);
		
		if ((inst1magnitude * inst2magnitude) == new Double("0.000000000000").doubleValue())
			return 0;
		
		return (product/(inst1magnitude * inst2magnitude));
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#distance(weka.core.Instance, weka.core.Instance, weka.core.neighboursearch.PerformanceStats)
	 */
	public double distance(Instance arg0, Instance arg1, PerformanceStats arg2) {
		// TODO Auto-generated method stub
		return this.distance(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#distance(weka.core.Instance, weka.core.Instance, double)
	 */
	public double distance(Instance arg0, Instance arg1, double arg2) {
		// TODO Auto-generated method stub
		double value = this.distance(arg0, arg1);
		return value > arg2 ? Double.POSITIVE_INFINITY : value;
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#distance(weka.core.Instance, weka.core.Instance, double, weka.core.neighboursearch.PerformanceStats)
	 */
	public double distance(Instance arg0, Instance arg1, double arg2,
			PerformanceStats arg3) {
		// TODO Auto-generated method stub
		return this.distance(arg0, arg1 , arg2);
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#getAttributeIndices()
	 */
	public String getAttributeIndices() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#getInstances()
	 */
	public Instances getInstances() {
		// TODO Auto-generated method stub
		return this.data;
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#getInvertSelection()
	 */
	public boolean getInvertSelection() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#postProcessDistances(double[])
	 */
	public void postProcessDistances(double[] arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#setAttributeIndices(java.lang.String)
	 */
	public void setAttributeIndices(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#setInstances(weka.core.Instances)
	 */
	public void setInstances(Instances arg0) {
		// TODO Auto-generated method stub
		this.data = arg0;
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#setInvertSelection(boolean)
	 */
	public void setInvertSelection(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see weka.core.DistanceFunction#update(weka.core.Instance)
	 */
	public void update(Instance arg0) {
		// TODO Auto-generated method stub
		
	}

}
