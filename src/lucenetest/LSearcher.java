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

import preprocessinganalyser.MyStandardAnalyzer;
import preprocessinganalyser.NGramAnalyzer;
import customScore.NewScoreStorage;
import customScore.StandardTFIDFscore;

import java.util.regex.Matcher;  
import java.util.regex.Pattern; 
import java.util.Properties;

public class LSearcher {
	
	public int Stemchoice;
	public int LowCchoice;
	public int IgnoreStopchoice;
	public int numchoice;
	//public int Scoremodel;
	public int topcount;
	public int ifindex;
	public int tfidfchoice;
	
	public String TermQstring;
	public String PhraseQstring;
	public String InitialFields;
	public int queryindex;
	public float[] Lnewscore;
	
	private String Sdir;
	private String Bdir;
	private String Mdir;

	
	public StandardTFIDFscore stfidf;
	
	//public MyTFIDFSimilarity MySimilarity;
	
	public LSearcher()
	{
		queryindex = 0;
		tfidfchoice = 0;
		//Scoremodel = 0;
		InitialFields = new String("text");
		PhraseQstring = new String();
		//InputQuery = "";
		TermQstring = "";
		stfidf = new StandardTFIDFscore();
		Lnewscore = new float[topcount];
	
	}
	
	public boolean detectphrase(String InputQuery)
	{		
		//String a = "\"\\w*\"";
		//String a = "\"[\\W\\w]*?\"[\\w\\W]*?(?=[\\+;)])";
		String a = "\"[^\"]*\"";
		String tmp = ""; 
		boolean ifexistphrase = false;
		
		Pattern p = Pattern.compile(a);
		Matcher m = p.matcher(InputQuery);
		while(m.find()) {
		    for(int i=0; i<=m.groupCount(); i++)
		    	tmp += m.group(i);
		    this.PhraseQstring = tmp;
		    
		    tmp = InputQuery.replaceAll(tmp, "");
		    this.queryindex ++;
		    ifexistphrase = true;
		}
		
		if(ifexistphrase == true)
		{
			InputQuery = tmp;
			tmp = InputQuery.replaceAll("\"", "");
			this.TermQstring = tmp;
		}
		else
			this.TermQstring = InputQuery;
		
		
		return ifexistphrase;
	}
	
