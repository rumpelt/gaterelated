/**
 * 
 */
package datastore;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageResource;
import gate.corpora.CorpusImpl;
import gate.corpora.DocumentImpl;
import gate.corpora.SerialCorpusImpl;
import gate.creole.ResourceInstantiationException;
import gate.creole.annic.Constants;
import gate.creole.annic.Indexer;
import gate.creole.annic.lucene.LuceneIndexer;
import gate.creole.ir.DefaultIndexDefinition;
import gate.creole.ir.DocumentContentReader;
import gate.creole.ir.IndexException;
import gate.creole.ir.IndexField;
import gate.creole.ir.IndexedCorpus;
import gate.persist.LuceneDataStoreImpl;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;

/**
 * @author ashwani
 *
 */
public class DataStorage implements Collection<CorpusClass>{
	/**
	 * specefies if this lucene based data store or a serial data store.
	 */
	private DatastoreType datastoreType;
	/**
	 * @return the datastoreType
	 */
	public DatastoreType getDatastoreType() {
		return datastoreType;
	}

	/**
	 * @param datastoreType the datastoreType to set
	 */
	public void setDatastoreType(DatastoreType datastoreType) {
		this.datastoreType = datastoreType;
	}


	private DataStore datastore;
	private String encoding;
	
	/**
	 * Initiates the index location.
	 * Used when the data store is of type LuceneDataStore.
	 */
	private String indexlocation;
	/**
	 * @return the indexlocation
	 */
	public String getIndexlocation() {
		return indexlocation;
	}

	/**
	 * @param indexlocation the indexlocation to set
	 */
	public void setIndexlocation(String indexlocation) {
		this.indexlocation = indexlocation;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}


	private Set<String> annToPpreserve;
	/**
	 * this option is specified when we want to do a batch procesing
	 * directory specifies a set of file to be processed.
	 */
	
	private File directoryToProcess;
	/**
	 * @return the directoryToProcess
	 */
	public File getDirectoryToProcess() {
		return directoryToProcess;
	}

	/**
	 * @param directoryToProcess the directoryToProcess to set
	 */
	public void setDirectoryToProcess(File directoryToProcess) {
		this.directoryToProcess = directoryToProcess;
	}

	/**
	 * @return the annToPpreserve
	 */
	public Set<String> getAnnToPpreserve() {
		return annToPpreserve;
	}

	/**
	 * @param annToPpreserve the annToPpreserve to set
	 */
	public void setAnnToPpreserve(Set<String> annToPpreserve) {
		this.annToPpreserve = annToPpreserve;
	}

	/**
	 * @return the datastore
	 */
	public DataStore getDatastore() {
		return datastore;
	}

	/**
	 * @param datastore the datastore to set
	 */
	public void setDatastore(DataStore datastore) {
		this.datastore = datastore;
	}


	/**
	 * list of corpuses this unit of data storage has.
	 * Need to change it structure where we can a corpus class basedo n name or some
	 * feature
	 */
	
	private Set<Corpus> copusset;
	/**
	 * limits the size if each cropus in the corpus set
	 */
	private int sizeofcorpus;
	/**
	 * @return the sizeofcorpus
	 */
	public int getSizeofcorpus() {
		return sizeofcorpus;
	}

	/**
	 * @param sizeofcorpus the sizeofcorpus to set
	 */
	public void setSizeofcorpus(int sizeofcorpus) {
		this.sizeofcorpus = sizeofcorpus;
	}

	public DataStorage(DataStore datastore) {
		this.datastore =  datastore;
		this.sizeofcorpus = -1;
		this.copusset = new CopyOnWriteArraySet<Corpus>();
		this.directoryToProcess = null;
	}
	
	public DataStorage(DataStore datastore, int sizeofcorpus) {
		this.datastore =  datastore;
		this.sizeofcorpus = sizeofcorpus;
		this.copusset = new CopyOnWriteArraySet<Corpus>();
		this.directoryToProcess = null;
	}
	/**
	 * 
	 */
	public DataStorage() {
		// TODO Auto-generated constructor stub
		this.datastore = null;
		this.sizeofcorpus = -1;
		this.copusset = new CopyOnWriteArraySet<Corpus>();
		this.directoryToProcess = null;
	}
	
