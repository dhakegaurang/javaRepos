package com;

public class DocumentTable {
	
	private String docNo;
	private String title;
	private String reviewer;
	private String snippet;
	private String rate;
	
	
	
	public DocumentTable(String docNo, String title, String reviewer, String snippet, String rate) {
		this.docNo = docNo;
		this.title = title;
		this.reviewer = reviewer;
		this.snippet = snippet;
		this.rate = rate;
	}


	public DocumentTable() {} 
	
	
	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		if(!"".equals(title))
			this.title = title;
		else
			this.title = "[NO TITLE]";
	}
	public String getReviewer() {
		return reviewer;
	}
	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}
	public String getSnippet() {
		return snippet;
	}
	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	
	@Override
	public String toString() {
		return "DocumentTable [docNo=" + docNo + ", title=" + title + ", reviewer=" + reviewer + ", snippet=" + snippet
				+ ", rate=" + rate + "]";
	}
	
}
