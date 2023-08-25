package cecs429.indexing;

import java.util.*;
import java.io.*;
import java.lang.Math; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;


public class DiskIndexWriter{
 
  private String mFilePath;
  private List<String> mVocab;
  private List<Posting> mPostings;
  private HashMap<String, Long> tLocation;  //<term, pointer>, term's starting location in disk
  private Index ii;
  private int corpusSize;
  private HashMap<Integer, Double> Ld;  //<docID, Accumulated Wdt>, will keep accumulating "Wdt"
                                        //while creating posting.bin file, and will have the final 
                                        //"Wd" (not square root yet, will do it while writing in Bin).
  

  public DiskIndexWriter(Index II, String filePath, int size){  
    mFilePath = filePath; 
    mVocab = II.getVocabulary();
    ii = II;
    corpusSize = size;
    tLocation = new HashMap<String, Long>();
    Ld = new HashMap<Integer, Double>();
    writeToFile_postingBin();
    writeToSQL();
    writeToFile_docWeightsBin();

  }

  
  public void writeToSQL(){

    Connection conn = null;

    try{ 

      conn = DriverManager.getConnection("jdbc:sqlite:" + mFilePath + "test.db");
      Statement stat = conn.createStatement();
      stat.executeUpdate("drop table if exists vocab;");
      stat.executeUpdate("create table vocab (term string, location long);");
      PreparedStatement prep = conn.prepareStatement(
        "insert into vocab values (?, ?);");

      for(HashMap.Entry<String, Long> set : tLocation.entrySet()){
        prep.setString(1, set.getKey());
        prep.setLong(2, set.getValue());
        prep.addBatch();
      
      }

      conn.setAutoCommit(false);
      prep.executeBatch();
      conn.setAutoCommit(true);

      //conn.close();
      
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

    
  }  // Func writeToSQL
  


  
  public void writeToFile_postingBin(){  //throws FileNotFoundException

    Long pointer;
    int df;  //doc frequency
    int id;
    double Wdt;
    int tf;  //term frequency
    ArrayList<Integer> positions;
    
    try{
      
      RandomAccessFile raf = new RandomAccessFile(mFilePath + "postings.bin", "rw");
      raf.seek(0);

      for(String term : mVocab){
        pointer = raf.getFilePointer();
        tLocation.put(term, pointer);  //store term's starting pointer
        
        mPostings = ii.getPostings(term);
        
        /////////////
        mPostings = postingFilter_tfOrder(mPostings);
        /////////////
        
        df = mPostings.size();
        raf.writeInt(df);  //write doc frequency

        for(Posting p : mPostings){

          id = p.getDocumentId();
          raf.writeInt(id);  //write a id; 

          positions = p.getPositions();
          tf = positions.size();

          Wdt = 1.0 + Math.log((double)tf);

          raf.writeDouble(Wdt);  //write Wdt (8 byte)
          raf.writeInt(tf);  //write term frequency
          
          updateDocWeight(id, Wdt);  //update the docWeight value while reading data

          for(int pos : positions){
            raf.writeInt(pos);  //write each position
          }
           
        }
        
      }

      raf.close();
      
    } catch (IOException ex) {
         ex.printStackTrace();
      }

    
  }  // Func writeToFile()
  
  
  
  private List<Posting> postingFilter_tfOrder(List<Posting> list){ 
  	//change the order of posting list into decreasing tf order
  	
  	List<Posting> result = new ArrayList<Posting>();
  	HashMap<Integer, List<Posting>> tfMap = new HashMap<Integer, List<Posting>>();  //<tf, p>
  	int tf;
  	PriorityQueue<Integer> prq = new PriorityQueue<>(Collections.reverseOrder());
  	HashSet<Integer> set = new HashSet<Integer>();
  	
  	for(Posting p : list){
  		tf = p.getPositions().size();
  		
  		if(tfMap.containsKey(tf) == false){
  			List<Posting> subList= new ArrayList<Posting>();
  			subList.add(p);
  			tfMap.put(tf, subList);
  		}
  		else{
  			tfMap.get(tf).add(p);
  		}
  		
  		set.add(tf);
  	}
  	
  	List<Integer> reverseSet = new ArrayList<Integer>(set);
    Collections.reverse(reverseSet);
  	
  	for(int i=0; i<reverseSet.size(); i++){
  		result.addAll(tfMap.get(reverseSet.get(i)));
  	}
  	
  	
  	return result;
  }  //func postingFilter()
  
  
  



  private void updateDocWeight(int id, double Wdt){  //Total DocWeight = "Ld"
    
    if(Ld.containsKey(id) == false){      
      Ld.put(id, Math.pow(Wdt, 2));
    }
    else{
      Ld.put(id, Ld.get(id) + Math.pow(Wdt, 2));
    }
    
  }  //Func updateDocweight()

  


  public void writeToFile_docWeightsBin(){  //HashMap "Ld"

    try{
      
      RandomAccessFile raf = new RandomAccessFile(mFilePath + "docWeights.bin", "rw");
      raf.seek(0);
      double LValue;

      for(int i=0; i<corpusSize; i++){
    	  if(Ld.get(i) == null){
    	    //it means the "Ld" of this doc is null, which means this doc doesn't has any content
    	    raf.writeDouble(1.0);
    	    continue;
    	  }
        
    	  LValue = Math.sqrt(Ld.get(i));  //square root here
        raf.writeDouble(LValue);  //sort in the order of id: (0,1,2,...)
      }
      
      raf.close();

    } catch (IOException ex) {
         ex.printStackTrace();
      }
    
  }  //Func writeToFile()


  
}