package cecs429.querying;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;
	
	public AndQuery(List<QueryComponent> components) {
		mComponents = components; 
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<Posting>();
    //List<String> s = new ArrayList<String>(mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));

    
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
    List<Posting> s1 = mComponents.get(0).getPostings(index);
    List<Posting> s2 = mComponents.get(1).getPostings(index);
    int n = mComponents.size()-1;
    int c = 1;
    //System.out.println("(AND)size? "+mComponents.size() + mComponents.get(0).getClass() + " " + mComponents.get(1).getClass());

    while(n >= c){
      List<Posting> temp = new ArrayList<Posting>();

      int x = 0;
      int y = 0;
      int docID_1 = 0;
      int docID_2 = 0;
      int size_1 = s1.size()-1;
      int size_2 = s2.size()-1;
      Boolean done;
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
        
          Posting p = new Posting(docID_1);
          temp.add(p);

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
        
        }
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
        else{ // (docID_1 > docID_2)
          if(y<size_2){
            y++;
            docID_2 = s2.get(y).getDocumentId();
            continue;
          }
          else{
            done = true;
          }
        }
      
      } //while loop -1

      result = temp;
      c++;
      if(n >= c){
        s1 = temp;
        s2 =  mComponents.get(c).getPostings(index);
      }
      
    } // n while
    
		
		return result;
	}

	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
