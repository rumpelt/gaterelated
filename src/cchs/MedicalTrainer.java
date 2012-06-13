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
import java.util.Collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import mahout.classifiers.LogisticRegression;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.IntCounter;
import edu.stanford.nlp.util.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import specialstruct.StringCounter;
import stats.KLDivergence;
import stats.Sampling;
import tokenizers.LucenePTBTokenizer;
import tokenizers.NgramTokenizer;
import tokenizers.StopWordList;
import tokenizers.StopwordType;
import topicmodel.MultinomialDocumentModel;
import weka.WekaInstances;
import weka.WekaRoutines;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.ClassifierType;
import weka.classifiers.CommonClassifierRoutines;
import weka.classifiers.Evaluation;
import weka.classifiers.J48Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * a set of very specific routines for my work on medical data.
 * 
 * @author ashwani an example of the commandline argument to invoke this is as
 *         following  --ifile /home/ashwani/xyz/allfeedtype.csv --tcol 3
 *         --agecol 2 --idcol 1 --lcol 7 --cltype=adaboost --eval --lage 0.0
 *         --uage 1.0 --idname mrn --ngrams 1
 *         --cloptions="-W weka.classifiers.functions.pLogistic,-P 100"
 * 
 *         another example is as following
 * 
 *          --ifile /home/ashwani/xyz/allfeedtype.csv --tcol 3 --agecol 2
 *         --idcol 1 --lcol 7 --lage 0.0 --uage 1.0 --idname=mrn --ngrams 1:2:3
 *         --dumparff=/home/ashwani/xyz/arff/biasedsettrigram.arff --dense
 * 
 */
public class MedicalTrainer extends Object {
	private static Options options = new Options();
	private List<Record> population = null;
	private HashMap<StringCounter, FeedCategories> map = null;
	private String[] cloptions;
	private ClassifierType cltype;
	private boolean languageModel = false;
	private int minngram = -1;
	private int samplesize;
	private int numIteration;
	private boolean removeCommonCounters=false;
	private boolean manualruleset = false;
	private String rulefile = null;
    
	private static void addOptions() {
	    
		options.addOption("manualruleset","manualruleset",false, "are we going to use a manual rulset to categorize");
		options.addOption("rulefile","rulefile",true, "a rulefile to use categorize some of the text. These are human defined rules");
		options.addOption("removeCommonCounters", "removeCommonCounters", false,
		"if this option is present the remove highly frequent counters from the model");
		
		options.addOption("samplesize", "samplesize", true,
				"samplesize to work on");
		options.addOption("numi", "numi", true, "num iterations to perform");
		options.addOption("minngram", "minngram", true,
				"the min ngram for ngram tokenizer"
						+ "the minngram for ngram tokemizer");
		options.addOption("lm", "languagemodel", false,
				"whether to use language"
						+ "modelling or the simple indicator spaces");
		options.addOption("dense", "denseinstance", false,
				"whether to use dense" + "instance or not");
		options.addOption("cloptions","cloption",true,
				"option to the classifier must be included in brackets"
						+ "< cl options >, also mustbe at the end of the classifer");

		options.addOption("removesinglecount", "removesinglecount", false,
				"Remove terms with" + "single count in the termspace");
		options.addOption("eval", "eval", false,
				"Are we doing just evaluatin? Using the weka"
						+ "api Evalatuation, by default it  is set to false");
		options.addOption("dumparff", "dumparff", true,
				"dump the training set arff, this is" + "for debugging purpose");
		options.addOption("predict", "predict", false,
				"run the classifer built to predict and dump");
		options.addOption("cltype", "classifierType", true,
				"the type of weka classifier to use");
		options.addOption("ifile", "inputfile", true,
				"the csv file to  be oprated on");
		options.addOption("dfile", "dumpfile", true,
				"the csv file to which we will dump the output"
						+ "of the prediction");
		options.addOption("st", "stem", true,
				"do we want to stem. defaults to true");
		options.addOption("lc", "lowercase", true,
				"do we want to lowercase, defualts to" + "true");
		options.addOption("stopword", "stopword", true,
				"do we want to use the stop words, "
						+ "defaults to StopWordList.medicalWords");
		options.addOption("tcol", "textcol", true, "text column number");
		options.addOption("lcol", "labelcol", true, "label col number");
		options.addOption("agecol", "agecol", true, "age column number");
		options.addOption("lage", "lowage", true, "the low age value");
		options.addOption("uage", "upage", true, "the up age value");
		options.addOption("idcol", "idcol", true, "identifier column number");
		options.addOption("idname", "idname", true, "nameOfIdentifier");
		options.addOption("lclass", "labelclass", true,
				"List of Labels of the class, "
						+ "currently defaults to FeedCategories");
		options.addOption("ngrams", "ngrams", true,
				"the ngrams to operate upon, the listofngrams"
						+ "is separated by semicolon :");
		options.addOption("rmsinglecount", "removesinglecount", true,
				"remove singlecount terms");
		options.addOption("dumpindex", "indexestodump", true,
				"indexes which needs to be dumped"
						+ "for pretty output. bydefault set to {0,1}. "
						+ "Just to avoid making command line argument big");
		options.addOption(
				"rmindex",
				"removeindexes",
				true,
				"indexes which needs to be removed"
						+ "before building the classifer. bydefault set to {0,1}. "
						+ "os as to avoid making command line argument big");
	}

	public static class Record extends Object {
		private final String id;

		/**
		 * @return the text
		 */
		public String getText() {
			return text;
		}

