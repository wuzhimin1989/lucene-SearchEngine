package lucenetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;  
import org.apache.commons.cli.CommandLineParser;  
import org.apache.commons.cli.Options;  
  
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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import customScore.StandardTFIDFscore;

import java.util.regex.Matcher;  
import java.util.regex.Pattern; 
import java.util.Properties;

public class LSearcher {
	
	public int Stemchoice;
	public int LowCchoice;
	public int IgnoreStopchoice;
	public int Scoremodel;
	public int topcount;
	
	public String InputQuery;
	public String TermQstring;
	public String[] PhraseQstring;
	public String InitialFields;
	public int queryindex;
	
	public StandardTFIDFscore stfidf;
	
	//public MyTFIDFSimilarity MySimilarity;
	
	public LSearcher()
	{
		queryindex = 0;
		Scoremodel = 0;
		InitialFields = new String("content");
		PhraseQstring = new String[5];
		InputQuery = "";
		TermQstring = "";
		stfidf = new StandardTFIDFscore();
	}
	
	public boolean detectphrase(String q)
	{		
		String a = "\"\\w*\"";
		String tmp = ""; 
		boolean ifexistphrase = false;
		
		Pattern p = Pattern.compile(a);
		Matcher m = p.matcher(this.InputQuery);
		while(m.find()) {
		    for(int i=0; i<=m.groupCount(); i++)
		    	tmp += m.group(i);
		    this.PhraseQstring[this.queryindex] = tmp;
		    
		    this.InputQuery.replaceAll(tmp, "");
		    this.queryindex ++;
		    ifexistphrase = true;
		}
		
		this.InputQuery.replaceAll("\"", "");
		this.TermQstring = this.InputQuery;
		
		return ifexistphrase;
	}
	
	public void Lucenesearcher()
	{
		try{
			Directory directory = FSDirectory.open(new File("I:\\zhimin\\courses\\ir\\resultdoc\\index"));
			IndexReader ir = DirectoryReader.open(directory);
			IndexSearcher isearch = new IndexSearcher(ir);
			
			//MySimilarity = new MyTFIDFSimilarity();
			//isearch.setSimilarity(MySimilarity);
			Query tquery = null;
			PhraseQuery pquery = null;
			BooleanQuery bquery = null;
			TopDocs top = null;
			int i = 0;
		
			if(this.TermQstring != null)
			{
				QueryParser parser=new QueryParser(Version.LUCENE_47, this.InitialFields ,new StandardAnalyzer(Version.LUCENE_47));	
				tquery = parser.parse(this.TermQstring);
			}
			
			if(this.PhraseQstring!= null)
			{
				pquery = new PhraseQuery();
				for(String s : this.PhraseQstring)
				{	
					if(i < this.queryindex)
						pquery.add(new Term(s,this.InitialFields));
					i++;
				}
				pquery.setSlop(0);
			}
			
			if(this.TermQstring != null && this.PhraseQstring!=null)
			{
				bquery.add(tquery, BooleanClause.Occur.MUST);
				bquery.add(pquery, BooleanClause.Occur.MUST);
				
				top = isearch.search(bquery, 1);
			}
			
			if(this.TermQstring != null && this.PhraseQstring == null)
			{
				top = isearch.search(tquery, 200);
			}
			if(this.TermQstring == null && this.PhraseQstring != null)
			{
				top = isearch.search(pquery, 1);
			}
			//QueryParser parser=new QueryParser(Version.LUCENE_47, "content",new StandardAnalyzer(Version.LUCENE_47));		
			//Query query=parser.parse(InputQueries[0]);
			
			//BooleanClause.Occur[] clauses = { BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD }; 
			//Query query = MultiFieldQueryParser.parse(Version.LUCENE_47, InputQueries, InputFields, clauses, new StopAnalyzer(Version.LUCENE_47));  
	        
			ScoreDoc[] sdoc=top.scoreDocs;
			System.out.println("num of docs:"+sdoc.length);
			
			int thit = top.totalHits;
			int [] olddoc = new int[this.topcount];
			int [] newdoc = new int[this.topcount];
			float [] newscore = new float[this.topcount];
			int c=0;
			for(ScoreDoc sd:sdoc)
			{
				Document doc = isearch.doc(sd.doc);
				olddoc[c] = sd.doc;
				c++;
				//String[] scoreExplain = null;  
		           // scoreExplain shows how it score  
		        //scoreExplain = isearch.explain(query,sd.doc).toString().split(" ", 2);  
		        //String scores = scoreExplain[0];  
		             //assertEquals("Thisis the text to be indexed.", hitDoc.get("fieldname"));  
		        //System.out.println(doc.get("fieldname") +"\n*score* "+ scores); 
							
				System.out.println("LuceneDoc:");
                System.out.println("Docname："+doc.get("filename"));
                System.out.println("Path："+doc.get("path"));
                System.out.println("content:"+doc.get("content"));
                System.out.println("totalhits"+ Integer.toString(thit));
			}
			
			if(this.TermQstring != null && this.PhraseQstring!=null)
			{
				newscore = this.stfidf.newtopdoc(ir, olddoc, bquery);
			}
			if(this.TermQstring != null && this.PhraseQstring == null)
			{
				newscore = this.stfidf.newtopdoc(ir, olddoc, tquery);
			}
			if(this.TermQstring == null && this.PhraseQstring != null)
			{
				newscore = this.stfidf.newtopdoc(ir, olddoc, pquery);
			}
			
			newdoc = this.sortdoc(newscore, olddoc);
			
			for(int j=0;j<200;j++)
			{
				Document ndoc = isearch.doc(newdoc[j]);
				System.out.println("LuceneDoc:");
                System.out.println("Docname："+ndoc.get("filename"));
                System.out.println("Path："+ndoc.get("path"));
                System.out.println("content:"+ndoc.get("content"));
                System.out.println("totalhits"+ Integer.toString(thit));
			}
			ir.close();
		}catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	public int[] sortdoc(float []score, int[]docid)
	{	
		Float f1,f2;
		float tmpfloat;
		int tmpint;
		
		for(int j = 0; j < this.topcount - 1; j++)
		{
			for(int m = 0; m < this.topcount- 1 - j; m++)
			{
				f1 = new Float(score[m]);
				f2 = new Float(score[m+1]);
				if(f1.compareTo(f2) < 0)
				{
					tmpfloat = score[m+1];
					tmpint = docid[m+1];
					score[m+1] = score[m];
					score[m] = tmpfloat;
					docid[m+1] = docid[m];
					docid[m] = tmpint;
				}
			}
		}
		
		return docid;
	}
	public void readconfig()
	{
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("Configuration.properties");   
		Properties p = new Properties();   
		try {   
			p.load(inputStream);   
		  } catch (IOException e1) {   
		   e1.printStackTrace();   
		}   
		this.Stemchoice = Integer.parseInt(p.getProperty("Stemchoice"));
		this.LowCchoice = Integer.parseInt(p.getProperty("LowCchoice"));
		this.IgnoreStopchoice = Integer.parseInt(p.getProperty("IgnoreStopchoice"));
		this.Scoremodel = Integer.parseInt(p.getProperty("Scoremodel"));
		this.topcount = Integer.parseInt(p.getProperty("topcount"));
	}
	
	public static void main(String[] args) throws IOException
	{
		LIndexer idx = new LIndexer();
		idx.Luceneindexer();
		
		LSearcher ls = new LSearcher();
		ls.readconfig();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));  
		String x = in.readLine();
		 
		ls.detectphrase(x);
		ls.Lucenesearcher();
	}
}
