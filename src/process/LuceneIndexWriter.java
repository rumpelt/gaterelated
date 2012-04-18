/**
 * 
 */
package process;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import tokenizers.LucenePTBTokenizer;
import tokenizers.StopWordList;

import annotation.SortedAnnotationList;

import gate.Annotation;
import gate.Corpus;
import gate.Document;
import gate.FeatureMap;
import gate.LanguageAnalyser;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.annic.Constants;
import gate.util.InvalidOffsetException;


/**
 * @author ashwani
 * Index creation using lucene for custome made for my data set of child records.
 */
public class LuceneIndexWriter implements LanguageAnalyser{
	private FeatureMap features;
	/**
	 * required parameters are  as following.
	 *  INDEX_LOCATIONS : path where index will be saved.
	 *  LUCENEVERSION: the lucene version number. By default it is LUCENE_30
	 *  
	 *  e.g --dir="/home/ashwani/xyz/test1"  --processes="process.LucenIndexWriter"
	 *   --pparams="-process.LucenIndexWriter INDEX_LOCATIONS:/home/ashwani/xyz/indexontest1"
	 */
	private FeatureMap parameters;
	private IndexWriter indexwriter;
  
	private Version versionNumber;
	public LuceneIndexWriter() {
		this.versionNumber = Version.LUCENE_30;
		
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
		try {
			this.indexwriter.optimize();
			this.indexwriter.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public static class MedicalRecordAnalyzer extends Analyzer {

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
		 */
    	private Version versionNumber;
    	public MedicalRecordAnalyzer(Version versionNumber) {
    		this.versionNumber  = versionNumber;
    	}
		@Override
		public TokenStream tokenStream(String arg0, Reader arg1) {
			// TODO Auto-generated method stub
			return new StopFilter(this.versionNumber,
					new LowerCaseFilter(this.versionNumber, 
					new LucenePTBTokenizer(arg1)), StopWordList.getMedicalStopWordList(this.versionNumber), true);
			/*
			return new StopFilter(true,
					new PorterStemFilter(
							new LowerCaseFilter(
									new StandardFilter(
											new StandardTokenizer(
													this.versionNumber, arg1)))),
					StopAnalyzer.ENGLISH_STOP_WORDS_SET);
					*/
		}
		@Override
		public TokenStream reusableTokenStream(String fieldName, Reader reader) throws
		IOException{
			Tokenizer tokenizer = (Tokenizer)getPreviousTokenStream();
			if (tokenizer == null) {
				return tokenStream(fieldName, reader);
			}
			else
				tokenizer.reset(reader);
			return tokenizer;
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
	public void setParameterValue(String paramaterName, Object parameterValue)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.parameters.put(paramaterName, parameterValue);
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
		return (String)this.parameters.get("name");
	}
    
	public List<org.apache.lucene.document.Document> getDocumentFromNotes(Document gatedoc) {
		List<org.apache.lucene.document.Document> luceneDoc = new Vector<org.apache.lucene.document.Document>();
		Set<Annotation> allannotations = gatedoc.getAnnotations("Original markups");
		SortedAnnotationList tattrs = new SortedAnnotationList();
		SortedAnnotationList fvalues = new SortedAnnotationList();
		for (Annotation a : allannotations) {
			if (a.getType().equalsIgnoreCase("TIMEBASEDATTRIBUTE"))
				tattrs.addSortedExclusive(a);
			else if(a.getType().equalsIgnoreCase("FieldValue"))
				fvalues.addSortedExclusive(a);
		}
		if (tattrs.size() <=0)
			return luceneDoc;
		int tindex = 0;
		
		org.apache.lucene.document.Document l_doc = new org.apache.lucene.document.Document();
		/*
		l_doc.add(new Field("test", " saying, our $400 blender can't handle something this hard!", Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
		luceneDoc.add(l_doc);
		return luceneDoc;
		*/
		Annotation curr_attr = tattrs.get(tindex);
		l_doc.add(new Field("age",(String)curr_attr.getFeatures().get("age"), Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
		l_doc.add(new Field("agetype",(String) curr_attr.getFeatures().get("agetype"),
				Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
		
		for (Annotation fvalue : fvalues) {
			if (fvalue.getStartNode().getOffset()  > curr_attr.getEndNode().getOffset()) {
				luceneDoc.add(l_doc);
				l_doc = new org.apache.lucene.document.Document();
				tindex = tindex+1;
				curr_attr = tattrs.get(tindex);
				l_doc.add(new Field("age",(String) curr_attr.getFeatures().get("age"),
						Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
				l_doc.add(new Field("agetype",(String) curr_attr.getFeatures().get("agetype"),
						Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
				}
			String fdtype = (String) fvalue.getFeatures().get("fieldtype");
			String content =null;
			try {
				content = gatedoc.getContent().getContent(fvalue.getStartNode().getOffset(),
						fvalue.getEndNode().getOffset()).toString();
			} catch (InvalidOffsetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (content != null && content.length() >0) {
				l_doc.add(new Field(fdtype,content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
			}
		}
		luceneDoc.add(l_doc);		
		return luceneDoc;
		
	}
	public List<Fieldable> getGenericFields(Document doc) {
		List<Fieldable> fields = new Vector<Fieldable>();
		Set<Annotation> allannotations = doc.getAnnotations("Original markups");
		for (Annotation a : allannotations) {
			if (a.getType().equalsIgnoreCase("ATTRIBUTES")) {
				FeatureMap fts = a.getFeatures();
				String name = (String)fts.get("name");
				String content = null;
				try {
					content = doc.getContent().getContent(a.getStartNode().getOffset(), 
							a.getEndNode().getOffset()).toString();
				} catch (InvalidOffsetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (name.equalsIgnoreCase("DOB")) {
					fields.add(new Field("dob",content, Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
				}
				else if (name.equalsIgnoreCase("UID")) {
					fields.add(new Field("uid",content, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.NO));
				}
				else if (name.equalsIgnoreCase("LASTNAME")) {
					fields.add(new Field("lastname",content, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.NO));
				}
				else if (name.equalsIgnoreCase("SEX")) {
					fields.add(new Field("sex",content, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.NO));
				}
				else if (name.equalsIgnoreCase("MRN")) {
					fields.add(new Field("mrn",content, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.NO));
				}
				else if (name.equalsIgnoreCase("GIVENNAME")) {
					fields.add(new Field("givenname",content, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.NO));
				}
				else if (name.equalsIgnoreCase("MOTHERMRN") ) {
					fields.add(new Field("mothermrn",content, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.NO));
				}
			}
			
		}
		return fields;
		
	}
	/* (non-Javadoc)
	 * @see gate.Executable#execute()
	 */
	@Override
	public void execute() throws ExecutionException {
		
		// TODO Auto-generated method stub
		Corpus corpus = this.getCorpus();
		Document doc = this.getDocument();
		if (corpus == null && doc == null)
			return;
		if (this.indexwriter == null) {
			if (this.parameters.get("LUCENEVERSION") != null)
				this.versionNumber = Version.valueOf((String)this.parameters.get("LUCENEVERSION"));
			Analyzer analyzer = new MedicalRecordAnalyzer(this.versionNumber);
			SimpleFSDirectory directory;
			try {
				directory = new SimpleFSDirectory(new File((String)this.parameters.get(Constants.INDEX_LOCATIONS)));
				this.indexwriter = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		int count=0;
		for (Object object : corpus.toArray()) {
			Document document = (Document)object;
			List<org.apache.lucene.document.Document> luceneDocs = this.getDocumentFromNotes(document);
			List<Fieldable> generics =  this.getGenericFields(document);
			for (org.apache.lucene.document.Document l_doc : luceneDocs) {
				l_doc.add(new Field("docid",new String(""+count), Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
				count++;
				for (Fieldable f : generics) {
					l_doc.add(f);
				}
				try {
					this.indexwriter.addDocument(l_doc);
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
		this.parameters.put("document",document);
	}

	/* (non-Javadoc)
	 * @see gate.LanguageAnalyser#getDocument()
	 */
	@Override
	public Document getDocument() {
		// TODO Auto-generated method stub
		return this.parameters.get("document") != null ?  (Document)this.parameters.get("document")
				: null;
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
		return this.parameters.get("corpus") != null ?  (Corpus)this.parameters.get("corpus")
				: null;
	}

}