		/**
		 * @param text
		 *            the text to set
		 */
		public void setText(String text) {
			this.text = text;
		}

		/**
		 * @return the age
		 */
		public float getAge() {
			return age;
		}

		/**
		 * @param age
		 *            the age to set
		 */
		public void setAge(float age) {
			this.age = age;
		}

		/**
		 * @return the textcategory
		 */
		public String getTextcategory() {
			return textcategory;
		}

		/**
		 * @param textcategory
		 *            the textcategory to set
		 */
		public void setTextcategory(String textcategory) {
			this.textcategory = textcategory;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		private String text;
		private float age;
		private String textcategory = null;

		public Record(String id) {
			this.id = id;
		}
	}

	/**
	 * launch pad for this
	 * 
	 * @throws Exception
	 */
	public void launchPad() throws Exception {
		
		if (this.rulefile != null)
			this.map = StopWordList.getCounterToFilter(this.rulefile, false);
		this.populateRecords();
		
		if (this.languageModel) {
			this.doLanguageModelWithSampling(this.samplesize, 
					this.numIteration);
		} else if (this.manualruleset) {
	     		//this.generateRuleFile();
			this.doManualClassification();		
		}
		else if (ClassifierType.isWekaClassifer(this.cltype.toString())){
			this.doWekaClassificationWithSampling(this.samplesize, this.numIteration);
		}
	}

	public String[] cloptionparser(String opt) {
		StringTokenizer st = new StringTokenizer(opt, ",");
		ArrayList<String> opts = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			opts.add(st.nextToken());
		}
		return opts.size() == 0 ? null : opts.toArray(new String[opts.size()]);
	}

	public static MedicalTrainer parserArgument(String[] commandargs)
			throws Exception {

		MedicalTrainer.addOptions();
		MedicalTrainer mt = new MedicalTrainer();
		CommandLineParser cmdLineGnuParser = new GnuParser();

		CommandLine command = cmdLineGnuParser.parse(options, commandargs);
		
		if (command.hasOption("manualruleset"))
			mt.manualruleset = true;
		if (command.hasOption("rulefile"))
			mt.rulefile = command.getOptionValue("rulefile");		
		
		if (command.hasOption("removeCommonCounters"))
			mt.removeCommonCounters = true;
		
		if (command.hasOption("samplesize"))
			mt.samplesize = Integer.parseInt(command
					.getOptionValue("samplesize"));
		if (command.hasOption("numi"))
			mt.numIteration = Integer.parseInt(command.getOptionValue("numi"));
		if (command.hasOption("minngram"))
			mt.minngram = Integer.parseInt(command.getOptionValue("minngram"));

		if (command.hasOption("lm"))
			mt.languageModel = true;
		if (command.hasOption("dense"))
			mt.denseinstance = true;
		if (command.hasOption("dumparff")) {
			mt.dumparff = command.getOptionValue("dumparff");
		}
		if (command.hasOption("predict")) {
			mt.predict = true;
		}
		if (command.hasOption("eval")) {
			mt.setEval(true);
		}
		if (command.hasOption("cltype")) {
			if (command.hasOption("cloptions")) {
				mt.cloptions = mt.cloptionparser(command
						.getOptionValue("cloptions"));
			}
			mt.cltype = ClassifierType
					.valueOf(command.getOptionValue("cltype"));
			mt.initClassifier(mt.cltype, mt.cloptions);
		}
		if (command.hasOption("ifile")) {
			mt.setFilename(command.getOptionValue("ifile"));
		}
		if (command.hasOption("dfile")) {
			mt.dumpfile = command.getOptionValue("dfile");
		}

		if (command.hasOption("st")) {
			mt.setStem(new Boolean(command.getOptionValue("st")));
		}

		if (command.hasOption("lc")) {
			mt.setLowercase(new Boolean(command.getOptionValue("lc")));
		}
		if (command.hasOption("stopword")) {
			throw new UnsupportedOperationException("Havn't yet implemented "
					+ "the command line parsing for stop words");
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
			mt.setIdentifierCol(Integer.parseInt(command
					.getOptionValue("idcol")));
		}

		if (command.hasOption("idname")) {
			mt.setIdentifierName(command.getOptionValue("idname"));
		}
		if (command.hasOption("ngrams")) {
			String ngrams = command.getOptionValue("ngrams");
			StringTokenizer st = new StringTokenizer(ngrams, ":");
			List<Integer> ngramsToGet = new ArrayList<Integer>();
			while (st.hasMoreTokens()) {
				ngramsToGet.add(Integer.parseInt(st.nextToken()));
			}
			mt.setNgramsToGet(ngramsToGet);
		}

		if (command.hasOption("lclass")) {
			throw new UnsupportedOperationException("Havn't yet implemented "
					+ "the command line parsing for this label classes");
		}
		return mt;
	}

	public void initClassifier(ClassifierType ctype, String[] options)
			throws Exception {
		if (ctype.equals(ClassifierType.j48)) {
			this.classifier = new J48Classifier();
			this.classifier.setOptions(options);

		} else if (ctype.equals(ClassifierType.simplelogistic)) {
			this.classifier = new SimpleLogistic(100, true, false);
			this.classifier.setOptions(options);
		} else if (ctype.equals(ClassifierType.logistic)) {
			this.classifier = new Logistic();
			this.classifier.setOptions(options);
		} else if (ctype.equals(ClassifierType.adaboost)) {
			this.classifier = new AdaBoostM1();
			this.classifier.setOptions(options);
		}
		else if (ctype.equals(ClassifierType.randomforest)) {
			this.classifier = new RandomForest();
			this.classifier.setOptions(options);
		}
	}

