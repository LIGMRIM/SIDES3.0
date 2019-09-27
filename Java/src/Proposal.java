
public class Proposal {
	private String proposalID;
	private String proposalText;
	private boolean proposalStatus;
	
	public Proposal(String proposalID, String proposalText, boolean proposalStatus){
		this.proposalID=proposalID;
		this.proposalText=proposalText;
		this.proposalStatus =proposalStatus;
		
	}
	
	public String getProposalID() {
		return proposalID;
	}
	public void setProposalID(String proposalID) {
		this.proposalID = proposalID;
	}
	public String getProposalText() {
		return proposalText;
	}
	public void setProposalText(String proposalText) {
		this.proposalText = proposalText;
	}
	public boolean isProposalStatus() {
		return proposalStatus;
	}
	public void setProposalStatus(boolean proposalStatus) {
		this.proposalStatus = proposalStatus;
	}
    @Override
	public boolean equals(Object obj) {
        return (this.proposalID.equals(((Proposal) obj).getProposalID()));
	}


}
