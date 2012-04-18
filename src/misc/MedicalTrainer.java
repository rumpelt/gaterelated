/**
 * 
 */
package misc;

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
import java.util.Set;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.util.Version;
import org.junit.Test;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Distribution;
import edu.stanford.nlp.stats.IntCounter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import tokenizers.LucenePTBTokenizer;
import tokenizers.NgramTokenizer;
import tokenizers.StopWordList;
import weka.WekaInstances;
import weka.core.Attribute;

/**
 * a set of very specific routines for my work on medical data.
 * @author ashwani
 *
 */
public class MedicalTrainer {
    private String identifierName;
	private int textcol;
	private int identifierCol;
	private int agecol;
	private int labelcol;
	private float lowageval;
	private float upageval;
	private List<Integer> ngramsToGet;
	private Set<?> stopwordlist = StopWordList.getMedicalStopWordList(Version.LUCENE_35) ;
	private boolean stem;
	private boolean lowercase;
	private boolean removestopword;
	private String filename;
	List<String> classvalues;
	private String datasetname;
	private boolean skipHeader;
	public MedicalTrainer() {
		this.classvalues = FeedCategories.returnAllValues();
		stopwordlist = StopWordList.getMedicalStopWordList(Version.LUCENE_35) ;
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
				Set<String> terms = MedicalTrainer.returnUniqueNgramTermspace(text, ngram, 
						this.lowercase,this.stem, this.stopwordlist);
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
	
	public  Counter<String> returnFreqDistOnSetOfNgrams(String filename ,
			int textcol,int agecol,float lowvalue, float upvalue, List<Integer> ngramsToget,
			boolean tolowercase, boolean stem, Set<?> stopwordlist, boolean skipHeader	) throws NumberFormatException, IOException {
		Counter<String> result = new IntCounter<String>();
		for (int ngram : ngramsToget) {
			result.addAll(MedicalTrainer.returnFreqDist(ngram));
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
	public  Counter<String> returnFreqDist( int ngramsToget)	throws NumberFormatException, IOException {
		
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
	
	public WekaInstances returnRelativeProbabilityWeight(HashMap<Integer, Counter<String>> counters, boolean addunknownClass)
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
			if (!addunknownClass || this.lclass == null ) {
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
}
