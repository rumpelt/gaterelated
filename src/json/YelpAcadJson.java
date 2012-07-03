/**
 * 
 */
package json;

import gate.creole.annic.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import tokenizers.LucenePTBTokenizer;
import tokenizers.StopWordList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author ashwani
 *
 */
public class YelpAcadJson {
	private static Gson gson = new Gson();
	public static class YelpAnalyzer extends Analyzer {

		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String, java.io.Reader)
		 */
    	private Version versionNumber;
    	public YelpAnalyzer(Version versionNumber) {
    		this.versionNumber  = versionNumber;
    	}
		@Override
		public TokenStream tokenStream(String arg0, Reader arg1) {
		    return new StopFilter(this.versionNumber , 
					new PorterStemFilter(new LowerCaseTokenizer(this.versionNumber , arg1)) , 
					StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		
		}
		@Override
		public TokenStream reusableTokenStream(String fieldName, Reader reader) throws
		IOException{
			Tokenizer tokenizer = (Tokenizer)getPreviousTokenStream();
			if (tokenizer == null) {
				return tokenStream(fieldName, reader);
			}
			else
				tokenizer.reset(reader);
			return tokenizer;
		}
    	
    }
	
	private static JsonParser parser = new JsonParser();
	public static class YelpObject {
		private String type = null;
		private String business_id = null;
		private String full_address = null;
		private String name = null;
		private  ArrayList<String> neighborhoods = new ArrayList<String>();
		private String city = null;
		private String state = null;
		private Float latitude = null;
		private Float longitude = null;
		private Float stars = null;
		private Integer  review_count = null;
		private String photourl=null;
	        private ArrayList<String> categories = new ArrayList<String>();
		private Boolean open = null;
		private ArrayList<String> schools = new ArrayList<String>();
		private String url=null;
		private String user_id=null;
		private String text=null;
		private Float average_stars = null;
		private String date = null;
		private HashMap<String, Integer> votes = new HashMap<String, Integer>();
		
		public YelpObject() {
		}
		
	}
	
	public static void indexYelpObject(String filename, String indexdir, boolean newindex) throws IOException, ParseException {
		FileReader fr = new FileReader(new File(filename));
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		Analyzer analyzer = new YelpAnalyzer(Version.LUCENE_36);
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36, analyzer);
		if (newindex)
			conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		else
			conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
		SimpleFSDirectory directory = new SimpleFSDirectory(new File(indexdir));
		IndexWriter indexwriter = new IndexWriter(directory, conf);
	
		while((line = br.readLine()) != null) {
			Document doc = new Document();
			YelpObject yelpobject = getYelpObject(line);
			doc.add(new Field("type", false, yelpobject.type,Field.Store.YES, Field.Index.NO, Field.TermVector.NO) );
			if (yelpobject.business_id != null)
				doc.add(new Field("business_id", false, yelpobject.business_id ,Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
			if (yelpobject.full_address != null)
				doc.add(new Field("full_address", false, yelpobject.full_address ,Field.Store.NO, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
			if (yelpobject.name != null)
			    doc.add(new Field("name", false, yelpobject.name ,Field.Store.NO, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
			if(yelpobject.neighborhoods.size() >0  ) {
				for (String neig :yelpobject.neighborhoods )
				doc.add(new Field("neighborhoods", false, neig ,Field.Store.NO,Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
			}
			if (yelpobject.city != null)
				doc.add(new Field("city", false, yelpobject.city ,Field.Store.NO, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
			if (yelpobject.state != null)
				doc.add(new Field("state", false, yelpobject.state ,Field.Store.NO, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
			if (yelpobject.latitude != null) 
				doc.add(new NumericField("latitude", Field.Store.YES, false).setFloatValue(yelpobject.latitude));
			if (yelpobject.longitude != null) 
				doc.add(new NumericField("longitude", Field.Store.YES, false).setFloatValue(yelpobject.longitude));
			if (yelpobject.stars != null) 
				doc.add(new NumericField("stars", Field.Store.YES, false).setFloatValue(yelpobject.stars));
			if (yelpobject.review_count != null) 
				doc.add(new NumericField("review_count", Field.Store.YES, false).setIntValue(yelpobject.review_count));
			if (yelpobject.photourl != null) 
				doc.add(new Field("photourl", false, yelpobject.photourl ,Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
			if(yelpobject.categories.size() >0  ) {
				for (String cat :yelpobject.categories )
				doc.add(new Field("categories", false, cat ,Field.Store.NO,Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
			}
					
			if (yelpobject.open != null) 
		            doc.add(new Field("open", false, yelpobject.open.toString() ,Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
			if(yelpobject.schools.size() >0  ) {
				for (String sch :yelpobject.schools )
				doc.add(new Field("schools", false, sch ,Field.Store.NO,Field.Index.ANALYZED_NO_NORMS, Field.TermVector.YES));
			}
			if (yelpobject.url != null) 
				doc.add(new Field("url", false, yelpobject.url ,Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
			if (yelpobject.user_id != null) 
				doc.add(new Field("user_id", false, yelpobject.user_id ,Field.Store.YES, Field.Index.NO, Field.TermVector.NO));
			if (yelpobject.text != null) 
				doc.add(new Field("text", false, yelpobject.text ,Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));

			if (yelpobject.average_stars != null) 
	                    doc.add(new NumericField("average_stars", Field.Store.YES, false).setFloatValue(yelpobject.average_stars));

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			if (yelpobject.date != null)  {
				Date dt = formatter.parse(yelpobject.date);
				doc.add(new NumericField("date", Field.Store.YES, false).setLongValue(dt.getTime()));
			}
			if(yelpobject.votes.size() >0  ) {
				for (String key : yelpobject.votes.keySet())
					doc.add(new NumericField(key, Field.Store.YES, false).setIntValue(yelpobject.votes.get(key)));
			}
			indexwriter.addDocument(doc);
		}
		indexwriter.close();
	}
	
	public static YelpObject getYelpObject(String input) {
	    // System.out.println(input);
		 JsonObject jobject = parser.parse(input).getAsJsonObject();
		 YelpObject yelpobject = gson.fromJson(jobject, YelpObject.class);
		 return yelpobject;
	}
	
	public static void main(String[] args) {
	    String jsonfile = args[0];
            String indexDir = args[1];
            try {
                indexYelpObject(jsonfile, indexDir, true);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
	}
	
}
