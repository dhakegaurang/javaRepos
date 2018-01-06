package com;

public class Dictionary {
	
	private String term;
	private int docFreq;
	private int offset;
	
	public Dictionary(String term, int docFreq, int offset) {
		this.term = term;
		this.docFreq = docFreq;
		this.offset = offset;
	}
	
	public Dictionary() {
	}

	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public int getDocFreq() {
		return docFreq;
	}
	public void setDocFreq(int docFreq) {
		this.docFreq = docFreq;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
}
