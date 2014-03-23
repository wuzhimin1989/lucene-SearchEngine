package lucenetest;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

public abstract class TFIDFScoring extends Similarity {
	public TFIDFScoring(){;}
	public abstract float coord(int overlap, int maxOverlap);
	
	public abstract float queryNorm(float sumOfSquaredWeights);
	
	public abstract float tf(float freq);
	
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
	
	public abstract float idf(long docFreq, long numDocs);

	public abstract float lengthNorm(FieldInvertState state);
	
	public final long computeNorm(FieldInvertState state)
	{
		    float normValue = lengthNorm(state);
		    return encodeNormValue(normValue);
	}
	
	public abstract float decodeNormValue(long norm);
	
	public abstract long encodeNormValue(float f);

	public abstract float sloppyFreq(int distance);

	public abstract float scorePayload(int doc, int start, int end, BytesRef payload);
	
	public final SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) 
	{
		final Explanation idf = termStats.length == 1
			? idfExplain(collectionStats, termStats[0])
		    : idfExplain(collectionStats, termStats);
		return new IDFStats(collectionStats.field(), idf, queryBoost);
	}
	
	public final SimScorer simScorer(SimWeight stats, AtomicReaderContext context) throws IOException 
	{
		IDFStats idfstats = (IDFStats) stats;
		return new TFIDFSimScorer(idfstats, context.reader().getNormValues(idfstats.field));
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
		
		@Override
		public Explanation explain(int doc, Explanation freq) 
		{
			return explainScore(doc, freq, stats, norms);
		}
	}
				   
	/** Collection statistics for the TF-IDF model. The only statistic of interest
		   * to this model is idf. */
	private static class IDFStats extends SimWeight 
	{
	    private final String field;
	    /** The idf and its explanation */
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
		 
	private Explanation explainScore(int doc, Explanation freq, IDFStats stats, NumericDocValues norms)
	{
		Explanation result = new Explanation();
		result.setDescription("score(doc="+doc+",freq="+freq+"), product of:");
		// explain query weight
		Explanation queryExpl = new Explanation();
		queryExpl.setDescription("queryWeight, product of:");
		Explanation boostExpl = new Explanation(stats.queryBoost, "boost");
		if (stats.queryBoost != 1.0f)
			queryExpl.addDetail(boostExpl);
		
	    queryExpl.addDetail(stats.idf);
	    Explanation queryNormExpl = new Explanation(stats.queryNorm,"queryNorm");

	    queryExpl.addDetail(queryNormExpl);
		queryExpl.setValue(boostExpl.getValue() * stats.idf.getValue() * queryNormExpl.getValue());

		result.addDetail(queryExpl);
		    // explain field weight
		Explanation fieldExpl = new Explanation();

		fieldExpl.setDescription("fieldWeight in "+doc+ ", product of:");

		Explanation tfExplanation = new Explanation();
		tfExplanation.setValue(tf(freq.getValue()));

		tfExplanation.setDescription("tf(freq="+freq.getValue()+"), with freq of:");
		tfExplanation.addDetail(freq);
		fieldExpl.addDetail(tfExplanation);

		fieldExpl.addDetail(stats.idf);
		Explanation fieldNormExpl = new Explanation();

		float fieldNorm = norms != null ? decodeNormValue(norms.get(doc)) : 1.0f;
		fieldNormExpl.setValue(fieldNorm);
		fieldNormExpl.setDescription("fieldNorm(doc="+doc+")");

		fieldExpl.addDetail(fieldNormExpl);
		fieldExpl.setValue(tfExplanation.getValue() * stats.idf.getValue() *fieldNormExpl.getValue());
		result.addDetail(fieldExpl);

		// combine them
		result.setValue(queryExpl.getValue() * fieldExpl.getValue());
		if (queryExpl.getValue() == 1.0f)
		      return fieldExpl;
		return result;
	}
}



	
