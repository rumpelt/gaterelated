/**
 * 
 */
package datastore;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import gate.Factory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.FeatureMap;
import gate.LanguageResource;
import gate.Resource;
import gate.corpora.DocType;
import gate.corpora.DocumentImpl;
import gate.creole.ResourceInstantiationException;
import gate.event.CorpusListener;
import gate.persist.PersistenceException;
import gate.security.SecurityException;

/**
 * @author ashwani TODO : automatic synching to datastore.
 * 
 */
public class CorpusClass implements Corpus {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7425718821625427433L;
	private String encoding;

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding
	 *            the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	private File directoryToDump;

	/**
	 * @return the directoryToDump
	 */
	public File getDirectoryToDump() {
		return directoryToDump;
	}

	/**
	 * @param directoryToDump
	 *            the directoryToDump to set
	 */
	public void setDirectoryToDump(File directoryToDump) {
		this.directoryToDump = directoryToDump;
	}

	private Corpus corpus;
	private String corpusname;
	private DataStorage datastorage;
	private String prefix;
    public void  deleteDoc(Document doc) {
    	Factory.deleteResource(doc);
    	this.corpus.remove(doc);
    }
    public void  deleteAllDoc() {
    	int count = 0;
    	while(count < this.corpus.size()) {
    		
    		((Resource)this.corpus.get(count)).cleanup();
    		DocumentImpl doc = (DocumentImpl) this.corpus.get(count);
    		
    		Factory.deleteResource(doc);
    		doc.setContent(null);
    		doc.setStringContent(null);
    		count++;
    	}   
    	this.corpus.clear();
    }
	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix
	 *            the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * the annotation set to finally keep. This is in addition to the original
	 * annotation set in the document.
	 */
	private Set<String> annotationSet;

	/**
	 * @return the annotationSet
	 */
	public Set<String> getAnnotationSet() {
		return annotationSet;
	}

	/**
	 * @param annotationSet
	 *            the annotationSet to set
	 */
	public void setAnnotationSet(Set<String> annotationSet) {
		this.annotationSet = annotationSet;
	}

	/**
	 * Returns the data storage (A wrapper around DataStore to provide auto
	 * sunching) associate with this
	 * 
	 * @return the datastorage
	 */
	public DataStorage getDatastorage() {
		return datastorage;
	}

	public CorpusClass(DataStorage datastore, String encoding, String corpusname) {
		this.datastorage = datastore;
		this.encoding = encoding;
		this.corpusname = corpusname;
	}

	public CorpusClass(DataStorage datastore, String encoding,
			String corpusname, Set<String> annSet) {
		this.datastorage = datastore;
		this.encoding = encoding;
		this.corpusname = corpusname;
		this.annotationSet = annSet;
	}

	public CorpusClass(DataStorage datastore, String encoding,
			String corpusname, Set<String> annSet, File dirToDump) {
		this.datastorage = datastore;
		this.encoding = encoding;
		this.corpusname = corpusname;
		this.annotationSet = annSet;
		this.directoryToDump = dirToDump;
	}

	

	public CorpusClass(String encoding, String corpusname) {
		this.datastorage = new DataStorage();
		this.encoding = encoding;
		this.corpusname = corpusname;
	}

