package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.text.Utilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Processor {
	
	Scanner scr  =new Scanner(System.in);
	private String tokenNumbers = "";
	private String tokenWords = "";
	private LinkedList<DocumentTable> docsTableList = new LinkedList<>();
	private LinkedList<Dictionary> dictionaryList = new LinkedList<>();
	private LinkedList<Postings> postingsList = new LinkedList<>();
	private HashMap<String, LinkedList<HashMap<String, Integer>>> tfMap = new HashMap<>();
	
	public Processor(String path, String documentTable, String dictionary, String posting) { 
		parseAllDocument(path, documentTable, dictionary, posting);
	}
	
	public void parseAllDocument(String path, String documentTable, String dictionary, String posting) {
		
		/*Parse Documents*/ 
		try {
			
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			
			for(File file : listOfFiles) {
				//System.out.println("********"+file.getName()+"**********");
				makeTokens(Jsoup.parse(new File(file.getAbsolutePath()), "UTF-8"),file.getName());
				//System.out.println("____________________________________"); 
			}
			
			buildDictionaryAndPosting(new TreeMap<String, LinkedList<HashMap<String, Integer>>>(tfMap));
			exportFiles(documentTable, dictionary, posting);
			computeQuery(documentTable, dictionary, posting);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		/*End of Parse Documents*/
		
	}

	public void computeQuery(String documentTable, String dictionary, String posting) {	
		
		while(true) {
			System.out.println("Enter the query to be searched...");
			String query = scr.nextLine();
			generateResult(query, documentTable, dictionary, posting);
			scr.reset();
			System.out.println("Do you want to continue? (Enter 'EXIT' to discontinue | 'NO' to continue)");
			String exit = scr.nextLine();
			if("EXIT".equalsIgnoreCase(exit))
				break;
		}
		System.out.println("Program terminates...");
		
	}
	
	public void generateResult(String query, String documentTable, String dictionary, String posting) {	
		
		LinkedList<String> dictRetrieved = new LinkedList<>();
		LinkedList<LinkedList<String>> postRetrieved = new LinkedList<>();
		Set<String> finalList = null;
		String[] queryTerms = null;
		if(query.length() >= 2) {
			query = query.replace("(", "").replace(")", "").toLowerCase();// trimming the parenthesis
			queryTerms = query.split(" ");
			
			if(queryTerms.length > 1) {
				String csvFile = dictionary;
		        BufferedReader br = null;
		        String line = "";
		        String cvsSplit = ",";
		        for(String singleQTerms : queryTerms) {
		        	if(!(singleQTerms.equalsIgnoreCase("AND") || singleQTerms.equalsIgnoreCase("OR"))) {
				        try {
				            br = new BufferedReader(new FileReader(csvFile));
				            while ((line = br.readLine()) != null) {
				            	String[] dictionaryTerms = line.split(cvsSplit);
				            	if(!"".equals(dictionaryTerms[0]) && dictionaryTerms[0] != null && !"".equals(singleQTerms) && singleQTerms != null) {
					            	if(dictionaryTerms[0].equals(singleQTerms) && !(singleQTerms.equalsIgnoreCase("and") || singleQTerms.equalsIgnoreCase("or"))) {
					            		line = line.replace("'", "");
					            		dictRetrieved.add(line);
					            	}
				            	}
				            }
				            br.close();
				        }
				        catch(Exception e) {
				        	System.out.println("Error in reading dictionary file:"+e.getMessage());
				        }
		        	}
		        }
		        
			}
			else {
				System.out.println("Please enter proper query !!");
			}
			/**/
			
			String csvFile = posting;
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplit = ",";
	        for(int i=0;i<dictRetrieved.size();i++) {
	        	String postLine = dictRetrieved.get(i);
	        	String[] postLineArr = postLine.split(cvsSplit);
	        	LinkedList<String> subList = new LinkedList<>();
	        	if(!"".equals(postLine) && postLine != null) {
			        try {
			            br = new BufferedReader(new FileReader(csvFile));	
			            int offset = 0;
			            boolean flag = false;
			            while ((line = br.readLine()) != null) {
			            	if((offset == Integer.parseInt(postLineArr[2])) || flag) {
			            		flag = true;
			            		String[] docIds = line.split(cvsSplit);
			            		subList.add(docIds[0].substring(1, docIds[0].length()));
			            		if((Integer.parseInt(postLineArr[1]) + Integer.parseInt(postLineArr[2])) == (offset+1) ) {
			            			break;
			            		}
			            	}
			            	offset++;
			            }
			            br.close();
			        }
			        catch(Exception e) {
			        	e.printStackTrace();
			        }
	        	}
	        	postRetrieved.add(subList);
	        }
	        //System.out.println(postRetrieved); 
	        if(dictRetrieved.isEmpty() || postRetrieved.isEmpty()) {
				System.out.println("*****************QUERY*****************"); 
				System.out.println(query); 
				System.out.println("*****************OUTPUT START*****************");
				System.out.println("--NO RESULTS--");
				System.out.println("*****************OUTPUT END*****************");
				return;
	        }
	        
	        if("AND".equalsIgnoreCase(queryTerms[0])) {
	        	finalList = intersection(postRetrieved);
	        }
	        else if("OR".equalsIgnoreCase(queryTerms[0])) {
	        	finalList = union(postRetrieved);
	        }
	        
	        List<String> mainOutput = createOutput(finalList, documentTable);
	        System.out.println("*****************QUERY*****************"); 
	        System.out.println(query); 
	        System.out.println("*****************OUTPUT START*****************");
	        if(mainOutput != null && !mainOutput.isEmpty()) {
	        	for(String entry : mainOutput) {
		        	System.out.println(entry);
		        }
	        }
	        else {
	        	System.out.println("--NO RESULTS--");
	        }
	        createOutput(mainOutput,query);
	        System.out.println("*****************OUTPUT END*****************");
		}
		else {
			System.out.println("Please enter proper query in format (AND|OR)<space>(term1 term2 ...) ");
		}
		
	}
	public void createOutput(List<String> mainOutput, String query) {
		try {
			
			PrintWriter pWriter = new PrintWriter("output.txt", "UTF-8");
			pWriter.println("********************QUERY*********************"); 
			pWriter.println(query); 
			pWriter.println("*****************OUTPUT START*****************");
			if(mainOutput != null && !mainOutput.isEmpty()) {
	        	for(String entry : mainOutput) {
	        		pWriter.println(entry);
	        	}
			}
			else {
				pWriter.println("--NO RESULTS--");
			}
			pWriter.println("*****************OUTPUT END*****************");
			pWriter.close();
		}
		catch(Exception e) {
			System.out.println("Exception while writing in output file"+e.getMessage());
		}
	}
	
	public List<String> createOutput(Set<String> finalList, String documentTable) {
		//System.out.println(finalList); 
		LinkedList<String> list1 = null;
		if(finalList != null && !finalList.isEmpty()) {
			list1 = new LinkedList<>();
			List listF = new ArrayList(finalList);
			String txtFile = documentTable;
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplitDoc = ",";
	        
	        try {
	        	System.out.println(txtFile); 
	            br = new BufferedReader(new FileReader(txtFile));
	            while ((line = br.readLine()) != null) {
	            	String singleLine = line;
	            	String[] array = singleLine.split("\\|");
	            	
	            	String doc = array[0];
	            	if(listF.contains(doc)) {
		            	if(!"".equals(array[4]) && array[4] != null) {
		            		if("P".trim().equalsIgnoreCase(array[4])) {
		            			list1.addFirst(line);
		            			 
		            		}
		            		else if("N".trim().equalsIgnoreCase(array[4]) || "NA".trim().equalsIgnoreCase(array[4])) {
		            			list1.addLast(line);
		            		}
		            	}
	            	}	
	            }
	            
	            br.close();
	        }
	        catch(Exception e) {
	        	System.out.println("Error in reading posting file"+e.getMessage());
	        }
		}
		
        return list1;
        
	}
	public Set<String> intersection(List<LinkedList<String>> setOfLists){
		
		System.out.println("setOfLists:"+setOfLists); 
		if(setOfLists.size() >= 2) {
			List<String> intersectionList = new ArrayList<>(setOfLists.get(0));
			
			for(LinkedList<String> list : setOfLists) {
				intersectionList.retainAll(list);
			}
			Set<String> unionList = new HashSet<String>(intersectionList);
			
			return unionList;
		}
		System.out.println("No common retrieved intersection");
		
		return null;
		
	}
	
	public Set<String> union(List<LinkedList<String>> setOfLists){
		
		if(setOfLists.size() >= 1) {
			List<String> allElelemts = new ArrayList<>(setOfLists.get(0)); 
			
			for(LinkedList<String> list : setOfLists) {
				allElelemts.addAll(list);
			}
			Set<String> unionList = new HashSet<String>(allElelemts);
			return unionList;
		}
		return null;
	}

	public void makeTokens(Document document, String documentName) { 
		documentName = documentName.replaceFirst("[.][^.]+$", "");
		tokenNumbers = "";
		tokenWords = "";
		DocumentTable dTable = new DocumentTable();
		dTable.setDocNo(documentName.trim());
		/*reviewer*/
		String[] spstr = document.selectFirst("pre").text().split("\n");
		dTable.setTitle(spstr[0].trim());
		String[] spstr2 = spstr[spstr.length-1].trim().split(" ");
		String reviewer = "";
		for(int i=2;i<spstr2.length;i++) {
			reviewer += spstr2[i]+" ";
		}
		dTable.setReviewer(reviewer.trim());
		/*capsule review*/
		String capReview = "";
		if(!"".equals(document.select("p:contains(capsule)").text())) {
			capReview = document.select("p:contains(capsule)").text().split(":")[1];
		}
		else {
			capReview = document.selectFirst("p").text();
		}
		dTable.setSnippet(capReview.trim());
		/*rate*/
		dTable.setRate(""); 
		if(!"".equals(document.select("*:contains(scale)").text())) {
			String ratePara = document.select("*:contains(scale)").text();
			String[] rateParaArray = ratePara.split(" ");
			String actualRate = "";
			for(String eachWord : rateParaArray) {
				try {
					Integer.parseInt(eachWord);
					if(Integer.parseInt(eachWord) > (-4) && Integer.parseInt(eachWord) < (4)) {
						if(!"".equals(actualRate)) {
							if((Integer.parseInt(eachWord) >= Integer.parseInt(actualRate))) {
								actualRate = eachWord;
							}
						}
						else {
							actualRate = eachWord;
						}
					}
				}
				catch(Exception e){
					
				}
			}
			if(!"".equals(actualRate)) {
				if(Integer.parseInt(actualRate) >= 0)
					dTable.setRate("P");
				else
					dTable.setRate("N"); 
			}
		}
		else{
			String[] capsuleArr = capReview.trim().split(" ");
			int posWord = 0;
			int negWord = 0;
			for(String eachWord : capsuleArr) {
				if(Arrays.asList(new Commons().getPosWords()).contains(eachWord.toUpperCase())) {
					posWord++;
				}
				else if(Arrays.asList(new Commons().getNegWords()).contains(eachWord.toUpperCase())) {
					negWord++;
				}	
			}
			if(posWord >= negWord) {
				dTable.setRate("P");
			}
			else {
				dTable.setRate("N");
			}
		}
		if("".equals(dTable.getRate()))
			dTable.setRate("NA");
		docsTableList.add(dTable);
				
		String[] rawTokens = document.select("body").text().replaceAll("(?m)^[ \t]*\r?\n", "").split(" ");
		for(String str : rawTokens) {
			if(!"".equals(str) && str != null) { 
				
				str = str.replaceAll("[^a-zA-Z0-9-'\\s+]", "").replaceAll("(?m)^[ \t]*\r?\n", "").toLowerCase().trim();// remove punctuation and lowering case
				//remove stop words
				if(Arrays.asList(new Commons().getRemoveWordList()).contains(str)) {
					continue;
				}
								
				//remove apostrophe
				str = str.replaceAll("\'[a-z]$", "").replaceAll("[s]\'$", "");
				
				//applying minimal stemming
				if(str.matches("[a-z]{0,}[^ae]{1}ies$")) {
					str = str.substring(0,str.length()-3) + "y";
				}
				else if (str.matches("[a-z]{0,}[^aeo]{1}es$")) {
					str = str.substring(0,str.length()-2) + "e";
				}
				else if(str.matches("[a-z]{0,}[^us]{1}s$")) {
					str = str.substring(0,str.length()-1);
				}
				//remove single character words
				if(str.length() == 1) {
					continue;
				}
				//indexing all numbers together and separating words and numbers with hyphens
				if(str.matches("-?\\d+") || str.matches("\\+?\\d+")) {
					
					if(str.contains("-")) {
						for(String s : str.split("-")) {
							if(!"".equals(s))
								tokenNumbers += s+",";
						}
					}
					else {
						tokenNumbers += str+",";
					}
				}
				else {
					if(str.contains("-")) {
						for(String s : str.split("-")) {
							if(!"".equals(s))
								tokenWords += s+",";
						}
					}
					else {
						if(!"".equals(str))
							tokenWords += str+",";
					}
				}				
			}
		}
		tokenWords += tokenNumbers;
		String[] allTokens = tokenWords.split(",");
		for(String eachToken : allTokens) {
			if(tfMap.containsKey(eachToken)) {
				boolean isDocPresent = false;
				LinkedList<HashMap<String, Integer>> list = tfMap.get(eachToken);
				for(HashMap<String, Integer> map : list) {
					for(Entry<String, Integer> entry : map.entrySet()) {
						if(entry.getKey().equals(documentName)) {
							int value = map.get(entry.getKey());
							map.put(entry.getKey(), ++value);
							isDocPresent = true;
						}
					}
				}
				if(!isDocPresent) {
					HashMap<String, Integer> newMap = new HashMap<>();
					newMap.put(documentName, 1);
					list.add(newMap);
				}
				
			}
			else {
				LinkedList<HashMap<String, Integer>> list = new LinkedList<>();
				HashMap<String, Integer> newMap = new HashMap<>();
				newMap.put(documentName, 1);
				list.add(newMap);
				tfMap.put(eachToken, list); 
			}
		}
		
	}
	
	public void buildDictionaryAndPosting(Map<String, LinkedList<HashMap<String, Integer>>> tfMapSorted) {	
		/*Build Dictionary and posting list*/
		int offset = 0;
		Entry<String, LinkedList<HashMap<String, Integer>>> prev = null;
		for(Entry<String, LinkedList<HashMap<String, Integer>>> entry: tfMapSorted.entrySet()) {
			Dictionary dictionaryObj = new Dictionary();
			String keyTerm = entry.getKey();
			if(!"".equals(keyTerm)) {	
				LinkedList<HashMap<String, Integer>> mainList = entry.getValue();
				for(HashMap<String, Integer> map : mainList) {
					for(Entry<String, Integer> eachMap : map.entrySet()) {
						String docId = eachMap.getKey();
						int tf = eachMap.getValue();
						postingsList.add(new Postings(docId, tf));
					}
				}
				if(dictionaryList.isEmpty()) {
					dictionaryList.add(new Dictionary(keyTerm, mainList.size(), offset));
				}
				else if(!dictionaryList.isEmpty()) {
					dictionaryList.add(new Dictionary(keyTerm, mainList.size(), offset += prev.getValue().size()));
				}
				prev = entry;
			}
		}
	}

	public void exportFiles(String documentTable, String dictionary, String posting) { 
		
		try {
			
			PrintWriter pWriter = new PrintWriter(documentTable, "UTF-8");
			
			for(DocumentTable dt : docsTableList) {	
				pWriter.println(dt.getDocNo()+"|"+dt.getTitle()+"|"+dt.getReviewer()+"|"+dt.getSnippet()+"|"+dt.getRate());
			}
			pWriter.close();
			System.out.println("Document Table created successfully!!!");
		}
		catch(Exception e) {
			System.out.println("Exception while writing in DocTable file"+e.getMessage());
		}
		
		try {
			
			File file = new File(dictionary);
			if(file.exists()) {
				file.delete();
			}
			PrintWriter pWriter = new PrintWriter(dictionary, "UTF-8");
			for(Dictionary entry : dictionaryList) { 
				pWriter.println(entry.getTerm()+","+entry.getDocFreq()+","+entry.getOffset());
			}
			pWriter.close();
			System.out.println("Dictionary created successfully!!!");
		}
		catch(Exception e) {
			System.out.println("Exception while writing in Dictionary file"+e.getMessage());
		}
		
		try {
			
			File file = new File(posting);
			
			if(file.exists()) {
				file.delete();
			}
			PrintWriter pWriter = new PrintWriter(posting, "UTF-8");
			for(Postings postingObj : postingsList) {
				
				pWriter.println("'"+postingObj.getDocID()+","+postingObj.getTermFreq());
			}
			pWriter.close();
			System.out.println("Postings created successfully!!!");
		}
		catch(Exception e) {
			System.out.println("Exception while writing in posting's File"+e.getMessage());
		}
		
	}
}
