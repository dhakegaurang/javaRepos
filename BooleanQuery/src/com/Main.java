package com;

public class Main {
	
	public static void main(String[] args) {
		
		String documentTable = "DocsTable.txt";
		String dictionary = "Dictionary.csv";
		String posting = "Postings.csv";
		String inputFolder = "input";
		
		if(args.length >= 1) {
			if(!"".equals(args[0]) && args[0] != null) {
				documentTable = args[0];
			}
			if(!"".equals(args[1]) && args[1] != null) {
				dictionary = args[1];
			}
			if(!"".equals(args[2]) && args[2] != null) {
				posting = args[2];
			}
			if(!"".equals(args[3]) && args[3] != null) {
				inputFolder = args[3];
			}
		}
		
		Processor processor = new Processor(inputFolder, documentTable, dictionary, posting);//args[0]
	}
	
}