/**
 * 
 */
package specialstruct;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author ashwani
 * A simple class to give name to a Collection of Vectors.
 * Each Vector has identifier attached to it. Identifiers are not bound
 * to be unique. 
 */
public class MappedVector {
	private List<String> keys;
	private Vector<Vector> values;
	/**
	 * @return the keys
	 */
	public List<String> getKeys() {
		return keys;
	}
	
	public int size() {
		return keys.size() == values.size() ? keys.size() : -1; 
	}
	/**
	 * @return the values
	 */
	public Vector<Vector> getValues() {
		return values;
	}

	public MappedVector() {
		this.keys = new Vector<String>();
		this.values = new Vector<Vector>();
	}
	
	public boolean addListOfValues(String id, Vector values) {
		if (id == null)
			return false;
		this.keys.add(id);
		this.values.add(values);
		return true;
	}
	
	/**
	 * 
	 * @param key
	 * @return Returns the value  associate with first key which will be encountered when iterating over the 
	 * identifiers.
	 */
	public Vector getValues(String key) {
		int count = 0;
		for (String id : keys ) {
			if (key.equals(id))
				return this.values.get(count);
			count++;
		}
		return null;
	}
	
	
	public String returnKeys(Vector values) {
		int outerCounter=0;
		for (Vector v : this.values) {
			int count;
			for (count = 0; count < values.size(); count++ ) {
				if (v.get(count) != values.get(count))
					break;
			}
			if (count == values.size())
				return keys.get(outerCounter);
			outerCounter++;
		}
		return null;
	}
}