	private int numcrossfold;
	private boolean removesinglecount = true;
	private boolean denseinstance = false;
	/**
	 * Dump the training set to arff
	 */
	private String dumparff = null;
	/**
	 * file to dump the results to.
	 */
	private String dumpfile = null;
	private String[] classifieroptions = null;
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
	 * @param eval
	 *            the eval to set
	 */
	public void setEval(boolean eval) {
		this.eval = eval;
	}

	private AbstractClassifier classifier = null;
	private String identifierName;

	/**
	 * @return the identifierName
	 */
	public String getIdentifierName() {
		return identifierName;
	}

	/**
	 * @param identifierName
	 *            the identifierName to set
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
	 * @param textcol
	 *            the textcol to set
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
	 * @param identifierCol
	 *            the identifierCol to set
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
	 * @param agecol
	 *            the agecol to set
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
	 * @param labelcol
	 *            the labelcol to set
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
	 * @param lowageval
	 *            the lowageval to set
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
	 * @param upageval
	 *            the upageval to set
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
	 * @param ngramsToGet
	 *            the ngramsToGet to set
	 */
	public void setNgramsToGet(List<Integer> ngramsToGet) {
		this.ngramsToGet = ngramsToGet;
	}

	/**
	 * @return the stem
	 */
	public boolean isStem() {
		return stem;
	}

	/**
	 * @param stem
	 *            the stem to set
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
	 * @param lowercase
	 *            the lowercase to set
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
	 * @param filename
	 *            the filename to set
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
	 * @param classvalues
	 *            the classvalues to set
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
	 * @param datasetname
	 *            the datasetname to set
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
	 * @param skipHeader
	 *            the skipHeader to set
	 */
	public void setSkipHeader(boolean skipHeader) {
		this.skipHeader = skipHeader;
	}

	private int[] indicesTodump = { 0, 1 };
	private int[] indicesToRemove = { 0, 1 };
	private int textcol;
	private int identifierCol;
	private int agecol;
	private int labelcol=-1;
	private float lowageval;
	private float upageval;
	private List<Integer> ngramsToGet;
	private boolean stopwordlist;
	private boolean stem;
	private boolean lowercase;
	private String filename;
	List<String> classvalues;
	private String datasetname;
	private boolean skipHeader;

	public MedicalTrainer() {
		this.classvalues = FeedCategories.returnAllValues();
		// stopwordlist = StopWordList.getMedicalStopWordList(Version.LUCENE_35)
		// ;
		this.stopwordlist = true;
		this.lowercase = true;
		this.stem = true;
		this.skipHeader = true;
		this.datasetname = "medicalTrainer";
	}

