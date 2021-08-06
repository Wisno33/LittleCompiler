
//Class used to pair a register and its operand for optimization of register reuse.
public class RegisterOperandPair {

	private String register = null;
	private String operand = null;
	
	public RegisterOperandPair(String r, String o) {
		this.register = r;
		this.operand = o;
	}
	
	public String getRegister() {
		return this.register;
	}
	
	public String getOperand() {
		return this.operand;
	}
	
}
