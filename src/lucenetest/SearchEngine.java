package lucenetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class SearchEngine {
	public TFIDFScoring tfidfs;
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
			directory = FSDirectory.open(new File("I:\\zhimin\\courses\\ir\\resultdoc\\index"));
			directory2 = FSDirectory.open(new File("I:\\zhimin\\courses\\ir\\resultdoc\\index1"));
			
			iwc = new IndexWriterConfig(Version.LUCENE_47, new StopAnalyzer(Version.LUCENE_47));
			iwc2 = new IndexWriterConfig(Version.LUCENE_47, new NGramAnalyzer(2, 2));
			
			iw = new IndexWriter(directory, iwc);
			iw2 = new IndexWriter(directory2, iwc2);
		
			Document doc = null;
			File f = new File("I:\\zhimin\\courses\\ir\\resultdoc\\lucenetxt");
			
			for(File file : f.listFiles())
			{
				FileReader r = new FileReader(file);
				BufferedReader bf = new BufferedReader(new FileReader(file));
				  
				String tmpc = "";
				StringBuilder sb = new StringBuilder();
				  
				while(tmpc != null)
				{
					tmpc = bf.readLine();
				   
					if(tmpc == null){
						break;
					}
				   
					sb.append(tmpc.trim());
				}
				  
				bf.close();
				
				System.out.println(sb.toString());
				
				doc = new Document();
								
				Field content = new Field("content", sb.toString(), ftype);
                Field filename = new Field("filename", file.getName(), ftype);
                Field path = new Field("path", file.getAbsolutePath(), ftype);
                doc.add(content);
                doc.add(filename);
                doc.add(path);
                
                iw.addDocument(doc);
                iw2.addDocument(doc);
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
	
	public void Lucenesearcher()
	{
		try{
			Directory directory = FSDirectory.open(new File("I:\\zhimin\\courses\\ir\\resultdoc\\index"));
			IndexReader ir = DirectoryReader.open(directory);
			IndexSearcher isearch = new IndexSearcher(ir);
			QueryParser parser=new QueryParser(Version.LUCENE_47, "content",new StandardAnalyzer(Version.LUCENE_47));
			
			Query query=parser.parse("buy KFC");
			
			TopDocs top=isearch.search(query, 1);
			ScoreDoc[] sdoc=top.scoreDocs;
			System.out.println("num of docs:"+sdoc.length);
			
			int thit = top.totalHits;
			for(ScoreDoc sd:sdoc)
			{
				Document doc = isearch.doc(sd.doc);
				
				String[] scoreExplain = null;  
		           // scoreExplain可以显示文档的得分详情，这里用split截取总分  
		        scoreExplain = isearch.explain(query,sd.doc).toString().split(" ", 2);  
		        String scores = scoreExplain[0];  
		             //assertEquals("Thisis the text to be indexed.", hitDoc.get("fieldname"));  
		        System.out.println(doc.get("fieldname") +"\n*score* "+ scores); 
				
				
				System.out.println("Doc:");
                System.out.println("Docname："+doc.get("filename"));
                System.out.println("Path："+doc.get("path"));
                System.out.println("content:"+doc.get("content"));
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
	
	public static void main(String[] args)
	{
		SearchEngine se = new SearchEngine();
		se.Luceneindexer();
		se.Lucenesearcher();
	}
}