	/**
	 * 
	 * @param filename
	 *            : Name of the csv file from where I am getting data.
	 * @param termspace
	 *            : The term space which I need to check for text in each row in
	 *            csv file.
	 * @param identifierCol
	 *            : The column number of the identifier in csv file.
	 * @param identifierName
	 *            : The name of identifier. (Need it to name the attribute of
	 *            weka instances)
	 * @param textcol
	 *            : The column number of the text field in csv.
	 * @param agecol
	 *            : The column number of age column in csv file.
	 * @param lowvalue
	 *            : The lower limit of the age.
	 * @param upvalue
	 *            : The upper limit of age.
	 * @param labelcol
	 *            : The column number which contains the label we have assigne
	 *            for the text column.
	 * @param classvalues
	 *            : List of the possible label values.
	 * @param ngramsToget
	 *            : ngram to get. U
	 * @param tolowercase
	 *            : Do you want to convert the input text to lower case ?
	 * @param stopwordlist
	 *            : List of stop word to be removed from the text.
	 * @param datasetname
	 *            : The name of the weka instances data set.
	 * @param skipHeader
	 *            : Skips the first line of the csv file.
	 * @param stem
	 *            : stems the input
	 * @return : The weka Instances.
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public WekaInstances returnIndicatorVectorOfTermSpace(List<Record> records,
			Set<String> termspace, boolean doNotAddUnkownClass , boolean removeCommonCounters)
			throws NumberFormatException, IOException {
		if (records == null)
			records = this.population;
		int numattributes = 0;
		if (termspace != null)
			numattributes = termspace.size();

		if (this.identifierCol >= 0)
			numattributes++;
		if (this.agecol >= 0)
			numattributes++;
		if (this.labelcol >= 0)
			numattributes++;
		WekaInstances wekainstances = new WekaInstances(this.datasetname,
				new ArrayList<Attribute>(), numattributes);
		wekainstances.setSparseInstance(!this.denseinstance);
		int index = 0;
		if (this.identifierCol >= 0) 
			wekainstances.insertAttributeAt(new Attribute(this.identifierName),
					index++);
		if (this.agecol >= 0) 
			wekainstances.insertAttributeAt(new Attribute("age"), index++);
		if (termspace != null) {
			for (String s : termspace) {
				wekainstances.insertAttributeAt(new Attribute(s), index++);
			}
		}

		if (this.labelcol >= 0) {
			wekainstances.insertAttributeAt(new Attribute("class", classvalues), index++);
			wekainstances.setClassIndex(index - 1);
		}
	
		for (Record record : records) {
			wekainstances.initWorkingInstance();
			wekainstances.addWorkingInstance();
			if (this.identifierCol >= 0)
				wekainstances.setValueOfWorkingInstance(this.identifierName,
						Integer.parseInt(record.getId()));
			if (this.agecol >= 0)
				wekainstances.setValueOfWorkingInstance("age", record.getAge());
			
			if (termspace != null) {
				for (String t : termspace) {
					wekainstances.setValueOfWorkingInstance(t, 0.0);
				}
			}
			
			for (int ngram : this.ngramsToGet) {
				Set<String> terms = this
						.returnUniqueNgramTermspace(record.getText(), ngram, removeCommonCounters);
				if (termspace != null) {
					for (String t : terms) {
						if (termspace.contains(t))
							wekainstances.setValueOfWorkingInstance(t, 1.0);
					}
				} else {
					for (String t : terms)
						wekainstances.setValueOfWorkingInstance(t, 1.0, true);
				}
			}
			
			if (record.getTextcategory() != null) {
				wekainstances.setValueOfWorkingInstance("class", record.getTextcategory());
			}
			else if (record.getTextcategory() == null && doNotAddUnkownClass) {
				wekainstances.delete(wekainstances.numInstances() - 1);
			}
		}
		wekainstances.setValueForEachInstance(false, 0.0);
		return wekainstances;
	}

	public List<String> returnTokens(String input, int ngram, boolean removeCommonCounters) {
		List<String> terms = new ArrayList<String>();
		List<String> temptokens = MedicalTrainer.ptbTokenizer(input);

		List<String> tokens = new ArrayList<String>();
		for (String tok : temptokens) {
			if (tok.contains("-") || tok.contains("/") || tok.contains(";")
					|| tok.contains(",") || tok.contains("?")
					|| tok.contains("(") || tok.contains(")")) {
				StringTokenizer st = new StringTokenizer(tok, "-/;,()");
				while (st.hasMoreTokens())
					tokens.add(st.nextToken());
			} else
				tokens.add(tok);
		}

		if (this.lowercase)
			MedicalTrainer.toLowerCase(tokens);
		if (this.stopwordlist)
			tokens = MedicalTrainer.removeStopWords(tokens);
		if (this.stem)
			MedicalTrainer.simpleStem(tokens);
		if (this.stopwordlist)
			tokens = MedicalTrainer.removeStopWords(tokens);
		if (removeCommonCounters && StopWordList.acceptInput(tokens, this.map) != null)
			return null;
		if (ngram == 1)
			return tokens;
		if (this.minngram == -1)
			this.minngram = ngram;
		NgramTokenizer ntokenizer = new NgramTokenizer(null, ngram,
				this.minngram);
		ntokenizer.setTokens(tokens);
		ntokenizer.setCurroffset(0);
		while (ntokenizer.hasNext()) {
			terms.add((String) ntokenizer.next());
		}
		// return terms;
		return terms.size() > 0 ? terms : null;
	}

	public HashSet<String> returnUniqueNgramTermspace(String input, int ngram , 
			boolean removeCommonCounters) {
		LinkedHashSet<String> uniqueTerms = new LinkedHashSet<String>();
		List<String> tokens = this.returnTokens(input, ngram, removeCommonCounters);
		for (String toks : tokens) {
			uniqueTerms.add(toks);
		}

		return uniqueTerms.size() > 0 ? uniqueTerms : null;
	}

	public static void simpleStem(List<String> input) {
		Morphology mp = new Morphology();
		for (int i = 0; i < input.size(); i++) {
			// String orig = input.get(i);
			// System.out.println("As"+input.get(i)+"As");
			String stem = mp.stem(input.get(i));
			if (!input.get(i).equals(stem)) {
				// System.out.println(orig+"  "+ stem);
				input.set(i, stem);
			}
		}
	}

	/**
	 * Coversts the token to lower case. remove the stop words using the medical
	 * stop word list defined by me
	 * 
	 * @param csvFile
	 * @param lowage
	 *            : lowage filter
	 * @param upage
	 *            : high age filter
	 * @param agecol
	 *            : columne number in the csv file which contains age
	 * @param textcol
	 *            : column number in the csv file which contains the text
	 * @param ngram
	 *            : the ngram
	 * @return
	 */
	public HashSet<String> returnUniqueNgramTermSpace(int ngram) {

		CSVReader csvreader;
		LinkedHashSet<String> uniqueTerms = new LinkedHashSet<String>();
		try {

			csvreader = new CSVReader(new FileReader(this.filename));
			String[] nextLine = null;
			if (this.skipHeader)
				csvreader.readNext();
			while ((nextLine = csvreader.readNext()) != null) {
				Double age = Double.parseDouble(nextLine[this.agecol - 1]);
				if (age < this.lowageval || age > this.upageval)
					continue;
				if (nextLine[this.textcol - 1].length() <= 0)
					continue;
				String input = nextLine[this.textcol - 1];
				List<String> tokens = this.returnTokens(input, ngram, this.removeCommonCounters);
				for (String tok : tokens) {
					uniqueTerms.add(tok);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uniqueTerms.size() > 0 ? uniqueTerms : null;
	}

	public static List<String> ptbTokenizer(String input) {
		StringReader reader = new StringReader(input);
		LucenePTBTokenizer lptb = new LucenePTBTokenizer(reader);
		List<String> result = new ArrayList<String>();
		while (lptb.getPtbtokenizer().hasNext()) {
			CoreLabel label = (CoreLabel) lptb.getPtbtokenizer().next();
			result.add(label.word());
		}
		return result;
	}

	public static List<String> toLowerCase(List<String> input) {
		for (int index = 0; index < input.size(); index++) {
			input.set(index, input.get(index).toLowerCase());
		}
		return input;
	}

	public static List<String> removeStopWords(List<String> input) {
		return StopWordList.filterTokens(input, true, StopwordType.cchs);
	}

	public static Counter<String> addToCounter(Counter<String> cntr,
			List<String> tokens) {
		for (String tok : tokens) {
			cntr.incrementCount(tok);
		}
		return cntr;
	}

	public Counter<String> returnFreqDist(String string, int ngram,
			boolean tolowercase, boolean stem, Set<?> stopwordlist) {
		Counter<String> cntr = new IntCounter<String>();
		
		MedicalTrainer.addToCounter(cntr, this.returnTokens(string, ngram, false));
		return cntr;
	}

	public Counter<String> returnFreqDistOnSetOfNgrams(List<Record> record)
			throws NumberFormatException, IOException {
		Counter<String> result = new IntCounter<String>();
		for (int ngram : this.ngramsToGet) {
			result.addAll(this.returnFreqDist(ngram , record));
		}
		return result;
	}

	/**
	 * Returns the global FreqDist
	 * 
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
	public Counter<String> returnFreqDist(int ngramsToget, List<Record> records)
			throws NumberFormatException, IOException {
        Counter<String> cntr = new IntCounter<String>();
        if (records == null)
        	records = this.population;
		for (Record record : records) {
			List<String> tokens = this.returnTokens(record.getText(), ngramsToget, false);
			for (String tok : tokens)
				cntr.incrementCount(tok);
		}
		return cntr;
	}

	public static void printToFile(String filename, Counter<String> counter,
			int numDocs) {

		try {
			CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(
					filename)));
			String[] row = new String[4];
			for (String key : counter.keySet()) {
				row[0] = key;
				double val = counter.getCount(key);
				row[1] = new String("" + val);
				row[2] = new String("" + (val / numDocs));
				csvWriter.writeNext(row);

			}
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void evaluate() throws Exception {
		/*
		 * if (this.cltype.equals(ClassifierType.simplelogistic)) { Enumeration
		 * enm = ((SimpleLogistic)this.classifier).enumerateMeasures();
		 * while(enm.hasMoreElements()) System.out.println(enm.nextElement()); }
		 */
		Instances instances = CommonClassifierRoutines.removeAttributes(
				this.trainingSet, this.indicesToRemove);
		Evaluation eval = new Evaluation(instances);
		eval.crossValidateModel(this.classifier, instances,
				instances.numInstances(), new Random(1000));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));

	}

