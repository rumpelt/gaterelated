/**
 * 
 */
package process;

import gate.Controller;
import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.FeatureMap;
import gate.LanguageAnalyser;
import gate.LanguageResource;
import gate.ProcessingResource;
import gate.Factory;
import gate.corpora.DocumentImpl;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ConditionalSerialController;
import gate.creole.ExecutionException;
import gate.creole.RealtimeCorpusController;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.SerialController;
import gate.persist.PersistenceException;
import gate.security.SecurityException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import datastore.CorpusClass;
import datastore.DataStorage;


/**
 * @author ashwani
 * Executes a set of processing resource in a particular order based on the
 *  type of controller controller.
 */
public class ProcessController {
	List<ProcessResourceImpl> processes;
	Controller controller;
	String directoryToDump;
	
	/**
	 * @return the directoryToDump
	 */
	public String getDirectoryToDump() {
		return directoryToDump;
	}
	/**
	 * @param directoryToDump the directoryToDump to set
	 */
	public void setDirectoryToDump(String directoryToDump) {
		this.directoryToDump = directoryToDump;
	}

	final DataStorage datastorage;
	/**
	 * There are various type of controllers.
	 * Look at the java doc of gate api.
	 * By Default this is set to SerialAnalyserController
	 */
	final ControllerType ctype;
	
	public ProcessController () {
		this.datastorage = null;
		this.ctype = ControllerType.serialAnalyserController;
	}
	public ProcessController(DataStorage datastorage) {
		this.processes = new Vector<ProcessResourceImpl>(); 
		this.ctype = ControllerType.serialAnalyserController;
		this.datastorage = datastorage;
	}
	
	public ProcessController(ControllerType ctype, DataStorage datastorage) {
		this.processes = new Vector<ProcessResourceImpl>();
		this.ctype = ctype;
		this.datastorage = datastorage;
	}
	
	public ProcessingResource returnProcessingResourse(String processName) {
		for (ProcessingResource pr : this.processes) {
			if (pr.getName().equalsIgnoreCase(processName))
				return pr;
		}
		return null;
	}
	
	public List<ProcessResourceImpl> getprocesses() {
		return this.processes;
	}
	public void addPrsToController(List<ProcessResourceImpl> prs) {
		for (ProcessResourceImpl pr: prs ) {
				pr.setResource(null);
				try {
					pr.init();
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		this.controller.setPRs(prs);
	}
	
	/**
	 * execute the processing resources specified in list sequentially processes
	 * @param corpus	
	 */
	public void executePRSequentially(Corpus corpus) {
		for (ProcessingResource pr : this.processes) {
			try {
				try {
					pr.setParameterValue("corpus", corpus);
					for (Object doc : corpus) 
						pr.setParameterValue("document", doc); // some default prs require this param
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pr.execute();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (this.directoryToDump != null) // if this option is specified then we need to dump also. Beware the pr it self also not dump.
			CorpusClass.dumpondisk(corpus, 
					this.directoryToDump,  
					this.datastorage.getAnnToPpreserve());
	}
	
	/**
	 * Implements batch processing. Go over each file in the directory and executes the 
	 * procesing resources.
	 * Parameter copusesize: if we want to bunch some documents together. If this is -1 then 
	 * each corpus can have maximum one document in it.
	 * @param corpussize
	 */
	public void doBatchProcessing(int corpussize) {
		Corpus corpus = null;
		try {
			corpus = Factory.newCorpus("BatchProcessApp Corpus");
		} catch (ResourceInstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (this.controller instanceof LanguageAnalyser)
			((CorpusController)this.controller).setCorpus(corpus);
		
		List<File> filesToProcess = new Vector<File>();
		if (this.datastorage.getDirectoryToProcess() != null)
			DataStorage.getAllFiles(this.datastorage.getDirectoryToProcess(), filesToProcess);
		else {
			this.executePRSequentially(corpus);
		}
		int doccount = 0;
		for (File file : filesToProcess) {
			Document doc = null;
			if (doccount == corpussize) {
				this.executePRSequentially(corpus);
				try {
					for (Object obj : corpus) {
						DocumentImpl di = (DocumentImpl)obj;
						Factory.deleteResource(di);
					}
					Factory.deleteResource(corpus);
					
					corpus.clear();
					corpus = Factory.newCorpus("BatchProcessApp Corpus");
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				doccount = 0;
			}
			try {
				doc = Factory.newDocument(file.toURI().toURL(), this.datastorage.getEncoding());
			} catch (ResourceInstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			corpus.add(doc);
			if (corpussize == -1) {
				this.executePRSequentially(corpus);
				Factory.deleteResource(doc);
				corpus.clear();
			}
			
			doccount++;
		}
		if (corpus.size() > 0) {
			this.executePRSequentially(corpus);
			for (Object obj : corpus) {
				DocumentImpl di = (DocumentImpl)obj;
				Factory.deleteResource(di);
			}
			Factory.deleteResource(corpus);
		}
		for (ProcessingResource pr : this.processes) {
			pr.cleanup();
		}
	}
	
	/**
	 * Give a list of processing resource add this to list of processes of this
	 * controller
	 * @param prs
	 * @return
	 */
	public boolean addAllProcesses(List<ProcessResourceImpl> prs) {
		return this.processes.addAll(prs);
	}
	public void execute() throws ResourceInstantiationException {
		if (this.controller.getPRs().size() == 0) {
			List<ProcessingResource> pr = new Vector<ProcessingResource>();
			for(ProcessResourceImpl pi : this.processes) {
				pi.init();
				pr.add(pi.getResource());
			}
			this.controller.setPRs(pr);
		}
		try {
			Set<Corpus> cset = this.datastorage.getCorpusSet();
			if (cset == null || cset.size() <= 0) {
				doBatchProcessing(this.datastorage.getSizeofcorpus());
			}
			for (Corpus corpus: cset) {
				
				if (this.controller instanceof LanguageAnalyser)
					((CorpusController)this.controller).setCorpus(corpus);
				this.controller.execute();		
				CorpusClass.dumpondisk(corpus, this.getDirectoryToDump(),this.datastorage.getAnnToPpreserve());
				for (Object doc : corpus) {
					DocumentImpl di = (DocumentImpl)doc;
					Factory.deleteResource(di);
					corpus.unloadDocument(di);
				
				}
				Factory.deleteResource(corpus);				
			}
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			System.out.println("The contoller executiion failed");
			e.printStackTrace();
		}
			
	}
	public void initController() throws ResourceInstantiationException {
		FeatureMap features = Factory.newFeatureMap();
		FeatureMap params = Factory.newFeatureMap();
		switch (this.ctype) {
		case conditionalSerialAnalyserController:
			this.controller = (ConditionalSerialAnalyserController)Factory.createResource("gate.creole.ConditionalSerialAnalyserController", features, params);
			break;
		case conditionalSerialController:
			this.controller  = (ConditionalSerialController)Factory.createResource("gate.creole.ConditionalSerialController", features, params);
			break;
		case realTimeCorpusController:
			this.controller = (RealtimeCorpusController)Factory.createResource("gate.creole.RealTimeCorpusController", features, params);
			
			break;
		case serialAnalyserController:
			this.controller = (SerialAnalyserController)Factory.createResource("gate.creole.SerialAnalyserController", features, params);
			break;
		case serialController:
			this.controller = (SerialController)Factory.createResource("gate.creole.SerialController", features, params);
			break;
			default:
				throw new ResourceInstantiationException("Cannot create the desired controller as wrong controller type specified");
		}
	}
	
	public boolean addProcessingResource(ProcessResourceImpl pr) {
		return this.processes.add(pr);
	}

	
}
