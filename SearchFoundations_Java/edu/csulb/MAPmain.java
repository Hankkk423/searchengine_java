package edu.csulb;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.File;  
import java.io.FileNotFoundException; 
import java.io.*;
import java.util.Scanner; 

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtilities; 


import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.indexing.DiskIndexWriter;
import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.InvertedIndex;
import cecs429.indexing.Posting;
import cecs429.querying.BooleanQueryParser;
import cecs429.querying.QueryComponent;
import cecs429.retrievals.RankedRetrieval;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;






public class MAPmain{
	
	private static DocumentCorpus Corpus = null;
	private static DiskPositionalIndex DI = null;
	private static TokenProcessor TP = new AdvancedTokenProcessor(); 
	private static BooleanQueryParser BQparser = new BooleanQueryParser();
	
	private static String globalPath = "/Users/hank/Desktop/relevance_cranfield";  //parks OR cranfield
	
	private static String dataPath      = globalPath + "/DATA/";  // the folder of Bin files
  private static String corpusPath    = globalPath + "/";
  private static String fileName      = "Documents";
  private static String fileExtension = ".json";
  private static String queryFile     = globalPath + "/relevance/queries";
  private static String qrelFile      = globalPath + "/relevance/qrel";
  private static String imageFilePath = globalPath + "/relevance/P-R curve.jpeg";
  
  private static List<ArrayList<String>> retriList = new ArrayList<ArrayList<String>>();
  private static double averageAd = 0.0;  //will be calculated in "retrivalQueries()" 
  private static Long averageTime = 0L;  //will be calculated in "retrivalQueries()" 
  private static List<Integer> pATkList;  //P@K list for the first query
  private static int numOfRel;  //store the total number of relevant doc for the first query
  private static List<Double> APlist = new ArrayList<Double>(); //the list of AP of all return doc
  

	public static void main(String[] args){
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Current folder: " + globalPath);
		
		System.out.println("-----------------------------------------------------");
		
		System.out.println("Re-Index? (1:Yes/ 2:No)");
		Boolean reIndex = true;
		if(scan.nextInt() == 2) {
			reIndex = false;
		}
		//scan.nextInt();
		
		System.out.println("loading documents...");
		loadDocuments(reIndex);  //load doc
		System.out.println("retrival queries...");
		retrievalQueries();  //get top 50 docs for each query
		
		System.out.println("-----------------------------------------------------");
		double MAP = calculateMAP();  //calculate mean average precision
		System.out.println("MAP: " + MAP);
		System.out.println("Average number of nonzero accumulators: " + averageAd);
		System.out.println("-----------------------------------------------------");
		System.out.println("Ranked retrieval results for the first query (.json file names):");
		System.out.println(retriList.get(0));
		System.out.println();
		System.out.println("The Average Precision Score for Default ranking at K=50 (for each query):");
		System.out.println(APlist);
		System.out.println("-----------------------------------------------------");
		
		System.out.println("Mean response time to satisfy a 'Ranked' query: " + averageTime + "(ms)");
		System.out.println("Throughput of Ranked queries: " + 1000/averageTime);
		System.out.println("-----------------------------------------------------");
		
		retrivalQueries_boolean();
		System.out.println("Mean response time to satisfy a 'Boolean' query: " + averageTime + "(ms)");
		System.out.println("Throughput of Boolean queries: " + 1000/averageTime);
		
		System.out.println("-----------------------------------------------------");
		System.out.println("Drawing P-R curve...");
		drawPRcurve();  //precision-recall curve for the first query,
		                //will save this curve as an image
		System.out.println("-----------------------------------------------------");
		
		System.out.println("Want to search? (1:Yes/ 2:No)");
		if(scan.nextInt() == 1) {
			System.out.println("Start Saerching: (':q' to break)");
			scan.nextLine();
			
			while(true) {
				String query = scan.nextLine();
				if(query.equals(":q")) {
					System.out.println("Done");
					break;
				}
				
				query = query.replace("-", " ").replace("(", "").replace(")", "");
				//query = query.replaceAll("[^a-zA-Z0-9]", "");
				
				RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
        List<ArrayList<Double>> Scores = RR.getScores(50);  //get top 50 doc
        
        if(Scores.isEmpty()){
          System.out.println("no document match");
          continue;
        }
        
        int id = 0;
        String fName;
        for(ArrayList<Double> i : Scores){
        	id = (int)Math.round(i.get(0));
        	fName = Corpus.getDocument(id).getName();
        	
        	System.out.println("File: " + fName + ".json" + ", Score: " + i.get(1));
        }
        System.out.println(Scores.size() + " documents");
				
			}
			
		}
		else {
			System.out.println("Done");
		}

	}
	
	
	private static void drawPRcurve(){
		double[] x = new double[50];  //Recall
		double[] y = new double[50];	//Precision
		
		int kPosition = 0;
		int target = 0;
		int RetANDRel = 0;  //the number of documents that are return AND Relevant so far
		for(int i=0; i<50; i++){
			if(target < pATkList.size()){
			  kPosition = pATkList.get(target);
			}
			
			if(i == kPosition){
				RetANDRel++;
				target++;
				
				x[i] = (double)RetANDRel/numOfRel;
				y[i] = (double)RetANDRel/(i + 1);
			}
			else{
				x[i] = (double)RetANDRel/numOfRel;
				y[i] = (double)RetANDRel/(i + 1);
			}
			
		}
		
		
		try {
			final XYSeries pr = new XYSeries( "pr" );
			
			for(int i=0; i<x.length; i++){
				pr.add(x[i], y[i]);
			}
			
			final XYSeriesCollection dataset = new XYSeriesCollection();
      dataset.addSeries(pr);
      
      JFreeChart xylineChart = ChartFactory.createXYLineChart(
          "precision-recall curve", 
          "Recall",
          "Precision", 
          dataset,
          PlotOrientation.VERTICAL, 
          true, true, false);
      
      
      int width = 640;   
      int height = 480; 
      File XYChart = new File(imageFilePath);
      ChartUtilities.saveChartAsJPEG(XYChart, xylineChart, width, height);
			
		} catch(Exception e) {
			System.out.print("error: " + e);
		}
		
		
	}  //Func drawPRcurve
	
	
	
