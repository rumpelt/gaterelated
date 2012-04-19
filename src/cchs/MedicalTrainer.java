/**
 * 
 */
package cchs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.lucene.util.Version;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Distribution;
import edu.stanford.nlp.stats.IntCounter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import stats.KLDivergence;
import tokenizers.LucenePTBTokenizer;
import tokenizers.NgramTokenizer;
import tokenizers.StopWordList;
import weka.WekaInstances;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.ClassifierType;
import weka.classifiers.CommonClassifierRoutines;
import weka.classifiers.Evaluation;
import weka.classifiers.J48Classifier;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * a set of very specific routines for my work on medical data.
 * @author ashwani
 *
 */
public class MedicalTrainer {
	private static Options options = new Options();
	
	private static void addOptions() {
		
		options.addOption("removesinglecount", "removesinglecount", false, "Remove terms with" +
				"single count in the termspace");
		options.addOption("eval", "eval", false, "Are we doing just evaluatin? Using the weka" +
				"api Evalatuation, by default it  is set to false");
		options.addOption("dumparff", "dumparff", true, "dump the training set arff, this is" +
				"for debugging purpose");
		options.addOption("predict", "predict", false, "run the classifer built to predict and dump");
		options.addOption("cltype", "classifierType", true, "the type of weka classifier to use");
		options.addOption("ifile", "inputfile", true, "the csv file to  be oprated on");
		options.addOption("dfile", "dumpfile", true, "the csv file to which we will dump the output" +
				"of the prediction");
		options.addOption("st", "stem", true, "do we want to stem. defaults to true");
		options.addOption("lc", "lowercase", true, "do we want to lowercase, defualts to" +
				"true");
		options.addOption("stopword", "stopword", true, "do we want to use the stop words, " +
		"defaults to StopWordList.medicalWords");
		options.addOption("tcol", "textcol", true, "text column number");
		options.addOption("lcol", "labelcol", true, "label col number");
		options.addOption("agecol", "agecol", true, "age column number");
		options.addOption("lage", "lowage", true, "the low age value");
		options.addOption("uage", "upage", true, "the up age value");
		options.addOption("idcol", "idcol", true, "identifier column number");
		options.addOption("idname", "idname", true, "nameOfIdentifier");
		options.addOption("lclass", "labelclass", true, "List of Labels of the class, " +
				"currently defaults to FeedCategories");
		options.addOption("ngrams", "ngrams", true, "the ngrams to operate upon, the listofngrams" +
				"is separated by semicolon :");
		options.addOption("rmsinglecount","removesinglecount", true, "remove singlecount terms");
		options.addOption("dumpindex","indexestodump", true, "indexes which needs to be dumped" +
				"for pretty output. bydefault set to {0,1}. " +
				"Just to avoid making command line argument big");
		options.addOption("rmindex","removeindexes", true, "indexes which needs to be removed" +
				"before building the classifer. bydefault set to {0,1}. " +
				"os as to avoid making command line argument big");
	}

	
	public void launchPad() throws Exception {
		Counter<String> termspace = this.returnFreqDistOnSetOfNgrams();
		if (this.removesinglecount)
			termspace = KLDivergence.removeSingleCounteTerms(termspace);
		this.trainingSet = this.returnIndicatorVectorOfTermSpace(termspace.keySet(),
				true);
		if (this.predict) {
			this.classifier = CommonClassifierRoutines.trainOnInstances(classifier, 
					this.trainingSet, this.indicesToRemove,this.classifieroptions );
			WekaInstances testingSet= this.returnIndicatorVectorOfTermSpace(termspace.keySet(),
					false);
			testingSet.setClassMissingForEachInstance();
		}
		else if (this.eval)
			this.evaluate();
		else {
			CommonClassifierRoutines.leaveOneOutCrossValidation(this.classifier, this.trainingSet, 
					this.indicesToRemove, this.indicesTodump, this.classifieroptions
					, this.dumpfile);
		}
			
	}
	public static MedicalTrainer  parserArgument(String[] commandargs) throws Exception {
		MedicalTrainer.addOptions();
		MedicalTrainer mt = new MedicalTrainer();
		CommandLineParser cmdLineGnuParser = new GnuParser();
		CommandLine command = cmdLineGnuParser.parse(options, commandargs);
		if (command.hasOption("predict")) {
			 mt.predict = true;
		}
		if (command.hasOption("eval")) {
			mt.setEval(true);
		}
		if (command.hasOption("cltype")) {
			mt.initClassifier(ClassifierType.valueOf(command.getOptionValue("cltype")),
					null);
		}
		if (command.hasOption("ifile")) {
			mt.setFilename(command.getOptionValue("ifile"));
		}
		if (command.hasOption("ifile")) {
			mt.setFilename(command.getOptionValue("ifile"));
		}
		
		if (command.hasOption("st")) {
			mt.setStem(new Boolean(command.getOptionValue("st")));
		}
		
		if (command.hasOption("lc")) {
			mt.setLowercase(new Boolean(command.getOptionValue("lc")));
		}
		if (command.hasOption("stopword")) {
			throw new UnsupportedOperationException("Havn't yet implemented " +
					"the command line parsing for stop words");
		}
		
		if (command.hasOption("tcol")) {
			mt.setTextcol(Integer.parseInt(command.getOptionValue("tcol")));
		}
		
		if (command.hasOption("lcol")) {
			mt.setLabelcol(Integer.parseInt(command.getOptionValue("lcol")));
		}
		
		if (command.hasOption("agecol")) {
			mt.setAgecol(Integer.parseInt(command.getOptionValue("agecol")));
		}
		
		if (command.hasOption("lage")) {
			mt.setLowageval(Float.parseFloat(command.getOptionValue("lage")));
		}
		
		if (command.hasOption("uage")) {
			mt.setUpageval(Float.parseFloat(command.getOptionValue("uage")));
		}
		
		if (command.hasOption("idcol")) {
			mt.setIdentifierCol(Integer.parseInt(command.getOptionValue("idcol")));
		}
		
		if (command.hasOption("idname")) {
			mt.setIdentifierName(command.getOptionValue("uage"));
		}
		if (command.hasOption("ngrams")) {
			String ngrams = command.getOptionValue("ngrams");
			StringTokenizer st = new StringTokenizer(ngrams,":");
			List<Integer> ngramsToGet = new ArrayList<Integer>();
			while (st.hasMoreTokens()) {
				ngramsToGet.add(Integer.parseInt(st.nextToken()));
			}
			mt.setNgramsToGet(ngramsToGet);
		}
		
		if (command.hasOption("lclass")) {
			throw new UnsupportedOperationException("Havn't yet implemented " +
					"the command line parsing for this label classes");
		}
		return mt;
	}
	
