package com.junit.test;

import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.junit.BeforeClass;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.indexing.DiskIndexWriter;
import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.Index;
import cecs429.indexing.InvertedIndex;
import cecs429.indexing.Posting;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;


public class TestQueryProcessing{
	private static DocumentCorpus Corpus = null;
	private static Index DI = null;
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
	public void testPhraseQuery() {
		//test phrase
		
		String query = "\"f e\"";  //doc2
		
		BooleanQueryParser BQparser = new BooleanQueryParser();
		QueryComponent qc = BQparser.parseQuery(query, TP);
    List<Posting> P = qc.getPostings(DI);
    if(P.isEmpty()){
      System.out.println("no document match");
    }
    
    int id = P.get(0).getDocumentId();
    String title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.2", title);
    
    
    query = "\"f h\"";  //no such phrase
    qc = BQparser.parseQuery(query, TP);
    P = qc.getPostings(DI);
    assertEquals(true, P.isEmpty());

  }
	
	
	@Test
	public void testAndQueries() {
		//test AND
		
		String query = "a b";  //doc1
		
		BooleanQueryParser BQparser = new BooleanQueryParser();
		QueryComponent qc = BQparser.parseQuery(query, TP);
    List<Posting> P = qc.getPostings(DI);
    if(P.isEmpty()){
      System.out.println("no document match");
    }
    
    int id = P.get(0).getDocumentId();
    String title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.1", title);
    
    
    query = "c m";  //doc3
    qc = BQparser.parseQuery(query, TP);
    P = qc.getPostings(DI);

    id = P.get(0).getDocumentId();
    title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.3", title);
    
	}
	
	
	@Test
	public void testOrQueries() {
		//test OR
		
		String query = "a + b";  //doc1
		
		BooleanQueryParser BQparser = new BooleanQueryParser();
		QueryComponent qc = BQparser.parseQuery(query, TP);
    List<Posting> P = qc.getPostings(DI);
    if(P.isEmpty()){
      System.out.println("no document match");
    }
    
    int id = P.get(0).getDocumentId();
    String title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.1", title);
    
    
    query = "b + m";  //"b":doc 1~2, "m":doc3~5
    qc = BQparser.parseQuery(query, TP);
    P = qc.getPostings(DI);
    
    assertEquals(5, P.size());
    	
	}
	
	
	@Test
	public void testTwoPhraseQueries() {
		//test "A (B + C)"
		
		String query = "\"d (f + i)\"";  //doc2(d f), doc3(d i)
		
		BooleanQueryParser BQparser = new BooleanQueryParser();
		QueryComponent qc = BQparser.parseQuery(query, TP);
    List<Posting> P = qc.getPostings(DI);
    if(P.isEmpty()){
      System.out.println("no document match");
    }
    
    int id = P.get(0).getDocumentId();
    String title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.2", title);
    
    id = P.get(1).getDocumentId();
    title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.3", title);
    
    
    query = "\"f (e + h)\"";  //doc2(f e), not such doc has phrase "f h"
    qc = BQparser.parseQuery(query, TP);
    P = qc.getPostings(DI);
    
    id = P.get(0).getDocumentId();
    title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.2", title);
    	
	}
	
	
	@Test
	public void testParenthesesQueries() {
		//test OR
		
		String query = "(a + x)";  //doc1, doc6
		
		BooleanQueryParser BQparser = new BooleanQueryParser();
		QueryComponent qc = BQparser.parseQuery(query, TP);
    List<Posting> P = qc.getPostings(DI);
    if(P.isEmpty()){
      System.out.println("no document match");
    }
    
    int id = P.get(0).getDocumentId();
    String title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.1", title);
    
    id = P.get(1).getDocumentId();
    title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.6", title);
    	
	}
	
	
	
	@Test
	public void testNearQuery() {
		//test phrase
		
		String query = "[k near/3 d]";  //doc3
		
		BooleanQueryParser BQparser = new BooleanQueryParser();
		QueryComponent qc = BQparser.parseQuery(query, TP);
    List<Posting> P = qc.getPostings(DI);
    if(P.isEmpty()){
      System.out.println("no document match");
    }
    
    int id = P.get(0).getDocumentId();
    String title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.3", title);
    
    
    
    query = "[k near/4 d]";  //doc2(near/4) and doc3, doc4(near/7)
    qc = BQparser.parseQuery(query, TP);
    P = qc.getPostings(DI);
    if(P.isEmpty()){
      System.out.println("no document match");
    }
    
    id = P.get(0).getDocumentId();
    title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.2", title);
    
    id = P.get(1).getDocumentId();
    title = Corpus.getDocument(id).getTitle();
    assertEquals("test document no.3", title);
    
  }
	
		

}
