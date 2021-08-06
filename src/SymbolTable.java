//Standard Libraries
import java.util.ArrayList;
import java.util.HashMap;

/*
 *  The Symbol Table data structure. This is built on the HashMap and ArrayList container classes.
 *  A Hash Table (HashMap) is used as the main container for all symbol data, associates
 *  the name of the symbol (key) to the symbol's attributes (value) (separate class). Then an
 *  additional attribute denotes the scope of the Symbol Table. Finally a List container for the names
 *  of the symbols is used to access the symbols in order of declaration from the Symbol Table.
 */
class SymbolTable{
	
	// The scope of the symbol table essentially its identifying name.
	private String scope;
	
	// The core symbol table structure holds all symbol data.
	private HashMap<String, SymbolAttibutes> symbolTable;
	
	// Holds the symbol names in the order they are encountered.
	private ArrayList<String> symbolNames;
	
	public SymbolTable(String scope) {
		this.scope = scope;
		this.symbolTable = new HashMap<String, SymbolAttibutes>();
		this.symbolNames = new ArrayList<String>();
	}
	
	// Return scope of the table.
	public String getScope() {
		return this.scope;
	}
	
	// Adds a symbol to the symbol table, if it already exists an error is printed to the console and the compiler exits.
	public void addSymbol(String symbolName, SymbolAttibutes attributes){
		if(this.symbolTable.containsKey(symbolName)) {
			System.out.printf("DECLARATION ERROR %s\n", symbolName);
			System.exit(1);
		}
		
		this.symbolTable.put(symbolName, attributes);
		this.symbolNames.add(symbolName);
	}
	
	// Returns a symbol from the table.
	public SymbolAttibutes getSymbolData(String symbol) {
		return this.symbolTable.get(symbol);
	}
	
	// Prints all the symbols in the symbol table in the order the are added.
	public ArrayList<String> getSymbols() {
		return this.symbolNames;
	}
}