	private static double calculateMAP(){
		double MAP = 0.0;
		pATkList = new ArrayList<Integer>();  //it only stores the first query's information
		
		try {
			
      File myObj = new File(qrelFile);
      Scanner myReader = new Scanner(myObj);
      
      int target = 0;  //means we are on which query right now
      double ap;
      int count;  //count how many relevant doc we got so far in retriList for one query
      while (myReader.hasNextLine()){
      	ap = 0.0;
      	count = 0;
      	
        String data = myReader.nextLine();
        //System.out.println(data);
        
        String[] relDocs = data.split(" ");
        if(target == 0){
        	numOfRel = relDocs.length;  //this is for P-R curve later
        }
        
        
        for(int i=0; i<retriList.get(target).size(); i++){
        	
        	for(int j=0; j<relDocs.length; j++){
        		
        		if(retriList.get(target).get(i).equals(relDocs[j]) || 
        				Integer.parseInt(retriList.get(target).get(i)) == Integer.parseInt(relDocs[j])){
        			
        			if(target == 0){  //means we store the first query's P@K information for precision-recall curve later
        				pATkList.add(i);
        			}
        			
        			count++;
        			ap = ap + (double)count/(i + 1);  //accumulate AP for this query
        			
        			break;
        			
        		}
        		
        	}
        	
        }
        
        ap = ap/relDocs.length;
        APlist.add(ap);        
        
        target++;
        
      }
      
      double totalAP = 0.0;
      for(double i : APlist){
      	totalAP = totalAP + i;
      }
      
      MAP = totalAP/APlist.size();
      
      myReader.close();
    } catch (FileNotFoundException e) {
    	
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
		
		
		return MAP;
		
	}  //Func calculateMAP
	
	
	
	
	private static void retrievalQueries(){
		int id;
		String fName;
		int totalAd = 0;
		Long startTime;
    Long endTime;
    Long elapsedTime;
    Long totalTime = 0L;
		
		try {
			
      File myObj = new File(queryFile);
      Scanner myReader = new Scanner(myObj);
      
      while (myReader.hasNextLine()){
        String query = myReader.nextLine();
        query = query.replace("-", " ").replace("(", "").replace(")", "");
        //System.out.println(query);
        
        startTime = System.currentTimeMillis();
        
        RankedRetrieval RR = new RankedRetrieval(query, TP, dataPath);
        List<ArrayList<Double>> Scores = RR.getScores(50);  //get top 50 doc
        
        endTime = System.currentTimeMillis();
        elapsedTime = (endTime - startTime);
        totalTime = totalTime + elapsedTime;
        
        totalAd = totalAd + RR.getAdSize();
        
        
        ArrayList<String> eachQueryPosting = new ArrayList<String>();
        for(ArrayList<Double> i : Scores){
        	id = (int)Math.round(i.get(0));
        	fName = Corpus.getDocument(id).getName();
        	
        	eachQueryPosting.add(fName);  //store the return documents' file names in order to compare 
        	                              //with 'qrel' file later
        }
        
        retriList.add(eachQueryPosting);
        
      }
      
      averageAd = (double)totalAd/retriList.size();
      averageTime = totalTime/retriList.size();
      
      myReader.close();
    } catch (FileNotFoundException e) {
    	
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
		
	}
	
	
	
	private static void retrivalQueries_boolean(){
		averageTime = 0L;
		Long startTime;
    Long endTime;
    Long elapsedTime;
    Long totalTime = 0L;
    int count = 0;
		
		try {
			
      File myObj = new File(queryFile);
      Scanner myReader = new Scanner(myObj);
      
      while (myReader.hasNextLine()){
        String query = myReader.nextLine();
        query = query.replace("-", " ").replace("(", "").replace(")", "");
        //System.out.println(query);
        
        startTime = System.currentTimeMillis();
        
        QueryComponent qc = BQparser.parseQuery(query, TP);
        List<Posting> P = qc.getPostings(DI);
        
        endTime = System.currentTimeMillis();
        elapsedTime = (endTime - startTime);
        totalTime = totalTime + elapsedTime;  
        
        count++;
      }
      
      averageTime = totalTime/count;
      
      myReader.close();
    } catch (FileNotFoundException e) {
    	
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
		
	}
	
	
	private static void loadDocuments(Boolean reindex){
    
		DirectoryCorpus JDirCorpus = new DirectoryCorpus(Paths.get(corpusPath + fileName).toAbsolutePath());
    JDirCorpus.registerFileDocumentFactory(fileExtension, JsonFileDocument::loadJsonFileDocument);
    Corpus = JDirCorpus;
    
    AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
    InvertedIndex ii = new InvertedIndex();
    
    if(reindex == true) {  //build index
    	for (Document d : Corpus.getDocuments()) {
        EnglishTokenStream ets = new EnglishTokenStream(d.getContent());
  	    //System.out.println("Found document " + d.getTitle() + " ID " + d.getId());
  	    //System.out.println("-- " + d.getName());
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
    	
    }
    else {  //simply read documents
    	for (Document d : Corpus.getDocuments()) {
        d.getContent();
      } 
    }
	  
	  DI = new DiskPositionalIndex(dataPath);
	  
	}	

}
