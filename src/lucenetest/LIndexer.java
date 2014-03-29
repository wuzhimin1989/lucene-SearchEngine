package lucenetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONException;
import org.json.JSONObject;

import preprocessinganalyser.NGramAnalyzer;
import customSimilarity.MyTFIDFSimilarity;

public class LIndexer {
	//public TFIDFScoring tfidfs;	
	//public MyTFIDFSimilarity MySimilarity; optional
	
	public LIndexer(){}
	
	public void Luceneindexer()
	{
		Directory directory = null;
		Directory directory2 = null;
		IndexWriterConfig iwc = null;
		IndexWriterConfig iwc2 = null;
		IndexWriter iw = null;
		IndexWriter iw2 = null;
		FieldType ftype = new FieldType();
		
		ftype.setStoreTermVectors(true);
		ftype.setStoreTermVectorOffsets(true);
		ftype.setStoreTermVectorPositions(true);
		ftype.setStoreTermVectorPayloads(true);
		ftype.setTokenized(true);
		ftype.setIndexed(true);
		ftype.setStored(true);
		
		try{
			directory = FSDirectory.open(new File("I:\\zhimin\\courses\\ir\\resultdoc\\SWindex"));
			directory2 = FSDirectory.open(new File("I:\\zhimin\\courses\\ir\\resultdoc\\BiWindex"));
			
			iwc = new IndexWriterConfig(Version.LUCENE_47, new StopAnalyzer(Version.LUCENE_47));
			iwc2 = new IndexWriterConfig(Version.LUCENE_47, new NGramAnalyzer(2, 2));
			
			/*MySimilarity = new MyTFIDFSimilarity();
			iwc.setSimilarity(MySimilarity);
			iwc2.setSimilarity(MySimilarity);*/
			
			iw = new IndexWriter(directory, iwc);
			iw2 = new IndexWriter(directory2, iwc2);
			
			
			
		
			Document doc = null;
			File f = new File("I:\\zhimin\\courses\\ir\\resultdoc\\lucenetxt");
			
			for(File file : f.listFiles())
			{
				FileReader r = new FileReader(file);
				BufferedReader bf = new BufferedReader(r);
				  
				String tmpline = "";
				//StringBuilder sb = new StringBuilder();
				  
				while(tmpline != null)
				{
					tmpline = bf.readLine();
				   
					if(tmpline == null){
						break;
					}
				   
					//sb.append(tmpline.trim()); 
					//bf.close();			
					//System.out.println(sb.toString());
					try{
						JSONObject jsonobj = new JSONObject(tmpline);
						String Svotes = jsonobj.getString("votes"); 
						String Suid = jsonobj.getString("user_id");
						String Srevid = jsonobj.getString("review_id");
						String Sstars = jsonobj.getString("stars");
						String Sdate = jsonobj.getString("date");
						String Stext = jsonobj.getString("text");
					
						doc = new Document();
								
						Field votes = new Field("votes", Svotes, ftype);
						Field userid = new Field("user_id", Suid, ftype);
						Field reviewid = new Field("review_id", Srevid, ftype);
						Field stars =  new Field("stars", Sstars, ftype);
						Field dates = new Field("date", Sdate, ftype);
						Field content = new Field("text", Stext, ftype);
						
						doc.add(votes);
						doc.add(userid);
						doc.add(reviewid);
						doc.add(stars);
						doc.add(dates);
						doc.add(content);
			
					}catch(JSONException e){
						e.printStackTrace();
					}finally{
						iw.addDocument(doc);
						iw2.addDocument(doc);
					}
				}
				bf.close();
			}
		}catch (CorruptIndexException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (LockObtainFailedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } finally {
	            try {
	                iw2.close();
	                iw.addIndexes(directory2);
	                iw.close();
	                
	            } catch (CorruptIndexException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
	}
	
}