	public List<String> getDocIds() throws IOException {
		ArrayList<String> docs = new ArrayList<String>();

		CSVReader csvreader = new CSVReader(new FileReader(this.filename));
		if (this.skipHeader)
			csvreader.readNext();
		String[] row = null;
		while ((row = csvreader.readNext()) != null) {
			float age = Float.parseFloat(row[this.agecol - 1]);
			if (age < this.lowageval || age > this.upageval)
				continue;
			if (row.length < this.labelcol
					|| row[this.labelcol - 1].trim().length() <= 0)
				continue;
			if (!docs.contains(row[this.identifierCol - 1]))
				docs.add(row[this.identifierCol - 1]);
			else
				System.out
						.println("don't want you to be here method  : getDocIds");
		}
		return docs;

	}

	public List<MultinomialDocumentModel> populateModels(List<Record> records)
			throws IOException {
		java.util.Vector<MultinomialDocumentModel> models = new java.util.Vector<MultinomialDocumentModel>();

		for (int ngram : this.ngramsToGet) {
			MultinomialDocumentModel md = new MultinomialDocumentModel(
					new String("" + ngram));
			for (Record record : records) {
				md.addDocModel(record.getId(),
						this.returnTokens(record.getText(), ngram, this.removeCommonCounters),
							record.getTextcategory());
				}
			models.add(md);
		}

		return models;
	}

	public Instance testInstanceOnLanguageModel(List<Record> records,
			List<MultinomialDocumentModel> containers, WekaInstances instances,
			String docId) throws IOException {

		List<String> unitopics = new ArrayList<String>();
		for (MultinomialDocumentModel md : containers) {
			for (String topic : md.getTopics()) {
				if (!unitopics.contains(topic))
					unitopics.add(topic);
			}
		}
		Collections.sort(unitopics);

		for (Record record : records) {
			if (!docId.equals(record.getId()))
				continue;
			String text = record.getText();
			instances.initWorkingInstance();
			instances.setValueOfWorkingInstance("id", record.getId());
			instances.setValueOfWorkingInstance("age", record.getAge());

			for (MultinomialDocumentModel md : containers) {
				List<String> X = null;
				if (md.getModelName().equalsIgnoreCase("1"))
					X = this.returnTokens(text, 1, this.removeCommonCounters);
				else if (md.getModelName().equalsIgnoreCase("2"))
					X = this.returnTokens(text, 2,  this.removeCommonCounters);
				else if (md.getModelName().equalsIgnoreCase("3"))
					X = this.returnTokens(text, 3,  this.removeCommonCounters);
				for (String topic : unitopics) {
					String attname = topic + "_" + md.getModelName();
					instances.setValueOfWorkingInstance(attname,
									    md.getProbOfClassGivenDocument(X, topic, true));
				}
			}
			instances.setValueOfWorkingInstance("class",
					record.getTextcategory());
			Instance toreturn = instances.getWorkingInstance();
			instances.initWorkingInstance();
			return toreturn;
		}
		return null;
	}

