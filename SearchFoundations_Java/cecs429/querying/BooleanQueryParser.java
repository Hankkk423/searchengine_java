package cecs429.querying;

import java.util.ArrayList;
import java.util.List;
import cecs429.text.TokenProcessor;

/**
 * Parses boolean queries according to the base requirements of the CECS 429 project.
 * Does not handle phrase queries, NOT queries, NEAR queries, or wildcard queries... yet.
 */
public class BooleanQueryParser {
	/**  
	 * Identifies a portion of a string with a starting index and a length.
	 */
	private static class StringBounds {
		int start;
		int length;
		
		StringBounds(int start, int length) {
			this.start = start;
			this.length = length;
		}
	}
	
	/**
	 * Encapsulates a QueryComponent and the StringBounds that led to its parsing.
	 */
	private static class Literal {
		StringBounds bounds;
		QueryComponent literalComponent;
		
		Literal(StringBounds bounds, QueryComponent literalComponent) {
			this.bounds = bounds;
			this.literalComponent = literalComponent;
		}
	}
	
	/**
	 * Given a boolean query, parses and returns a tree of QueryComponents representing the query.
	 */
	public QueryComponent parseQuery(String query, TokenProcessor processor) {
		int start = 0;

    // TODO: nomalize query
    query = query.replace("-", "");
    query = query.toLowerCase();
    
		
		// General routine: scan the query to identify a literal, and put that literal into a list.
		//	Repeat until a + or the end of the query is encountered; build an AND query with each
		//	of the literals found. Repeat the scan-and-build-AND-query phase for each segment of the
		// query separated by + signs. In the end, build a single OR query that composes all of the built
		// AND subqueries.
		
		List<QueryComponent> allSubqueries = new ArrayList<>();
		do {
			// Identify the next subquery: a portion of the query up to the next + sign.
			StringBounds nextSubquery = findNextSubquery(query, start);
			// Extract the identified subquery into its own string.
			String subquery = query.substring(nextSubquery.start, nextSubquery.start + nextSubquery.length);
			int subStart = 0;
     
			// Store all the individual components of this subquery.
			List<QueryComponent> subqueryLiterals = new ArrayList<>(0);

			do {
				// Extract the next literal from the subquery.
				Literal lit = findNextLiteral(subquery, subStart, processor);
				
				// Add the literal component to the conjunctive list.
				subqueryLiterals.add(lit.literalComponent);
				
				// Set the next index to start searching for a literal.
				subStart = lit.bounds.start + lit.bounds.length;
				
			} while (subStart < subquery.length());
			
			// After processing all literals, we are left with a conjunctive list
			// of query components, and must fold that list into the final disjunctive list
			// of components.
			
			// If there was only one literal in the subquery, we don't need to AND it with anything --
			// its component can go straight into the list.
			if (subqueryLiterals.size() == 1) {
				allSubqueries.add(subqueryLiterals.get(0));
			}
			else {

        if(subquery.indexOf('"') >= 0 && subquery.indexOf('(') > 0){ // means -> "(mango+banana) shake"

          if(subqueryLiterals.size() > 2){ // [ "(mango+banana) shake" rose ], OR two phrase and AND the rest.
            List<QueryComponent> temp = new ArrayList<>(0);
            temp.add(subqueryLiterals.get(0));
            temp.add(subqueryLiterals.get(1));
            QueryComponent tempQC = new OrQuery(temp);

            List<QueryComponent> result = new ArrayList<>(0);
            result.add(tempQC);
            result.add(subqueryLiterals.get(2));
            
            allSubqueries.add(new AndQuery(result));
            
          }
          else{
            // use "OR" merge to "Two Phrase"
            allSubqueries.add(new OrQuery(subqueryLiterals));
          }
        }
        else{
          // With more than one literal, we must wrap them in an AndQuery component.
          allSubqueries.add(new AndQuery(subqueryLiterals));
        }
        
			}
			start = nextSubquery.start + nextSubquery.length;
		} while (start < query.length());
		
		// After processing all subqueries, we either have a single component or multiple components
		// that must be combined with an OrQuery.
		if (allSubqueries.size() == 1) {
			return allSubqueries.get(0);
		}
		else if (allSubqueries.size() > 1) {
			return new OrQuery(allSubqueries);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Locates the start index and length of the next subquery in the given query string,
	 * starting at the given index.
	 */
	private StringBounds findNextSubquery(String query, int startIndex) {
		int lengthOut;
    
		// Find the start of the next subquery by skipping spaces and + signs.
		char test = query.charAt(startIndex);
		while (test == ' ' || test == '+') {
			test = query.charAt(++startIndex);
		}
    
    int nextParenthesis_f = query.indexOf('(', startIndex);

    if(nextParenthesis_f < 0){
      // No parenthesis
      
      // Find the end of the next subquery.
		  int nextPlus = query.indexOf('+', startIndex + 1);
		  
		  if (nextPlus < 0) {
			  // If there is no other + sign, then this is the final subquery in the
			  // query string.
			  lengthOut = query.length() - startIndex;
		  }
		  else {
			  // If there is another + sign, then the length of this subquery goes up
			  // to the next + sign.
		
			  // Move nextPlus backwards until finding a non-space non-plus character.
			  test = query.charAt(nextPlus);
			  while (test == ' ' || test == '+') {
				  test = query.charAt(--nextPlus);
			  }
			  
			  lengthOut = 1 + nextPlus - startIndex;
		  }
      
    }
    else{
      // Have parentheses
      
      int nextParenthesis_s = query.indexOf(')', startIndex + 1);
      int nextPlus = query.indexOf('+', nextParenthesis_s + 1);

      if (nextPlus < 0) {
			  // If there is no other + sign, then this is the final subquery in the
			  // query string.
			  lengthOut = query.length() - startIndex;
		  }
		  else {
			  // If there is another + sign, then the length of this subquery goes up
			  // to the next + sign.
		
			  // Move nextPlus backwards until finding a non-space non-plus character.
			  test = query.charAt(nextPlus);
			  while (test == ' ' || test == '+') {
				  test = query.charAt(--nextPlus);
			  }
			
			  lengthOut = 1 + nextPlus - startIndex;
		  }
      
    }

		// startIndex and lengthOut give the bounds of the subquery.
		return new StringBounds(startIndex, lengthOut);
	}
	
	/**
	 * Locates and returns the next literal from the given subquery string.
	 */
	private Literal findNextLiteral(String subquery, int startIndex, TokenProcessor processor) {
    
		int subLength = subquery.length();
		int lengthOut;
		boolean error = false;
    
		// Skip past white space.
		while (subquery.charAt(startIndex) == ' ') {
			++startIndex;
			if(startIndex == subLength) {
				error = true;
				break;
			}
		}
		
		if(error == true) {
			startIndex--;
			lengthOut = subLength - startIndex;
			return new Literal(
		      new StringBounds(startIndex, lengthOut),
		      new TermLiteral(" "));
		}
		
		
		
    
    if(subquery.charAt(startIndex) == '"' || subquery.charAt(startIndex) == '+'){
      //// double-quote ////
      // "A B" or "A (B + C)"

      int endParenthesis  = subquery.indexOf(')', startIndex);
      if(endParenthesis < 0){
        // no parenthesis, "A" Phrase 
        // do...
        
        List<String> phrase = new ArrayList<String>();
      
        int nextMark = subquery.indexOf('"', startIndex+1);
        int nextSpace = subquery.indexOf(' ', startIndex+1);
      
        String f_quote = subquery.substring(startIndex+1, nextSpace);
        String s_quote = subquery.substring(nextSpace+1, nextMark);
        phrase.add(f_quote);
        phrase.add(s_quote);
      
        nextSpace = subquery.indexOf(' ', nextMark);
      
        if (nextSpace < 0) {
			    // No more literals in this subquery.
			    lengthOut = subLength - startIndex;
		    }
		    else {
			    lengthOut = nextSpace - startIndex;
		    }
        
        
        for(int i=0; i<phrase.size(); i++) {
        	List<String> temp = processor.processToken(phrase.get(i));
        	phrase.set(i, temp.get(0));
        }

        
        return new Literal(
		      new StringBounds(startIndex, lengthOut),
		      new PhraseLiteral(phrase));
        
      }
      else{
        // Two Phrase, "A (B + C)"

        List<String> phrase = new ArrayList<String>();
        int nextMark = subquery.indexOf('"', startIndex+1);
        int nextSpace = subquery.indexOf(' ', 0);
        
        String f_quote = subquery.substring(1, nextSpace);
        String s_quote = "";
        
        int nextPlus = subquery.indexOf('+', startIndex);
        lengthOut = 0;

        if(subquery.charAt(startIndex) == '"'){
          // first time 

          int startParenthesis = subquery.indexOf('(', startIndex);
          
          s_quote = subquery.substring(startParenthesis+1, nextPlus-1);
          lengthOut = nextPlus - startIndex - 1;
          
        }
        else if(subquery.charAt(startIndex) == '+'){
          // second time

          s_quote = subquery.substring(startIndex+2, endParenthesis);
          lengthOut = nextMark - startIndex + 1; // out loop // "+1" bc it skip space one time
          
        }
        else{
          //lengthOut = 0;
        }
        
        phrase.add(f_quote);
        phrase.add(s_quote);
        
        for(int i=0; i<phrase.size(); i++) {
        	List<String> temp = processor.processToken(phrase.get(i));
        	phrase.set(i, temp.get(0));
        }
        
        
        return new Literal(
		    new StringBounds(startIndex, lengthOut),
		    new PhraseLiteral(phrase));
        
      }
      
    }
    else if(subquery.charAt(startIndex) == '['){
      /// NEAR/K ///

      int endBracket = subquery.indexOf(']', startIndex);
      int nextSpace = subquery.indexOf(' ', endBracket);
		  if (nextSpace < 0) {
		    // No more literals in this subquery.
		    lengthOut = subLength - startIndex;
	    }
	    else {
		    lengthOut = nextSpace - startIndex;
		  }
     
      int kPosition_start = subquery.indexOf('/', startIndex) + 1;
      int kPosition_end = subquery.indexOf(' ', kPosition_start);
      int kValue = Integer.valueOf(subquery.substring(kPosition_start, kPosition_end));

      String f = subquery.substring(startIndex + 1, subquery.indexOf(' ', startIndex));  //first term in []
      String s = subquery.substring(kPosition_end + 1, endBracket); //second term in []

      List<String> phrase = new ArrayList<String>();
      phrase.add(f);
      phrase.add(s);
      
      for(int i=0; i<phrase.size(); i++) {
      	List<String> temp = processor.processToken(phrase.get(i));
      	phrase.set(i, temp.get(0));
      }
      
      
      return new Literal(
		      new StringBounds(startIndex, lengthOut),
		      new NearLiteral(phrase, kValue));
		
    }
    else{
      //// NO phrase ////
      
      //IF "parentheses"
      if(subquery.charAt(startIndex) == '('){
        int nextPlus = subquery.indexOf('+', startIndex);
        int endParenthesis = subquery.indexOf(')', startIndex);

        
        String f = subquery.substring(startIndex + 1, nextPlus - 1);  //first term in ()
        String s = subquery.substring(nextPlus + 2, endParenthesis); //second term in ()

        List<String> phrase = new ArrayList<String>();
        phrase.add(f);
        phrase.add(s);
        
        for(int i=0; i<phrase.size(); i++) {
        	List<String> temp = processor.processToken(phrase.get(i));
        	phrase.set(i, temp.get(0));
        }
        
        
        

        int nextSpace = subquery.indexOf(' ', endParenthesis);
		    if (nextSpace < 0) {
			    // No more literals in this subquery.
			    lengthOut = subLength - startIndex;
		    }
		    else {
		  	  lengthOut = nextSpace - startIndex;
		    }

        // This is a parentheses literal containing term"s".
		    return new Literal(
		      new StringBounds(startIndex, lengthOut),
		      new ParenthesesLiteral(phrase));
        
      }
      else{ //NO parentheses: single term

        // Locate the next space to find the end of this literal.
		    int nextSpace = subquery.indexOf(' ', startIndex);
		    if (nextSpace < 0) {
			    // No more literals in this subquery.
			    lengthOut = subLength - startIndex;
		    }
		    else {
		  	  lengthOut = nextSpace - startIndex;
		    }

        String term = subquery.substring(startIndex, startIndex + lengthOut);
        
        List<String> temp = processor.processToken(term);
        term = temp.get(0);
        

		    // This is a term literal containing a single term.
		    return new Literal(
		      new StringBounds(startIndex, lengthOut),
		      new TermLiteral(term));

      }

    }
		
	}
}