	public void Lucenesearcher()
	{
		
		int choice = 0;
		if(this.Stemchoice != 0)
			choice = -1;
		if(this.LowCchoice != 0)
			choice = -2;
		if(this.IgnoreStopchoice != 0)
			choice = -3;
		if(this.numchoice != 0)	
			choice = -4;
		
		try{
			
			Directory directory ;
			if(!this.PhraseQstring.isEmpty() && this.TermQstring.isEmpty() )
				directory = FSDirectory.open(new File(this.Bdir));
			else if(!this.TermQstring.isEmpty() && this.PhraseQstring.isEmpty())
				directory = FSDirectory.open(new File(this.Sdir));
			else
				directory = FSDirectory.open(new File(this.Mdir));
			
			IndexReader ir = DirectoryReader.open(directory);
			IndexSearcher isearch = new IndexSearcher(ir);
			
			//MySimilarity = new MyTFIDFSimilarity();
			//isearch.setSimilarity(MySimilarity);
			Query tquery = null;
			Query pquery = null;
			BooleanQuery bquery = new BooleanQuery();
			TopDocs top = null;
			int i = 0;
		
			if(!this.TermQstring.isEmpty())
			{
				MyStandardAnalyzer msa = new MyStandardAnalyzer(Version.LUCENE_47);
				msa.setchoice(choice);
				
				QueryParser parser=new QueryParser(Version.LUCENE_47, this.InitialFields, msa);	
				tquery = parser.parse(this.TermQstring);
			}
			
			if(!this.PhraseQstring.isEmpty())
			{
				NGramAnalyzer nga = new NGramAnalyzer(2,2);
				nga.setchoice(choice);
				
				QueryParser pparser = new QueryParser(Version.LUCENE_47, this.InitialFields, nga);
				pquery = pparser.parse(this.PhraseQstring);				
			}
			
			if(!this.TermQstring.isEmpty() && !this.PhraseQstring.isEmpty())
			{
				bquery.add(tquery, BooleanClause.Occur.MUST);
				bquery.add(pquery, BooleanClause.Occur.MUST);
				
				top = isearch.search(bquery, topcount);
			}
			
			if(!this.TermQstring.isEmpty() && this.PhraseQstring.isEmpty())
			{
				top = isearch.search(tquery, topcount);
			}
			if(this.TermQstring.isEmpty() && !this.PhraseQstring.isEmpty())
			{
				top = isearch.search(pquery, topcount);
			}
			if(!this.TermQstring.isEmpty() && !this.PhraseQstring.isEmpty())
			{
				top = isearch.search(bquery, topcount);
			}
			//QueryParser parser=new QueryParser(Version.LUCENE_47, "content",new StandardAnalyzer(Version.LUCENE_47));		
			//Query query=parser.parse(InputQueries[0]);
			
			//BooleanClause.Occur[] clauses = { BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD }; 
			//Query query = MultiFieldQueryParser.parse(Version.LUCENE_47, InputQueries, InputFields, clauses, new StopAnalyzer(Version.LUCENE_47));  
	        
			ScoreDoc[] sdoc=top.scoreDocs;
			System.out.println("num of docs:"+sdoc.length);
			
			int thit = top.totalHits;
			int [] olddoc = new int[this.topcount];
			
			float [] newscore = new float[this.topcount];
			int c=0;
			NewScoreStorage nss = new NewScoreStorage();
			nss.initial(this.topcount);
			
			for(ScoreDoc sd:sdoc)
			{
				Document doc = isearch.doc(sd.doc);
				nss.olddoc[c] = sd.doc;
				c++;
				String[] scoreExplain = null;  
		        //scoreExplain shows how it score  
				if(!this.TermQstring.isEmpty() && this.PhraseQstring.isEmpty())
				{
					scoreExplain = isearch.explain(tquery,sd.doc).toString().split(" ", 2);  
				}
				if(this.TermQstring.isEmpty() && !this.PhraseQstring.isEmpty())
				{
					scoreExplain = isearch.explain(pquery,sd.doc).toString().split(" ", 2);  
				}
				if(this.TermQstring!="" && !this.PhraseQstring.isEmpty())
				{
					scoreExplain = isearch.explain(bquery,sd.doc).toString().split(" ", 2);  
				}
		        String scores = scoreExplain[0];  
		        //assertEquals("Thisis the text to be indexed.", hitDoc.get("fieldname"));  
		        //System.out.println(doc.get("fieldname") +"\n*score* "+ scores); 
							
				System.out.println("LuceneDoc-Rank:" + Integer.toString(c));
                System.out.println("ID："+Integer.toString(sd.doc));
                System.out.println("stars："+doc.get("stars"));
                System.out.println("score:"+scores);
                System.out.println("Content"+ doc.get("text"));
                System.out.println("totalhits"+ Integer.toString(thit));
			}
			
			this.stfidf.setcount(topcount);
			
			if(this.PhraseQstring.isEmpty())
			{
				System.out.println("$$$$$$$$$$$$$LET'S GET NEW SCORE DOCS$$$$$$$$$$$$$$$$$$");
				this.stfidf.settfidfchoice(this.tfidfchoice);
				this.stfidf.newtopdoc(ir, nss, tquery);
			
				this.sortdoc(nss);
				Document doc;
				for(int j=0;j<topcount;j++)
				{
					Document ndoc = isearch.doc(nss.olddoc[j]);
					doc = isearch.doc(nss.olddoc[j]);
					System.out.println("NEWLuceneDoc-Rank:"+Integer.toString(j));
					System.out.println("ID："+ Integer.toString(nss.olddoc[j]));
					System.out.println("scores：" + Float.toString(nss.newscore[j]));
					System.out.println("Content"+ doc.get("text"));
					System.out.println("totalhits"+ Integer.toString(thit));
				}				
			}
			
			if(this.TermQstring.isEmpty())
			{
				System.out.println("$$$$$$$$$$$$$LET'S GET NEW SCORE DOCS$$$$$$$$$$$$$$$$$$");
				this.stfidf.settfidfchoice(this.tfidfchoice);
				this.stfidf.newtopdoc(ir, nss, pquery);
				
				this.sortdoc(nss);
				
				Document doc;
				
				for(int j=0;j<topcount;j++)
				{
					doc = isearch.doc(nss.olddoc[j]);
					Document ndoc = isearch.doc(nss.olddoc[j]);
					System.out.println("NEWLuceneDoc-Rank:"+Integer.toString(j));
					System.out.println("ID："+ Integer.toString(nss.olddoc[j]));
					System.out.println("scores：" + Float.toString(nss.newscore[j]));
					System.out.println("Content"+ doc.get("text"));
					System.out.println("totalhits"+ Integer.toString(thit));
				}				
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
	
	
	public void sortdoc(NewScoreStorage nss)
	{	
		Float f1,f2;
		float tmpfloat;
		int tmpint;
		
		for(int j = 0; j < this.topcount - 1; j++)
		{
			for(int m = 0; m < this.topcount- 1 - j; m++)
			{
				f1 = new Float(nss.newscore[m]);
				f2 = new Float(nss.newscore[m+1]);
				if(f1.compareTo(f2) < 0)
				{
					tmpfloat = nss.newscore[m+1];
					tmpint = nss.olddoc[m+1];
					nss.newscore[m+1] = nss.newscore[m];
					nss.newscore[m] = tmpfloat;
					nss.olddoc[m+1] = nss.olddoc[m];
					nss.olddoc[m] = tmpint;
				}
			}
		}
		
	}
	public void readconfig(LIndexer idx)
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
		this.IgnoreStopchoice = Integer.parseInt(p.getProperty("Ignorepunction"));
		this.numchoice = Integer.parseInt(p.getProperty("numchoice"));
		//this.Scoremodel = Integer.parseInt(p.getProperty("Scoremodel"));
		this.topcount = Integer.parseInt(p.getProperty("topcount"));
		this.ifindex = Integer.parseInt(p.getProperty("ifindex"));
		this.tfidfchoice = Integer.parseInt(p.getProperty("tfidf"));
		
		this.Sdir = p.getProperty("SingleIndexDir");
		this.Bdir = p.getProperty("BiWindexDir");
		this.Mdir = p.getProperty("MixedIndexDir");
		
		idx.setDatasetDir(p.getProperty("datasetdir"));
		idx.setBdir(this.Bdir);
		idx.setSdir(this.Sdir);
		idx.setMdir(this.Mdir);
		idx.setStemchoice(this.Stemchoice);
		idx.setLowCchoice(this.LowCchoice);
		idx.setIgnoreStopchoice(this.IgnoreStopchoice);
		idx.setHygenchoice(this.numchoice);
	}
	
	public static void main(String[] args) throws IOException
	{
		LIndexer idx = new LIndexer();
		LSearcher ls = new LSearcher();
		
		ls.readconfig(idx);
		
		if(ls.ifindex == 1)
		{	
			idx.Luceneindexer();
			System.out.println("Finish Index!You can begin search");
			System.out.println("Please input your query:");
		}
		else
			System.out.println("Please input your query:");
		
		while(true)
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));  
			String x = in.readLine();
			if(x == null)
			{
				System.out.println("Please input your query or exit to end the program:");
				continue;
			}
	//	String x = "husband";
			if(x.equals(new String("exit")))
			{
				System.out.println("%%%%%%%%%Search EXIT!%%%%%%%%%%%%%%");
				break;
			}
			ls.detectphrase(x);
			ls.Lucenesearcher();
			System.out.println("Please input your query or exit to end the program:");
		}
	}
}
