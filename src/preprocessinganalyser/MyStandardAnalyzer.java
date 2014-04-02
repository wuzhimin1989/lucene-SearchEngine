package preprocessinganalyser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.Object;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.HyphenatedWordsFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterIterator;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;


public class MyStandardAnalyzer extends StopwordAnalyzerBase{
	
	private int filterchoice;
	public static final CharArraySet ENGLISH_STOP_WORDS_SET;
	
	static {
		     final List<String> stopWords = Arrays.asList(
		       "a", "an", "and", "are", "as", "at", "be", "but", "by",
		       "for", "if", "in", "into", "is", "it",
		       "no", "not", "of", "on", "or", "such",
		       "that", "the", "their", "then", "there", "these",
		       "they", "this", "to", "was", "will", "with","-"
		    		 );
		     final CharArraySet stopSet = new CharArraySet(Version.LUCENE_47, stopWords, false);
		     ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet); 
	 }
	
	public MyStandardAnalyzer(Version matchVersion) {
	    this(matchVersion, ENGLISH_STOP_WORDS_SET);
	}
	public MyStandardAnalyzer(Version matchVersion, CharArraySet stopWords) {
	    super(matchVersion, stopWords);
	}
	
	public MyStandardAnalyzer(Version matchVersion, File stopwordsFile) throws IOException {
	     this(matchVersion, loadStopwordSet(stopwordsFile, matchVersion));
	}
	
	public MyStandardAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
	     this(matchVersion, loadStopwordSet(stopwords, matchVersion));
	}
	
	public void setchoice(int c)
	{
		this.filterchoice = c;
	}
	
	@Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) 
    {
		Tokenizer source;
		if(filterchoice == -4)
			source = new StandardTokenizer(Version.LUCENE_47, reader);
		else 
			source = new LetterTokenizer(Version.LUCENE_47, reader);
		
			//source = new SentenceTokenizer(reader);
		
		//Tokenizer source = new StandardTokenizer(Version.LUCENE_47, reader);
		TokenStream filter = new StopFilter(Version.LUCENE_47, source,StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		//TokenStream filter = new StandardFilter(Version.LUCENE_47,source);
		
		if(filterchoice != -2)
	    {
	    	filter = new LowerCaseFilter(Version.LUCENE_47, filter);  //optional
	    }
	    
	    if(filterchoice != -3)
	    {
	    	filter = new StopFilter(Version.LUCENE_47, filter,MyStandardAnalyzer.ENGLISH_STOP_WORDS_SET);
	    }
	    
	    if(filterchoice != -1)
	    {
	    	filter = new PorterStemFilter(filter);
	    }
	    
	    
	    //filter = new WordDelimiterFilter(filter,1,WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE);
	    return new TokenStreamComponents(source, filter);
    }
}
