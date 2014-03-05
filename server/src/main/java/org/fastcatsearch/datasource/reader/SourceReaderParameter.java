package org.fastcatsearch.datasource.reader;

public class SourceReaderParameter {

	public static String TYPE_STRING; //single-line short
	public static String TYPE_STRING_LONG; //single-line long
	public static String TYPE_TEXT; //multi-line
	public static String TYPE_NUMBER;
	
	private String id;
	private String name;
	private String description;
	private String type;
	private boolean required;
	private String defaultValue;
	private boolean isMultiLine;
	
	private String value;
	
	/**
	 * @param type : 위에 정의된 TYPE_STRING등..
	 * 
	 */
	public SourceReaderParameter(String id, String name, String description, String type, boolean required, String defaultValue){
		this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
		this.required = required;
		this.defaultValue = defaultValue;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value != null ? value : defaultValue;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public boolean isRequired() {
		return required;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isMultiLine() {
		return isMultiLine;
	}
	
	
	
}
