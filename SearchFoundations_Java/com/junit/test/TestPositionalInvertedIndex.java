package com.junit.test;

import static org.junit.Assert.*;

import java.nio.file.Paths;
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


public class TestPositionalInvertedIndex {
	private static DocumentCorpus Corpus = null;
	private static InvertedIndex ii;
	
	
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
    InvertedIndex II = new InvertedIndex();
        
	  for (Document d : Corpus.getDocuments()) {
      EnglishTokenStream ets = new EnglishTokenStream(d.getContent());
	    System.out.println("Found document " + d.getTitle() + " ID " + d.getId());
      int position = 0;
      int id = d.getId();
      
      for(String s : ets.getTokens()){
        List<String> sList = processor.processToken(s);
        
        for(String each : sList){
          II.addTerm(each, id, position);
        }
        
        position++;  // T-T-T will has a same position
      }
    }
	  
	  ii = II;
	  
  }  
	 
	

	@Test
	public void testInvertedIndex_1() {
		//test term with single position
		
		InvertedIndex iiTest = new InvertedIndex(); //a: doc1/ b: doc1, doc2 
		iiTest.addTerm("a", 0, 0);
		iiTest.addTerm("b", 0, 1);
		iiTest.addTerm("b", 3, 0);
		
		
		List<Posting> list = ii.getPostings("a");
		List<Posting> listTest = iiTest.getPostings("a");
		
		for(int i=0; i<1; i++) {
			assertEquals(listTest.get(0).getPositions().get(i), list.get(0).getPositions().get(i));
		}
		
		list = ii.getPostings("b");
		listTest = iiTest.getPostings("b");
		
		for(int i=0; i<2; i++) {
			assertEquals(listTest.get(i).getPositions().get(0), list.get(i).getPositions().get(0));
		}
		
		
	}
	
	
	@Test
	public void testInvertedIndex_2() {
		//test term with 2 positions.
		
		InvertedIndex iiTest = new InvertedIndex(); //c: doc1 w 3 positions/ b: doc1 w 2 positions 
		iiTest.addTerm("c", 0, 2);
		iiTest.addTerm("c", 0, 11);
		iiTest.addTerm("c", 0, 13);
		iiTest.addTerm("b", 0, 1);
		iiTest.addTerm("b", 0, 12);
		
		List<Posting> list = ii.getPostings("c");
		List<Posting> listTest = iiTest.getPostings("c");
		
		for(int i=0; i<3; i++) {
			assertEquals(listTest.get(0).getPositions().get(i), list.get(0).getPositions().get(i));
		}
		
		
		list = ii.getPostings("b");
		listTest = iiTest.getPostings("b");
		
		for(int i=0; i<2; i++) {
			assertEquals(listTest.get(0).getPositions().get(i), list.get(0).getPositions().get(i));
		}
		
	}
	
	
	@Test
	public void testInvertedIndex_3() {
		//test vocab doesn't exist
		
		List<Posting> list = ii.getPostings("z");
		assertEquals(true, list.isEmpty());
		
	}
	

}
