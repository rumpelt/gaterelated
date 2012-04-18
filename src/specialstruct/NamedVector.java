/**
 * 
 */
package specialstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 *  A vector to give names to indexes and also to store the datatypes at these Indexes;
 * All named vectors will share the same indexNames and indexDatatype.
 * This is done to avoid space consumption. So be careful with setting of indexNames and 
 * indexDatatypes.
 * Also implements Comparable to implement ordering based on the primaryKey an secondarykey.
 * primaryKey is the indexName of the index which is to be used as primary key to be
 * sued for sorting
 * secondarKey is the indexName of the index which is to be used as secondaryy key to be
 * sued for sorting
 * @author ashwani
 *
 */
public class NamedVector extends Vector implements Comparable{
	private static HashMap<Integer, String> indexNames= new HashMap<Integer, String>();
	private static HashMap<Integer, LearningDataType> indexdatatype = new HashMap<Integer, LearningDataType>();
	private static HashMap<String, List<String>> possibleValues=null;
	
	public static void addPossibleValuesForIndex(String indexName , List<String> values) {
		if(NamedVector.possibleValues == null) {
			NamedVector.possibleValues = new HashMap<String, List<String>>();		
		}
		NamedVector.possibleValues.put(indexName, values);
	}
	
	public static List<String> getPossibleValuesForIndex(String indexName) {
		if(NamedVector.possibleValues == null) {
			return null;		
		}
		return NamedVector.possibleValues.get(indexName);
	}
	private static String primaryKey= null;
	/**
	 * @return the primaryKey
	 */
	public static String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 */
	public static void setPrimaryKey(String primaryKey) {
		NamedVector.primaryKey = primaryKey;
	}

	/**
	 * @return the secondaryKey
	 */
	public static String getSecondaryKey() {
		return secondaryKey;
	}

	/**
	 * @param secondaryKey the secondaryKey to set
	 */
	public static void setSecondaryKey(String secondaryKey) {
		NamedVector.secondaryKey = secondaryKey;
	}
	private static String secondaryKey=null;
	
	
	public NamedVector() {
		super();
	}
	
	public NamedVector(String primaryKey, String secondaryKey) {
		super();
		this.primaryKey = primaryKey;
		this.secondaryKey = secondaryKey;
	}
	public void add(Object val, String indexName) {
		NamedVector.indexNames.put(super.size(), indexName);
		super.add(val);
	}
	
	/**
	 * re creates the indexName and indexdatatype map
	 */
	public void reinit() {
		this.indexdatatype =  new HashMap<Integer, LearningDataType>();
		this.indexNames =  new HashMap<Integer, String>();
	}
	public void add(Object val, String indexName, 
			String indexDataType) {
		NamedVector.indexNames.put(super.size(), indexName);
		NamedVector.indexdatatype.put(super.size(), LearningDataType.valueOf(indexDataType));
		super.add(val);
		
	}
	
	
	public static LearningDataType getIndexDatatype(int index) {
		return NamedVector.indexdatatype.get(index);
	} 
	
	public static void setIndexDatatype(int index, String dataType) {
		NamedVector.indexdatatype.put(index, LearningDataType.valueOf(dataType));
	}
	
	public static void setIndexName(int index, String indexName) {
		NamedVector.indexNames.put(index, indexName);
	}
	
	public static int getIndex(String indexName) {
		for (int key : NamedVector.indexNames.keySet()) {
			if (NamedVector.indexNames.get(key).equals(indexName))
				return key;
		}
		return -1;
	}
	public String getIndexName(int index) {
		return (String)NamedVector.indexNames.get(index);
	}
	
	public void set(int index, Object element, String indexName) {
		NamedVector.indexNames.put(index, indexName);
		super.set(index, element);
	}
	
	public void set(int index, Object element, String indexName, 
			String indexDataType) {
		
		NamedVector.indexNames.put(index, indexName);
		NamedVector.indexdatatype.put(index, LearningDataType.valueOf(indexDataType));
		super.set(index, element);
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		NamedVector nv = (NamedVector) arg0;
		if (NamedVector.primaryKey == null || NamedVector.getIndex(NamedVector.primaryKey) == -1)
			return 0;
		int pindex = NamedVector.getIndex(NamedVector.primaryKey);
		LearningDataType pdatatype = NamedVector.getIndexDatatype(pindex);
		int sindex = NamedVector.getIndex(NamedVector.secondaryKey);
		LearningDataType sdatatype = NamedVector.getIndexDatatype(sindex);
		if (pdatatype == LearningDataType.STRING 
				|| pdatatype == LearningDataType.NOMINAL ) {
			String thisVal = (String)super.get(pindex) ;
			if (sindex != -1 && thisVal.compareTo((String)nv.get(pindex)) == 0) {
				if (sdatatype == LearningDataType.STRING || 
						sdatatype == LearningDataType.NOMINAL )
					return ((String)super.get(sindex)).compareTo(
							(String)nv.get(sindex));
				else
					return ((Double)super.get(sindex)).compareTo(
							(Double)nv.get(sindex));
			}
			else
				return thisVal.compareTo((String)nv.get(pindex));
		}
		else {
			if (sindex != -1 && ((Double)super.get(pindex)).compareTo(
					(Double)nv.get(pindex)) == 0) {
				if (sdatatype == LearningDataType.STRING || 
						sdatatype == LearningDataType.NOMINAL )
					return ((String)super.get(sindex)).compareTo(
							(String)nv.get(sindex));
				else
					return ((Double)super.get(sindex)).compareTo(
							(Double)nv.get(sindex));
			}
			else
				return ((Double)super.get(pindex)).compareTo(
						(Double)nv.get(pindex));
		}
	}	 
}
