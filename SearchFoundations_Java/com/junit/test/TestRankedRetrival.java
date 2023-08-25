package com.junit.test;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import org.junit.BeforeClass;
import org.junit.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.retrievals.RankedRetrieval;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;


public class TestRankedRetrival{
	private static DocumentCorpus Corpus = null;
	private TokenProcessor TP = new AdvancedTokenProcessor();
	private static String dataPath = null;
	
	@BeforeClass  
  public static void setUpBeforeClass() {
		
		System.out.println("before class");  
    dataPath = "/Users/hank/Desktop/testFiles/testDATA/";  // file path: /Users/hank/Desktop/
    String corpusPath = "/Users/hank/Desktop/testFiles/";
    String fileName = "testDocuments";
    String fileExtension = ".json";
		DirectoryCorpus JDirCorpus = new DirectoryCorpus(Paths.get(corpusPath + fileName).toAbsolutePath());
    JDirCorpus.registerFileDocumentFactory(fileExtension, JsonFileDocument::loadJsonFileDocument);
    Corpus = JDirCorpus;
    	
		for (Document d : Corpus.getDocuments()) {
			d.getContent();
			System.out.println("Found document " + d.getTitle() + " ID " + d.getId());
    }
		
	}
	
	
	@Test
	public void testScores() {
		//simply verify if the score is correct, Wdt*Wqt/Ld
		
		String query = "n";  //top1: doc4/ both doc4 doc5 have "n" but doc4 has higher Wdt 
		
		RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
    List<ArrayList<Double>> Scores = RR.getScores(1);  //get top 1 doc
    
    
    int id = (int)Math.round(Scores.get(0).get(0));
    double score = Scores.get(0).get(1);
    String title = Corpus.getDocument(id).getTitle();
    //verify doc title
    assertEquals("test document no.4", title);
   
    //verify score
    assertEquals(0.744793, Math.round(score*100000.0)/ 100000.0, 0.04);
    
	}
	
	
	@Test
	public void testHigherWdt() {
		//focus on higher Wdt with same Wqt and Ld, both doc4 doc5 have "n" "o" and they have a same Ld,
		//but doc5 has higher Wdt for term "o"
		
		String query = "n o";  //top1: doc5 
		
		RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
    List<ArrayList<Double>> Scores = RR.getScores(1);  //get top 1 doc
    
    
    int id = (int)Math.round(Scores.get(0).get(0));
    double score = Scores.get(0).get(1);
    String title = Corpus.getDocument(id).getTitle();
    //verify doc title
    assertEquals("test document no.5", title);
    
    //verify score
    assertEquals(1.48746, Math.round(score*100000.0)/ 100000.0, 0.04);
    
	}
	
	
	@Test
	public void testHigherLd() {
		//focus on doc which has a higher Wdt but with a higher Ld as well, 
		//means it does not necessarily be more relevant.
		//doc8 should not be more relevant because it has very high "Ld".
		
		String query = "p q";  //top1: doc7/ both doc7 doc8 have "p" "q" but doc8 has higher "Ld" 
		
		RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
    List<ArrayList<Double>> Scores = RR.getScores(2);  //get top 1 doc
    
    
    int id = (int)Math.round(Scores.get(0).get(0));
    double score = Scores.get(0).get(1);
    String title = Corpus.getDocument(id).getTitle();
    //verify doc title
    assertEquals("test document no.7", title);
    
    //verify score
    System.out.println("doc 7 8 scores for ['p q']: " + Scores);
    assertEquals(1.93284, Math.round(score*100000.0)/ 100000.0, 0.04);
    
	}
	
	
	@Test
	public void testHigherWqt() {
		//focus on doc which contains a very uncommon term even though it has a very high Ld, 
		//means the term's Wqt is very high and will be considered as an very important information 
		//even though its Wdt is reletively low.
		//In this scenario, the score of doc8 will become larger than doc7.
		
		String query = "p q s";  //top1: doc8/ doc8 has term "s" which is very uncommon in corpus,
		                         //therefore, the Wqt of "s" is extremely high.
		
		RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
    List<ArrayList<Double>> Scores = RR.getScores(2);  //get top 1 doc
    
    
    int id = (int)Math.round(Scores.get(0).get(0));
    double score = Scores.get(0).get(1);
    String title = Corpus.getDocument(id).getTitle();
    //verify doc title 
    assertEquals("test document no.8", title);
    
    //verify score
    System.out.println("doc7 8 scores for ['p q s']: " + Scores);
    assertEquals(1.95175, Math.round(score*100000.0)/ 100000.0, 0.04);
    
	}
	
	
	
	@Test
	public void testStopWord() {
		String query = "e c";  //top1: doc1/ doc123 have both "e" and "c", and "e" is almost everywhere
		                       //in corpus. therefore, even though doc3 has the largest tf for "e"(5),
		                       //"e" is similar to the stop word, so its Wqt is very low. 
		                       //so in this case, we care more about "c".

    RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
    List<ArrayList<Double>> Scores = RR.getScores(3);  //get top 1 doc

    
    int id = (int)Math.round(Scores.get(0).get(0));
    double score = Scores.get(0).get(1);
    String title = Corpus.getDocument(id).getTitle();
    //verify doc title
    assertEquals("test document no.1", title);

    //verify score
    assertEquals(0.91285, Math.round(score*100000.0)/ 100000.0, 0.04);
		
	}
	

}
