package cecs429.retrievals;

import java.util.*;
import java.lang.Math;
import cecs429.indexing.DiskPositionalIndex;
import cecs429.text.TokenProcessor;


public class RankedRetrieval {      
  private List<String> mTerms = new ArrayList<String>(); 
  private DiskPositionalIndex DI = null;   
  private int sizeOfAd = 0;
  

  public RankedRetrieval(String terms, TokenProcessor processor, String dataPath){
    DI = new DiskPositionalIndex(dataPath);
    terms = terms.replace("-", "").toLowerCase(); 
    mTerms.addAll(Arrays.asList(terms.split(" ")));
    
    for(int i=0; i<mTerms.size(); i++){
    	if(mTerms.get(i).equals("")){
    		continue;
    	}
    	
    	List<String> temp = processor.processToken(mTerms.get(i));
    	mTerms.set(i, temp.get(0));
    }
         
    //System.out.println("mTerms: " + mTerms); 
  }
  
  
  
  public List<ArrayList<Double>> getScores(int k){  //id_score
    if(mTerms.isEmpty()){
      return null;
    }
    
    List<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
    HashMap<Integer, Double> Ad = new HashMap<Integer, Double>();
    List<Double> Ld = DI.getDocWeights();
    int N = Ld.size(); 
    double threshold = 1.5;  //set a threshold of Wdt(1+ln(tf)). ln(3)=1.09. ln(2)=0.69
    
    
    List<ArrayList<Double>> termInfo;  //id_wdt
    for(String s : mTerms){

      termInfo = DI.getTermWeightsInfo(s);
      int df = termInfo.size();
      double Wqt = Math.log(1.0 + ((double)N/(double)df));

      int id;
      double Wdt;
      for(ArrayList<Double> i : termInfo){
        id = (int)Math.round(i.get(0));
        Wdt = i.get(1);
        
        if(Wdt < threshold){
        	//means the Wdt is to low, we dont care about the rest documents.
        	break;
        }
        

        if(Ad.containsKey(id) == false){
          
          Ad.put(id, Wdt*Wqt);
        }
        else{
          Ad.put(id, Ad.get(id) + Wdt*Wqt);
        }
        
      }
      
    }
    
    sizeOfAd = Ad.size();  //"sizeOfAd" is global value
    

    //nomalized "Ad" by dividing Ld
    PriorityQueue<Double> prq = new PriorityQueue<>(Collections.reverseOrder());
    double temp;
    for(int key : Ad.keySet()){
      temp = Ad.get(key) / Ld.get(key);  //(Ad) / (the specific document's Ld)
      
      Ad.put(key, temp);
      prq.add(temp);
    }

    List<Double> maxList = new ArrayList<Double>();

    for(int i=0; i<k; i++){  //k = how many documents we want to return
      maxList.add(prq.poll());
    }
    
    
    double val;
    for(Double score : maxList){
    	if(score == null) {
    		break;
    	}
    	
      for(int key : Ad.keySet()){
        val = Ad.get(key);
        
        if(val == score){
          ArrayList<Double> id_score = new ArrayList<Double>();
          id_score.add((double)key);
          id_score.add(score);
          result.add(id_score);
          
          break;
        }
        
      }
      
    }

    return result;
  }
  
  
  public int getAdSize(){
  	return sizeOfAd;
  }
  
  
}