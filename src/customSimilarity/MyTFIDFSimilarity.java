package customSimilarity;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

public class MyTFIDFSimilarity extends TFIDFSimilarity{
	
	private static final float[] NORM_TABLE = new float[256];
	
	static {
	for (int i = 0; i < 256; i++) {
	     NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte)i);
		}
	}
	
	public MyTFIDFSimilarity(){}
	
	public float coord(int overlap, int maxOverlap)
	{
		return 1.0f;
	}	
	
	public float queryNorm(float sumOfSquaredWeights)
	{
		return 1.0f;
	}
	
	
	public float tf(float freq)
	{
		return freq;
	}
	
	public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) 
	{
		final long df = termStats.docFreq();
		final long max = collectionStats.maxDoc();
		final float idf = idf(df, max);
		return new Explanation(idf, "idf(docFreq=" + df + ", maxDocs=" + max + ")");
	}
	
	public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats[])
	{
		    final long max = collectionStats.maxDoc();
		    float idf = 0.0f;
		    final Explanation exp = new Explanation();
		    exp.setDescription("idf(), sum of:");
		    for (final TermStatistics stat : termStats ) 
		    {
		      final long df = stat.docFreq();
		      final float termIdf = idf(df, max);
		      exp.addDetail(new Explanation(termIdf, "idf(docFreq=" + df + ", maxDocs=" + max + ")"));
		      idf += termIdf;
		    }
		    exp.setValue(idf);
		    return exp;
	}
	
	public float idf(long docFreq, long numDocs) {
		   return (float)(Math.log(numDocs+1/(double)(docFreq+1)));
	}
	
	public float lengthNorm(FieldInvertState state)
	{
		return 1.0f;
	}
	
	public final long encodeNormValue(float f) {
	   return SmallFloat.floatToByte315(f);
	}
	
	public final float decodeNormValue(long norm) {
	    return NORM_TABLE[(int) (norm & 0xFF)];  // & 0xFF maps negative bytes to positive above 127
	}

	public float sloppyFreq(int distance)
	{
		return 1.0f;
	}
	
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		return 1.0f;
	}
	
	private final class TFIDFSimScorer extends SimScorer
	{
	    private final IDFStats stats;
	    private final float weightValue;
	    private final NumericDocValues norms;
	    TFIDFSimScorer(IDFStats stats, NumericDocValues norms) throws IOException
	    {
	    	this.stats = stats;
		    this.weightValue = stats.value;
		    this.norms = norms;
		}
		     
		@Override
		public float score(int doc, float freq) 
		{
		    final float raw = tf(freq) * weightValue; // compute tf(f)*weight
		    return norms == null ? raw : raw * decodeNormValue(norms.get(doc));  // normalize for field
		}

		@Override
		public float computeSlopFactor(int distance) 
		{
		    return sloppyFreq(distance);
		}
		
		@Override
		public float computePayloadFactor(int doc, int start, int end, BytesRef payload)
		{
			return scorePayload(doc, start, end, payload);
		}
		
	}
				   
	/** Collection statistics for the TF-IDF model. The only statistic of interest
		   * to this model is idf. */
	private static class IDFStats extends SimWeight 
	{
	    private final String field;
	   
	    private final Explanation idf;
	    private float queryNorm;
	    private float queryWeight;
	    private final float queryBoost;
	    private float value;
	
	    public IDFStats(String field, Explanation idf, float queryBoost) 
	    {
	    	// TODO: Validate?
	    	this.field = field;
		    this.idf = idf;
		    this.queryBoost = queryBoost;
		    this.queryWeight = idf.getValue() * queryBoost; // compute query weight	
	    }

	    @Override
	    public float getValueForNormalization()
	    {
		    // TODO: (sorta LUCENE-1907) make non-static class and expose this squaring via a nice method to subclasses?
		    return queryWeight * queryWeight;  // sum of squared weights
		}

	    @Override
	    public void normalize(float queryNorm, float topLevelBoost) 
	    {
		    this.queryNorm = queryNorm * topLevelBoost;
		    queryWeight *= this.queryNorm;              // normalize query weight
		    value = queryWeight * idf.getValue();         // idf for document
		}
	}  
		 
}



	
