/**
 * 
 */
package gate;


import gate.corpora.DocType;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import process.ControllerType;
import process.ProcessController;
import process.ProcessResourceImpl;

import datastore.CorpusClass;
import datastore.DataStorage;
import datastore.DatastoreType;


/**
 * @author ashwani
 *
 */
public class MainEntry {

	/**
	 * @param args
	 */
	static boolean syncds = false;
	static String gatehome = "/home/ashwani/CS/GATE-6.1";
	static String pluginshome = "/home/ashwani/CS/GATE-6.1/plugins";
	/**
	 * resource directories to be loaded by the gate.
	 * Must be fully qualified names.
	 * TODO: implement the command line argument to add resource Directories.
	 */
	static Vector<String> resourceDirs = new Vector<String>();
	
	static DataStorage datastorage = null;
	static ProcessController controller = null;
	static int corpussize = -1;
	static String encoding = "utf-8";
	static URL inputDataStore  = null;
	static DataStore ds = null;
	static File corpusfile = null;
	static String filenameprefix = null;
	static String corpusfiletype = null;
	static String corpusrootelement = null;
	static Set<String> annotationSet = null;
	static String dumptodisk = null;
	static Options options = new Options();
	public static void main(String[] args) throws ResourceInstantiationException {
		// TODO Auto-generated met hod stub3
	
       addoptions(MainEntry.options);
       datastorage = new DataStorage(corpussize);
       parsearguments(args);
       datastorage.setEncoding(encoding);
       resourceDirs.add(Gate.getPluginsHome()+"/"+ANNIEConstants.PLUGIN_DIR);
	   try {
		Gate.init();
	   } catch (GateException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
	   
	   for (String dirs : resourceDirs) {
		   try {
			Gate.getCreoleRegister().registerDirectories(new File(dirs).toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   if (inputDataStore != null) {
		   ds = DataStorage.createDataStore(inputDataStore, datastorage.getDatastoreType().toString());
		   DataStorage.open(ds);
		   datastorage.setDatastore(ds);
		   if (annotationSet != null)
			   datastorage.setAnnToPpreserve(annotationSet);
	   }
	   
	   if (corpusfile != null) {
		   if(!corpusfile.isDirectory()) {
		   CorpusClass cc = null;
		   if (dumptodisk != null)
			   cc = new CorpusClass(datastorage, datastorage.getEncoding(), corpusfile.getName(), annotationSet, new File(dumptodisk));
		   else
			   cc = new CorpusClass(datastorage, datastorage.getEncoding(), corpusfile.getName(), annotationSet);
		   try {
			   cc.populatecorpus(corpusfile.toURI().toURL(), corpusrootelement,  
					   -1, filenameprefix, DocType.valueOf(corpusfiletype.toUpperCase()));
		   	} catch (MalformedURLException e) {
		   		// TODO Auto-generated catch block
		   		e.printStackTrace();
		   	}
		    datastorage.add(cc);
		    cc.deleteAllDoc();
		    Factory.deleteResource(cc.getCorpus());
		    cc.setCorpus(null);
		    cc = null;
		   }
		   else {
			   datastorage.populateDataStore(corpusfile);

		   }
	   	}
	   
	   if (controller != null) {
		   if (dumptodisk !=null)
			   controller.setDirectoryToDump(dumptodisk);
		   controller.execute();
	   }
	   if (syncds && datastorage.getDatastore() != null) {
		   

	   }
	   try {
		datastorage.close();
	} catch (PersistenceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       
	}
	
	/**
	 * TODO: doctype should be a list of document we are working on.
	 * @param options
	 */
	public static void addoptions(Options options) {
		options.addOption("ids", "inputdatastore", true, "The input Serial Data Store");
		options.addOption("cds", "createdatastore", true, "The DataStore to be created");
		options.addOption("dstype", "datastoretype", true, "The type of data store");
		options.addOption("gatehome", "gatehome", true, "The root directory of gate installation");
		options.addOption("plugins", "pluginshome", true, "The directory where the gate plugin for this run will be found");
		options.addOption("dd", "dumptodisk", true, "dumps a corpus to the disk");
		options.addOption("cf", "corpusfile", true, "A file/Directory containing various docs");
		options.addOption("cftype", "corpusfiletype", true, "type of documents which we might expect in the corpus");
		options.addOption("rootelement", "rootelement", true, "the root element of the documnent");
		options.addOption("encoding", "encoding", true, "the encoding to be used for current run");
		options.addOption("processes", "processes", true, "the gate processing resources to be created");
		options.addOption("pparams","processparams",true, "the parameters of process which have already been created " +
				"The value should be in format -<processname> <parameter>:<value> -<processname> <parameter>:<value>");
		options.addOption("controllertype", "controllertype", true, "the controllertype we want to want for this run");
		options.addOption("annset", "annotationset", true, "names of the annotation which we will try to preserve. Use to discar other annotation \n" +
				"generated by intermediate process.");
		options.addOption("filenameprefix", "filenameprefix", true, "The prefix to be added before the filename");
		options.addOption("corpussize", "corpusszie", true, "The maximum number of documents which each corpus can hold in \n" +
				"the datastorage");
		options.addOption("syncds", "syncdatastore", false, " if the data store should the synced with the inmemory version of the language resourses");
		options.addOption("dir", "directory", true, " Option to specify a directoy whose files we want to process (used for batch Processing)");
		options.addOption("luceneindex", "luceneindex", true, " location of the lucene index directory");
		
	}
	/**
	 * ALso initiate the Gate
	 * @param commandargs
	 * @throws ResourceInstantiationException 
	 */
    public static void parsearguments(String[] commandargs) throws ResourceInstantiationException {
    	CommandLineParser cmdLineGnuParser = new GnuParser();
		try {
			CommandLine command = cmdLineGnuParser.parse(options, commandargs);
			if (command.hasOption("gatehome")) {
				gatehome = command.getOptionValue("gatehome");
				
			}
			if (command.hasOption("dir")) {
				String dir = command.getOptionValue("dir");
				datastorage.setDirectoryToProcess(new File(dir));
				
			}
			if (command.hasOption("luceneindex")) {
				datastorage.setIndexlocation(command.getOptionValue("luceneindex"));
			}
			Gate.setGateHome(new File(gatehome));
			try {
				Gate.init();
			} catch (GateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (command.hasOption("plugins")) {
				pluginshome = command.getOptionValue("plugins");
				Gate.setPluginsHome(new File(pluginshome));
			}
			if (command.hasOption("ids")) {
				try {
					inputDataStore = new File(command.getOptionValue("ids")).toURI().toURL();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			if (command.hasOption("corpussize")) {
				corpussize = Integer.parseInt(command.getOptionValue("corpussize"));
				if (datastorage != null) {
					datastorage.setSizeofcorpus(corpussize);
				}
			}
			if (command.hasOption("cds")) {
				try {
					inputDataStore =new File(command.getOptionValue("cds")).toURI().toURL();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (command.hasOption("dstype")) {
				datastorage.setDatastoreType(DatastoreType.valueOf(command.getOptionValue("dstype").toUpperCase())) ;
			}			
			
			if (command.hasOption("annset")) {
				annotationSet = new ConcurrentSkipListSet<String>();
				StringTokenizer st = new StringTokenizer( command.getOptionValue("annset"));	
				while(st.hasMoreTokens()) {
					annotationSet.add(st.nextToken());
				}
			}
			if (command.hasOption("filenameprefix")) {
				filenameprefix = command.getOptionValue("filenameprefix");
			}
			if (command.hasOption("cf")) {
				corpusfile = new File(command.getOptionValue("cf"));
			}
			if (command.hasOption("cftype")) {
				corpusfiletype = new String(command.getOptionValue("cftype"));
			}
			if (command.hasOption("rootelement")) {
				corpusrootelement = command.getOptionValue("rootelement");
			}
			if (command.hasOption("encoding")) {
				encoding = command.getOptionValue("encoding");
				datastorage.setEncoding(encoding);
			}
			if (command.hasOption("dd")) {
				dumptodisk = command.getOptionValue("dd");
			}
	
			/*
			 * controller type option should preferably be processesed before the processess and 
			 * pparams and pfeatures option
			 */
			if (command.hasOption("controllertype")) {
				ControllerType controllertype = ControllerType.valueOf(command.getOptionValue("controllertype"));
				if (controller != null) {
					ProcessController pr = new ProcessController(controllertype, datastorage);
					pr.addAllProcesses(controller.getprocesses());
					controller = pr;
					
				   }
				 else 
				     controller = new ProcessController(controllertype, datastorage);
				controller.initController();
			}
			/*
			 * controller type option should preferably be processesed before the processess and 
			 * pparams and pfeatures option
			 */
			if (command.hasOption("processes")) {
				addProcessesToController(command.getOptionValue("processes"));
			}
			/*
			 * controller type option should preferably be processesed before the processess and 
			 * pparams and pfeatures option
			 */
			if (command.hasOption("pparams")) {
				String processparameters = command.getOptionValue("pparams");
				addprocessparam(processparameters);
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
    
    /**
     * space separated list of process to be created and added to controller
     * @param listofprocesses
     * @throws ResourceInstantiationException 
     */
    public static void addProcessesToController(String listofprocesses) throws ResourceInstantiationException {
    	if (controller == null) {
    		controller = new ProcessController(datastorage);
    		controller.initController();
    	}
    	StringTokenizer st = new StringTokenizer(listofprocesses);	
    	while(st.hasMoreTokens()) {
    		controller.addProcessingResource(new ProcessResourceImpl(st.nextToken()));
    	}
    }
    
    /**
     * Add features to the ProcessingResource
     * TODO : while adding the data features we do not consider data type 
     * of the value of feature. Might be required in future to add cast to
     * specific data type
     * @param features : this argument has specific format type as in followng.
     *  -<processname> <feature>:<value> -<feature> <parameter>:<value>
     */
    public static void addfeatures(String features) {
    	StringTokenizer st = new StringTokenizer(features);
    	String processName = null;

    	FeatureMap fm = null;
    	while(st.hasMoreTokens()) {
    		String s = st.nextToken();
    		if (s.startsWith("-")) {
    			processName = s.substring(1);
    			fm  = controller.returnProcessingResourse(processName).getFeatures();
    		}
    		else {
    			String[] vals = s.split(":");
    			fm.put(vals[0], vals[1]);
    		}
    	}
    }
    /**
     * To add the parameters to a ProcessingResource.
     * TODO : while adding the data features we do not consider data type 
     * of the value of parameters. Might be required in future to add cast to
     * specific data type
     * the first
     * @param params : this aragument follows a particular format type. as in the following
     *  -<processname> <parameter>:<value> -<processname> <parameter>:<value>
     * 
     */
    public static void addprocessparam(String params) {
    	StringTokenizer st = new StringTokenizer(params);
    	String processName = null;
    	ProcessingResource pr = null;
    	while(st.hasMoreTokens()) {
    		String s = st.nextToken();
    		if (s.startsWith("-")) {
    			processName = s.substring(1);
    			pr = controller.returnProcessingResourse(processName);
    		}
    		else {
    			String[] vals = s.split(":",2);
    			try {
    				pr.setParameterValue("fieldtype", "Feeding :");
					pr.setParameterValue(vals[0], vals[1]);
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		
    	}
    	
    }
    
}

