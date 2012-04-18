package search;



import java.util.HashMap;

import gate.FeatureMap;
import gate.creole.annic.Constants;
import gate.creole.annic.SearchException;
import gate.creole.annic.lucene.LuceneSearcher;

public class AnnicSearch {
	private FeatureMap features;
	private static HashMap parameters;
	private static LuceneSearcher lucenesearcher;
	public AnnicSearch() {
		this.lucenesearcher = new LuceneSearcher();
	}
	public static void main(String[] args) {
		search();
	}
	public static void search() {
		lucenesearcher = new LuceneSearcher();
		parameters = new HashMap<String,Object>();
		parameters.put(Constants.INDEX_LOCATION_URL, "c:\\xyz\\index1");
		try {
			System.out.println(lucenesearcher.search("obese", parameters));
		} catch (SearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