	public void initClassifier(ClassifierType ctype, String[] options) 
	throws Exception {
		if (ctype.equals(ClassifierType.j48)) {
			this.classifier = new J48Classifier();
			this.classifier.setOptions(options);
		}
		else if (ctype.equals(ClassifierType.simplelogistic)) {
			this.classifier = new SimpleLogistic(100, true , false);
			this.classifier.setOptions(options);
		}
	}
	private boolean removesinglecount=true;
	private String dumpfile = null;
	private String[] classifieroptions =null;
	private boolean predict = false;
	private WekaInstances trainingSet = null;
	private boolean eval = false;
	/**
	 * @return the eval
	 */
	public boolean isEval() {
		return eval;
	}

	/**
	 * @param eval the eval to set
	 */
	public void setEval(boolean eval) {
		this.eval = eval;
	}
	private AbstractClassifier classifier=null;
    private String identifierName;
	/**
	 * @return the identifierName
	 */
	public String getIdentifierName() {
		return identifierName;
	}

	/**
	 * @param identifierName the identifierName to set
	 */
	public void setIdentifierName(String identifierName) {
		this.identifierName = identifierName;
	}

	/**
	 * @return the textcol
	 */
	public int getTextcol() {
		return textcol;
	}

	/**
	 * @param textcol the textcol to set
	 */
	public void setTextcol(int textcol) {
		this.textcol = textcol;
	}

	/**
	 * @return the identifierCol
	 */
	public int getIdentifierCol() {
		return identifierCol;
	}

	/**
	 * @param identifierCol the identifierCol to set
	 */
	public void setIdentifierCol(int identifierCol) {
		this.identifierCol = identifierCol;
	}

