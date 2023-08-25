package cecs429.querying;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Collections;  

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<Posting>();
    HashSet<Integer> allID_Has = new HashSet<Integer>();
    		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
		// unioning the resulting postings.
    List<Posting> s1 = mComponents.get(0).getPostings(index);
    List<Posting> s2 = mComponents.get(1).getPostings(index);

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
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
