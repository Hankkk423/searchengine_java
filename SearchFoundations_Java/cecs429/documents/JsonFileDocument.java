package cecs429.documents;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.simple.*;
import org.json.simple.parser.*;

public class JsonFileDocument implements FileDocument {
	private int mDocumentId;
	private Path mFilePath;
  private String mTitle;
	
	public JsonFileDocument(int id, Path absoluteFilePath) { 
		mDocumentId = id;
		mFilePath = absoluteFilePath;
	}
	
	@Override
	public Path getFilePath() {
		return mFilePath;
	}
	
	@Override
	public int getId() {
		return mDocumentId;
	}
	
	@Override
	public Reader getContent() {
    JSONParser parser = new JSONParser();

		try {
      
      Object obj = parser.parse(new FileReader(mFilePath.toString()));
      JSONObject jsonObject = (JSONObject) obj;
      String body = (String) jsonObject.get("body");
      mTitle = (String) jsonObject.get("title");

      StringReader reader = new StringReader(body); 

      return reader;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
      throw new RuntimeException(e);
    }
	}
	
	@Override
	public String getTitle() {
      return mTitle;
	}
	
	@Override
	public String getName(){
		String name = mFilePath.getFileName().toString();
		name = name.substring(0, name.indexOf(".", 0));
		
		return name;
	}
	
	public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
}
