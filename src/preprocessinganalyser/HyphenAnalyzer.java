package preprocessinganalyser;

import java.io.Reader;
import java.lang.Object;

import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.miscellaneous.HyphenatedWordsFilter;


public class HyphenAnalyzer extends Analyzer{
	@Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
      Tokenizer source = new LowerCaseTokenizer(Version.LUCENE_47, reader);
      return new TokenStreamComponents(source, new HyphenatedWordsFilter(source));
    }
}
