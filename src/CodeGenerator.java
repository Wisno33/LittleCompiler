//Standard Libraries
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/*
 * Walks the AST created from the code extraction of the full parse tree and using the AST and symbol table generates an
 * intermediate representation of the target instructions. IR is represented in 3 address code.
 */
class CodeGenerator{
	
	private LinkedList<SymbolTable> symbolTables;
	
	private AST ast;
	
	//Sequential order of IR instructions.
	private ArrayList<String> IRCode;
	
	//Counts the use of temporaries for result storage, no limit on the number.
	private int temporaryCounter = 0;
	
	public CodeGenerator(LinkedList<SymbolTable> symTabs, AST ast) {
		this.symbolTables = symTabs;
		this.ast = ast;
		this.IRCode = new ArrayList<String>();
	}
	
	// Returns the generated IR, in linked list form for future conversion.
	public LinkedList<String> getIR(){
		
		LinkedList<String> irCodeQueue = new LinkedList<String>();
		
		for(String line: this.IRCode) {
			
			irCodeQueue.add(line);
		}
		
		return irCodeQueue;
	}
	
	// Generate IR instructions from the symbol table and AST.
	public void generateIR() {
		
		LinkedList<String> scopes = new LinkedList<String>();
		
		SymbolTable prevSymTab = null;
		
		// Creates a label, for each scope along with link and returns for functions.
		for(SymbolTable symTab: this.symbolTables) {
			
			String currentScope = symTab.getScope();
			
			if(!currentScope.equals("GLOBAL")) {
				this.IRCode.add("LABEL " + currentScope);
				this.IRCode.add("LINK");
				this.generate();
				this.IRCode.add("RETURN");
			}
			
			scopes.addLast(currentScope);
			
			// Creates IR code for variable and constant declarations.
			for(String symbol: symTab.getSymbols()) {
				
				if(prevSymTab != null && prevSymTab.getSymbolData(symbol) != null) {
					continue;
				}
				
				SymbolAttibutes data = symTab.getSymbolData(symbol);
				
				// INT and FLOAT
				if(!data.getType().equals("STRING")) {
					this.IRCode.add("VAR " + symbol);
				}
				//STRING
				else {
					this.IRCode.add("STRING " + symbol + " " + data.getValue());
				}
			}
			
			prevSymTab = symTab;
		}
	}
	
	//Performs a post order walk of the AST to generate IR code.
	private void generate() {
		
		HashMap<String, Integer> isValueLoaded = new HashMap<String, Integer>();
		
		for(ASTNode subTreeRoot: this.ast.getRoot()) {
		
			this.postOrder(subTreeRoot, isValueLoaded);
		}
	}
	
