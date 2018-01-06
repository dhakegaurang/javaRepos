package com;

public class Postings {
	
	private String docID;
	private int termFreq;
	
	public Postings(String docID, int termFreq) {
		this.docID = docID;
		this.termFreq = termFreq;
	}
	
	public Postings() {}

	public String getDocID() {
		return docID;
	}
	
	public void setDocID(String docID) {
		this.docID = docID;
	}
	
	public int getTermFreq() {
		return termFreq;
	}
	
	public void setTermFreq(int termFreq) {
		this.termFreq = termFreq;
	}
	
	@Override
	public String toString() {
		return "Postings [docID=" + docID + ", termFreq=" + termFreq + "]";
	}
	
}