	public List<Record> populateRecords() throws IOException {
		if (this.population == null)
			this.population = new ArrayList<Record>();
		CSVReader csvreader = new CSVReader(new FileReader(this.filename));
		if (this.skipHeader)
			csvreader.readNext();
		String[] row = null;
		HashSet<String> ids = new HashSet<String>();
		int specialId = 1;
		while ((row = csvreader.readNext()) != null) {
			float age = Float.parseFloat(row[this.agecol - 1]);
			if (age < this.lowageval || age > this.upageval)
				continue;
			// if labelcol is -1 or less than 0 then all rows of the csv file
			// will beprocessed
			if (this.labelcol > 0
					&& (row.length < this.labelcol || row[this.labelcol - 1]
							.trim().length() <= 0))
				continue;
			
			if (this.removeCommonCounters &&
					this.returnTokens(row[this.textcol - 1].trim(),
							1, this.removeCommonCounters) == null)
					continue;
			
			String id = row[this.identifierCol - 1].trim();
			Record record = null;
			if (!ids.contains(id))
				record = new Record(id);
			else {
				record = new Record(id + "-" + specialId);
				specialId++;
			}

			record.setAge(age);
			record.setText(row[this.textcol - 1].trim());
			if (this.labelcol > 0) 				
				record.setTextcategory(row[this.labelcol - 1].trim());
			else
				record.setTextcategory("NA");
			ids.add(record.getId());
			this.population.add(record);
		}
		return this.population;
	}

	/**
	 * the second value in the arraylistmultimap is the vector
	 * 
	 * @param records
	 * @param containers
	 * @param doctoexclude
	 * @return
	 */
	public List<Vector> mahoutTrainingSetOnLanguageModel(List<Record> records,
			List<MultinomialDocumentModel> containers, String doctoexclude) {

		// ArrayListMultimap<String, Object> output =
		// ArrayListMultimap.create();
		ArrayList<Vector> output = new ArrayList<Vector>();
		List<String> unitopics = new ArrayList<String>();
		for (MultinomialDocumentModel md : containers) {
			for (String topic : md.getTopics()) {
				if (!unitopics.contains(topic))
					unitopics.add(topic);
			}
		}
		Collections.sort(unitopics);

		for (Record record : records) {

			if (doctoexclude != null && record.getId().equals(doctoexclude))
				continue;
			/*
			 * cardinality of the densevector is plus 1 to include the class
			 * label
			 */
			Vector dv = new DenseVector(
						    (containers.size() * unitopics.size()) + 1);
			double[] val = new double[(containers.size() * unitopics.size()) + 1];
			// output.put(record.getId(), record.getAge());
			// output.put(record.getId(), dv) ;
			int index = 0;
			for (MultinomialDocumentModel md : containers) {
				List<String> X = null;
				if (md.getModelName().equalsIgnoreCase("1"))
					X = this.returnTokens(record.getText(), 1,  this.removeCommonCounters);
				else if (md.getModelName().equalsIgnoreCase("2"))
					X = this.returnTokens(record.getText(), 2,  this.removeCommonCounters);
				else if (md.getModelName().equalsIgnoreCase("3"))
					X = this.returnTokens(record.getText(), 3,  this.removeCommonCounters);
				for (String topic : unitopics) {
				    val[index++] = md.getProbOfClassGivenDocument(X, topic, true);
				}
			}
			float label = unitopics.indexOf(record.getTextcategory());
			val[index++] = label;
			dv.assign(val);
			output.add(dv);
		}
		return output;
	}

	public WekaInstances trainingOnLanguageModel(List<Record> records,
			List<MultinomialDocumentModel> containers, String doctoexclude)
			throws IOException {

		WekaInstances instances = new WekaInstances(this.datasetname,
				new ArrayList<Attribute>(), 0);
		int index = 0;
		instances.addStringAttribute("id", index++);
		instances.insertAttributeAt(new Attribute("age"), index++);

		List<String> unitopics = new ArrayList<String>();
		for (MultinomialDocumentModel md : containers) {
			for (String topic : md.getTopics()) {
				if (!unitopics.contains(topic))
					unitopics.add(topic);
			}
		}
		Collections.sort(unitopics);

		for (MultinomialDocumentModel md : containers) {
			for (String topic : unitopics) {
				instances
						.insertAttributeAt(
								new Attribute(topic + "_" + md.getModelName()),
								index++);
			}
		}

		instances.addClassAttribute("class", unitopics);
		// int count = 0;
		for (Record record : records) {
			instances.initWorkingInstance();
			if (doctoexclude != null && record.getId().equals(doctoexclude))
				continue;

			instances.setValueOfWorkingInstance("id", record.getId());
			instances.setValueOfWorkingInstance("age", record.getAge());
			for (MultinomialDocumentModel md : containers) {
				List<String> X = null;
				if (md.getModelName().equalsIgnoreCase("1"))
					X = this.returnTokens(record.getText(), 1,  this.removeCommonCounters);
				else if (md.getModelName().equalsIgnoreCase("2"))
					X = this.returnTokens(record.getText(), 2,  this.removeCommonCounters);
				else if (md.getModelName().equalsIgnoreCase("3"))
					X = this.returnTokens(record.getText(), 3,  this.removeCommonCounters);
				for (String topic : unitopics) {
					String attname = topic + "_" + md.getModelName();
					// System.out.println(count);
					instances.setValueOfWorkingInstance(attname,
									    md.getProbOfClassGivenDocument(X, topic, true));
				}

			}
			// count++;
			// System.out.println( topics);
			// System.out.println( record.getTextcategory());
			instances.setValueOfWorkingInstance("class",
					record.getTextcategory());
			instances.addWorkingInstance();
		}

		return instances;
	}
	
