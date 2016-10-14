package com.yashoid.data.jsonparsable;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;

public interface JSONParsable {
	
	String JSONPARSABLE_CREATOR_NAME = "JSONPARSABLE_CREATOR";
	
	Object onFieldParsed(String name, Object parsedField, Context context);
	
	/**
	 * 
	 * @param name
	 * @param reader
	 * @return true is you have consumed the value.
	 */
	boolean onUnknownName(String name, JsonReader reader) throws IOException;
	
	interface JsonParsableCreator<T extends JSONParsable> {
		
		T newInstance(Context context);
		
	}
	
}
