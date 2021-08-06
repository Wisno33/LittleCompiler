//Standard Libraries
import java.util.LinkedList;
import java.util.Stack;

/* 
 * An extension of the ANTLR listener class for the Little grammar. This class overrides parser rules associated with symbols, assignments,
 * simple expressions, and syscalls. This class implements the symbol table construction, expressions, assignment, and syscall semantic actions
 *  and accordingly uses a symbol table, ASTs, and a queue of generated code.
 */
class CodeExtractor extends LittleBaseListener {
	
	// Symbol Table stack holds all active symbol tables with the current scope on the top of the stack.
	private Stack<SymbolTable> symbolTableStack;
	
	// Holds the current symbol table, could just peek the stack but this is nice to have defined.
	private SymbolTable currentSymbolTable;
	
	// Queue of symbol tables, new table created per scope. Used to output symbol tables in order they are made.
	private LinkedList<SymbolTable> symbolTables;
	
	// Holds generated AST.
	private AST tree;
	
	// Counts the number of block statement scopes encountered.
	private int blockCounter = 0;
	
	// Constructor tracks symbol tables and builds an AST with a root that has no children.
	public CodeExtractor() {

		this.symbolTableStack = new Stack<SymbolTable>();
		this.currentSymbolTable = null;
		this.symbolTables = new LinkedList<SymbolTable>();
		this.tree = new AST();
	}
	
	// Returns all the symbol tables in scope order.
	public LinkedList<SymbolTable> getSymbolTables(){
		return this.symbolTables;
	}
	
	// Returns the AST all relevant expressions can be accessed from here.
	public AST getAST(){
		return this.tree;
	}
	
	
	// Enters at the start rule program for extraction. A new symbol table of scope global is created and queued.
	@Override
	public void enterProgram(LittleParser.ProgramContext ctx) {
		
		this.symbolTableStack.push(new SymbolTable("GLOBAL"));
		this.currentSymbolTable = symbolTableStack.peek();
		this.symbolTables.addLast(this.currentSymbolTable);
		
	}
	
	// Exits the entry point (the start rule) now all symbol tables in the queue will be output.
	@Override
	public void exitProgram(LittleParser.ProgramContext ctx) { }
	
	// Used to enter the program body branches of the tree and exit as needed items are under these nodes.
	@Override 
	public void enterPgm_body(LittleParser.Pgm_bodyContext ctx) { }
	@Override 
	public void exitPgm_body(LittleParser.Pgm_bodyContext ctx) { }
	
	// Used to enter the deceleration branches of the tree and exit as needed items are under these nodes.
	@Override 
	public void enterDecl(LittleParser.DeclContext ctx) { }
	@Override 
	public void exitDecl(LittleParser.DeclContext ctx) { }
	
	// Enters a string declaration, gets the identifier and value, then the structure is added to the current symbol table.
	@Override 
	public void enterString_decl(LittleParser.String_declContext ctx){
		
		this.currentSymbolTable.addSymbol(ctx.id().IDENTIFIER().getText(), new SymbolAttibutes("STRING", ctx.str().STRINGLITERAL().getText()));
		
	}
	
	// Used to exit the string deceleration branches of the tree as needed items are under these nodes.
	@Override 
	public void exitString_decl(LittleParser.String_declContext ctx) { }
	
	// Enters a variable declaration, gets the type then all declared values (1 - many). Then these values are added to 
	// the symbol table along with the type in the order they are encountered.
	@Override 
	public void enterVar_decl(LittleParser.Var_declContext ctx) { 
		
		String type = ctx.var_type().getText();
		
		String[] ids = ctx.id_list().getText().split(",");
		
		int i = 0;
		for(; i < ids.length; i++) {
			
			this.currentSymbolTable.addSymbol(ids[i], new SymbolAttibutes(type, null));
		}	
	}
	
	// Used to exit the variable deceleration branches of the tree as needed items are under these nodes.
	@Override 
	public void exitVar_decl(LittleParser.Var_declContext ctx) { }
	
	// Enters an assignment statement creates a binary sub tree root with the left node as the result and assignment operator as the root.
	@Override public void enterAssign_expr(LittleParser.Assign_exprContext ctx) { 
		
		ASTNode lValue = new ASTNode(CodeType.LValue, ctx.id().IDENTIFIER().getText());
		ASTNode operator = new ASTNode(CodeType.Operator, ":=");
		
		operator.setLChild(lValue);
		lValue.setParent(operator);
	
		this.tree.getRoot().add(operator);
	}
	
	// Used to exit the assignment expression branches of the tree as needed items are under these nodes.
	@Override public void exitAssign_expr(LittleParser.Assign_exprContext ctx) { }
	