	//Post order walk of the AST (left to right then root).
	private void postOrder(ASTNode root, HashMap<String, Integer> temporaryMap) {
		
		//Recursively walk the tree until a leaf node is reached and walk back up to process the node.
		if(root != null) {
			
			//Left
			this.postOrder(root.getLChild(), temporaryMap);
			
			//Right
			this.postOrder(root.getRChild(), temporaryMap);
			
			//Root
			CodeObject code = root.getData();
			
			//Processing of L-Values nothing occurs, L-Values are handled during a := operator processing.
			if(code.getCodeType() == CodeType.LValue) {
				return;
			}
			
			//Processing of R-Values generates store instructions and a associated temporary then reformats the node
			//as a data object. These values are literals, variables are handled by arithmetic operators.
			else if(code.getCodeType() == CodeType.RValue) {
				
				//Checks if a value is in temporary.
				if(temporaryMap.get(code.getCode()) != null) {
					return;
				}
				
				//Store IR instruction, building.
				String instruction = "STORE";
				
				SymbolAttibutes symbol = this.getSymbol(code.getCode());
				
				String dataType = "";
				
				//Verifies value is not a symbol or a temporary.
				if(symbol != null) {
					return;
				}
				else if(code.getCodeType() == CodeType.Temporary) {
					return;
				}
				
				//Parse the value as either a FLOAT or INT. 
				else {
					
					try {
						Float.parseFloat(code.getCode());
						dataType = "F";
					} catch (Exception e) {
						System.exit(1);
					}
					try {
						Integer.parseInt(code.getCode());
						dataType = "I";
					} catch (Exception e) {
					}
					
					instruction = instruction.concat(dataType);
				}
				
				//Build the IR instruction for store of INT or FLOAT.
				
				String temporaryTarget = "$T" + ++this.temporaryCounter;
				
				instruction = instruction.concat(" " + code.getCode() + " " + temporaryTarget);
				
				root.getData().setCode(temporaryTarget);
				
				this.symbolTables.get(0).addSymbol(temporaryTarget, new SymbolAttibutes(dataType.equals("I") ? "INT":"FLOAT", null));
				
				//Add generates IR.
				this.IRCode.add(instruction);
				
				//Places value in temporary tracker.
				temporaryMap.put(code.getCode(), this.temporaryCounter);
			}
			
			//Operator IR code generation handles operator and variable nodes.
			else if(code.getCodeType() == CodeType.Operator){
				
				String instruction = "";
				
				//Assignment operator, stores right node into left node value L-Value processing occurs here.
				if(code.getCode().equals(":=")) {
					
					instruction = "STORE";
					
					SymbolAttibutes symbol = this.getSymbol(root.getLChild().getData().getCode());
					
					instruction = instruction.concat(symbol.getType().equals("INT") ? "I " : "F ");	
					
					instruction = instruction.concat(root.getRChild().getData().getCode() + " " + root.getLChild().getData().getCode());
					
					//Add operation IR instruction.
					this.IRCode.add(instruction);
					
					return;
				}
				
				//Build a operator instruction first get operand.
				
				else if(code.getCode().equals("+")) {
					
					instruction = "ADD";
							
				}
				
				else if(code.getCode().equals("-")) {
					
					instruction = "SUB";
		
				}
				
				else if(code.getCode().equals("*")) {
					
					instruction = "MUL";
							
				}
				
				else if(code.getCode().equals("/")) {
					
					instruction = "DIV";
							
				}
				
				//Get symbol data and specify operation type INT or FLOAT>
				SymbolAttibutes symbol = this.getSymbol(root.getLChild().getData().getCode());
				
				//Create operation instruction.
				instruction = instruction.concat(symbol.getType().equals("INT") ? "I " : "F ");	
				
				String operandL = root.getLChild().getData().getCode();
				String operandR = root.getRChild().getData().getCode();
				String temporaryTarget = "$T" + ++this.temporaryCounter;
				
				instruction = instruction.concat(operandL + " " + operandR + " " + temporaryTarget);
				
				root.getData().setCode(temporaryTarget);
				
				//Add operation IR instruction.
				this.IRCode.add(instruction);
					
			}
			
			//Processes read nodes.
			else if(code.getCodeType() == CodeType.Read) {
				
				//Get all variables to be read to, and split into individual operands.
				String vars[] = code.getCode().split(",");
				
				//Generate a READ IR instruction for each operand.
				for(String var: vars) {
					
					String instruction = "READ";
					
					SymbolAttibutes symbol = this.getSymbol(var);
					
					//Specify read type INT or FLOAT.
					instruction = instruction.concat(symbol.getType().equals("INT") ? "I " : "F ");
					
					instruction = instruction.concat(var);
					
					this.IRCode.add(instruction);
					
				}
				
			}
			
			//Processes write nodes.
			else if(code.getCodeType() == CodeType.Write) {
				
				//Get all variables to be written out, and split into individual operands.
				String vars[] = code.getCode().split(",");
				
				//Generate a WRITE IR instruction for each operand.
				for(String var: vars) {
					
					String instruction = "WRITE";
					
					SymbolAttibutes symbol = this.getSymbol(var);
					
					String symType = symbol.getType();
					
					//Specify write type INT or FLOAT.
					instruction = instruction.concat(symType.charAt(0) + " ");
					
					instruction = instruction.concat(var);
					
					this.IRCode.add(instruction);
					
				}
				
			}
		}
	}
	
	//Get a symbol from the set of symbol tables. Looks through all valid symbol tables.
	private SymbolAttibutes getSymbol(String symbolName) {
		
		for(SymbolTable symTab: this.symbolTables) {
			
			SymbolAttibutes symbol = symTab.getSymbolData(symbolName);
			
			if(symbol != null) {
				return symbol;
			}
		}
		
		return null;
	}
}