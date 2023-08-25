package cecs429.querying;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Collections;  

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * Represents a Parentheses literal consisting of one or more terms that must occur in sequence.
 */
public class ParenthesesLiteral implements QueryComponent {
	// The list of individual terms in the Parentheses.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a ParenthesesLiteral with the given individual Parentheses terms.
	 */
	public ParenthesesLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a ParenthesesLiteral given a string with one or more individual terms separated by spaces.
	 */
	public ParenthesesLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}

  
	@Override
	public List<Posting> getPostings(Index index) {
    List<Posting> result = new ArrayList<Posting>();
    HashSet<Integer> allID_Has = new HashSet<Integer>();
    
		// TODO: program this method. Retrieve the postings for the individual terms in the Parentheses,
		// and unioning the resulting postings.

    List<Posting> s1 = index.getPostings_OnlyID(mTerms.get(0));
    List<Posting> s2 = index.getPostings_OnlyID(mTerms.get(1));

    for(Posting i : s1){
      allID_Has.add(i.getDocumentId());
    }

    for(Posting i : s2){
      allID_Has.add(i.getDocumentId());
    }

    List<Integer> allID_Lis = new ArrayList<Integer>(allID_Has);
    Collections.sort(allID_Lis);

    Posting p;
    for(int id : allID_Lis){
      p = new Posting(id);
      result.add(p);
    }
    
    return result;
	}
  
	
	@Override
	public String toString() {
		String terms = 
			mTerms.stream()
			.collect(Collectors.joining(" + "));
		return "\"" + terms + "\"";
	}
}
