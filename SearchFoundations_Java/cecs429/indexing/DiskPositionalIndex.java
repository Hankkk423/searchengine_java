package cecs429.indexing;

import java.util.*;
import java.io.*;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;



public class DiskPositionalIndex implements Index{
  
  private String mFilePath;
  //private RandomAccessFile raf; 
  
  public DiskPositionalIndex(String FilePath){  //file: .../DATA/
    mFilePath = FilePath;
  } 

  
  
  // posting with "position" 
  @Override
  public List<Posting> getPostings(String term){
    List<Posting> result = new ArrayList<Posting>(); 
    Long location = getLocation(term);
    Posting p;
    if(location == null){
    	//this means we don't have this term in our vocabularies
    	if(term.isBlank() == false) {
	  		//System.out.println(term + " << This term doesn't exist in our vocabularies!!");
	  	}
    	return result;
    }
     

    try{
      
      RandomAccessFile raf = new RandomAccessFile(mFilePath + "postings.bin", "r");
      raf.seek(location);
      int df = raf.readInt();
      int id, tf, position;
      for(int i=0; i<df; i++){
        id = raf.readInt();
        raf.seek(raf.getFilePointer() + 8);  //skip "Wdt", beccuse we don't need Wdt in Boolean retrievals
        tf = raf.readInt();
        position = raf.readInt();
        p = new Posting(id, position);
      
        for(int j=0; j<tf-1; j++){
          p.addPosition(raf.readInt());
        }

        result.add(p);
      
      }

      raf.close();
    } catch (IOException ex) {
        ex.printStackTrace();
      }
    
    /////////
    result = postingFilter_idOrder(result);
    /////////
    
    return result;
  }
  
  
  
  
  private List<Posting> postingFilter_idOrder(List<Posting> list){
    //change the order of posting list into increasing id order
  	
  	List<Posting> result = new ArrayList<Posting>();
  	HashMap<Integer, Posting> idMap = new HashMap<Integer, Posting>();  //<id, p>
  	int id;
  	PriorityQueue<Integer> prq = new PriorityQueue<Integer>();
  	
  	for(Posting p : list){
  		id = p.getDocumentId();
  		idMap.put(id, p);
  		prq.add(id);
  	}
  	
  	int temp;
  	for(int i=0; i<list.size(); i++){
      temp = prq.poll();
      result.add(idMap.get(temp));
  	}
  	
  	return result;
  }
  
  
  
  
  
  @Override
  public List<Posting> getPostings_OnlyID(String term){
	  List<Posting> result = new ArrayList<Posting>();
	  Long location = getLocation(term);
	  Posting p;
	  if(location == null){
	    //this means we don't have this term in our vocabularies
	  	if(term.isBlank() == false) {
	  		//System.out.println(term + " << This term doesn't exist in our vocabularies!!");
	  	}
	    return result;
	   }
	    

    try{
	      
	    RandomAccessFile raf = new RandomAccessFile(mFilePath + "postings.bin", "r");
	    raf.seek(location);
	    int df = raf.readInt();
	    int id, tf;
	    for(int i=0; i<df; i++){
	      id = raf.readInt();
	      raf.seek(raf.getFilePointer() + 8);  //skip "Wdt", beccuse we don't need Wdt in Boolean retrievals
	      tf = raf.readInt();
	      p = new Posting(id);
	      raf.seek(raf.getFilePointer() + tf*4);

	      result.add(p);
	      
	    }

	    raf.close();
	  } catch (IOException ex) {
	      ex.printStackTrace();
	    }
	    
    
    // filter posting list into id order
    result = postingFilter_idOrder(result);
    
    
	  return result;
	  
  }
  

  
  public List<ArrayList<Double>> getTermWeightsInfo(String term){
    List<ArrayList<Double>> info = new ArrayList<ArrayList<Double>>();
    ArrayList<Double> id_wdt;
    
    Long location = getLocation(term);  //get term's binary location from SQLite
    if(location == null){
      //this means we don't have this term in our vocabularies
    	if(term.isBlank() == false) {
	  		//System.out.println(term + " << This term doesn't exist in our vocabularies!!");
	  	}
      return info;
    }
    
    try{
      
      RandomAccessFile raf = new RandomAccessFile(mFilePath + "postings.bin", "r");
      raf.seek(location);
      int df = raf.readInt();
      int id, tf;
      double wdt;
      for(int i=0; i<df; i++){
        id = raf.readInt();
        wdt = raf.readDouble();
        tf = raf.readInt();
        id_wdt = new ArrayList<Double>();
        id_wdt.add((double)id);
        id_wdt.add(wdt);
        info.add(id_wdt);

        raf.seek(raf.getFilePointer() + (tf*4));  //skip the gap to next id
      }

      raf.close();
    } catch (IOException ex) {
        ex.printStackTrace();
      }
    
    return info;
  }


  
  public List<Double> getDocWeights(){
    List<Double> result = new ArrayList<Double>();
    
    try{
      
      RandomAccessFile raf = new RandomAccessFile(mFilePath + "docWeights.bin", "r");
      raf.seek(0);

      Long len = raf.length()/8;

      for(int i=0; i<len; i++){
        result.add(raf.readDouble());
      }


      raf.close();
    } catch (IOException ex) {
        ex.printStackTrace();
      }

    return result;
  }

  
  
  //connect SQLite and get the term's location from .db file
  public Long getLocation(String term){
    Connection conn = null;
    Long location = null;

    try{

      conn = DriverManager.getConnection("jdbc:sqlite:" + mFilePath + "test.db");
      Statement stat = conn.createStatement();

      ResultSet rs = stat.executeQuery("select location from vocab where term = '" + term + "';");
      if (rs.next()) {
        location = rs.getLong("location");
      }
      
      rs.close();
      conn.close();

    } catch (SQLException e) {
        System.err.println(e.getMessage());
      }
    finally{
      try{
        
        if(conn != null)
          conn.close();
      } catch(SQLException e) {
          // connection close failed.
          System.err.println(e.getMessage());
        }
      
    }


    return location;
  }  //Func getLocation()


          
  
  @Override
  public List<String> getVocabulary(){
	List<String> result = new ArrayList<String>(); 
	Connection conn = null;
	Long location = null;
	
	try{

    conn = DriverManager.getConnection("jdbc:sqlite:" + mFilePath + "test.db");
	  Statement stat = conn.createStatement();
	  ResultSet rs = stat.executeQuery("select term from vocab;");
	  
	  while(rs.next()){
        result.add(rs.getString("term"));
      }
	  
	  rs.close();
      conn.close();

    } catch (SQLException e) {
        System.err.println(e.getMessage());
      }
	finally{
	  try{  
	    if(conn != null)
	      conn.close();
	  } catch(SQLException e) {
	      // connection close failed.
	      System.err.println(e.getMessage());
	    }
	      
	}
	
	Collections.sort(result);
	  
    return result;
    
  }


  
}