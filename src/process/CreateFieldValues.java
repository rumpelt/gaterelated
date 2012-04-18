/**
 * 
 */
package process;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import datastore.CorpusClass;

import annotation.SortedAnnotationList;

import gate.Annotation;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.Resource;
import gate.annotation.AnnotationSetImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.InvalidOffsetException;

/**
 * @author ashwani
 *
 */
public class CreateFieldValues implements LanguageAnalyser{
	/**
	 * run time parameters to be specified for this class.
	 * One parameter need t be specified to avoid crash is "name".
	 * Give any name to the instance of this resorce.
	 * Other parameter are as following.
	 * dumpdirectory: directory where  you want to dump the contents
	 */
	private FeatureMap parameter;
	private FeatureMap features;
	/* (non-Javadoc)
	 * @see gate.ProcessingResource#reInit()
	 */
	@Override
	public void reInit() throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see gate.Resource#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		this.parameter = null;
		this.features = null;
	}

	/* (non-Javadoc)
	 * @see gate.Resource#getParameterValue(java.lang.String)
	 */
	@Override
	public Object getParameterValue(String arg0)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		return this.parameter.get(arg0);
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
	 * @see gate.Resource#setParameterValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setParameterValue(String arg0, Object arg1)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.parameter.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see gate.Resource#setParameterValues(gate.FeatureMap)
	 */
	@Override
	public void setParameterValues(FeatureMap arg0)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.parameter = arg0;
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
	public void setFeatures(FeatureMap arg0) {
		// TODO Auto-generated method stub
		this.features = arg0;
	}

	/* (non-Javadoc)
	 * @see gate.util.NameBearer#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return (String)this.parameter.get("name");
		
	}

	/* (non-Javadoc)
	 * @see gate.util.NameBearer#setName(java.lang.String)
	 */
	@Override
	public void setName(String arg0) {
		// TODO Auto-generated method stub
		this.parameter.put("name", arg0);
	}
	
	private  Set<Annotation> newFieldValues(Document doc) {
		AnnotationSetImpl toKeep = new AnnotationSetImpl(doc);
		SortedAnnotationList fields = new SortedAnnotationList();
		SortedAnnotationList timeattrs = new SortedAnnotationList();
		for(Annotation a : doc.getAnnotations("Original markups")) {
			if (a.getType().equalsIgnoreCase("Field"))
				fields.addSortedExclusive(a);
			else if (a.getType().equalsIgnoreCase("TIMEATTRIBUTES"))
				timeattrs.addSortedExclusive(a);
		}
		int fcount = 0;
		int tcount = 0;
		int tsize = timeattrs.size();
		while (fcount < fields.size()) {
			Annotation pfield = (Annotation)fields.get(fcount);
			Annotation nfield = null;
			Annotation tattr = (Annotation)timeattrs.get(tcount);
			long startoffset;
			long endoffset;
			startoffset = pfield.getEndNode().getOffset() + 1;
			if (fcount != fields.size() - 1) {
				nfield  = (Annotation)fields.get(fcount + 1);
			}
			if(nfield == null) {
				endoffset = tattr.getEndNode().getOffset();
			}
			else {
				if (nfield.getStartNode().getOffset() >= tattr.getEndNode().getOffset()) {
					endoffset = tattr.getEndNode().getOffset();
					tcount++;
				}
				else {
					endoffset = nfield.getStartNode().getOffset() - 1;
				}
					
			}
			if (endoffset >= startoffset) {
				FeatureMap fm = Factory.newFeatureMap();
				try {
					fm.put("fieldtype", 
							doc.getContent().getContent(pfield.getStartNode().getOffset(),
									pfield.getEndNode().getOffset()).toString());
					toKeep.add(startoffset, endoffset, "FieldValue", fm);
				} catch (InvalidOffsetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			fcount++;
		}
		return toKeep;
	}
	
	/* (non-Javadoc)
	 * @see gate.Executable#execute()
	 */
	@Override
	public void execute() throws ExecutionException {
		// TODO Auto-generated method stub
		Corpus corpus = this.getCorpus();
		Iterator docI = corpus.iterator();
		while (docI.hasNext()) {
			Document doc = (Document) docI.next();
			CorpusClass.dumpdocumentondisk(doc, (String)this.parameter.get("dumpdirectory"),
					this.newFieldValues(doc));
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
	 * @see gate.LanguageAnalyser#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		// TODO Auto-generated method stub
		return (Corpus)this.parameter.get("corpus");
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#getDocument()
	 */
	@Override
	public Document getDocument() {
		// TODO Auto-generated method stub
		return (Document)this.parameter.get("document");
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#setCorpus(gate.Corpus)
	 */
	@Override
	public void setCorpus(Corpus arg0) {
		// TODO Auto-generated method stub
		this.parameter.put("corpus",arg0);
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#setDocument(gate.Document)
	 */
	@Override
	public void setDocument(Document arg0) {
		// TODO Auto-generated method stub
		this.parameter.put("document",arg0);
	}

	
}
