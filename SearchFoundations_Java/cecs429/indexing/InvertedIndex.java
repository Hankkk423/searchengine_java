package cecs429.indexing;

import java.util.*;

public class InvertedIndex implements Index{
  
  private HashMap<String, List<Posting>> infoList;

  
  public InvertedIndex(){
    infoList = new HashMap<String, List<Posting>>();
  }

  
  //add term's position 
  public void addTerm(String term, int id, int position){ 

    if(infoList.containsKey(term) == false){
      // new term
      List<Posting> list = new ArrayList<Posting>();
      Posting p = new Posting(id, position);
      list.add(p);
      infoList.put(term, list);
    }
    else if(infoList.get(term).get( infoList.get(term).size()-1 ).getDocumentId() == id){
      // term exist, and still in same document
      infoList.get(term).get( infoList.get(term).size()-1 ).addPosition(position);
    }
    else{
      // different id, meaning in different document
      Posting p = new Posting(id, position);
      infoList.get(term).add(p);
    }
    
  }

  
  @Override
  public List<String> getVocabulary() {

    List<String> s = new ArrayList<String>(infoList.keySet());
    Collections.sort(s);
    
    return s;
  }


  @Override
  public List<Posting> getPostings(String term){

    if(infoList.containsKey(term) == false){
      List<Posting> results = new ArrayList<Posting>();
      return results;
    }
    else{
      return infoList.get(term);
    }
    
  }
  
} 