	public void generateRuleFile() throws IOException {
		HashMap<StringCounter, Integer> cntfreq= new HashMap<StringCounter, Integer>();
		CSVWriter cv = new CSVWriter(new FileWriter(this.dumpfile));
		HashMap<StringCounter, Record> cntToRecord = new HashMap<StringCounter, Record>();
		//List<Counter<String>> allcntrs = new ArrayList<Counter<String>>();
    	for (Record record : this.population) {
    		List<String> toks = this.returnTokens(record.getText(), 1, 
        			false);
    		StringCounter cnt = new StringCounter();
    		for (String tok : toks) {
    			cnt.incrementCount(tok);
    		}
    		if (cntfreq.containsKey(cnt)) 
    			cntfreq.put(cnt, cntfreq.get(cnt) + 1);    		
    		else  {
    			cntfreq.put(cnt,1);
    			cntToRecord.put(cnt, record);
    		}
    	}
    	String[] row = new String[2];
    	for (Counter<String> cnt : cntfreq.keySet()) {
    		row[0] = StringUtils.join(this.returnTokens(cntToRecord.get(cnt).getText(), 1, 
        			false));
    		row[1] = ""+cntfreq.get(cnt);
    		cv.writeNext(row);
    	}
    	cv.close();
	}
	
    public void doManualClassification() throws IOException {
    	CSVWriter cv = new CSVWriter(new FileWriter(this.dumpfile));
    	String[] row = new String[4];
    	for (Record record : this.population) {
    		List<String> toks = this.returnTokens(record.getText(), 1, 
    			false);
    		row[0] = record.getId();
			row[1] = record.getText();
			row[2] = ""+record.getAge();
			
    		String cat = StopWordList.acceptInput(toks, this.map);
    		if (cat != null) 
    			row[3] = cat;
    		else
    			row[3] = "NA";
			cv.writeNext(row);
		}
    	cv.close();
    }
    
    public void doWekaClassificationWithSampling(int samplesize, int numtime) 
    throws Exception {
    	
    	if (samplesize == -1)
    		samplesize = this.population.size();
    	for (int i = 0; i < numtime; i++) {
			List<?> objs = Sampling.sampleWithoutReplacement(
					this.population, samplesize, false,false , new ArrayList<Object>());
			ArrayList<Record> records =  (ArrayList<Record>) objs; // not safe here be
			Counter<String> termspace = this.returnFreqDistOnSetOfNgrams(records);
			if (this.removesinglecount)
				termspace = KLDivergence.removeSingleCounteTerms(termspace);
			 WekaInstances trainingSet = this.returnIndicatorVectorOfTermSpace(records,
					termspace.keySet(), true, this.removeCommonCounters);
			double miss = CommonClassifierRoutines.leaveOneOutCrossValidation(
					this.classifier, trainingSet, this.indicesToRemove,
					this.indicesTodump, this.classifieroptions, this.dumpfile);
			System.out.println(miss+ " ," + samplesize );
			if (this.dumparff != null) {
				WekaRoutines.dumpArff(this.dumparff, trainingSet);
			}
		}
    	
    }
    
	public void doLanguageModelWithSampling(int samplesize, int numtime)
			throws Exception {
		ArrayList<Float> result = new ArrayList<Float>();
		CSVWriter cv  = null;
		if (this.dumpfile != null)
			cv = new CSVWriter(new FileWriter(this.dumpfile));
		if (this.samplesize == -1)
			this.samplesize = this.population.size();
		for (int i = 0; i < numtime; i++) {
			List<?> objs = Sampling.sampleWithoutReplacement(
					this.population, this.samplesize, false,false , new ArrayList<Object>());
			ArrayList<Record> records =  (ArrayList<Record>) objs; // not safe here be
		
        	String[] row = new String[2];
			row[0] = "" + records.size();
			row[1] = ""+this.doLanguageModelClassification(records, cv);
		//	cv.writeNext(row);
		}
		if (cv != null)
		cv.close();
		//System.out.println(result);
	}

	public void debugLanguageModel(List<Record> records, String fileName)
			throws IOException {
		List<MultinomialDocumentModel> container = this.populateModels(records);
		FileWriter fw = new FileWriter(new File(fileName));

		for (MultinomialDocumentModel md : container) {
			// fw.write(md.toString());
			fw.write(md.printGlobalCounter(false));
		}

		fw.close();
	}

    public int doTournament(Vector input , List<String> topics, int nummodels) {
	int numtopics = topics.size();
        int[] modelwinners = new int[nummodels];
	int presentmodelnum = 0;
	int topicindex = 0;
	double max = input.get(0);
	int winner = 0;
	for (int count = 0; count < numtopics * nummodels; count++ ) {
	   
	    if(input.get(count) > max) {
		max = input.get(count);
		winner = topicindex;
	    }
	    
	    if (((count + 1) % numtopics) == 0){
	    	topicindex = 0;
	    	max = 0;
	    	modelwinners[presentmodelnum] = winner;
	    	presentmodelnum++;		
	    }
	    else
		topicindex++;   
	}
	IntCounter<Integer> labelcounter = new IntCounter<Integer>();
	for(int labelnum : modelwinners) {
	    labelcounter.incrementCount(labelnum);
        }
	if (labelcounter.getCount(labelcounter.argmax()) == labelcounter.getCount(labelcounter.argmin()))
	    return modelwinners[0];
	return labelcounter.argmax();
	
    }
    
