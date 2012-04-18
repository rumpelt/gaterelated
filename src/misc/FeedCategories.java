/**
 * 
 */
package misc;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author ashwani
 *
 */
public  enum FeedCategories {
	BREAST_AND_NO_INFORMATION_ON_CEREALS("breast and no information on cereals"),
	BOTTLE_AND_NO_INFORMATION_ON_CEREALS("bottle and no information on cereals"),
	BREAST_AND_BOTTLE_AND_NO_INFORMATION_ON_CEREALS("breast and bottle and no information on cereals"),
	BREAST_AND_BOTTLE_AND_JUICES_AND_NO_INFORMATION_ON_CEREALS("breast and bottle and juices and no information on cereals"),
	BREAST_AND_BOTTLE_AND_JUICES_AND_FRUITS_AND_NO_INFORMATION_ON_CEREALS("breast and bottle and juices and fruits and no information on cereals"),
	
	BREAST_AND_CEREALS("breast and cereals"),
	BOTTLE_AND_CEREALS("bottle and cereals"),
	BREAST_AND_BOTTLE_AND_CEREALS("breast and bottle and cereals"),
	BREAST_AND_BOTTLE_AND_JUICES_AND_CEREALS("breast and bottle and juices and cereals"),
	BREAST_AND_BOTTLE_AND_JUICES_AND_FRUITS__CEREALS("breast and bottle and juices and fruits and cereals"),
	
	
	BREAST_AND_NO_CEREALS("breast and no cereals"),
	BOTTLE_AND_NO_CEREALS("bottle and no cereals"),
	BREAST_AND_BOTTLE_AND_NO_CEREALS("breast and bottle and no cereals"),
	BREAST_AND_BOTTLE_AND_JUICES_NO_CEREALS("breast and bottle and juices and no cereals"),
	BREAST_AND_BOTTLE_AND_JUICES_AND_FRUITS_AND_NO_CEREALS("breast and bottle and juices and fruits and no cereals"),
	
	BREAST_AND_JUICES("breast and juices"),
	BOTTLE_AND_JUICES("bottle and juices"),
	BOTTLE_AND_CEREALS_AND_FRUITS("bottle and cereals and fruits"),
	BOTTLE_AND_CEREALS_AND_JUICES("bottle and cereals and juices"),
	BOTTLE_AND_CEREALS_AND_JUICES_AND_FRUITS("bottle and cereals and juices and fruits"),
	BOTTLE_AND_CEREALS_AND_NO_JUICES("bottle and cereals and no juices"),
	BOTTLE_AND_NO_CEREALS_AND_NO_JUICES("bottle and no cereals and no juices"),
	BOTTLE_AND_NO_JUICES("bottle and no juices"),
	
	UNKNOWN("UNKNOWN");
	
	private String categoryname;
	
	private FeedCategories(String categoryName) {
		this.categoryname = categoryName;
	}
	
	public String toString() {
		return this.categoryname;
	}
	
	public static FeedCategories getValueOf(String value) {
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(value);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			sb.append(tok.toUpperCase());
			sb.append("_");
		}
		sb.deleteCharAt(sb.length()-1);
		return valueOf(sb.toString());
	}
	/**
	 * 
	 * @return
	 */
	public static  List<String> returnAllValues() {
		Vector<String> allvals = new Vector<String>();
		for(FeedCategories f : FeedCategories.values()) {
			allvals.add(f.toString());
		}
		return allvals;
	}
}
