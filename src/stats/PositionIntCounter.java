/**
 * 
 */
package stats;

import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.stats.IntCounter;

/**
 * @author ashwani
 *
 */
public class PositionIntCounter extends IntCounter<String> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7291532286623685108L;
	private HashMap<Integer, String> postionToKey = new HashMap<Integer, String>();
	
	public PositionIntCounter() {
		super();
		this.postionToKey = new HashMap<Integer, String>();
	}
	
	public void addAll(List<String> input) {
		
		for (int count = 0; count < input.size(); count++) {
			String key = input.get(count);
			super.incrementCount(key);
			this.postionToKey.put(count, key);
		}
	}
	
	/**
	 * Given the current position you want to get the key which was numItems 
	 * before this iterm. For example for the following input
	 *  "I  am the best" 
	 * If currentPos is 2 (that is key "the") then invoking this function
	 * with 1 as numItemsBefore will fetch key "am". 
	 * @param currentPos
	 * @param beforePos
	 * @return
	 */
	public String getPreviousKey(int currentPos , int numItemsBefore) {
		return this.postionToKey.get(currentPos -  numItemsBefore);
	}
	
	/**
	 * Given the current position you want to get the key which was numItems 
	 * after this iterm. For example for the following input
	 *  "I  am the best" 
	 * If currentPos is 2 (that is key "the") then invoking this function
	 * with 1 as numItemsAfter will fetch key "best". 
	 * @param currentPos
	 * @param beforePos
	 * @return
	 */
	public String getAfterkey(int currentPos, int numItemsAfter) {
		return this.postionToKey.get(currentPos + numItemsAfter);
	}
	
	/**
	 * Counts number of times two keys co occur at specifc distance
	 * 
	 * @param key : 
	 * @param previousKey 
	 * @param distance : previouskey should occur distance offset before key.
	 * for example if distance is 1 then previous key must occure just before
	 * key. 
	 * @return Returns total such occurence	 of pairing at specified  distance
	 */
	public double countOccurence(String key, String previousKey,int distance) {
		double count = 0;
		for (int index=0 ; index < this.postionToKey.size(); index++) {
			if (this.postionToKey.get(index).equals(key) &&
					 this.postionToKey.get(index -distance  ) != null && 
	  this.postionToKey.get(index -distance ).equals(previousKey))
					count++;
			
		}
		return count;
	}
}
