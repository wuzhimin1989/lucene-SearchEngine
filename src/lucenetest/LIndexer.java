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

import preprocessinganalyser.MyStandardAnalyzer;
import preprocessinganalyser.NGramAnalyzer;
//import customSimilarity.MyTFIDFSimilarity;

public class LIndexer {
	//public TFIDFScoring tfidfs;	
	//public MyTFIDFSimilarity MySimilarity; optional
	private int Stemchoice;
	private int LowCchoice;
	private int Ignorepunction;
	private int numchoice;
	
	private String Sdir;
	private String Bdir;
	private String Mdir;
	private String DatasetDir;
	
	public LIndexer()
	{
		Stemchoice = 0;
		LowCchoice = 0;
		Ignorepunction = 0;
		numchoice = 0;
	}
	
	public void setSdir(String d)
	{
		this.Sdir = d;
	}
	
	public void setBdir(String d)
	{
		this.Bdir = d;
	}
	
	public void setMdir(String d)
	{
		this.Mdir = d;
	}
	
	public void setDatasetDir(String d)
	{
		this.DatasetDir = d;
	}
	
	public void setStemchoice(int c)
	{
		this.Stemchoice = c;
	}
	
	public void setLowCchoice(int c)
	{
		this.LowCchoice = c;
	}
	
	public void setIgnoreStopchoice(int c)
	{
		this.Ignorepunction = c;
	}
	
	public void setHygenchoice(int c)
	{
		this.numchoice = c;
	}
	
	public void Luceneindexer()
	{
		Directory directory = null;
		Directory directory2 = null;
		Directory directory3 = null;
		IndexWriterConfig iwc = null;
		IndexWriterConfig iwc2 = null;
		IndexWriterConfig iwc3 = null;
		IndexWriter iw = null;
		IndexWriter iw2 = null;
		IndexWriter iw3 = null;
		FieldType ftype = new FieldType();
		
		ftype.setStoreTermVectors(true);
		ftype.setStoreTermVectorOffsets(true);
		ftype.setStoreTermVectorPositions(true);
		ftype.setStoreTermVectorPayloads(true);
		ftype.setTokenized(true);
		ftype.setIndexed(true);
		ftype.setStored(true);
		
		int choice = 0;
		if(this.Stemchoice != 0)
			choice = -1;
		if(this.LowCchoice != 0)
			choice = -2;
		if(this.Ignorepunction != 0)
			choice = -3;
		if(this.numchoice != 0)	
			choice = -4;
		
		try{
			directory = FSDirectory.open(new File(this.Mdir));
			directory2 = FSDirectory.open(new File(this.Bdir));
			directory3 = FSDirectory.open(new File(this.Sdir));
			
			NGramAnalyzer nga = new NGramAnalyzer(2,2);
			nga.setchoice(choice);
			
			MyStandardAnalyzer msa = new MyStandardAnalyzer(Version.LUCENE_47);
			msa.setchoice(choice);
			
			//MyStandardAnalyzer msd = new MyStandardAnalyzer();
			
			iwc = new IndexWriterConfig(Version.LUCENE_47, msa);
			iwc2 = new IndexWriterConfig(Version.LUCENE_47, nga);
			iwc3 = new IndexWriterConfig(Version.LUCENE_47, msa);
			/*MySimilarity = new MyTFIDFSimilarity();
			iwc.setSimilarity(MySimilarity);
			iwc2.setSimilarity(MySimilarity);*/
			
			iw = new IndexWriter(directory, iwc);
			iw2 = new IndexWriter(directory2, iwc2);
			iw3 = new IndexWriter(directory3, iwc3);
			
			Document doc = null;
			File f = new File(this.DatasetDir);
			
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
						iw3.addDocument(doc);				
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
	                iw3.close();
	                
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
