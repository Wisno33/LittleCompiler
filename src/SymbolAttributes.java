//Holds data related to symbols their type (STRING, INT, FLOAT) and in the case of s STRING its value. 
//(For INT and FLOAT value is null)
class SymbolAttibutes{
	
	private String type;
	private String value;
	
	public SymbolAttibutes() {
		this.type = null;
		this.value = null;
	}
	
	public SymbolAttibutes(String type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setType(String t) {
		this.type = t;
	}
	
	public void setValue(String v) {
		this.value = v;
	}
}