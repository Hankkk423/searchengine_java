package cecs429.indexing;

import java.util.*;
import java.util.ArrayList;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
  private ArrayList<Integer> mPositions;
  private int mFrequency;

  //for the posting that doesn't need position information
  public Posting(int oneId){
    mDocumentId = oneId;
  }

  public Posting(int id, int position){
    mDocumentId = id;
    mPositions = new ArrayList<Integer>();
    mPositions.add(position);
  }

  //for the posting that only need id and tf
  /*
  public Posting(int id, int termFrequency){
    mDocumentId = id;
    mFrequency = termFrequency;
  }*/
	
	public int getDocumentId() {
		return mDocumentId;
	}

  public ArrayList<Integer> getPositions(){
    return mPositions;
  }

  public int getFrequency(){
    return mFrequency;
  }

  public void addFrequency(int termFrequency){
    mFrequency = termFrequency;
  }

  public void addPosition(int position){
    mPositions.add(position);
  }

  
  
}