	public DataStorage(int corpussize) {
		// TODO Auto-generated constructor stub
		this.datastore = null;
		this.sizeofcorpus = corpussize;
		this.copusset = new CopyOnWriteArraySet<Corpus>();
		this.directoryToProcess = null;
	}


	public void addDataStore(DataStore ds) {
		this.datastore = ds;
		
	}
	
	/**
	 * @param directoryToProcess
	 * @return
	 */
	public static void getAllFiles(File directoryToProcess,List<File> filesToProcess) {
		// TODO Auto-generated method stub
		for (File file : directoryToProcess.listFiles()) {
			if (!file.isDirectory())
				filesToProcess.add(file);
			else
				getAllFiles(directoryToProcess, filesToProcess);
		}
	}
	public Set<Corpus> getCorpusSet() {
		if (this.datastore == null)
			return this.copusset;
		else {
			CopyOnWriteArraySet<Corpus> result = new CopyOnWriteArraySet<Corpus>();
			
			List ids=null;
			try {
				ids = this.datastore.getLrIds("gate.corpora.SerialCorpusImpl");
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (Object id : ids) {
				FeatureMap feat = Factory.newFeatureMap();
				feat.put(DataStore.LR_ID_FEATURE_NAME, id);
				feat.put(DataStore.DATASTORE_FEATURE_NAME, this.datastore);
				try {
					result.add((Corpus)Factory.createResource("gate.corpora.SerialCorpusImpl",feat));
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return result;
		}
	}
	
	public static void open(DataStore ds) {
		try {
			ds.open();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * close the data store
	 * TODO: add a parameter to sync the datastore before closing it.
	 * Also implement functionality to implement many corpuses. 
	 * @throws PersistenceException 
	 */
	public void close() throws PersistenceException {
		if (this.datastore == null) {
			for (Corpus cc : this.copusset) {
				CorpusClass cclass = (CorpusClass)cc;
				cclass.deleteAllDoc();
				Factory.deleteResource(cclass.getCorpus());
			}
			this.copusset = null;
		}
		else {
			this.datastore.close();
		}
	}
	/**
	 * Populate the data store with file contained in the directory
	 * @param directory
	 */
	public void populateDataStore(File directory) {
		List<File> allfiles = new Vector<File>();
		getAllFiles(directory,allfiles);
		SerialCorpusImpl corpus = null;
		Corpus tempcorpus = null;
		if (this.datastore == null)
			return;
		try {
			tempcorpus = Factory.newCorpus(this.datastoreType.toString());
			
		} catch (ResourceInstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			corpus = (SerialCorpusImpl) this.datastore.adopt(tempcorpus, null);
			Factory.deleteResource(tempcorpus);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		
		for (File file : allfiles) {
			Document doc = null;
			try {
				doc = Factory.newDocument(file.toURI().toURL(), this.getEncoding());
				doc.setName(file.getName());
			//	adoptedDoc = (Document)this.datastore.adopt(doc, null);
				
			} catch (ResourceInstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch ( MalformedURLException e) {
				e.printStackTrace();
			}
		//	} catch (PersistenceException e) {
				// TODO Auto-generated catch block
		//.printStackTrace();
		//	} catch (SecurityException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			corpus.add(doc);
			try {
				this.datastore.sync(corpus);
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Factory.deleteResource(doc);
		//	Factory.deleteResource(adoptedDoc);
			corpus.unloadDocument(doc,true);	
			//corpus.cleanup();
			//corpus.clear();
		}
		
		if (this.datastoreType.toString().equalsIgnoreCase("lucene")) {
			LuceneDataStoreImpl ds = (LuceneDataStoreImpl)this.datastore;
			Indexer indexer = null;
			try {
				indexer = new LuceneIndexer(new File(this.getIndexlocation()).toURI().toURL());
				Map parameters = new HashMap();
				parameters.put( Constants.INDEX_LOCATION_URL , new File(this.getIndexlocation()).toURI().toURL());
				parameters.put( Constants.BASE_TOKEN_ANNOTATION_TYPE , "Token");
				parameters.put( Constants.CREATE_TOKENS_AUTOMATICALLY ,new Boolean( true ));
				List < String > setsToInclude = new ArrayList <String >();
				setsToInclude.add("<null>");
				setsToInclude.add("Original markups");
				parameters.put( Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE , setsToInclude);
				parameters.put( Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE , new ArrayList<String>());
				parameters.put( Constants.FEATURES_TO_INCLUDE , new ArrayList <String >());
				parameters.put( Constants.FEATURES_TO_EXCLUDE , new ArrayList <String >());
				indexer.setCorpus(corpus);
				ds.setIndexer(indexer , parameters );
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (gate.creole.annic.IndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}
	private static SerialDataStore createserialDataStore(URL dsName) {
		SerialDataStore sds = null;
		try {
			sds = (SerialDataStore) Factory.createDataStore("gate.persist.SerialDataStore",dsName.toString());
		} catch (PersistenceException e1) {
			// TODO Auto-generated catch block
			System.out.println("Could not create new input seral data store, check if there is" +
					"alreasy existing one");
			try {
				sds = new SerialDataStore(dsName.toString());
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
			}
		} catch (UnsupportedOperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return sds;
	}
	private static LuceneDataStoreImpl createLuceneDataStore(URL dsName) {
		LuceneDataStoreImpl lds = null;
		 try {
			return (LuceneDataStoreImpl) Factory.createDataStore("gate.persist.LuceneDataStoreImpl", dsName.toString());
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not create new input Lucene data store, check if there is" +
					"alreasy existing one");
			lds = new LuceneDataStoreImpl();
			try {
				lds.setStorageUrl(dsName.toString());
			} catch (PersistenceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			//LuceneDataStoreImpl s = new LuceneDataStoreImpl();
			//s.setStorageUrl(dsName.toString());
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;	 
	}
	public static DataStore createDataStore(URL dsName, String dsType) {
		if (dsType.equalsIgnoreCase("serial")) {
			return createserialDataStore(dsName);
		}
		else if (dsType.equalsIgnoreCase("lucene")){
			return createLuceneDataStore(dsName);
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(CorpusClass cc) {
		
		// TODO Auto-generated method stub
		if (this.sizeofcorpus == -1)
				this.copusset.add( cc);
		else {
			int count = 0;
			ListIterator<Document> docIterator = cc.listIterator();
			CorpusClass temp = new CorpusClass(cc.getDatastorage(),cc.getEncoding(),Gate.genSym(),cc.getAnnotationSet(),cc.getDirectoryToDump());
			temp.createCorpus();
			while (docIterator.hasNext()) {
				DocumentImpl docI = (DocumentImpl) docIterator.next();
				if (count >= this.sizeofcorpus) {
					this.copusset.add(temp);
					temp = new CorpusClass(cc.getDatastorage(),cc.getEncoding(),Gate.genSym(),cc.getAnnotationSet(),cc.getDirectoryToDump());
					temp.createCorpus();
					count = 0;
				}
				temp.add(docI);
				count++;			
			}
		}
		
		if (this.datastore !=null ) {
			for (Corpus cclass : this.copusset) {
				try {
					Corpus persistCorpus = (Corpus) this.datastore.adopt(((CorpusClass)cclass).getCorpus(), null);
					this.datastore.sync(persistCorpus);
				} catch (PersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cc.deleteAllDoc();
			}
			for (Corpus cclass : this.copusset) {
				Factory.deleteResource(((CorpusClass)cclass).getCorpus());
			}
			this.copusset = null;
		}
		
		return true;
	}

	


	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		if (this.datastore == null) {
			this.copusset.clear();
		}
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(CorpusClass cc) {
		// TODO Auto-generated method stub
		if (this.datastore == null) {
			return this.copusset.contains(cc);
		}
		else {
			try {
				if (this.datastore.getLrName((LanguageResource)cc.getLRPersistenceId()) != null)
					return true;
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
	}



	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		if (this.datastore == null)
			return this.copusset.isEmpty();
		return false;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		if (this.datastore == null) {
			return this.copusset.iterator();
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 * 
	 */
	@Override
	public boolean remove(Object cc) {
		// TODO Auto-generated method stub
		if (this.datastore == null) {
			this.copusset.remove(cc);
			return true;
		}
		return false;
	}


	


	

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}


	


	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends CorpusClass> corpusset) {
		// TODO Auto-generated method stub
		if (this.datastore == null) {
			this.copusset.addAll(corpusset);
			return true;
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}


	




	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
}
