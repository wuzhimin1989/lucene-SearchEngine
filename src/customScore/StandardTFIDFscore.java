package customScore;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Set;

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
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.util.regex.Matcher;  
import java.util.regex.Pattern; 
import java.util.Properties;

public class StandardTFIDFscore {
		public int count;
		private int tfidfchoice;
		public StandardTFIDFscore(){}
		
		public void setcount(int c)
		{
			this.count = c;
		}
		
		public void settfidfchoice(int c)
		{
			this.tfidfchoice = c;
		}
		
		public void newtopdoc(IndexReader ir, NewScoreStorage nss, Query query) throws IOException
		{
			//int [] newtop = new int[count];
			
			int numDocs = ir.numDocs();
			float qdocfreq,qtfidf;
			float qnorm, dnorm;
			float tmpresult = (float)0.0;
			
			ArrayList<String> qryterm = new ArrayList<String>();
			
			ArrayList<Float> qvsm = new ArrayList<Float>();
			Query qry = query.rewrite(ir);
			
			Set<Term> sterm = new HashSet<Term>();
			qry.extractTerms(sterm);
			
			Iterator<Term> qtenum = sterm.iterator(); 
			
			
			//pingfanuse
			float totalindexsize;
			
			totalindexsize = ir.getSumDocFreq("text");
			System.out.println("total index size = " + Float.toString(totalindexsize));
			
			while(qtenum.hasNext())
			{
				Term tm = (Term)qtenum.next();
				
				//System.out.println("term name:"+ tm.toString());
				qryterm.add(tm.text());
				qdocfreq = ir.docFreq(tm);
				
				if(this.tfidfchoice == 0 || this.tfidfchoice == 1)
					qtfidf = (float)(Math.log10((numDocs+1)/(double)(qdocfreq)+1));
				else
					qtfidf=(float)(Math.log10(1+(numDocs+1)/(double)(qdocfreq)+1));
				
				qvsm.add(qtfidf);
				
				tmpresult += (float)(Math.pow((double)qtfidf, 2));
			}
			
			qnorm = (float)(Math.sqrt((double)tmpresult));
			
			
			for(int i=0; i<count; i++)
			{
				tmpresult = (float)0.0;

				ArrayList<String> termslist = new ArrayList<String>();

				ArrayList<Float> vsm = new ArrayList<Float>();
				
				int docid = nss.olddoc[i];
				Terms terms = null;
				try {
						terms = ir.getTermVector(docid, "text");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(terms == null)
					continue;
				
				TermsEnum tenum = null;
				
				try {
					 tenum = terms.iterator(null);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				BytesRef thisterm = null;
				float tf = (float) 0.0;
				float idf = (float) 0.0;
				float tfidf = (float) 0.0;
				
				while ((thisterm = tenum.next()) != null) {  
                    String termText = thisterm.utf8ToString();
                    termslist.add(termText);
                    //System.out.println("name:"+ termText);
                    
                    if(this.tfidfchoice == 0 || this.tfidfchoice == 1)
                    	idf = (float)(Math.log10((numDocs)/(double)(tenum.docFreq())));
                    else
                    	idf = (float)(Math.log10(1+ (numDocs)/(double)(tenum.docFreq())));
                    
                    DocsEnum docsEnum = tenum.docs(null, null); 
                    
                    while ((docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {                    
                        //System.out.println("termText:"+termText+" TF:  "+docsEnum.freq());  
                        if(this.tfidfchoice == 0 || this.tfidfchoice == 2)
                        	tf = (float)(Math.log10(1+docsEnum.freq()));
                        else
                        	tf = (float)(1-(double)1.0/(1+docsEnum.freq()));
                        
                        tfidf = (float)((double)tf * (double)idf);
                   }                   
                   vsm.add(tfidf);  
                   tmpresult+=(float)(Math.pow((double)tfidf,2));
               }			
			   
			   dnorm = (float)(Math.sqrt((double)tmpresult));
				
			   tmpresult = 0;
			   for(int j = 0;j < qvsm.size(); j++)
			   {
				   int index;
				   //System.out.println(qryterm.get(j).toString());
				   if((index = termslist.indexOf(qryterm.get(j))) != -1)
				   {
					   tmpresult += (float)qvsm.get(j).floatValue() * (float)vsm.get(index).floatValue(); 
				   }
			   }
			   
			   nss.newscore[i] = (float)(tmpresult/(double)(dnorm * qnorm));
			}
			
		}
}
