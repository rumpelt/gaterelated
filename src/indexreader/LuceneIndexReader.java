/**
 * 
 */
package indexreader;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import java.util.Set;

import java.util.Vector;



import cchs.FeedCategories;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.document.Document;


import specialstruct.MappedVector;


import weka.WekaInstances;

import weka.classifiers.J48Classifier;

import weka.core.Attribute;


/**
 * @author ashwani
 *	A lucene index reader to act specialized utility to get term vector representation for documents
 *  based on various specialized routines on the field of the documents.
 *  A sepcialized set of routines used to get data in various formats for machine learning algorithms
 *  like Weka , Mahout
 */
public class LuceneIndexReader {
	/**
	 * index location.
	 */
	private FieldSelector fieldSelector=null;
	private final File indexdirectory;
	private IndexReader indexreader;
	private String fieldtoget;
	private boolean debug;
	static Logger logger = Logger.getLogger(LuceneIndexReader.class.getName());	
	public FieldSelector createFieldSelector(Set<String> fieldsToLoad, Set<String> lazyFields) {
		if (lazyFields == null)
			lazyFields = new HashSet<String>();
		if (fieldsToLoad == null)
			fieldsToLoad = new HashSet<String>();
		FieldSelector fieldSelector = new SetBasedFieldSelector(fieldsToLoad, lazyFields);
		if (fieldsToLoad != null)
			for (String field : fieldsToLoad) {
				fieldSelector.accept(field);
			}
		if (lazyFields != null)
			for (String field : lazyFields) {
				fieldSelector.accept(field);
			}
		return fieldSelector;
	}
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
	public LuceneIndexReader(String indexdirectory) {
		this.indexreader = null;
		this.indexdirectory = new File(indexdirectory);
	}
	/**
	 * A method to populate the index reader of this class
	 * @return
	 */
	private void createIndexReader() {
		try {
			this.indexreader = IndexReader.open(new SimpleFSDirectory(indexdirectory));
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Returns a term space of field from the index
	 * @param field : field of interest
	 * @param filterField : a filed which is filtering criterion for the documents which 
	 * needs to be selected for getting term space.
	 * @param filtervalue : the value of fileter value. Either this will be set or
	 *  low value and upvalue.
	 * @param lowvalue : the low bound value of the filterfield when filtervalue is 
	 * not set. 
	 * @param upvalue : the upper bound of the filterfield when filtervalue is not
	 * set.
	 * @return
	 */
	public List<String> getFileteredTermSpace(String field, String filterField,
			String filtervalue ,float lowvalue, float upvalue) {
		LinkedHashSet<String> uniqueTerms =new LinkedHashSet<String>();
		HashSet<String> fieldsToLoad = new HashSet<String>();
		fieldsToLoad.add(field);
		if (filterField == null)
			return this.getTermSpace(field);
		fieldsToLoad.add(filterField);
		FieldSelector fs = this.createFieldSelector(fieldsToLoad, new HashSet<String>());
		for (int count = 0; count < this.indexreader.maxDoc(); count++) {
			try {
				Document luceneDoc = this.indexreader.document(count, fs);
				String fvalue =  luceneDoc.get(filterField);
				if(filtervalue != null && !fvalue.equals(filtervalue)) 
					continue;				
				else {
					float floatvalue = Float.parseFloat(fvalue);
					if (floatvalue < lowvalue || floatvalue > upvalue)
						continue;						
				}
				TermFreqVector terms = this.indexreader.getTermFreqVector(count, field);
				if (terms == null)
					continue;
				for (String term : terms.getTerms()) {
					uniqueTerms.add(term);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<String> result = new Vector<String>();
		for (String terms: uniqueTerms) {
			result.add(terms);
		}
		logger.trace("term space for field :"+ field + ":  "+ result);
		return result;
	}
	
	public List<String> getTermSpace(String field) {
		LinkedHashSet<String> uniqueTerms =new LinkedHashSet<String>();
		for (int count = 0; count < this.indexreader.maxDoc(); count++) {
			try {
				TermFreqVector terms = this.indexreader.getTermFreqVector(count, field);
				if (terms == null)
					continue;
				for (String term : terms.getTerms()) {
					uniqueTerms.add(term);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<String> result = new Vector<String>();
		for (String terms: uniqueTerms) {
			result.add(terms);
		}
		logger.trace("term space for field :"+ field + ":  "+ result);
		return result;
	}
	
	
	public int returnIdf(Term t) {
		try {
			return this.indexreader.docFreq(t);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public MappedVector returnTfIdfOfField(String field , String identifierField
			,String filterfield, float lowval, float upval) {
		MappedVector result = new MappedVector();
		if (this.indexreader != null)
			this.createIndexReader();
		Set<String> fieldsToLoad = new HashSet<String>();
		fieldsToLoad.add(field);
		fieldsToLoad.add(identifierField);
		fieldsToLoad.add(filterfield);
		List<String> uniqueTerms = this.getTermSpace(field);
		int maxdoc = this.indexreader.maxDoc();
		for (int count = 0 ; count < maxdoc ; count++) {
			try {
				
				Document doc = this.indexreader.document(count,new SetBasedFieldSelector(fieldsToLoad,new HashSet<String>()));
				String identifier = doc.getField(identifierField).stringValue();
				Float val = Float.parseFloat(doc.getField(filterfield).stringValue());
				if (filterfield !=null && (val < lowval || val > upval ))
					continue;
				Vector<Double> values = new Vector<Double>(uniqueTerms.size()); 
				TermFreqVector termfreq = this.indexreader.getTermFreqVector(count, field);
				if (termfreq == null) {
					if (debug) {
						System.out.println("No term freq for id: "+ identifier+ " field: "+ field +" filter field: "+ filterfield+ " value "+ val);
					}
					continue;
				}
				int[] frequencies = termfreq.getTermFrequencies();
				for(String globalterm : uniqueTerms) {
					int index = termfreq.indexOf(globalterm);
					int docfreq = this.returnIdf(new Term(field,globalterm));
					if (index >= 0) 
						values.add((double)frequencies[index] * Math.log((double)maxdoc / docfreq));
					else
						values.add(0.0);
					
				}	
				
					logger.trace("id :"+identifier+ " filterfield : "+ filterfield+" filterfield value: "+val + " terms freq: "+ values);
				
					
				result.addListOfValues(identifier, values);
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return result;
	}
	public Vector<Integer> returnTermFreqVector(int docnumber, String field , String identifierField ,String filterfield, float lowval, float upval) {
		Vector<Integer> result = new Vector<Integer>();
		List<String> uniqueTerms = this.getTermSpace(field);
		Set<String> fieldsToLoad = new HashSet<String>();
		fieldsToLoad.add(field);
		fieldsToLoad.add(identifierField);
		fieldsToLoad.add(filterfield);
		try {
			Document doc = this.indexreader.document(docnumber,new SetBasedFieldSelector(fieldsToLoad,new HashSet<String>()));
			Float filtervalue = Float.parseFloat(doc.getField(filterfield).stringValue());
			if (filtervalue < lowval || filtervalue > upval)
				return result;
			
			TermFreqVector termfreq = this.indexreader.getTermFreqVector(docnumber, field);
			int[] frequencies = termfreq.getTermFrequencies();
			
			for (String globalterm : uniqueTerms){
				int index = termfreq.indexOf(globalterm);
				if (index >= 0) 
					result.add(frequencies[index]);
				else
					result.add(0);
			}
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Returns a list of Vectors of equal length for frequencies of  terms of  a given field.   
	 * @param field : field for which term vectors is to be fetched
	 * @param identifierfield: THis field will be used to identify each row of the TermVector
	 * @param filterfield : A field whose values must be satisified before the field to get can be
	 * included. For example if we have to get fetch field "HISTORY:" for kids with age equal to 3
	 * @param lowval: the lower bound value of the filterfield which should be satisfied.
	 * @param upvalue: the upper bound value of the filterfield which should be satisfied.
	 * @return
	 */
	public MappedVector returnTermFreqVectorsOfField(String field , String identifierField ,String filterfield, float lowval, float upval) {
		
		MappedVector result = new MappedVector();
		if (this.indexreader != null)
			this.createIndexReader();
		Set<String> fieldsToLoad = new HashSet<String>();
		fieldsToLoad.add(field);
		fieldsToLoad.add(identifierField);
		fieldsToLoad.add(filterfield);
		List<String> uniqueTerms = this.getTermSpace(field);
		for (int count = 0 ; count < this.indexreader.maxDoc() ; count++) {
			try {
				
				Document doc = this.indexreader.document(count,new SetBasedFieldSelector(fieldsToLoad,new HashSet<String>()));
				String identifier = doc.getFieldable(identifierField).stringValue();
				Float val = Float.parseFloat(doc.getFieldable(filterfield).stringValue());
				if (filterfield !=null && (val < lowval || val > upval ))
					continue;
				Vector<Integer> values = new Vector<Integer>(uniqueTerms.size()); 
				TermFreqVector termfreq = this.indexreader.getTermFreqVector(count, field);
				if (termfreq == null) {
					if (debug) {
						System.out.println("No term freq for id: "+ identifier+ " field: "+ field +" filter field: "+ filterfield+ " value "+ val);
					}
					continue;
				}
				int[] frequencies = termfreq.getTermFrequencies();
				for(String globalterm : uniqueTerms) {
					int index = termfreq.indexOf(globalterm);
					if (index >= 0) 
						values.add(frequencies[index]);
					else
						values.add(0);
					
				}	
				if (debug) {
					System.out.println("id :"+identifier+ " filterfield : "+ filterfield+" filterfield value: "+val + " terms freq: "+ values);
				}
					
				result.addListOfValues(identifier, values);
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return result;
	}
	
	public WekaInstances returnIndicatorVectorOfTermSpace(IndexReader ir ,
			String identifier, String fieldToGet, String filterfield, 
			String fiterfieldvalue, float lowvalue, float upvalue,
			String datasetname)
			throws CorruptIndexException, IOException {
		List<String> termspace = this.getFileteredTermSpace(fieldToGet, filterfield, 
				fiterfieldvalue, lowvalue, upvalue);
		Set<String> ftoload = new HashSet<String>();
		WekaInstances wekainstances = new WekaInstances(datasetname,
				new ArrayList<Attribute>(), termspace.size()+2);
		int index = 0;
		ftoload.add(fieldToGet);
		if (identifier != null) {
			ArrayList<String> ambiguity=null;
			wekainstances.insertAttributeAt(new Attribute(identifier,ambiguity),index);
			index++;
			ftoload.add(identifier);			
		}
		if (filterfield != null) {
			ArrayList<String> ambiguity=null;
			if (fiterfieldvalue != null)
				wekainstances.insertAttributeAt(new Attribute(filterfield,
						ambiguity),index);
			else
				wekainstances.insertAttributeAt(new Attribute(filterfield),index);
			index++;
			ftoload.add(filterfield);
		}
		
		FieldSelector fs = this.createFieldSelector(ftoload, null);
		
		List<String> possibleValues = new Vector<String>();
		possibleValues.add("1");
		possibleValues.add("0");
		
		for (String s : termspace) {
			wekainstances.insertAttributeAt(new Attribute(s,
					possibleValues),index);
			index++;
		}
		
		for (int count = 0; count < ir.maxDoc(); count++) {
			wekainstances.initWorkingInstance();
			Document doc = ir.document(count, fs);
			String id = doc.getFieldable(identifier).stringValue();
			wekainstances.setValueOfWorkingInstance(identifier, id);
			if (filterfield != null) {
				if (fiterfieldvalue != null) {
					String fvalue = doc.getFieldable(filterfield).stringValue();
					if (!fvalue.equals(fiterfieldvalue))						
						continue;
					wekainstances.setValueOfWorkingInstance(fiterfieldvalue, fvalue);
				}
				else  {
					double fvalue = Double.parseDouble(doc.getFieldable(filterfield).stringValue());
					if (fvalue < lowvalue || fvalue > upvalue) 
						continue;
					wekainstances.setValueOfWorkingInstance(filterfield, fvalue);
				}
			}
			
			TermFreqVector termfreq =
				this.indexreader.getTermFreqVector(count, fieldToGet);
		    if (termfreq == null)
		    	continue;
			for (String term : termspace) {
				int ind = termfreq.indexOf(term);
				if (ind >= 0) 
					wekainstances.setValueOfWorkingInstance(term, "1");
				else
					wekainstances.setValueOfWorkingInstance(term, "0");
			}
			wekainstances.add(wekainstances.getWorkingInstance());
			wekainstances.clearWorkingInstance();
			
		}
		return wekainstances;
	}
	
	public static void main(String[] argv){
		if (argv.length > 1) {
			PropertyConfigurator.configure(argv[1]);
		}
		LuceneIndexReader lr = new LuceneIndexReader(argv[0]);
		CollocationRoutines cr = new CollocationRoutines();
		
		lr.createIndexReader();
		//cr.initializeTerms(lr.indexreader);
		//lr.setDebug(true);
		String field = "Feeding:";
		String fieldvalue = null;
		try {
			WekaInstances testingSet= lr.returnIndicatorVectorOfTermSpace(
					lr.indexreader,"mrn", field,"age", fieldvalue, 
					(float)1.0, (float)3.0, "testset");
			testingSet.addClassAttribute("class", FeedCategories.returnAllValues());
			List<String> termspace = lr.getFileteredTermSpace(field, "age", 
					null,(float)1.0, (float)3.0);
		//	WekaInstances trainingSet = MedicalTrainer.returnTrainingSet(argv[2], 
			//		termspace, (float)0.0, (float)3.0, "mrn");
			J48Classifier j48 = new J48Classifier();
			
			
			try {
				j48.setOptions(new String[]{"-U","-O","-B"});
				int[] todelete = {0,1};
	//			System.out.println(trainingSet);
				testingSet.setClassMissingForEachInstance();
		//		WekaRoutines.dumpArff("/home/ashwani/xyz/testdumper.arff", testingSet);
			//	WekaRoutines.dumpArff("/home/ashwani/xyz/traindumper.arff", trainingSet);
			//	testingSet.setClassMissingForEachInstance();
	//			j48.trainOnInstances(trainingSet, todelete);
		//		j48.testInstances(testingSet, 0, todelete, todelete,
					//	"/home/ashwani/xyz/classifierdump-1-3-X.csv");
			//	j48.evaluate(j48.trainOnInstances(trainingSet, todelete),
				//		j48.testInstances(testingSet, 0, todelete2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//MappedVector mapvec = lr.returnTfIdfOfField(field, "mrn", "age", 10, 12);	
		//Instances instances= WekaRoutines.returnInstances(mapvec , 
		//		lr.getTermSpace(field));		
		//WekaRoutines.assignClusterToEachInstance(WekaRoutines.kmeanclusterexecution(instances), instances);
	//	WekaRoutines.dumpInArffFormat("/home/ashwani/xyz/feedat2.arff", "feedingAt2.3",mapvec,  lr.getTermSpace(field));
	}
	
	
}
