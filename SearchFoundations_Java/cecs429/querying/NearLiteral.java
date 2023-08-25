package cecs429.querying;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * Represents a Near literal consisting of two terms that must occur in sequence.
 */
public class NearLiteral implements QueryComponent {
	// The list of individual terms in the Near.
	private List<String> mTerms = new ArrayList<>();
  int mK;
	
	/**
	 * Constructs a NearLiteral with the given individual Near terms.
	 */
	public NearLiteral(List<String> terms ,int k) {
		mTerms.addAll(terms);
    mK = k;
	}
	
	/**
	 * Constructs a NearLiteral given a string with terms separated by spaces.
	 */
	public NearLiteral(String terms, int k) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
    mK = k;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
    List<Posting> result = new ArrayList<Posting>();
    
		// TODO: program this method. Retrieve the postings for the individual terms in the Near,
		// and positional merge them together.

    List<Posting> s1 = index.getPostings(mTerms.get(0));
    List<Posting> s2 = index.getPostings(mTerms.get(1));
    
    
    int x = 0;
    int y = 0;
    int docID_1 = 0;
    int docID_2 = 0;
    int size_1 = s1.size()-1;
    int size_2 = s2.size()-1;
    Boolean done;
    Boolean match;
    if(s1.isEmpty() || s2.isEmpty()){
      done = true;
    }
    else{
      done = false;
      docID_1 = s1.get(x).getDocumentId();
      docID_2 = s2.get(y).getDocumentId();
    }

    while(done != true){        

      if(docID_1 == docID_2){
        
        //call function
        match = checkLocation(s1.get(x), s2.get(y));
        if(match == true){
          Posting p = new Posting(docID_1);
          result.add(p);
        }

        if(x<size_1 && y<size_2){
          x++;
          y++;
          docID_1 = s1.get(x).getDocumentId();
          docID_2 = s2.get(y).getDocumentId();
          continue;
        }
        else{
          done = true;
        }
        
      }  //doc1==doc2
      else if(docID_1 < docID_2){
        if(x<size_1){
          x++;
          docID_1 = s1.get(x).getDocumentId();
          continue;
        }
        else{
          done = true;
        }
      }
      else{ //(docID_1 > docID_2)
        if(y<size_2){
          y++;
          docID_2 = s2.get(y).getDocumentId();
          continue;
        }
        else{
          done = true;
        }
      }
      
    } //while loop
    
    return result;
	}


  private Boolean checkLocation(Posting first, Posting second){
    Boolean match = false;

    int x = 0;
    int y = 0;
    int count;
    List<Integer> pos1 = first.getPositions();
    List<Integer> pos2 = second.getPositions();
    
    count = pos1.size();

    int p1;
    int p2;
    int pp1;
    
    while(count > 0){
      p1 = pos1.get(x);
      p2 = pos2.get(y);

      pp1 = p1 + mK;
      if(p1 < p2 && pp1 >= p2){
        match = true;
        break;
      }

      if(p1 > p2){
        y++;
        if(y == pos2.size()){
          break;
        }
        else{
          continue;
        }
      }
      else{ //(p2 > p1)
        x++;
        count--;
        continue;
      }
      
    } //while loop


    return match;
  }

	
	@Override
	public String toString() {
		String terms = 
			mTerms.stream()
			.collect(Collectors.joining(" "));
		return "\"" + terms + "\"";
	}
}
