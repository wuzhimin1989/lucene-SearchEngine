package preprocessinganalyser;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.HyphenatedWordsFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class NGramAnalyzer extends Analyzer {
	private int minGram;
	private int maxGram;
	
	private int filterchoice;

	public NGramAnalyzer(int minGram, int maxGram) 
	{
		
	    this.minGram = minGram;
	    this.maxGram = maxGram;
	}
	
	public void setchoice(int c)
	{
		this.filterchoice = c;
	}
	
	@Override
	protected TokenStreamComponents createComponents(String arg0, Reader reader) 
	{
		Tokenizer source;
		if(filterchoice == -4)
			source = new StandardTokenizer(Version.LUCENE_47, reader);
		else
			source = new LetterTokenizer(Version.LUCENE_47, reader);
		
		
	    TokenStream filter = new ShingleFilter(source, minGram, maxGram);

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
	    
	    return new TokenStreamComponents(source, filter);
	}
}
