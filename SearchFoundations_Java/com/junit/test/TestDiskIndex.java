package com.junit.test;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.indexing.DiskIndexWriter;
import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.Index;
import cecs429.indexing.InvertedIndex;
import cecs429.indexing.Posting;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

public class TestDiskIndex{
	private static DocumentCorpus Corpus = null;
	private static DiskPositionalIndex DI = null;
	//private boolean A_processor = true; 
	private TokenProcessor TP = new AdvancedTokenProcessor(); 
	
	
	@BeforeClass  
  public static void setUpBeforeClass() {  
    System.out.println("before class");  
    String dataPath = "/Users/hank/Desktop/testFiles/testDATA/";  // file path: /Users/hank/Desktop/
    String corpusPath = "/Users/hank/Desktop/testFiles/";
    String fileName = "testDocuments";
    String fileExtension = ".json";
		DirectoryCorpus JDirCorpus = new DirectoryCorpus(Paths.get(corpusPath + fileName).toAbsolutePath());
    JDirCorpus.registerFileDocumentFactory(fileExtension, JsonFileDocument::loadJsonFileDocument);
    Corpus = JDirCorpus;
    
    AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
    InvertedIndex ii = new InvertedIndex();
        
	  for (Document d : Corpus.getDocuments()) {
      EnglishTokenStream ets = new EnglishTokenStream(d.getContent());
	    System.out.println("Found document " + d.getTitle() + " ID " + d.getId());
      int position = 0;
      int id = d.getId();
      
      for(String s : ets.getTokens()){
        List<String> sList = processor.processToken(s);
        
        for(String each : sList){
          ii.addTerm(each, id, position);
        }
        
        position++;  // T-T-T will has a same position
      }
    }
	  
	  DiskIndexWriter DW = new DiskIndexWriter(ii, dataPath, Corpus.getCorpusSize());
	  
	  DI = new DiskPositionalIndex(dataPath);
	  
  }

	
	
	@Test
	public void testDocumentWeight() {
		//test doc weight "Ld", doc6
		
		List<Double> Ld = DI.getDocWeights();
		
		double doc6_wei = Math.round(Ld.get(1)*100000.0)/ 100000.0;
		assertEquals(3.93542, doc6_wei, 0.00002);
	}
	
	
	@Test
	public void testTermWeight() {
		//test Wdt of term "x".
		
		List<ArrayList<Double>> termWeight = DI.getTermWeightsInfo("x");
		
		assertEquals(2.60943, Math.round(termWeight.get(0).get(1)*100000.0)/ 100000.0, 0.00002);
	}
	
	
	@Test
	public void testGetPostingID() {
		//test function "getPostings_OnlyID()"
		
		List<Posting> p = DI.getPostings_OnlyID("x");
		int id = p.get(0).getDocumentId();
		
		String title = Corpus.getDocument(id).getTitle();
		assertEquals("test document no.6", title);
	}
	

}
