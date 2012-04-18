/**
 * 
 */
package process;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.LanguageAnalyser;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.annic.Constants;
import gate.creole.annic.IndexException;
import gate.creole.annic.Indexer;
import gate.creole.annic.lucene.LuceneIndexer;
import gate.persist.LuceneDataStoreImpl;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;

/**
 * @author ashwani
 *
 */
public class AnnicIndexWriter implements LanguageAnalyser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8932014211440490322L;
	private FeatureMap features;
	/**
	 * parameters for this processessing resource.
	 * Following parameters should be specified.
	 * datastore: url of the data store
	 * INDEX_LOCATIONS (mandatory) :  Value is a string which is path where index files will be kept
	 * BASE_TOKEN_ANNOTATION_TYPE :
	 * INDEX_UNIT_ANNOTATION_TYPE : 
	 *  FEATURES_TO_EXCLUDE:
	 *  FEATURES_TO_INCLUDE:  
	 */
	private FeatureMap parameter;
	private Indexer indexer = null;
	public AnnicIndexWriter() {
		this.indexer = null;
	}
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
		this.indexer = null;
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

	/* (non-Javadoc)
	 * @see gate.Executable#execute()
	 */
	@Override
	public void execute() throws ExecutionException {
		// TODO Auto-generated method stub
		LuceneDataStoreImpl ds = null;
		ds = new LuceneDataStoreImpl();
		ds.setStorageDir(new File((String)this.parameter.get("datastore")));
		
		if (this.indexer ==null) {
				try {
					this.indexer = new LuceneIndexer(new File((String)this.parameter.get(Constants.INDEX_LOCATIONS)).toURI().toURL());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		Map parameters = new HashMap();
		try {
			parameters.put( Constants.INDEX_LOCATION_URL , new File((String)this.parameter.get(Constants.INDEX_LOCATIONS)).toURI().toURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		parameters.put( Constants.BASE_TOKEN_ANNOTATION_TYPE , "Token");
		parameters.put( Constants.CREATE_TOKENS_AUTOMATICALLY ,new Boolean( true ));
		List < String > setsToInclude = new ArrayList <String >();
		setsToInclude.add("<null>");
		parameters.put( Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE , setsToInclude);
		parameters.put( Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE , new ArrayList<String>());
		parameters.put( Constants.FEATURES_TO_INCLUDE , new ArrayList <String >());
		parameters.put( Constants.FEATURES_TO_EXCLUDE , new ArrayList <String >());
		Corpus corpus = null;
		List corpusids=null;
		try {
			corpusids = ds.getLrIds("gate.corpora.SerialCorpusImpl");
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Object id : corpusids) {
			FeatureMap feat = Factory.newFeatureMap();
			feat.put(DataStore.LR_ID_FEATURE_NAME, id);
			feat.put(DataStore.DATASTORE_FEATURE_NAME, ds);
			try {
				corpus = (Corpus)Factory.createResource("gate.corpora.SerialCorpusImpl",feat);
				try {
					indexer.setCorpus(corpus);
					ds.setIndexer(indexer , parameters );
				} catch (IndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ResourceInstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (corpusids == null) {
			List docids=null;
			try {
				docids = ds.getLrIds("gate.corpora.DocumentImpl");
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
