package cecs429.text;

import java.util.List;
import java.util.ArrayList;
import org.tartarus.snowball.*;


/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class AdvancedTokenProcessor implements TokenProcessor {  

	
  @Override
	public List<String> processToken(String token){
  	List<String> result = new ArrayList<String>();
  	
  	try {
    
      String language = "english";
      Class stemClass = Class.forName("org.tartarus.snowball.ext." + language + "Stemmer");
      SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
    
      //// Remove all non-alphanumeric characters from the beginning and end of the token //
      char c = token.charAt(0);
    
      int start = 0;
      int end = token.length();
      boolean done = false;

      while (done != true) {
        if (false == Character.isLetterOrDigit(token.charAt(start))) {
          start++;
          if(start == end){
            break;
          }
        }
        else{
          break;
        }
      }

      while (done != true) {
        if(start == end){
          break;
        }
        else if (false == Character.isLetterOrDigit(token.charAt(end-1))) {
          end--;
        }
        else{
          break;
        }
      }

      if(start >= end){ //means this term is all non-alphanumeric character
        token = token.replaceAll("\\W", "");
      }
      else{
        token = token.substring(start, end);
      }
    

      // Remove all apostropes or quotation marks (single or double quotes) from anywhere in the string. //
      token = token.replace("\"", "").replace("'", "");
      

      // For hyphens in words //
      int check = token.indexOf("-", 0);
      String temp;
      if(check > 0){
        String[] tokenArray = token.split("-");

        temp = token.replace("-", "").toLowerCase();
        stemmer.setCurrent(temp);
        stemmer.stem();
        temp = stemmer.getCurrent();
      
        result.add(temp);
        for(String i : tokenArray){
          i = i.toLowerCase();
          stemmer.setCurrent(i);
          stemmer.stem();
          i = stemmer.getCurrent();
        
          result.add(i);
        }
      }
      else{
        temp = token.toLowerCase();
        stemmer.setCurrent(temp);
        stemmer.stem();
        temp = stemmer.getCurrent();
      
        result.add(temp);
      }
    
    
  	} catch (Throwable throwable) {
  		  System.out.println("stem error");
  		  System.out.println("token: " + token + "result: " + result);
  	  }

    
		return result;
	}
}
