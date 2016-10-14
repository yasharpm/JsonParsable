package com.yashoid.data.jsonparsable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonField {

	public static final String STRING = "str";
	public static final String INT = "int";
	public static final String LONG = "lng";
	public static final String FLOAT = "flt";
	public static final String DOUBLE = "dbl";
	public static final String BOOLEAN = "bln";
	public static final String ARRAY_LIST = "arl";
	public String name();
	public String type();
	public String subType() default "";
	
}
