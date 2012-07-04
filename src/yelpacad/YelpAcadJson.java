/**
 * 
 */
package yelpacad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntDocValuesField;
import org.apache.lucene.document.LongDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.document.Field;
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
		 * @see org.apache.lucene.analysis.nalyzer#tokenStream(java.lang.String, java.io.Reader)
		 */
    	private Version versionNumber;
    	public YelpAnalyzer(Version versionNumber) {
    		this.versionNumber  = versionNumber;
    	}
	
		/* (non-Javadoc)
		 * @see org.apache.lucene.analysis.Analyzer#createComponents(java.lang.String, java.io.Reader)
		 */
		@Override
		protected TokenStreamComponents createComponents(String fieldname,
				Reader reader) {
			// TODO Auto-generated method stub
			Tokenizer tokenizer = new LowerCaseTokenizer(this.versionNumber , reader);
			TokenStream tokenstream = new StopFilter(this.versionNumber , 
					new PorterStemFilter(tokenizer) , 
					StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			
			return new Analyzer.TokenStreamComponents(tokenizer ,tokenstream);
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
		Analyzer analyzer = new YelpAnalyzer(Version.LUCENE_40);
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		if (newindex)
			conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		else
			conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
		MMapDirectory directory = new MMapDirectory(new File(indexdir));
		IndexWriter indexwriter = new IndexWriter(directory, conf);
	
		while((line = br.readLine()) != null) {
			Document doc = new Document();
			YelpObject yelpobject = getYelpObject(line);
			doc.add(new StringField("type", yelpobject.type,Field.Store.NO));
			if (yelpobject.business_id != null)
				doc.add(new StringField("business_id", yelpobject.business_id ,Field.Store.NO));
			if (yelpobject.full_address != null)
				doc.add(new TextField("full_address", yelpobject.full_address ,Field.Store.NO));
			if (yelpobject.name != null)
			    doc.add(new StringField("name", yelpobject.name ,Field.Store.NO));
			if(yelpobject.neighborhoods.size() >0  ) {
				for (String neig :yelpobject.neighborhoods ) {
					if (doc.getField("neighborhoods") == null)
						doc.add(new TextField("neighborhoods",neig ,Field.Store.NO));
					else {
						IndexableField f = doc.getField("neighborhoods");
						String s = f.stringValue();
						doc.removeField("neighborhoods");
						doc.add(new TextField("neighborhoods",s + " "+ neig ,Field.Store.NO));
					}
				}
			}
			if (yelpobject.city != null)
				doc.add(new StringField("city", yelpobject.city ,Field.Store.NO));
			if (yelpobject.state != null)
				doc.add(new StringField("state", yelpobject.state ,Field.Store.NO));
			if (yelpobject.latitude != null) 
				doc.add(new FloatField("latitude", yelpobject.latitude, Field.Store.NO));
			if (yelpobject.longitude != null) 
				doc.add(new FloatField("longitude", yelpobject.longitude, Field.Store.NO));
			if (yelpobject.stars != null) 
				doc.add(new FloatDocValuesField("stars",yelpobject.stars));
			if (yelpobject.review_count != null) 
				doc.add(new IntDocValuesField("review_count", yelpobject.review_count));
			if (yelpobject.photourl != null) 
				doc.add(new StringField("photourl", yelpobject.photourl ,Field.Store.NO));
			if(yelpobject.categories.size() >0  ) {
				for (String cat :yelpobject.categories ) {
					if (doc.getField("categories") == null)
						doc.add(new TextField("categories",cat ,Field.Store.NO));
					else {
						IndexableField f = doc.getField("categories");
						String s = f.stringValue();
						doc.removeField("categories");
						doc.add(new TextField("categories",s + " "+ cat ,Field.Store.NO));
					}
				}
			}
			
			if (yelpobject.open != null) 
		            doc.add(new StringField("open", yelpobject.open.toString() ,Field.Store.NO));
			if(yelpobject.schools.size() >0  ) {
				for (String sc :yelpobject.schools ) {
					if (doc.getField("schools") == null)
						doc.add(new TextField("schools",sc ,Field.Store.NO));
					else {
						IndexableField f = doc.getField("schools");
						String s = f.stringValue();
						doc.removeField("schools");
						doc.add(new TextField("schools",s + " "+ sc ,Field.Store.NO));
					}
				}
			}
			if (yelpobject.url != null) 
				doc.add(new StringField("url", yelpobject.url ,Field.Store.NO));
			if (yelpobject.user_id != null) 
				doc.add(new StringField("user_id", yelpobject.user_id ,Field.Store.NO));
			if (yelpobject.text != null) 
				doc.add(new TextField("text", yelpobject.text ,Field.Store.NO));

			if (yelpobject.average_stars != null) 
	                    doc.add(new FloatDocValuesField("average_stars", yelpobject.average_stars));

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			if (yelpobject.date != null)  {
				Date dt = formatter.parse(yelpobject.date);
				doc.add(new LongDocValuesField("date", dt.getTime()));
			}
			if(yelpobject.votes.size() >0  ) {
				for (String key : yelpobject.votes.keySet())
					doc.add(new IntDocValuesField(key, yelpobject.votes.get(key)));
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
