package com.yashoid.data.jsonparsable;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import com.yashoid.data.jsonparsable.JSONParsable.JsonParsableCreator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class JSONParser {
	
	public static final String DONT_SET = "dont_set";
	
	private static class FieldTypeMap {
		
		private Field field;
		private String type;
		private String subType;
		
	}
	
	public static class JSONFieldParser {
		
		public static JSONFieldParser newInstance(Class<? extends JSONParsable> clazz) {
			return new JSONFieldParser(clazz, clazz.getClassLoader());
		}
		
		private HashMap<String, FieldTypeMap> map;
		private Class<? extends JSONParsable> clazz;
		private ClassLoader classLoader;
		
		private JSONFieldParser(Class<? extends JSONParsable> clazz, ClassLoader classLoader) {
			this.clazz = clazz;
			this.classLoader = classLoader;
			
			ArrayList<Field> fields = new ArrayList<>();
			getFields(fields, clazz);
			
			map = new HashMap<>(fields.size());
			
			for (Field field: fields) {
				boolean isAccessible = field.isAccessible();
				field.setAccessible(true);

				if (field.isAnnotationPresent(JsonField.class)) {
					JsonField jField = field.getAnnotation(JsonField.class);
					
					FieldTypeMap fieldTypeMap = new FieldTypeMap();
					fieldTypeMap.field = field;
					fieldTypeMap.type = jField.type();
					fieldTypeMap.subType = jField.subType();

					map.put(jField.name(), fieldTypeMap);
				}
				
				field.setAccessible(isAccessible);
			}
		}
		
	}
	
	private static JSONParser mInstance = null;
	
	public static JSONParser getInstance(Context context) {
		if (mInstance==null) {
			mInstance = new JSONParser(context);
		}
		
		return mInstance;
	}
	
	private Context mContext;
	
	private JSONParser(Context context) {
		mContext = context;
	}
	
	@SuppressWarnings("unchecked")
	public JSONParsable parse(JsonReader reader, JSONFieldParser parser)
			throws Exception {
		reader.beginObject();
		
		JSONParsable object = createInstance(parser.clazz);
		
		while (reader.hasNext()) {
			String name = reader.nextName();
			
			FieldTypeMap map = parser.map.get(name);
			
			if (map!=null) {
				final Field field = map.field;
				final String type = map.type;
				
				boolean accessible = field.isAccessible();
				field.setAccessible(true);
				
				if (JsonField.STRING.equals(type)) {
					if (reader.peek()!=JsonToken.NULL) {
						onFieldParsed(object, field, name, reader.nextString());
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, null);
					}
				}
				else if (JsonField.INT.equals(type)) {
					if (reader.peek()!=JsonToken.NULL) {
						onFieldParsed(object, field, name, reader.nextInt());
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, DONT_SET);
					}
				}
				else if (JsonField.LONG.equals(type)) {
					if (reader.peek()!=JsonToken.NULL) {
						onFieldParsed(object, field, name, reader.nextLong());
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, DONT_SET);
					}
				}
				else if (JsonField.FLOAT.equals(type)) {
					if (reader.peek()!=JsonToken.NULL) {
						onFieldParsed(object, field, name, (float) reader.nextDouble());
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, DONT_SET);
					}
				} else if (JsonField.DOUBLE.equals(type)) {
					if (reader.peek()!=JsonToken.NULL) {
						onFieldParsed(object, field, name, reader.nextDouble());
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, DONT_SET);
					}
				} else if (JsonField.BOOLEAN.equals(type)) {
					if (reader.peek()!=JsonToken.NULL) {
						onFieldParsed(object, field, name, reader.nextBoolean());
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, DONT_SET);
					}
				} else if (JsonField.ARRAY_LIST.equals(type)) {
					if (reader.peek()!=JsonToken.NULL) {
						ArrayList<?> list = (ArrayList<?>) field.get(object);
						parseArray(reader, list, map.subType, parser);
						onFieldParsed(object, field, name, list);
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, null);
					}
				} else {
					if (reader.peek()!=JsonToken.NULL) {
						Class<? extends JSONParsable> subClazz =
								(Class<? extends JSONParsable>) parser.classLoader.loadClass(type);
						JSONFieldParser subParser = JSONFieldParser.newInstance(subClazz);
						JSONParsable subObject = parse(reader, subParser);
						
						onFieldParsed(object, field, name, subObject);
					}
					else {
						reader.nextNull();
						onFieldParsed(object, field, name, null);
					}
				}
				
				field.setAccessible(accessible);
			}
			else if (!object.onUnknownName(name, reader)) {
				reader.skipValue();
			}
		}
		
		reader.endObject();
		return object;
	}
	
	public<T extends JSONParsable> void parseArray(JsonReader reader, ArrayList<T> list, JSONFieldParser parser) throws Exception {
		reader.beginArray();

		while (reader.hasNext()) {
			T object = (T) parse(reader, parser);
			list.add(object);
		}

		reader.endArray();
	}
	
	@SuppressWarnings("unchecked")
	private void parseArray(JsonReader reader, ArrayList<?> dest, String type, JSONFieldParser parser)
			throws Exception {
		reader.beginArray();
		
		if (JsonField.STRING.equals(type)) {
			ArrayList<String> list = (ArrayList<String>) dest;
			while (reader.hasNext()) {
				if (reader.peek()!=JsonToken.NULL) {
					list.add(reader.nextString());
				}
				else {
					reader.nextNull();
					list.add(null);
				}
			}
		}
		else if (JsonField.INT.equals(type)) {
			ArrayList<Integer> list = (ArrayList<Integer>) dest;
			while (reader.hasNext()) {
				if (reader.peek()!=JsonToken.NULL) {
					list.add(reader.nextInt());
				}
				else {
					reader.nextNull();
					list.add(null);
				}
			}
		}
		else if (JsonField.LONG.equals(type)) {
			ArrayList<Long> list = (ArrayList<Long>) dest;
			while (reader.hasNext()) {
				if (reader.peek()!=JsonToken.NULL) {
					list.add(reader.nextLong());
				}
				else {
					reader.nextNull();
					list.add(null);
				}
			}
		}
		else if (JsonField.FLOAT.equals(type)) {
			ArrayList<Float> list = (ArrayList<Float>) dest;
			while (reader.hasNext()) {
				if (reader.peek()!=JsonToken.NULL) {
					list.add((float) reader.nextDouble());
				}
				else {
					reader.nextNull();
					list.add(null);
				}
			}
		}
		else if (JsonField.DOUBLE.equals(type)) {
			ArrayList<Double> list = (ArrayList<Double>) dest;
			while (reader.hasNext()) {
				if (reader.peek()!=JsonToken.NULL) {
					list.add(reader.nextDouble());
				}
				else {
					reader.nextNull();
					list.add(null);
				}
			}
		}
		else if (JsonField.BOOLEAN.equals(type)) {
			ArrayList<Boolean> list = (ArrayList<Boolean>) dest;
			while (reader.hasNext()) {
				if (reader.peek()!=JsonToken.NULL) {
					list.add(reader.nextBoolean());
				}
				else {
					reader.nextNull();
					list.add(null);
				}
			}
		}
		else if (JsonField.ARRAY_LIST.equals(type)) {
			throw new IllegalArgumentException("Array inside array is not supported.");
		}
		else {
			ArrayList<JSONParsable> list = (ArrayList<JSONParsable>) dest;
			
			Class<? extends JSONParsable> subClazz = (Class<? extends JSONParsable>) parser.classLoader.loadClass(type);
			JSONFieldParser subParser = JSONFieldParser.newInstance(subClazz);
			
			while (reader.hasNext()) {
				if (reader.peek()!=JsonToken.NULL) {
					list.add(parse(reader, subParser));
				}
				else {
					reader.nextNull();
					list.add(null);
				}
			}
		}
		
		reader.endArray();
	}
	
	public JSONParsable parse(JSONObject jObject, Class<? extends JSONParsable> clazz) throws Exception {
		return parse(jObject, clazz, clazz.getClassLoader());
	}
	
	@SuppressWarnings("unchecked")
	public JSONParsable parse(JSONObject jObject, Class<? extends JSONParsable> clazz, ClassLoader classLoader) throws Exception {
		try {
			JSONParsable object = createInstance(clazz);
			
			ArrayList<Field> fields = new ArrayList<>();
			getFields(fields, clazz);
			
			for (Field field: fields) {
				if (field.isAnnotationPresent(JsonField.class)) {
					boolean isAccessible = field.isAccessible();
					field.setAccessible(true);
					
					JsonField jField = field.getAnnotation(JsonField.class);
					String jFieldName = jField.name();
					String type = jField.type();
					
					if (JsonField.STRING.equals(type)) {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							onFieldParsed(object, field, jFieldName, jObject.getString(jFieldName));
						}
						else {
							onFieldParsed(object, field, jFieldName, null);
						}
					}
					else if (JsonField.INT.equals(type)) {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							onFieldParsed(object, field, jFieldName, jObject.getInt(jFieldName));
						}
						else {
							onFieldParsed(object, field, jFieldName, DONT_SET);
						}
					}
					else if (JsonField.LONG.equals(type)) {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							onFieldParsed(object, field, jFieldName, jObject.getLong(jFieldName));
						}
						else {
							onFieldParsed(object, field, jFieldName, DONT_SET);
						}
					}
					else if (JsonField.FLOAT.equals(type)) {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							onFieldParsed(object, field, jFieldName, (float) jObject.getDouble(jFieldName));
						}
						else {
							onFieldParsed(object, field, jFieldName, DONT_SET);
						}
					}
					else if (JsonField.DOUBLE.equals(type)) {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							onFieldParsed(object, field, jFieldName, jObject.getDouble(jFieldName));
						}
						else {
							onFieldParsed(object, field, jFieldName, DONT_SET);
						}
					}
					else if (JsonField.BOOLEAN.equals(type)) {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							onFieldParsed(object, field, jFieldName, jObject.getBoolean(jFieldName));
						}
						else {
							onFieldParsed(object, field, jFieldName, DONT_SET);
						}
					}
					else if (JsonField.ARRAY_LIST.equals(type)) {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							JSONArray jArray = jObject.getJSONArray(jFieldName);
							ArrayList<?> list = (ArrayList<?>) field.get(object);
							String subType = jField.subType();
							parseArray(jArray, list, subType, classLoader);
							onFieldParsed(object, field, jFieldName, list);
						} else {
							onFieldParsed(object, field, jFieldName, null);
						}
					}
					else {
						if (jObject.has(jFieldName) && !jObject.isNull(jFieldName)) {
							JSONObject jSubObject = jObject.getJSONObject(jFieldName);
							Class<? extends JSONParsable> subClazz = (Class<? extends JSONParsable>) classLoader.loadClass(type);
							onFieldParsed(object, field, jFieldName, parse(jSubObject, subClazz, classLoader));
						}
						else {
							onFieldParsed(object, field, jFieldName, null);
						}
					}
					
					field.setAccessible(isAccessible);
				}
			}
			
			return object;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void onFieldParsed(JSONParsable parsingObject, Field field, String name, Object parsedField)
			throws IllegalAccessException, IllegalArgumentException {
		Object fieldValue = parsingObject.onFieldParsed(name, parsedField, mContext);
		
		if (fieldValue!=DONT_SET && fieldValue!=null) {
			field.set(parsingObject, fieldValue);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseArray(JSONArray source, ArrayList<?> dest, String type, ClassLoader classLoader) throws Exception{
		if (JsonField.STRING.equals(type)) {
			ArrayList<String> list = (ArrayList<String>) dest;
			for (int i=0; i<source.length(); i++) {
				if (!source.isNull(i)) {
					list.add(source.getString(i));
				}
				else {
					list.add(null);
				}
			}
		}
		else if (JsonField.INT.equals(type)) {
			ArrayList<Integer> list = (ArrayList<Integer>) dest;
			for (int i=0; i<source.length(); i++) {
				if (!source.isNull(i)) {
					list.add(source.getInt(i));
				} else {
					list.add(null);
				}
			}
		}
		else if (JsonField.LONG.equals(type)) {
			ArrayList<Long> list = (ArrayList<Long>) dest;
			for (int i=0; i<source.length(); i++) {
				if (!source.isNull(i)) {
					list.add(source.getLong(i));
				} else {
					list.add(null);
				}
			}
		}
		else if (JsonField.FLOAT.equals(type)) {
			ArrayList<Float> list = (ArrayList<Float>) dest;
			for (int i=0; i<source.length(); i++) {
				if (!source.isNull(i)) {
					list.add((float) source.getDouble(i));
				} else {
					list.add(null);
				}
			}
		}
		else if (JsonField.DOUBLE.equals(type)) {
			ArrayList<Double> list = (ArrayList<Double>) dest;
			for (int i=0; i<source.length(); i++) {
				if (!source.isNull(i)) {
					list.add(source.getDouble(i));
				} else {
					list.add(null);
				}
			}
		}
		else if (JsonField.BOOLEAN.equals(type)) {
			ArrayList<Boolean> list = (ArrayList<Boolean>) dest;
			for (int i=0; i<source.length(); i++) {
				if (!source.isNull(i)) {
					list.add(source.getBoolean(i));
				} else {
					list.add(null);
				}
			}
		}
		else if (JsonField.ARRAY_LIST.equals(type)) {
			throw new IllegalArgumentException("Array inside array is not supported.");
		}
		else {
			Class<? extends JSONParsable> clazz = (Class<? extends JSONParsable>) classLoader.loadClass(type);
			ArrayList<JSONParsable> list = (ArrayList<JSONParsable>) dest;
			for (int i=0; i<source.length(); i++) {
				if (!source.isNull(i)) {
					JSONObject jObject = source.getJSONObject(i);
					list.add(parse(jObject, clazz, classLoader));
				}
				else {
					list.add(null);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private<T extends JSONParsable> T createInstance(Class<T> clazz) throws NoSuchFieldException,
			IllegalAccessException, IllegalArgumentException {
		Field field = clazz.getField(JSONParsable.JSONPARSABLE_CREATOR_NAME);
		
		JsonParsableCreator<T> creator = (JsonParsableCreator<T>) field.get(null);
		
		return creator.newInstance(mContext);
	}
	
	@SuppressWarnings("unchecked")
	private static void getFields(ArrayList<Field> fields, Class<?> clazz) {
		try {
			Class<? extends JSONParsable> parsableClass = (Class<? extends JSONParsable>) clazz;
			
			Field[] classFields = parsableClass.getDeclaredFields();

			for (Field field: classFields) {
				fields.add(field);
			}
		} catch (Throwable t) { }
		
		Class<?> superClass = clazz.getSuperclass();
		
		if (superClass!=null) {
			getFields(fields, superClass);
		}
	}
	
}
