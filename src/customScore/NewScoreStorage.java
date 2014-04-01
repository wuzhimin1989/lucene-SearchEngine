package customScore;

public class NewScoreStorage {
	
	public int[] olddoc;
	//public int[] newdoc;
	public float[] newscore;
	
	public NewScoreStorage(){}
	
	public void initial(int c)
	{
		olddoc = new int[c];
		//newdoc = new int[c];
		newscore = new float[c];
	}
}
