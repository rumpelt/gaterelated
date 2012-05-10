/**
 * 
 */
package tokenizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;

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
}
