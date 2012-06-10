/**
 * 
 */
package tokenizers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;

import specialstruct.StringCounter;

import au.com.bytecode.opencsv.CSVReader;

import cchs.CounterComparison;
import cchs.FeedCategories;


import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.IntCounter;

/**
 * @author ashwani
 *
 */
public final class StopWordList {
	
	private static Pattern englishword = Pattern.compile("[a-zA-Z][a-zA-Z]+");
	
	public StopWordList() {
		
	}
	public static Set<?> getEnglishStopWordList() {
		return StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	}
	
	private static List<String> medicalWords=   Arrays.asList(
		      "a", "an",  "are", "as", "at", "be",  
		       "is", "it", "that", "the", "their", "then", "there", "these",
		      "they", "this", "to", "was", "will",",",".",";","-", "--","Voiding",
		      "voiding","(",")","+","/","hr","hours","q","hrs","x","?","ounces"
		      ,"oz","ozs", "d","was","be","ounce","quot","quot;");
	
	public static Set<?> getCchsStopWordList(Version version) {
		
		
		CharArraySet stopSet = new CharArraySet(version , 
				medicalWords.size(), false);
		stopSet.addAll(medicalWords);
		return stopSet;
	}
	
	public static Set<String> getCchsStopWordList() {
		
		HashSet<String> stops =new HashSet<String>();
		stops.addAll(medicalWords);
		return stops;
	}

	public static List<String> filterTokens(List<String> input, 
			boolean removeNonEnglish, StopwordType type) {
		Set<String> stopset =  null;
		switch(type) {
		case cchs:
			stopset = StopWordList.getCchsStopWordList();
		}
		
		ArrayList<String> result  = new ArrayList<String>();
		for (String in : input) {
			if (removeNonEnglish && 
					!englishword.matcher(in).matches()) 
				continue;
				
			if (!stopset.contains(in))
				result.add(in);
		}
		return result;
	}
	public static void addToMedicalStopList(String toAdd) {
		StopWordList.medicalWords.add(toAdd);
	}
	
	public static void removeFromMedicalStopList(String toRemove) {
		StopWordList.medicalWords.remove(toRemove);
	}
	
	public static String acceptInput(List<String> input, 
			HashMap<StringCounter, FeedCategories> map) {
		Counter<String> inputCounter = new IntCounter<String>();
		String category = null;
		for (String in : input)
			inputCounter.incrementCount(in);
		for (Counter<String> cnt :  map.keySet()) {
			if (CounterComparison.areEqual(cnt, inputCounter))
				return map.get(cnt).toString();
		}
		return category;
	}
		
	/**
	 * the rule file must have phrase in the first column and the labelled 
	 * category in third column.
	 * @param rulefile
	 * @param skipHeader
	 * @return
	 * @throws IOException
	 */
	public static HashMap<StringCounter, FeedCategories>  getCounterToFilter(
			String rulefile, boolean skipHeader) throws IOException {
		HashMap<StringCounter, FeedCategories> map = 
			new HashMap<StringCounter, FeedCategories>();
		
		CSVReader reader = new CSVReader(new FileReader(rulefile));
		
		if (skipHeader)
			reader.readNext();
		
		String[] row = null;
		while ((row = reader.readNext()) != null) {
			String text = row[0];
			if (row[2] == null)
				continue;
			String category = row[2].trim();
			if (category.length() <= 0)
				continue;
			StringCounter cntr = new StringCounter();
			for(String s : text.split("\\s+")) {
				cntr.incrementCount(s);
			}
			map.put(cntr, FeedCategories.getValueOf(category));
		}
		return map;
	}
    
}