	/**
	 * @return the agecol
	 */
	public int getAgecol() {
		return agecol;
	}

	/**
	 * @param agecol the agecol to set
	 */
	public void setAgecol(int agecol) {
		this.agecol = agecol;
	}

	/**
	 * @return the labelcol
	 */
	public int getLabelcol() {
		return labelcol;
	}

	/**
	 * @param labelcol the labelcol to set
	 */
	public void setLabelcol(int labelcol) {
		this.labelcol = labelcol;
	}

	/**
	 * @return the lowageval
	 */
	public float getLowageval() {
		return lowageval;
	}

	/**
	 * @param lowageval the lowageval to set
	 */
	public void setLowageval(float lowageval) {
		this.lowageval = lowageval;
	}

	/**
	 * @return the upageval
	 */
	public float getUpageval() {
		return upageval;
	}

	/**
	 * @param upageval the upageval to set
	 */
	public void setUpageval(float upageval) {
		this.upageval = upageval;
	}

	/**
	 * @return the ngramsToGet
	 */
	public List<Integer> getNgramsToGet() {
		return ngramsToGet;
	}

	/**
	 * @param ngramsToGet the ngramsToGet to set
	 */
	public void setNgramsToGet(List<Integer> ngramsToGet) {
		this.ngramsToGet = ngramsToGet;
	}

	/**
	 * @return the stopwordlist
	 */
	public Set<?> getStopwordlist() {
		return stopwordlist;
	}

	/**
	 * @param stopwordlist the stopwordlist to set
	 */
	public void setStopwordlist(Set<?> stopwordlist) {
		this.stopwordlist = stopwordlist;
	}

	/**
	 * @return the stem
	 */
	public boolean isStem() {
		return stem;
	}

	/**
	 * @param stem the stem to set
	 */
	public void setStem(boolean stem) {
		this.stem = stem;
	}

	/**
	 * @return the lowercase
	 */
	public boolean isLowercase() {
		return lowercase;
	}

