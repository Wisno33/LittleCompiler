//Standard Libraries
import java.util.LinkedList;

// Abstract Syntax Tree class contains the root node to all necessary nodes that contain information.
class AST{
	
	private LinkedList<ASTNode> root;
	
	public AST() {
		this.root = new LinkedList<ASTNode>();
	}
	
	public LinkedList<ASTNode> getRoot() {
		return this.root;
	}
}

// A node for the AST these nodes are binary as they only need handle simple expressions.
class ASTNode{
	
	private ASTNode parent;
	
	private ASTNode lChild;
	
	private ASTNode rChild;
	
	private CodeObject data;
	
	public ASTNode(CodeType type, String code) {
		
		this.parent = null;
		this.lChild = null;
		this.rChild = null;
		this.data = new CodeObject(type, code);
		
	}
	
	public CodeObject getData() {
		return this.data;
	}
	
	public void setParent(ASTNode p) {
		this.parent = p;
	}
	
	public ASTNode getParent() {
		return this.parent;
	}
	
	public void setLChild(ASTNode l) {
		this.lChild = l;
	}
	
	public ASTNode getLChild() {
		return this.lChild;
	}
	
	public void setRChild(ASTNode r) {
		this.rChild = r;
	}
	
	public ASTNode getRChild() {
		return this.rChild;
	}
}

// Holds code data in an AST node.
class CodeObject{
	
	// The type for the code in the node.
	CodeType type;
	
	String code;
	
	public CodeObject(CodeType type, String code) {
		
		this.type = type;
		this.code = code;
	}
	
	public CodeType getCodeType() {
		return this.type;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public void setCode(String s) {
		this.code = s;
	}
	
}
