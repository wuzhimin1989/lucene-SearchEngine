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
		public StandardTFIDFscore(){count = 10;}
		public float[] newtopdoc(IndexReader ir, int[] oldtop, Query query) throws IOException
		{
			int [] newtop = new int[count];
			float [] newscore = new float[count];
			
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
			while(qtenum.hasNext())
			{
				Term tm = (Term)qtenum.next();
			
				qryterm.add(tm.toString());
				qdocfreq = ir.docFreq(tm);
				qtfidf=(float)(Math.log10((numDocs)/(double)(qdocfreq)));
				qvsm.add(qtfidf);
				
				tmpresult += (float)(Math.pow((double)qtfidf, 2));
			}
			
			qnorm = (float)(Math.sqrt((double)tmpresult));
			
			
			for(int i=0; i<count; i++)
			{
				tmpresult = (float)0.0;

				ArrayList<String> termslist = new ArrayList<String>();

				ArrayList<Float> vsm = new ArrayList<Float>();
				
				int docid = oldtop[i];
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
				float idf = (float) 0.0;
				float tfidf = (float) 0.0;
				
				while ((thisterm = tenum.next()) != null) {  
                    String termText = thisterm.utf8ToString();
                    termslist.add(termText);
                    
                    
                    idf = (float)(Math.log10((numDocs)/(double)(tenum.docFreq())));
                    
                    DocsEnum docsEnum = tenum.docs(null, null); 
                    
                    while ((docsEnum.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {                    
                        System.out.println("termText:"+termText+" TF:  "+docsEnum.freq());  
                        
                        tfidf = (float)(docsEnum.freq() * (double)idf);
                   }                   
                   vsm.add(tfidf);  
                   tmpresult+=(float)(Math.pow((double)tfidf,2));
               }			
			   
			   dnorm = (float)(Math.sqrt((double)tmpresult));
				
			   for(int j = 0;j < qvsm.size(); j++)
			   {
				   int index;
				   if((index = termslist.indexOf(qryterm.get(j))) != -1)
				   {
					   tmpresult += (float)qvsm.get(j).floatValue() * (float)vsm.get(index).floatValue(); 
				   }
			   }
			   
			   newscore[i] = (float)(tmpresult/(double)(dnorm * qnorm));
			}
			
			return newscore;
		}
}
