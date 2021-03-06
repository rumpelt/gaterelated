/**
 * 
 */
package process;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import annotation.SortedAnnotationList;
import au.com.bytecode.opencsv.CSVWriter;

import datastore.CorpusClass;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.LanguageAnalyser;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;

/**
 * @author ashwani
 * Very very specific file . 
 * Not suitable for general purpose
 */
public class DumpAnnotation implements LanguageAnalyser {
	private FeatureMap features;
	/**
	 * paramterers we are interested are as following;
	 * dump_file : name file to used to dump the contents
	 * low_age: low_age
	 * up_age : up_age
	 * fieldtype : regular expression containing the field.
	 * command line example from the gate MainEntry
	 * --dir="/home/ashwani/xyz/test1"  --processes="process.DumpAnnotation" 
	 * --pparams="-process.DumpAnnotation dump_file:/home/ashwani/xyz/Appetite-4.csv  
	 * low_age:0.0 up_age:7.0"
	 */
	private FeatureMap parameters;
	/**
	 * name of file to dump the content
	 */
	private CSVWriter fwriter;
	/**
	 * the name of defualt annotation set
	 */
	private String defaultAnn;
	public DumpAnnotation() {
		this.features = Factory.newFeatureMap();
		this.parameters = Factory.newFeatureMap();
		this.fwriter = null;
		this.defaultAnn = "Original markups";
	}
	
	/* (non-Javadoc)
	 * @see gate.ProcessingResource#reInit()
	 */
	@Override
	public void reInit() throws ResourceInstantiationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see gate.Resource#init()
	 */
	@Override
	public Resource init() throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see gate.Resource#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		this.parameters = null;
		this.features = null;
		try {
			this.fwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see gate.Resource#getParameterValue(java.lang.String)
	 */
	@Override
	public Object getParameterValue(String paramaterName)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		return this.parameters.get(paramaterName);
	}

	/* (non-Javadoc)
	 * @see gate.Resource#setParameterValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setParameterValue(String parameterName, Object parameterValue)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.parameters.put(parameterName, parameterValue);
	}

	/* (non-Javadoc)
	 * @see gate.Resource#setParameterValues(gate.FeatureMap)
	 */
	@Override
	public void setParameterValues(FeatureMap parameters)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.parameters = parameters;
	}
	
	/* (non-Javadoc)
	 * @see gate.util.FeatureBearer#getFeatures()
	 */
	@Override
	public FeatureMap getFeatures() {
		// TODO Auto-generated method stub
		return this.features;
	}

	/* (non-Javadoc)
	 * @see gate.util.FeatureBearer#setFeatures(gate.FeatureMap)
	 */
	@Override
	public void setFeatures(FeatureMap features) {
		// TODO Auto-generated method stub
		this.features = features;
	}

	/* (non-Javadoc)
	 * @see gate.util.NameBearer#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.parameters.put("name", name);
	}

	/* (non-Javadoc)
	 * @see gate.util.NameBearer#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return (String) this.parameters.get("name");
	}
	/** This function is when there is progress note information for the
	 * baby but the corresponding field was not found in the notes.
	 * Such situation can arise if the field in not in the notes or we have 
	 * specified wrong regular expression for the field.
	 * We wnat to do dump such babies to find out how many such babies we have. 
	 */
    private void dumpNoInformation(String id, List<Annotation> anns) {
    	String[] row = new String[3];
		row[0] = id;
		for (Annotation a: anns) {
			row[1] = (String)a.getFeatures().get("age");
			row[2] = "";
			this.fwriter.writeNext(row);
		}
    }
	private void dumptoFile(String identifier , List<Annotation> anns, Document doc) {
		try {
			String[] row = new String[3];
			row[0] = identifier;

			for (Annotation a: anns) {
				row[1] = (String)a.getFeatures().get("age");
				String content = doc.getContent().getContent(a.getStartNode().getOffset()
						, a.getEndNode().getOffset()).toString();
				row[2] = "\""+content+"\"";
				this.fwriter.writeNext(row);
			}
		}
		catch (InvalidOffsetException e) {
			e.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see gate.Executable#execute()
	 */
	@Override
	public void execute() throws ExecutionException {
		// TODO Auto-generated method stub
		Corpus corpus = this.getCorpus();
		Iterator docI = corpus.iterator();
		
		try {
			if (this.fwriter == null) {
				try {
					FileWriter fw = new FileWriter((String)this.parameters.get("dump_file"));
					this.fwriter = new CSVWriter(fw);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			float lowage = Float.parseFloat((String)this.getParameterValue("low_age"));
			float upage = Float.parseFloat((String)this.getParameterValue("up_age"));
			String regex = (String) this.parameters.get("fieldtype"); // for example for feeding: ((feed[a-z]{0,3})|(appetite))\s*[:?]$
			Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
			
			while (docI.hasNext()) {
				Document doc = (Document) docI.next();
				AnnotationSet orig = doc.getAnnotations(this.defaultAnn);
				SortedAnnotationList fields = new SortedAnnotationList();
				SortedAnnotationList tatts = new SortedAnnotationList();
				
				for (Annotation a : orig) {
					if(a.getType().equalsIgnoreCase("TIMEBASEDATTRIBUTE")) {
						float age = Float.parseFloat((String)a.getFeatures().get("age"));
						if (age >= lowage && age <= upage) {
							tatts.add(a);
						}
					}
				}
				
				String identifier = null;
				for (Annotation a : orig) {
					if (identifier == null && a.getType().equalsIgnoreCase("ATTRIBUTES"))
					{
						if (a.getFeatures().get("name").equals("MRN")) {
							identifier = doc.getContent().getContent(
									a.getStartNode().getOffset(), a.getEndNode().getOffset()).toString();
						}
					}
					
					if (a.getType().equalsIgnoreCase("FieldValue")) {
						String fdtype = (String)a.getFeatures().get("fieldtype");
						if (pattern.matcher(fdtype).matches()) {
							for (Annotation t : tatts) {
								if (a.getStartNode().getOffset() >= t.getStartNode().getOffset() &&
									a.getEndNode().getOffset() <= t.getEndNode().getOffset()) {
									a.getFeatures().put("age", t.getFeatures().get("age"));
									fields.add(a);									
								}
							}
						}
					}
				}
				if (fields.size() > 0)
					this.dumptoFile(identifier , fields, doc);
				else if (tatts.size() > 0)
					this.dumpNoInformation(identifier ,tatts);
			}
		}
		catch (InvalidOffsetException e) {
			e.printStackTrace();
		}
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see gate.Executable#interrupt()
	 */
	@Override
	public void interrupt() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see gate.Executable#isInterrupted()
	 */
	@Override
	public boolean isInterrupted() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#setDocument(gate.Document)
	 */
	@Override
	public void setDocument(Document document) {
		// TODO Auto-generated method stub
		this.parameters.put("doucment", document);
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#getDocument()
	 */
	@Override
	public Document getDocument() {
		// TODO Auto-generated method stub
		return (Document) this.parameters.get("document");
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#setCorpus(gate.Corpus)
	 */
	@Override
	public void setCorpus(Corpus corpus) {
		// TODO Auto-generated method stub
		this.parameters.put("corpus", corpus);
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		// TODO Auto-generated method stub
		return (Corpus) this.parameters.get("corpus");
	}

}