	// Enters a read statement and creates a node with all values to be read.
	@Override public void enterRead_stmt(LittleParser.Read_stmtContext ctx) { 
		
		ASTNode readNode = new ASTNode(CodeType.Read, ctx.id_list().getText());
		this.tree.getRoot().add(readNode);
	}
	
	// Used to exit the read branches of the tree as needed items are under these nodes.
	@Override public void exitRead_stmt(LittleParser.Read_stmtContext ctx) { }
	
	// Enters a write statement and creates a node with all values to be written to output.
	@Override public void enterWrite_stmt(LittleParser.Write_stmtContext ctx) { 
		
		ASTNode readNode = new ASTNode(CodeType.Write, ctx.id_list().getText());
		this.tree.getRoot().add(readNode);	
	}
	
	// Used to exit the write branches of the tree as needed items are under these nodes.
	@Override public void exitWrite_stmt(LittleParser.Write_stmtContext ctx) { }
	
	
	// For the following methods no explicit action occurs, but they are needed to walk to methods where actions do occur.
	@Override public void enterExpr(LittleParser.ExprContext ctx) { }
	@Override public void exitExpr(LittleParser.ExprContext ctx) { }
	@Override public void enterExpr_prefix(LittleParser.Expr_prefixContext ctx) { }
	@Override public void exitExpr_prefix(LittleParser.Expr_prefixContext ctx) { }
	@Override public void enterFactor(LittleParser.FactorContext ctx) { }
	@Override public void exitFactor(LittleParser.FactorContext ctx) { }
	@Override public void enterFactor_prefix(LittleParser.Factor_prefixContext ctx) { }
	@Override public void exitFactor_prefix(LittleParser.Factor_prefixContext ctx) { }
	@Override public void enterExpr_list(LittleParser.Expr_listContext ctx) { }
	@Override public void exitExpr_list(LittleParser.Expr_listContext ctx) { }
	@Override public void enterExpr_list_tail(LittleParser.Expr_list_tailContext ctx) { }
	@Override public void exitExpr_list_tail(LittleParser.Expr_list_tailContext ctx) { }
	
	// Creates nodes for the right hand side of assignments.
	@Override public void enterPrimary(LittleParser.PrimaryContext ctx) {
		
		// Does not create extra nodes for ( or ) and processes the contents.
		if(ctx.getText().contains("(")){
			return;
		}
		
		ASTNode rValue = new ASTNode(CodeType.RValue, ctx.getText());
		
		ASTNode walker  = this.tree.getRoot().getLast();
		
		while(walker.getRChild() != null) {
			walker = walker.getRChild();
		}
		
		walker.setRChild(rValue);
		rValue.setParent(walker);
	}
	
	// Used to exit the write branches of the tree as needed items are under these nodes.
	@Override public void exitPrimary(LittleParser.PrimaryContext ctx) { }
	
	// Creates an operator node for an add or subtract.
	@Override public void enterAddop(LittleParser.AddopContext ctx) {
		
		ASTNode operator = new ASTNode(CodeType.Operator, ctx.getText());
		
		ASTNode walker  = this.tree.getRoot().getLast();
		
		while(walker.getRChild() != null && walker.getLChild() != null) {
			walker = walker.getRChild();
		}
		
		operator.setLChild(walker);
		walker.getParent().setRChild(operator);
		walker.setParent(operator);
		
		
	}

	// Used to exit the add and subtract branches of the tree as needed items are under these nodes.
	@Override public void exitAddop(LittleParser.AddopContext ctx) { }
	
	// Creates an operator node for an multiply or divide.
	@Override public void enterMulop(LittleParser.MulopContext ctx) {
		
		ASTNode operator = new ASTNode(CodeType.Operator, ctx.getText());
		
		ASTNode walker  = this.tree.getRoot().getLast();
		
		while(walker.getRChild() != null && walker.getLChild() != null) {
			walker = walker.getRChild();
		}
		
		operator.setLChild(walker);
		walker.getParent().setRChild(operator);
		walker.setParent(operator);
	}
	
	// Used to exit the multiply and divide branches of the tree as needed items are under these nodes.
	@Override public void exitMulop(LittleParser.MulopContext ctx) { }
	
	/*
	 * The following are leftover from Step 3 we kept them in case we want to make a full version.
	 */

	// Used to enter the function decelerations branches of the tree and exit as needed items are under these nodes.
	@Override 
	public void enterFunc_declarations(LittleParser.Func_declarationsContext ctx) { }
	@Override 
	public void exitFunc_declarations(LittleParser.Func_declarationsContext ctx) { }
	
