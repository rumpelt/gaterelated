/**
 * 
 */
package tokenizers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.util.Version;

/**
 * @author ashwani
 *
 */
public final class StopWordList {
	public StopWordList() {
		
	}
	public static Set<?> getEnglishStopWordList() {
		return StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	}
	static List<String> medicalWords=   Arrays.asList(
		      "a", "an",  "are", "as", "at", "be",  
		       "is", "it", "that", "the", "their", "then", "there", "these",
		      "they", "this", "to", "was", "will",",",".",";","-", "--","Voiding",
		      "voiding");
	public static Set<?> getMedicalStopWordList(Version version) {
		
		
		CharArraySet stopSet = new CharArraySet(version , 
				medicalWords.size(), false);
		stopSet.addAll(medicalWords);
		return stopSet;
	}
	
	public static void addToMedicalStopList(String toAdd) {
		StopWordList.medicalWords.add(toAdd);
	}
	
	public static void removeFromMedicalStopList(String toRemove) {
		StopWordList.medicalWords.remove(toRemove);
	}
}
