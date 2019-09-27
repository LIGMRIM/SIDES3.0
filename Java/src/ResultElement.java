
public class ResultElement implements Comparable<ResultElement>{
	private String qID;
	private String loID;
	private double score;
	public String getqID() {
		return qID;
	}
	public void setqID(String qID) {
		this.qID = qID;
	}
	public String getLoID() {
		return loID;
	}
	public void setLoID(String loID) {
		this.loID = loID;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public ResultElement(String qID, String loID, double score) {
		super();
		this.qID = qID;
		this.loID = loID;
		this.score = score;
	}
	@Override
	public int compareTo(ResultElement o) {
		if(this.getScore()>o.getScore())
			return -1;
		else if(this.getScore()==o.getScore())
			return 0;
		else
			return 1;

	}
	@Override
	public boolean equals(Object obj) {
		ResultElement item = (ResultElement) obj;
		return (this.qID.equals(item.getqID())&&this.loID.equals(item.getLoID()));
	}

	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getqID()+"\t"+getLoID()+"\t"+getScore();
	}
	
	

}