	// Enters an individual function declaration branch, a new symbol table is created as a new scope of <functionName> is encountered.
	@Override 
	public void enterFunc_decl(LittleParser.Func_declContext ctx) { 
		
		this.symbolTableStack.push(new SymbolTable(ctx.id().getText()));
		this.currentSymbolTable = this.symbolTableStack.peek();
		this.symbolTables.addLast(this.currentSymbolTable);
	}

	// Exits an individual function declaration branch, the current symbol table is removed from the stack as it is finished, and 
	// the current symbol table is the new top of the stack.
	@Override
	public void exitFunc_decl(LittleParser.Func_declContext ctx) {
		
		this.symbolTableStack.pop();
		this.currentSymbolTable = this.symbolTableStack.peek();
	}
	
	// Used to enter the parameter deceleration list branches of the tree and exit as needed items are under these nodes.
	@Override 
	public void enterParam_decl_list(LittleParser.Param_decl_listContext ctx) { }
	@Override 
	public void exitParam_decl_list(LittleParser.Param_decl_listContext ctx) { }
	
	// Enters a parameter declaration branch, gets a single parameter from a function parameter list and adds its name and type to the symbol table.
	@Override 
	public void enterParam_decl(LittleParser.Param_declContext ctx) { 
		
		this.currentSymbolTable.addSymbol(ctx.id().IDENTIFIER().getText(), new SymbolAttibutes(ctx.var_type().getText(), null));
		
	}
	
	// Used to exit the parameter declaration branches of the tree as needed items are under these nodes.
	@Override 
	public void exitParam_decl(LittleParser.Param_declContext ctx) { }

	// Used to enter the parameter decelerations tail branches of the tree and exit as needed items are under these nodes.
	@Override 
	public void enterParam_decl_tail(LittleParser.Param_decl_tailContext ctx) { }
	@Override 
	public void exitParam_decl_tail(LittleParser.Param_decl_tailContext ctx) { }

	// Enters an if statement branch since this is a conditional block the block counter is incremented, a new symbol table is also created 
	// for the scope of the conditional block with the name BLOCK<numBlocksEncountred>.
	@Override 
	public void enterIf_stmt(LittleParser.If_stmtContext ctx) {
		
		this.blockCounter++;
		
		this.symbolTableStack.push(new SymbolTable("BLOCK "+this.blockCounter));
		this.currentSymbolTable = this.symbolTableStack.peek();
		this.symbolTables.addLast(this.currentSymbolTable);
		
	}
	
	// Exits an if statement branch, the current symbol table is removed from the stack as it is finished, and 
	// the current symbol table is the new top of the stack.
	@Override public void exitIf_stmt(LittleParser.If_stmtContext ctx) { 
		this.symbolTableStack.pop();
		this.currentSymbolTable = this.symbolTableStack.peek();
	}
	
	// Enters an if statement branch, because an else statement does not need to exist as part of an else statement, if the node has no
	// children i.e. the node is empty text nothing is done. Otherwise since, this is a conditional block the block counter is incremented,
	// a new symbol table is also created for the scope of the conditional block with the name BLOCK<numBlocksEncountred>.
	@Override public void enterElse_part(LittleParser.Else_partContext ctx) {
		
		if(ctx.getText() != "") {
		
			this.blockCounter++;
			
			this.symbolTableStack.push(new SymbolTable("BLOCK "+this.blockCounter));
			this.currentSymbolTable = this.symbolTableStack.peek();
			this.symbolTables.addLast(this.currentSymbolTable);
		
		}
	}
	
	// Exits an else statement branch, because an else statement does not need to exist as part of an else statement, if the node has no
	// children i.e. the node is empty text nothing is done. Otherwise the current symbol table is removed from the stack as it is finished, and 
	// the current symbol table is the new top of the stack.
	@Override public void exitElse_part(LittleParser.Else_partContext ctx) { 
		
		if(ctx.getText() != "") {
			
			this.symbolTableStack.pop();
			this.currentSymbolTable = this.symbolTableStack.peek();
		}
	}
	
	// Enters an while statement branch since this is a conditional block the block counter is incremented, a new symbol table is also created 
	// for the scope of the conditional block with the name BLOCK<numBlocksEncountred>.
	@Override public void enterWhile_stmt(LittleParser.While_stmtContext ctx) {
		
		this.blockCounter++;
		
		this.symbolTableStack.push(new SymbolTable("BLOCK "+this.blockCounter));
		this.currentSymbolTable = this.symbolTableStack.peek();
		this.symbolTables.addLast(this.currentSymbolTable);
	}
	
	// Exits an while statement branch, the current symbol table is removed from the stack as it is finished, and 
	// the current symbol table is the new top of the stack.
	@Override public void exitWhile_stmt(LittleParser.While_stmtContext ctx) {
		
		this.symbolTableStack.pop();
		this.currentSymbolTable = this.symbolTableStack.peek();
	}
}