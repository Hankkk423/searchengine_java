package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.indexing.InvertedIndex;
import cecs429.indexing.DiskIndexWriter;
import cecs429.indexing.DiskPositionalIndex;

import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;

import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;

import cecs429.retrievals.RankedRetrieval;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import org.tartarus.snowball.*;


public class Project1main{
  public static void main(String args[]) throws Throwable{
    BooleanQueryParser BQparser = new BooleanQueryParser();
    Index II = null;
    Scanner scan = new Scanner(System.in);
    String dataPath = "/Users/hank/Desktop/SearchFoundations_Java/DATA/";  // file path: /Users/hank/Desktop/
    String corpusPath = "/Users/hank/Documents/Fall 2022/CECS 529 Search Engine/";
    String fileName = "all-nps-sites-extracted"; 
    String fileExtension = ".json";
    Long startTime;
    Long endTime;
    Long elapsedTime;
    boolean A_processor = true;
    TokenProcessor TP = new AdvancedTokenProcessor();
    boolean rankMode;
  
    
    System.out.println("Enter Bin file path: ");
    //dataPath = scan.nextLine();
    
    System.out.println("Enter corpus path: ");
    //corpusPath = scan.nextLine();
    
    System.out.println("Current Bin file path: " + "\"" + dataPath + "\"");
    System.out.println("Current corpus file: " + "\"" + corpusPath + fileName + "\"");
    DirectoryCorpus JDirCorpus = new DirectoryCorpus(Paths.get(corpusPath + fileName).toAbsolutePath());
    JDirCorpus.registerFileDocumentFactory(fileExtension, JsonFileDocument::loadJsonFileDocument);
    DocumentCorpus Corpus = JDirCorpus;
    
    System.out.println("Choose re-indexing? (1.Build index/ 2.Query index)");
    if(scan.nextInt() == 1){
      
      startTime = System.currentTimeMillis();
    
      System.out.println("Indexing...");
      II = constructInvertedIndex(Corpus); //InvertedIndex
      DiskIndexWriter DW = new DiskIndexWriter(II, dataPath, Corpus.getCorpusSize());

      endTime = System.currentTimeMillis();
      elapsedTime = (endTime - startTime) / 1000;
      System.out.println(elapsedTime + " second"); 
    }
    else{
      //no need to re-index, just simply read documents
      System.out.println("Simply read documents...");
      readDocuments(Corpus);
    }

    Index DI = new DiskPositionalIndex(dataPath);

    System.out.println("Choose Retrievals?: (1: Boolean Mode/ 2: Ranked Mode)");
    if(scan.nextInt() == 1){
      rankMode = false;
    }
    else{
      rankMode = true;
    }

    

    System.out.println("Start searching: ");    
    scan.nextLine();
    boolean done = false;
    String input;
    while(done != true){
      input = scan.nextLine();
      
      int colon = input.indexOf(":", 0);
      if(colon == 0){
        // special queries
        // do...
        String[] type = input.split(" "); 
        
        if(type[0].equals(":index")){
          // do: re-indexing

          if(type.length != 3){
            System.out.println("Invalid Input, :index <fileName> <fileExtension>");
            continue;
          }
          
          fileName = type[1];
          fileExtension = type[2];
          
          if(fileExtension.equals(".txt")){
            Corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusPath + fileName).toAbsolutePath(), fileExtension);

            startTime = System.currentTimeMillis();
            System.out.println("Indexing...");
            II = constructInvertedIndex(Corpus); //InvertedIndex
            DiskIndexWriter DW = new DiskIndexWriter(II, dataPath, Corpus.getCorpusSize());

            endTime = System.currentTimeMillis();
            elapsedTime = (endTime - startTime) / 1000;
            System.out.println(elapsedTime + " second");
          }
          else if(fileExtension.equals(".json")){
            JDirCorpus = new DirectoryCorpus(Paths.get(corpusPath + fileName).toAbsolutePath());
            JDirCorpus.registerFileDocumentFactory(fileExtension, JsonFileDocument::loadJsonFileDocument);
            Corpus = JDirCorpus;

            startTime = System.currentTimeMillis();
            System.out.println("Indexing...");
            II = constructInvertedIndex(Corpus); //InvertedIndex
            DiskIndexWriter DW = new DiskIndexWriter(II, dataPath, Corpus.getCorpusSize());

            endTime = System.currentTimeMillis();
            elapsedTime = (endTime - startTime) / 1000;
            System.out.println(elapsedTime + " second");
          }
          else{
            System.out.println("Can't register this file extension, check typo");
          }
          
        }
        else if(type[0].equals(":processor")){
          //do: switch the token processor
          
          if(type[1].equals("advanced")){
            startTime = System.currentTimeMillis();
            System.out.println("Indexing...");
            II = constructInvertedIndex(Corpus); //InvertedIndex
            DiskIndexWriter DW = new DiskIndexWriter(II, dataPath, Corpus.getCorpusSize());

            endTime = System.currentTimeMillis();
            elapsedTime = (endTime - startTime) / 1000;
            System.out.println(elapsedTime + " second");

            A_processor = true;
          }
          else if(type[1].equals("basic")){
            startTime = System.currentTimeMillis();
            System.out.println("Indexing...");
            II = constructInvertedIndex_Basic(Corpus); //InvertedIndex
            DiskIndexWriter DW = new DiskIndexWriter(II, dataPath, Corpus.getCorpusSize());

            endTime = System.currentTimeMillis();
            elapsedTime = (endTime - startTime) / 1000;
            System.out.println(elapsedTime + " second");

            A_processor = false;
          }
          else{
            System.out.println("No such processor (only advanced/basic)");
          }
          
        }
        else if(type[0].equals(":version")){
          // print the "current folder" and "TokenProcessor" 
          System.out.println("Current File: " + corpusPath + fileName + fileExtension);

          if(A_processor == true){
            System.out.println("Current Processor: advanced");
          }
          else{
            System.out.println("Current Processor: basic");
          }
          
        }
        else if(type[0].equals(":document")){
          // print the document's content and title by inputing doc id
          
          int id = Integer.valueOf(type[1]);
          if(Corpus.getDocument(id) == null){
            System.out.println("The document id doesn't exist!");
          }
          else{
            System.out.println("Title:");
            System.out.println(Corpus.getDocument(id).getTitle());
            System.out.println("Contents:");
            EnglishTokenStream ets = new EnglishTokenStream(Corpus.getDocument(id).getContent());
            for(String s : ets.getTokens()){
              System.out.print(s + " ");
            }
            System.out.println();  
          }
        }        
        else if(type[0].equals(":q")){
          // do: exit the program
          break;
        }
        else if(type[0].equals(":stem")){
          // do: stem token
          
          String language = "english";
          Class stemClass = Class.forName("org.tartarus.snowball.ext." + language + "Stemmer");
          SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
          
          String term = type[1].toLowerCase();
          stemmer.setCurrent(term);
          stemmer.stem();
          System.out.println("After Stem: " + stemmer.getCurrent());

        }
        else if(type[0].equals(":vocab")){
          // do: print first 1000 vocabulary

          List<String> all = DI.getVocabulary();
          List<String> sub = new ArrayList<String>();
          for(int i = 0; i<1000; i++){
            sub.add(all.get(i));
          }

          System.out.println(sub);
        }
        else{
          System.out.println("Can't recognize!!!");
        }
        
      }
      else{
        // input is searching words, not special queries

        String query = input;
        
        if(A_processor == false) {
          // means we are using basic processor right now
          // remove non-alphanumeric words but not "/""
          query = query.replace(".", "").replace(",", "").replace(":", "").replace("'", "");
          TP = new BasicTokenProcessor();
        }
        else {
        	TP = new AdvancedTokenProcessor();
        }
        

        if(rankMode == true){
          //rank mode
          RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
          List<ArrayList<Double>> Scores = RR.getScores(10);
          
          if(Scores.isEmpty()){
            System.out.println("no document match");
          }

          for(ArrayList<Double> i : Scores){
            int id = (int)Math.round(i.get(0));
            double score = i.get(1);
            String title = Corpus.getDocument(id).getTitle();
            System.out.print(title + " (ID: " + id + ")");
            System.out.println(", Score: " + score);
          }
          
        }
        else{
          //boolean mode
          QueryComponent qc = BQparser.parseQuery(query, TP);
          List<Posting> P = qc.getPostings(DI);
          
          if(P.isEmpty()){
            System.out.println("no document match");
          }

          for(Posting i : P){
            int id = i.getDocumentId();
            String title = Corpus.getDocument(id).getTitle();
            System.out.println(title + " (ID: " + id + ") ");
          }

          System.out.println(P.size() + " documents");
        }
        
      }
      
    } //while loop
    
  }



  //simply read documents
  private static void readDocuments(DocumentCorpus corpus) {
    for (Document d : corpus.getDocuments()) {
      d.getContent();
    } 
  }

  
  //store every term into "Inverted Index class" from documents
  private static InvertedIndex constructInvertedIndex(DocumentCorpus corpus){
    AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
    InvertedIndex ii = new InvertedIndex();
        
	  for (Document d : corpus.getDocuments()) {
      EnglishTokenStream ets = new EnglishTokenStream(d.getContent());
	    //System.out.println("Found document " + d.getTitle() + " ID " + d.getId());
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

    return ii;
  }
  
  
  //store every term into "Inverted Index class" from documents (Basic Processor)
  private static InvertedIndex constructInvertedIndex_Basic(DocumentCorpus corpus){
    BasicTokenProcessor processor = new BasicTokenProcessor();
    InvertedIndex ii = new InvertedIndex();
    
    for (Document d : corpus.getDocuments()) {
      EnglishTokenStream ets = new EnglishTokenStream(d.getContent());
			//System.out.println("Found document " + d.getTitle() + " ID " + d.getId());
      int position = 0;
      int id = d.getId();
      
      for(String s : ets.getTokens()){
      	List<String> sList = processor.processToken(s);
        ii.addTerm(sList.get(0), id, position);
        
        position++;
      }
      
    }

    return ii;
  }

}