	public void populatecorpus(URL url, String rootelement, int numdocs,
			String filenameprefix, DocType doctype) {

		if (rootelement != null) {
			if (this.corpus == null)
				try {
					this.corpus = Factory.newCorpus(this.corpusname);
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			try {
				this.corpus.populate(url, rootelement, this.encoding, numdocs,
						filenameprefix, doctype);

			} catch (ResourceInstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (this.corpus == null)
				try {
					this.corpus = Factory.newCorpus(this.corpusname);
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			try {
				this.corpus.populate(url, null, this.encoding, true);
			} catch (ResourceInstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @param corpus
	 *            the corpus to set
	 */
	public void setCorpus(Corpus corpus) {

		this.corpus = corpus;
	}

	public Corpus createCorpus() {
		if (this.corpus == null)
			try {
				this.corpus = Factory.newCorpus(this.corpusname);
			} catch (ResourceInstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return this.corpus;
	}
   
	public static void dumpdocumentondisk(Document doc, String dirName, Set<Annotation> aSourceAnnotationSet) {
		File dir = null;
		dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdir();
		} else if (!dir.isDirectory()) {
			System.out.println("Specified path is not a directory");
		}
		try {

			File tocreate = new File(dir.getAbsolutePath() + "/"
					+ doc.getName());
			if (!tocreate.createNewFile())
				return;
			FileOutputStream fos = new FileOutputStream(tocreate);
			OutputStreamWriter out = new OutputStreamWriter(fos);
			out.write(doc.toXml(aSourceAnnotationSet));
			out.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static long dumpondisk(Corpus corpus, String dirName, Set aSourceAnnotationSet) {
		File dir = null;
		dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdir();
		} else if (!dir.isDirectory()) {
			System.out.println("Specified path is not a directory");
			return -1;
		}
		Iterator<Document> doclist = corpus.iterator();
		long count = 0;
		while (doclist.hasNext()) {
			Document doc = doclist.next();
			try {

				File tocreate = new File(dir.getAbsolutePath() + "/"
						+ doc.getName());
				if (!tocreate.createNewFile())
					continue;
				FileOutputStream fos = new FileOutputStream(tocreate);
				OutputStreamWriter out = new OutputStreamWriter(fos);
				if (aSourceAnnotationSet == null)
					out.write(doc.toXml());
				else {

					Set<Annotation> tokeep = new ConcurrentSkipListSet<Annotation>();
					Iterator namesToKeep = aSourceAnnotationSet.iterator();
					while (namesToKeep.hasNext()) {

						String name = (String) namesToKeep.next();
						Set<Annotation> allannotation = doc.getAnnotations();
						for (Annotation a : allannotation) {

							if (a.getType().equalsIgnoreCase(name)) {
								tokeep.add(a);
							}
						}
					}
					out.write(doc.toXml(tokeep));
				}

				out.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			count++;
		}
		return count;

	}
   	/**
	 * Writes the documents in this corpus to the physical disk. TODO: Handle
	 * the case when Set aSourceAnnotationSet is not null
	 * 
	 * @param dirName
	 * @return: The number of file written to disk else returns -1
	 */
	public long dumptodisk(String dirName, Set aSourceAnnotationSet) {
		File dir = null;
		if (dirName == null)
			dir = this.directoryToDump;
		else
			dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdir();
		} else if (!dir.isDirectory()) {
			System.out.println("Specified path is not a directory");
			return -1;
		}
		if (aSourceAnnotationSet == null)
			aSourceAnnotationSet = this.annotationSet;
		Iterator<Document> doclist = this.corpus.iterator();
		long count = 0;
		while (doclist.hasNext()) {
			Document doc = doclist.next();
			try {

				File tocreate = new File(dir.getAbsolutePath() + "/"
						+ doc.getName());
				if (!tocreate.createNewFile())
					continue;
				FileOutputStream fos = new FileOutputStream(tocreate);
				OutputStreamWriter out = new OutputStreamWriter(fos,
						this.encoding);
				if (aSourceAnnotationSet == null)
					out.write(doc.toXml());
				else {

					Set<Annotation> tokeep = new ConcurrentSkipListSet<Annotation>();
					Iterator namesToKeep = aSourceAnnotationSet.iterator();
					while (namesToKeep.hasNext()) {

						String name = (String) namesToKeep.next();
						Set<Annotation> allannotation = doc.getAnnotations();
						for (Annotation a : allannotation) {

							if (a.getType().equalsIgnoreCase(name)) {
								tokeep.add(a);
							}
						}
					}
					out.write(doc.toXml(tokeep));
				}

				out.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			count++;
		}
		return count;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.SimpleCorpus#getDocumentName(int)
	 */
	@Override
	public String getDocumentName(int arg0) {
		// TODO Auto-generated method stub
		return this.corpus.getDocumentName(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.SimpleCorpus#getDocumentNames()
	 */
	@Override
	public List<String> getDocumentNames() {
		// TODO Auto-generated method stub
		return this.corpus.getDocumentNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.SimpleCorpus#populate(java.net.URL, java.io.FileFilter,
	 * java.lang.String, boolean)
	 */
	@Override
	public void populate(URL arg0, FileFilter arg1, String arg2, boolean arg3)
			throws IOException, ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.corpus.populate(arg0, arg1, arg2, arg3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.SimpleCorpus#populate(java.net.URL, java.io.FileFilter,
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void populate(URL arg0, FileFilter arg1, String arg2, String arg3,
			boolean arg4) throws IOException, ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.corpus.populate(arg0, arg1, arg2, arg3, arg4);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.SimpleCorpus#populate(java.net.URL, java.lang.String,
	 * java.lang.String, int, java.lang.String, gate.corpora.DocType)
	 */
	@Override
	public long populate(URL arg0, String arg1, String arg2, int arg3,
			String arg4, DocType arg5) throws IOException,
			ResourceInstantiationException {
		// TODO Auto-generated method stub
		return this.corpus.populate(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#getDataStore()
	 */
	@Override
	public DataStore getDataStore() {
		// TODO Auto-generated method stub
		return this.corpus.getDataStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#getLRPersistenceId()
	 */
	@Override
	public Object getLRPersistenceId() {
		// TODO Auto-generated method stub
		return this.corpus.getLRPersistenceId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#getParent()
	 */
	@Override
	public LanguageResource getParent() throws PersistenceException,
			SecurityException {
		// TODO Auto-generated method stub
		return this.corpus.getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#isModified()
	 */
	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return this.corpus.isModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#setDataStore(gate.DataStore)
	 */
	@Override
	public void setDataStore(DataStore arg0) throws PersistenceException {
		// TODO Auto-generated method stub
		this.corpus.setDataStore(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#setLRPersistenceId(java.lang.Object)
	 */
	@Override
	public void setLRPersistenceId(Object arg0) {
		// TODO Auto-generated method stub
		this.corpus.setLRPersistenceId(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#setParent(gate.LanguageResource)
	 */
	@Override
	public void setParent(LanguageResource arg0) throws PersistenceException,
			SecurityException {
		// TODO Auto-generated method stub
		this.setParent(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.LanguageResource#sync()
	 */
	@Override
	public void sync() throws PersistenceException, SecurityException {
		// TODO Auto-generated method stub
		this.corpus.sync();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Resource#cleanup()
	 */
	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		this.corpus.cleanup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Resource#getParameterValue(java.lang.String)
	 */
	@Override
	public Object getParameterValue(String arg0)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		return this.corpus.getParameterValue(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Resource#init()
	 */
	@Override
	public Resource init() throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		return this.corpus.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Resource#setParameterValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setParameterValue(String arg0, Object arg1)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.setParameterValue(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Resource#setParameterValues(gate.FeatureMap)
	 */
	@Override
	public void setParameterValues(FeatureMap arg0)
			throws ResourceInstantiationException {
		// TODO Auto-generated method stub
		this.corpus.setParameterValues(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.util.FeatureBearer#getFeatures()
	 */
	@Override
	public FeatureMap getFeatures() {
		// TODO Auto-generated method stub
		return this.corpus.getFeatures();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.util.FeatureBearer#setFeatures(gate.FeatureMap)
	 */
	@Override
	public void setFeatures(FeatureMap arg0) {
		// TODO Auto-generated method stub
		this.corpus.setFeatures(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.util.NameBearer#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.corpus.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.util.NameBearer#setName(java.lang.String)
	 */
	@Override
	public void setName(String arg0) {
		// TODO Auto-generated method stub
		this.corpus.setName(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean add(Object arg0) {
		// TODO Auto-generated method stub
		return this.corpus.add(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int arg0, Object arg1) {
		// TODO Auto-generated method stub
		this.corpus.add(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection arg0) {
		// TODO Auto-generated method stub
		return this.corpus.addAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int arg0, Collection arg1) {
		// TODO Auto-generated method stub
		return this.corpus.addAll(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		this.corpus.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return this.corpus.contains(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection arg0) {
		// TODO Auto-generated method stub
		return this.corpus.containsAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#get(int)
	 */
	@Override
	public Object get(int arg0) {
		// TODO Auto-generated method stub
		return this.corpus.get(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object arg0) {
		// TODO Auto-generated method stub
		return this.corpus.indexOf(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return this.corpus.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return this.corpus.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object arg0) {
		// TODO Auto-generated method stub
		return this.corpus.lastIndexOf(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator listIterator() {
		// TODO Auto-generated method stub
		return this.corpus.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator listIterator(int arg0) {
		// TODO Auto-generated method stub
		return this.corpus.listIterator(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return this.corpus.remove(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(int)
	 */
	@Override
	public Object remove(int arg0) {
		// TODO Auto-generated method stub
		return this.corpus.remove(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection arg0) {
		// TODO Auto-generated method stub
		return this.corpus.removeAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection arg0) {
		// TODO Auto-generated method stub
		return this.corpus.retainAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public Object set(int arg0, Object arg1) {
		// TODO Auto-generated method stub
		return this.corpus.set(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#size()
	 */
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return this.corpus.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List subList(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return this.corpus.subList(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray()
	 */
	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return this.corpus.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray(T[])
	 */
	@Override
	public Object[] toArray(Object[] arg0) {
		// TODO Auto-generated method stub
		return this.corpus.toArray(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Corpus#addCorpusListener(gate.event.CorpusListener)
	 */
	@Override
	public void addCorpusListener(CorpusListener arg0) {
		// TODO Auto-generated method stub
		this.corpus.addCorpusListener(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Corpus#isDocumentLoaded(int)
	 */
	@Override
	public boolean isDocumentLoaded(int arg0) {
		// TODO Auto-generated method stub
		return this.corpus.isDocumentLoaded(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Corpus#removeCorpusListener(gate.event.CorpusListener)
	 */
	@Override
	public void removeCorpusListener(CorpusListener arg0) {
		// TODO Auto-generated method stub
		this.corpus.removeCorpusListener(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gate.Corpus#unloadDocument(gate.Document)
	 */
	@Override
	public void unloadDocument(Document arg0) {
		// TODO Auto-generated method stub
		this.corpus.unloadDocument(arg0);
	}
}