	/**
	 * @param lowercase the lowercase to set
	 */
	public void setLowercase(boolean lowercase) {
		this.lowercase = lowercase;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the classvalues
	 */
	public List<String> getClassvalues() {
		return classvalues;
	}

	/**
	 * @param classvalues the classvalues to set
	 */
	public void setClassvalues(List<String> classvalues) {
		this.classvalues = classvalues;
	}

	/**
	 * @return the datasetname
	 */
	public String getDatasetname() {
		return datasetname;
	}

	/**
	 * @param datasetname the datasetname to set
	 */
	public void setDatasetname(String datasetname) {
		this.datasetname = datasetname;
	}

	/**
	 * @return the skipHeader
	 */
	public boolean isSkipHeader() {
		return skipHeader;
	}

	/**
	 * @param skipHeader the skipHeader to set
	 */
	public void setSkipHeader(boolean skipHeader) {
		this.skipHeader = skipHeader;
	}
	private int[] indicesTodump = {0,1};
	private int[] indicesToRemove = {0,1};
	private int textcol;
	private int identifierCol;
	private int agecol;
	private int labelcol;
	private float lowageval;
	private float upageval;
	private List<Integer> ngramsToGet;
	private Set<?> stopwordlist = null;
	private boolean stem;
	private boolean lowercase;
	private String filename;
	List<String> classvalues;
	private String datasetname;
	private boolean skipHeader;
	public MedicalTrainer() {
		this.classvalues = FeedCategories.returnAllValues();
		stopwordlist = StopWordList.getMedicalStopWordList(Version.LUCENE_35) ;
		this.lowercase = true;
		this.stem = true;
		this.skipHeader = true;
		this.datasetname = "medicalTrainer";
	}
	
	/**
	 * 
	 * @param filename : Name of the csv file from where I am getting data.
	 * @param termspace : The term space which I need to check for text in each row
	 * in csv file.
	 * @param identifierCol : The column number of the identifier in csv file.
	 * @param identifierName : The name of identifier. (Need it to name 
	 * the attribute of weka instances)
	 * @param textcol : The column number of the text field in csv.
	 * @param agecol : the column number of age column in csv file.
	 * @param lowvalue : The lower limit of the age.
	 * @param upvalue : The upper limit of age.
	 * @param labelcol : The column number which contains the label we have
	 * assigne for the text column.
	 * @param classvalues: List of the possible label values.
	 * @param ngramsToget : ngram to get. U
	 * @param tolowercase : Do you want to convert  the input text to lower case ?
	 * @param stopwordlist : List of stop word  to be removed from the text.
	 * @param datasetname : The name of the weka instances data set.
	 * @param skipHeader: Skips the first line of the csv file.
	 * @param stem: stems the input
	 * @return : The weka Instances.
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public  WekaInstances returnIndicatorVectorOfTermSpace(Set<String> termspace, boolean doNotAddUnkownClass)
	throws NumberFormatException, IOException {
		int numattributes = 0;
		if (termspace != null)
			numattributes = termspace.size();
		
		if (this.identifierCol >= 0)
			numattributes++;
		if (this.agecol >= 0)
			numattributes++;
		if (this.labelcol >= 0)
			numattributes++;
		WekaInstances wekainstances = new WekaInstances(this.datasetname, new ArrayList<Attribute>(),
				numattributes);
		wekainstances.setSparseInstance(true);
		int index = 0;
		if (this.identifierCol >= 0) {
			ArrayList<String> ambiguity=null;
			wekainstances.insertAttributeAt(new Attribute(this.identifierName,ambiguity),index++);
						
		}
		if (this.agecol >= 0) {
			wekainstances.insertAttributeAt(new Attribute("age"),index++);
		}
		
		if (termspace != null) {
			for (String s : termspace) {
				wekainstances.insertAttributeAt(new Attribute(s),index++);
				//	wekainstances.insertAttributeAt(new Attribute(s, possibleVal),index++);
			}
		}
		
		if (labelcol >= 0) {
			wekainstances.insertAttributeAt(new Attribute("class",
					classvalues),index++);
			wekainstances.setClassIndex(index -1);
		}
		CSVReader csvreader = new CSVReader(new FileReader(this.filename));
		String[] nextLine =null;
		if (this.skipHeader)
			csvreader.readNext();
		while ((nextLine = csvreader.readNext()) != null) {
			float age = (float)-1.0 ;
			if (this.agecol >= 0) {
				age = Float.parseFloat(nextLine[agecol-1]);
				if (age < this.lowageval || age > this.upageval) 
					continue;
			}
			wekainstances.initWorkingInstance();
			wekainstances.addWorkingInstance();
			if (this.identifierCol >= 0) 
				wekainstances.setValueOfWorkingInstance(this.identifierName,
						nextLine[this.identifierCol-1]);
			if (this.agecol >= 0)
				wekainstances.setValueOfWorkingInstance("age",age);
			String text = nextLine[this.textcol-1];
			if (termspace != null) {
				for (String t : termspace) {
					wekainstances.setValueOfWorkingInstance(t, 0.0);
				}
			}
		//	System.out.println(nextLine[identifierCol-1]+" "+ age);
			for (int ngram : this.ngramsToGet) {
				Set<String> terms = this.returnUniqueNgramTermspace(text, ngram);
				if (termspace != null) {
					for (String t : terms)	{
						if (termspace.contains(t))
							wekainstances.setValueOfWorkingInstance(t, 1.0);
					}
				}
				else {
					for (String t : terms)					
						wekainstances.setValueOfWorkingInstance(t, 1.0, true);
				}
			}
			String lclass = null;
			if (this.labelcol >= 0 && nextLine.length >= this.labelcol
					&& nextLine[labelcol -1].length() > 0) {
				lclass = nextLine[this.labelcol -1];
			//	System.out.println(lclass);
				wekainstances.setValueOfWorkingInstance("class", lclass);
			}
			if ( lclass == null && doNotAddUnkownClass) {
				wekainstances.delete(wekainstances.numInstances() -1 );
			}
		}
		wekainstances.setValueForEachInstance(false, 0.0);
		return wekainstances;
	}
	
	
	public  HashSet<String> returnUniqueNgramTermspace(String input, int ngram ) {
		LinkedHashSet<String> uniqueTerms = new LinkedHashSet<String>();
		List<String> tokens = MedicalTrainer.ptbTokenizer(input);
		if (this.lowercase)
			MedicalTrainer.toLowerCase(tokens);
		if (this.stopwordlist != null)
			MedicalTrainer.removeStopWords(tokens,this.stopwordlist);
		if (this.stem)
			MedicalTrainer.simpleStem(tokens);		
		if (this.stopwordlist != null)
			MedicalTrainer.removeStopWords(tokens,this.stopwordlist);
		
		StringBuilder sb = new StringBuilder();
		for(String tok: tokens) {
			sb.append(tok);
			sb.append(" ");
		}
		String newstring = sb.toString().trim();
		LucenePTBTokenizer lptb = 
			new LucenePTBTokenizer(new StringReader(newstring));
		NgramTokenizer ntokenizer = new NgramTokenizer(lptb, ngram);
		ntokenizer.tokenize();
		while(ntokenizer.hasNext()) {
			uniqueTerms.add((String)ntokenizer.next());
		}
		return uniqueTerms;
	}
	
	public static void simpleStem(List<String> input) {
		Morphology mp = new Morphology();
		for (int i = 0; i < input.size(); i++) {
			//String orig = input.get(i);
		//	System.out.println("As"+input.get(i)+"As");
			String stem = mp.stem(input.get(i));
			if (!input.get(i).equals(stem)) {
		//		System.out.println(orig+"  "+ stem);
				input.set(i, stem);
			}
		}
	}
	/**
	 * Coversts the token to lower case. remove the stop words using the medical 
	 * stop word list defined by me
	 * @param csvFile
	 * @param lowage : lowage filter
	 * @param upage : high age filter
	 * @param agecol : columne number in the csv file which contains age
	 * @param textcol : column number in the csv file which contains the text
	 * @param ngram : the ngram
	 * @return
	 */
	public  HashSet<String> returnUniqueNgramTermSpace(int ngram) {

		CSVReader csvreader;
		LinkedHashSet<String> uniqueTerms = new LinkedHashSet<String>();
		try {
			    
				csvreader = new CSVReader(new FileReader(this.filename));
				String[] nextLine =null;
				if (this.skipHeader)
					csvreader.readNext();
				while ((nextLine = csvreader.readNext()) != null) {
					Double age = Double.parseDouble(nextLine[this.agecol-1]);
					if (age < this.lowageval || age > this.upageval)
						continue;
					if (nextLine[this.textcol-1].length() <= 0)
						continue;
					List<String> tokens = MedicalTrainer.ptbTokenizer(
							nextLine[this.textcol -1]);
					MedicalTrainer.toLowerCase(tokens);
					MedicalTrainer.removeStopWords(tokens,this.stopwordlist);
					
					if (this.stem)
						MedicalTrainer.simpleStem(tokens);
					
					MedicalTrainer.removeStopWords(tokens, this.stopwordlist);
					StringBuilder sb = new StringBuilder();
					for(String tok: tokens) {
						sb.append(tok);
						sb.append(" ");
					}
					String newstring = sb.toString().trim();
					LucenePTBTokenizer lptb = 
						new LucenePTBTokenizer(new StringReader(newstring));
					NgramTokenizer ntokenizer = new NgramTokenizer(lptb, ngram);
					ntokenizer.tokenize();
					while(ntokenizer.hasNext()) {
						uniqueTerms.add((String)ntokenizer.next());
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return uniqueTerms;
	}
	
	
	public static List<String> ptbTokenizer(String input) {
		StringReader reader = new StringReader(input);
		LucenePTBTokenizer lptb = new LucenePTBTokenizer(reader);
		List<String> result = new Vector<String>();
		while (lptb.getPtbtokenizer().hasNext()) {
			CoreLabel label = (CoreLabel) lptb.getPtbtokenizer().next();
			result.add(label.word());
		}
		return result;
	}
	
	public static List<String> toLowerCase(List<String> input) {
		for (int index = 0; index < input.size();index++) {
			input.set(index, input.get(index).toLowerCase());
		}
		return input;
	}
	
	public static List<String> removeStopWords(List<String> input, 
			Set<?> stopwords) {
		if (stopwords == null)
			return input;
		List<String> toberemoved = new Vector<String>();
		for (String s : input) {
			if (stopwords.contains(s))
				toberemoved.add(s);
				
		}
		for (String remove : toberemoved) {
			input.remove(remove);
		}
//		input.removeAll();
		return input;
	}
	

	public static Counter<String> addToCounter(Counter<String> cntr, 
			List<String> tokens) {
		for (String tok : tokens) {
			cntr.incrementCount(tok);
		}
		return cntr;
	}
	
	
	public static Counter<String> returnFreqDist(String string , int ngram,
			boolean tolowercase, boolean stem,
			Set<?> stopwordlist) {
		Counter<String> cntr = new IntCounter<String>();
		List<String> tokens = MedicalTrainer.ptbTokenizer(
				string);
		MedicalTrainer.toLowerCase(tokens);
		MedicalTrainer.removeStopWords(tokens, 
				stopwordlist);
		if (stem)
			MedicalTrainer.simpleStem(tokens);
		MedicalTrainer.removeStopWords(tokens, 
				stopwordlist);
		
		StringBuilder sb = new StringBuilder();
		for(String tok: tokens) {
			sb.append(tok);
			sb.append(" ");
		}
		String newstring = sb.toString().trim();
		LucenePTBTokenizer lptb = 
			new LucenePTBTokenizer(new StringReader(newstring));
		NgramTokenizer ntokenizer = new NgramTokenizer(lptb, ngram);
		ntokenizer.tokenize();			
		Vector<String> filteredString = new Vector<String>();
		while(ntokenizer.hasNext())
			filteredString.add((String)ntokenizer.next());
		MedicalTrainer.addToCounter(cntr, filteredString);			

		return cntr;
	}
	
	public  Counter<String> returnFreqDistOnSetOfNgrams() throws NumberFormatException, IOException 
	 {
		Counter<String> result = new IntCounter<String>();
		for (int ngram : this.ngramsToGet) {
			result.addAll(this.returnFreqDist(ngram));
		}
		return result;
	}
	/**
	 * Returns the global FreqDist
	 * @param filename
	 * @param textcol
	 * @param agecol
	 * @param lowvalue
	 * @param upvalue
	 * @param ngramsToget
	 * @param tolowercase
	 * @param stopwordlist
	 * @param skipHeader
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public  Counter<String> returnFreqDist( int ngramsToget)	
		throws NumberFormatException, IOException {
		
		Counter<String> cntr = new IntCounter<String>();
		CSVReader csvreader = new CSVReader(new FileReader(this.filename));
		String[] nextLine =null;
		if (this.skipHeader)
			csvreader.readNext();
		while ((nextLine = csvreader.readNext()) != null) {
			float age = (float)-1.0 ;
			if (this.agecol >= 0) {
				age = Float.parseFloat(nextLine[this.agecol-1]);
				if (age < this.lowageval || age > this.upageval) 
					continue;
			}	
			List<String> tokens = MedicalTrainer.ptbTokenizer(
					nextLine[this.textcol -1]);
			MedicalTrainer.toLowerCase(tokens);
			MedicalTrainer.removeStopWords(tokens,	stopwordlist);
			
			if (this.stem)
				MedicalTrainer.simpleStem(tokens);
			
			MedicalTrainer.removeStopWords(tokens, 
					this.stopwordlist);
			
			StringBuilder sb = new StringBuilder();
			for(String tok: tokens) {
				sb.append(tok);
				sb.append(" ");
			}
			String newstring = sb.toString().trim();
			LucenePTBTokenizer lptb = 
				new LucenePTBTokenizer(new StringReader(newstring));
			NgramTokenizer ntokenizer = new NgramTokenizer(lptb, ngramsToget);
			ntokenizer.tokenize();			
			Vector<String> input = new Vector<String>();
			while(ntokenizer.hasNext())
				input.add((String)ntokenizer.next());
			MedicalTrainer.addToCounter(cntr, input);			
		}
		return cntr;
	}
	
	public WekaInstances returnRelativeProbabilityWeight(HashMap<Integer, 
			Counter<String>> counters, boolean addunknownClass)
	throws NumberFormatException, IOException {
		
		int numattributes = 0;
		HashMap<Integer, Distribution<String>> distmap
			= new HashMap<Integer,Distribution<String>>();
		
		
		for (Counter<String> cnt : counters.values()) {
			numattributes = numattributes + cnt.size();
			
		}		
		for (int i = 0; i < counters.size(); i++) {
			distmap.put(i,  Distribution.laplaceSmoothedDistribution(
					counters.get(i), counters.get(i).keySet().size()));
		}
		if (this.identifierCol >= 0)
			numattributes++;
		if (this.agecol >= 0)
			numattributes++;
		if (this.labelcol >= 0)
			numattributes++;
		WekaInstances wekainstances = new WekaInstances(this.datasetname, 	new ArrayList<Attribute>(),
				numattributes);
		wekainstances.setSparseInstance(true);
		int index = 0;
		if (this.identifierCol >= 0) {
			ArrayList<String> ambiguity=null;
			wekainstances.insertAttributeAt(new Attribute(this.identifierName,ambiguity),index++);
						
		}
		if (this.agecol >= 0) {
			wekainstances.insertAttributeAt(new Attribute("age"),index++);
		}
		
		for (Counter<String> cntr : counters.values()) {
			for (String s :cntr.keySet())
				wekainstances.insertAttributeAt(new Attribute(s),index++);
		}
		if (this.labelcol >= 0) {
			wekainstances.insertAttributeAt(new Attribute("class",	this.classvalues),index++);
			wekainstances.setClassIndex(index -1);
		}
		
		CSVReader csvreader = new CSVReader(new FileReader(this.filename));
		String[] nextLine =null;
		if (this.skipHeader)
			csvreader.readNext();
		while ((nextLine = csvreader.readNext()) != null) {
			float age = (float)-1.0 ;
			if (this.agecol >= 0) {
				age = Float.parseFloat(nextLine[this.agecol-1]);
				if (age < this.lowageval || age > this.upageval) 
					continue;
			}
			wekainstances.initWorkingInstance();
			wekainstances.addWorkingInstance();
			if (this.identifierCol >= 0) 
				wekainstances.setValueOfWorkingInstance(this.identifierName,
						nextLine[identifierCol-1]);
			if (agecol >= 0)
				wekainstances.setValueOfWorkingInstance("age",age);
		
			for (Counter<String> cntr : counters.values()) {
				for (String s :cntr.keySet())
					wekainstances.setValueOfWorkingInstance(s, 0.0);
			}
			
			for (int i=0; i < this.ngramsToGet.size(); i++) {
				Distribution<String> gdist = distmap.get(i);
				Counter<String> local = MedicalTrainer.returnFreqDist(
						nextLine[textcol-1],this.ngramsToGet.get(i), 
						this.lowercase,this.stem,	this.stopwordlist);
				Distribution<String> ldist = Distribution.laplaceSmoothedDistribution(
						local, local.keySet().size());
				for (String term : local.keySet()) {
					double localprob = ldist.probabilityOf(term);
					double value = gdist.probabilityOf(term) * (gdist.logProbabilityOf(term)
						- ldist.logProbabilityOf(term)); 
					wekainstances.setValueOfWorkingInstance(term, value, 
							true);
				}
			}
			String lclass = null;
			if (this.labelcol >= 0 && nextLine.length >= this.labelcol
					&& nextLine[this.labelcol -1].length() > 0) {
				lclass = nextLine[this.labelcol -1];
			//	System.out.println(lclass);
				wekainstances.setValueOfWorkingInstance("class", lclass);
			}
			if (!addunknownClass || lclass == null ) {
				wekainstances.delete(wekainstances.numInstances() -1 );
			}
		}
		
		return wekainstances;
	}
	
	public static void printToFile(String filename, Counter<String> counter , int numDocs) {
		
		try {
			CSVWriter csvWriter= new CSVWriter(new FileWriter(new File(filename)));
			String[] row = new String[4];
			for (String key : counter.keySet()) {
				row[0] = key;
				double val = counter.getCount(key);
				row[1] = new String(""+val);
				row[2] = new String("" + (val/numDocs));
				csvWriter.writeNext(row);
				
			}
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void evaluate() throws Exception {
		Instances instances = CommonClassifierRoutines.removeAttributes(
				this.trainingSet,this.indicesToRemove);
		Evaluation eval = new Evaluation(instances);
		eval.crossValidateModel(this.classifier, instances, instances.numInstances(), 
				new Random(1000));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
	}
	
}