	public float doLanguageModelClassification(List<Record> records,
			CSVWriter csvwriter) throws Exception {
		
		List<MultinomialDocumentModel> container = this.populateModels(records);
		for (MultinomialDocumentModel md : container) {
			md.removeSingleCountTerms();
		}
	//	container.get(0).printWordDistForEachTopic();
		int missclassifier = 0;

		int count = 0;
		for (Record record : records) {
			String docid = record.getId();
			for (MultinomialDocumentModel md : container) {
				md.storeAndRemoveDoc(docid);
			}

			List<String> unitopics = new ArrayList<String>();
			for (MultinomialDocumentModel md : container) {
				for (String topic : md.getTopics()) {
					if (!unitopics.contains(topic))
						unitopics.add(topic);
				}
			}
			Collections.sort(unitopics);
                        int numfeatures = unitopics.size() * container.size();
			if (ClassifierType.isWekaClassifer(this.cltype.toString())) {
				WekaInstances training = this.trainingOnLanguageModel(records,
						container, docid);
				AbstractClassifier classifier = CommonClassifierRoutines
						.trainOnInstances(this.classifier, training, new int[] {
								0, 1 }, this.cloptions);
				training.clear();
				Instance testinstance = this.testInstanceOnLanguageModel(
						records, container, training, docid);
				if (!CommonClassifierRoutines.testInstances(classifier,
						testinstance, training, new int[] { 0, 1 })) {
					missclassifier++;
					System.out.println(training.classAttribute());
					System.out.println("missclassified " + docid + " "
							+ testinstance);
				}
			} else if (this.cltype.equals(ClassifierType.mahoutOnlineLogisticRegression)){
				// System.out.println("contin suize "+container.size());
				ArrayList<Record> testrecord = new ArrayList<Record>();
				testrecord.add(record);
				List<Vector> testset = this.mahoutTrainingSetOnLanguageModel(
						testrecord, container, null);
				List<Vector> trainset = this.mahoutTrainingSetOnLanguageModel(
						records, container, docid);

				
				double[] predictedProb = new double[1];
				Vector test = testset.get(0);
				int result = LogisticRegression.test(test, true,
						LogisticRegression.trainOnVector(trainset, numfeatures ,
										 unitopics.size(), true,0, numfeatures -1, 10), false, 0, 
                                                                                     numfeatures -1, predictedProb);
				if (result == 0) {
					missclassifier++;
					if (csvwriter != null) {
						// + 1 for  missclassfication
						// + 1 for predicted class probability +1 for id
					    // also has the actual class label at the end
						String[] output = new String[test.size()  + 1 + 1
								+ 1];
						short ind = 0;
						output[ind++] = record.getId();
						output[ind++] = "0";
						output[ind++] = "" + predictedProb[0];
						for (int index = 0; index < test.size() ; index++)
							output[ind++] = "" + test.get(index);
						csvwriter.writeNext(output);
					}
				} else {
					// output -1 for class label in vector, + 1 for
					// missclassfication
					// + 1 for predicted class probability +1 for id
					String[] output = new String[test.size() + 1  + 1 + 1];
					if (csvwriter != null) {
						short ind = 0;
						output[ind++] = record.getId();
						output[ind++] = "1";
						output[ind++] = "" + predictedProb[0];
						for (int index = 0; index < test.size() ; index++)
							output[ind++] = "" + test.get(index);
						csvwriter.writeNext(output);
					}
				}
			}
			else if (this.cltype.equals(ClassifierType.tournamentmodel)) {
				ArrayList<Record> testrecord = new ArrayList<Record>();
				testrecord.add(record);
			    List<Vector> testset = this.mahoutTrainingSetOnLanguageModel(
						testrecord, container, null);
				Vector test = testset.get(0);
				double predicted = this.doTournament(test,
						unitopics, this.ngramsToGet.size());
				if (predicted != test.get(test.size() -1)) {
				/*
				    List<String> generatedWords = container.get(0).
				    generateWords(this.returnTokens(record.getText(), 1,
							this.removeCommonCounters),
							unitopics.get((int) test.get(test.size() -1))
							, 1000, 4000);
					
					RandomData randomData = new RandomDataImpl(); 
					Record re= new Record(record.getId()+"-"+randomData.nextInt(0, 9999999));
					re.setAge(this.lowageval);
					re.setText(StringUtils.join(generatedWords, " "));
					re.setTextcategory(unitopics.get((int) test.get(test.size() -1)));
					this.population.add(re);
				*/	
					missclassifier++;
					String[] output = new String[test.size() + 1  + 1];
					if (csvwriter != null) {
						short ind = 0;
						output[ind++] = record.getId();
						output[ind++] = "0";						
						for (int index = 0; index < test.size(); index++)
							output[ind++] = "" + test.get(index);
						csvwriter.writeNext(output);
					}
				}
				else {
					String[] output = new String[test.size()  + 1  + 1];
					if (csvwriter != null) {
						short ind = 0;
						output[ind++] = record.getId();
						output[ind++] = "1";						
						for (int index = 0; index < test.size() ; index++)
							output[ind++] = "" + test.get(index);
						csvwriter.writeNext(output);
					}
				}
					
			}
			for (MultinomialDocumentModel md : container) {
				md.restoreDocAndCounters(docid);
			}
			count++;
		}
		System.out.println(missclassifier + ", " + records.size());
		return (float) missclassifier / records.size();
	}
}
