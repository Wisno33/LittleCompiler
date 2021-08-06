//Standard Libraries
import java.util.LinkedList;
import java.util.HashMap;

/*
 * Converts IR code into tiny architecture instructions. Allocates registers and
 * performs several optimizations.
 */
public class InstructionConverter {
	
	private LinkedList<String> IRCode;
	
	private LinkedList<String> assemblyCode;
	
	//Holds items in a register. Maps register to item in register.
	private HashMap<String, String> registers;
	
	//Holds value of a given variable or temporary. Maps variable / temporary to its value.
	private HashMap<String, String> varValues;
	
	//Counts current register. NOTE: Naive allocation is used operations on the idea of infinite registers.
	private int registerCounter;
	
	//Initialize all containers.
	public InstructionConverter(LinkedList<String> ir) {
		this.IRCode = ir;
		this.assemblyCode = new LinkedList<String>();
		this.registers = new HashMap<String, String>();
		this.varValues = new HashMap<String, String>();
		this.registerCounter = 0;
	}
	
	//Get the tiny assembly code.
	public LinkedList<String> getAssemblyCode(){
		return this.assemblyCode;
	}
	
	//Converts IR Code to assembly
	/*
	 * Optimizations are performed in this method. They include:
	 * 1) Peep-hole optimizations
	 * 2) Constant folding
	 */
	public void convertToAssembly() {
		
		//While the IR has instructions consume and convert.
		while(!this.IRCode.isEmpty()) {
			
			//Consumes an IR instruction for conversion.
			String irInstruction = this.IRCode.remove();
			
			//Gets the operands in the 3AC code.
			String[] irInstructionElements = irInstruction.split(" ");
			
			//Instruction and its elements.
			String assemblyInstruction = "";
			String targetRegister = null;
			String operand = null;
			
			//For a VAR IR instruction create a tiny var declaration.
			if(irInstruction.contains("VAR")) {
				
				this.assemblyCode.add("var " + irInstructionElements[1]);
			}
			
			//For a STRING IR declaration create a tiny str declaration.
			else if(irInstruction.contains("STRING")) {
				
				this.assemblyCode.add("str " + irInstructionElements[1] +" " + irInstructionElements[2]);
			}
			
			//Process a store instruction two types, store a value into a register and store a value to memory.
			else if(irInstruction.contains("STORE")) {
				
				//The register and operand, positions can flip.
				String registerTarget = null;
				String valueOperand = null;
				
				//Store to memory instruction creates a tiny move register to variable instruction.
				if(irInstructionElements[1].contains("$T")) {
					registerTarget = this.convertTemporaryToReg(irInstructionElements[1]);
					valueOperand = irInstructionElements[2];
					
					this.assemblyCode.add("move " + registerTarget + " " + valueOperand);
					
					//Store the variables value for look up.
					this.varValues.put(valueOperand, this.registers.get(registerTarget));
					
					//Track the value in the register used.
					this.registers.put(registerTarget, valueOperand);
					
					this.registerCounter++;
				}
				
				//Store to register instruction loads a variable or literal into a register.
				else {
					valueOperand = irInstructionElements[1];
					registerTarget = this.convertTemporaryToReg(irInstructionElements[2]);
					
					this.assemblyCode.add("move " + valueOperand + " " + registerTarget);
					
					this.registers.put(registerTarget, valueOperand);
					
					String[] lookAhead = this.IRCode.peek().split(" ");
					
					//Check if the next instruction is not part of a load store pair, if not increment the register counter.
					if(!lookAhead[0].contains("STORE") || !lookAhead[1].equals(irInstructionElements[2])) {
						this.registerCounter++;
					}
				}
				
			}
			
			//Process a READ IR instruction.
			else if(irInstruction.contains("READ")) {
				
				//Creates a tiny system call read instruction (i for INT or r for FLOAT).
				assemblyInstruction = "sys read";
				
				//Determine read type.
				char type = irInstruction.charAt(4);
				
				if(type == 'I') {
					
					assemblyInstruction = assemblyInstruction.concat("i ");
					
				}
				
				else if(type == 'F') {
					
					assemblyInstruction = assemblyInstruction.concat("f ");
					
				}
				
				assemblyInstruction = assemblyInstruction.concat(irInstructionElements[1]);
				
				this.assemblyCode.add(assemblyInstruction);
				
			}
			
			//Process a WRITE IR instruction.
			else if(irInstruction.contains("WRITE")) {
				
				//Creates a tiny system call read instruction (i for INT or r for FLOAT or s for STRING).
				assemblyInstruction = "sys write";
				
				char type = irInstruction.charAt(5);
				
				if(type == 'I') {
					
					assemblyInstruction = assemblyInstruction.concat("i ");
					
				}
				
				else if(type == 'F') {
					
					assemblyInstruction = assemblyInstruction.concat("r ");
					
				}
				
				else if(type == 'S') {
					
					assemblyInstruction = assemblyInstruction.concat("s ");
					
				}
				
				assemblyInstruction = assemblyInstruction.concat(irInstructionElements[1]);
				
				this.assemblyCode.add(assemblyInstruction);
			}
			
			//Generate an instruction for a mathematical operation.
			else if(irInstruction.contains("ADD") || irInstruction.contains("SUB") || irInstruction.contains("MUL") || irInstruction.contains("DIV")) {
				
				//Process an ADD IR instruction and apply optimizations.
				if(irInstruction.contains("ADD")) {
							
					//Get the operands.
					String opVal1 = this.varValues.get(irInstructionElements[1]);
					String opVal2 = this.varValues.get(irInstructionElements[2]);
					
					//Used to check if constant for constant folding.
					boolean isConstant = false;
					
					//The assembly instruction to be built.	
					assemblyInstruction = "";
					
					//Get the operation type, int or real (FLOAT)
					char type = irInstruction.charAt(3);
		
					//Value of possible constants.
					Integer val1I = 0;
					Integer val2I = 0;
					Double val1F = 0.0;
					Double val2F = 0.0;
		
					//Check if integer constants and integer type.
					if(type == 'I') {
						
						assemblyInstruction = "addi ";
		
						try {
							val1I = Integer.parseInt(opVal1);
							val2I = Integer.parseInt(opVal2);
							isConstant = true;
						}catch(Exception e) {
						}	
					}
		
					//Check if floating point constant and floating point type.
					else if (type == 'F') {
							
						assemblyInstruction = "addr ";
	
						try {
							val1F = Double.parseDouble(opVal1);
							val2F = Double.parseDouble(opVal2);
							isConstant = true;
						}catch(Exception e) {
							
						}
					}
		
					//If the expression is constant perform constant folding.
					if(isConstant && type == 'I') {
		
						this.constantFolding(val1I, val2I, '+');
						continue;
					}
					
					else if(isConstant && type == 'F') {
	
						this.constantFolding(val1F, val2F, '+');
						continue;
					}
		
					//Not constant attempt other construction of instruction.
					else {
								
						//Perform peep-hole optimizations and constant peep-hole optimization.
						
						targetRegister = null;
						operand = null;
						RegisterOperandPair regOp;
						
						regOp = this.peepholeReduction(targetRegister, operand, irInstructionElements, true);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
						
						regOp = this.TemporaryPeepholeReduction(targetRegister, operand, irInstructionElements, true);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
								
						if(targetRegister != null) {
										
							assemblyInstruction = assemblyInstruction.concat(operand + " " + targetRegister);
										
						}
						
						//If no optimization can be performed generate a standard instruction(s)
						else {
						
							targetRegister = "r" + this.registerCounter;
							
							this.assemblyCode.add("move " + irInstructionElements[1] + " " + targetRegister); 
							
							assemblyInstruction = assemblyInstruction.concat(irInstructionElements[2] + " " + targetRegister);
							
						}
					}
				}
					
				//Process an SUB IR instruction and apply optimizations.
				else if(irInstruction.contains("SUB")) {
							
					//Get the operands.
					String opVal1 = this.varValues.get(irInstructionElements[1]);
					String opVal2 = this.varValues.get(irInstructionElements[2]);
					
					//Used to check if constant for constant folding.
					boolean isConstant = false;
					
					//The assembly instruction to be built.	
					assemblyInstruction = "";
					
					//Get the operation type, int or real (FLOAT)
					char type = irInstruction.charAt(3);
		
					//Value of possible constants.
					Integer val1I = 0;
					Integer val2I = 0;
					Double val1F = 0.0;
					Double val2F = 0.0;
		
					//Check if integer constants and integer type.
					if(type == 'I') {
						
						assemblyInstruction = "subi ";
		
						try {
							val1I = Integer.parseInt(opVal1);
							val2I = Integer.parseInt(opVal2);
							isConstant = true;
						}catch(Exception e) {
						}	
					}
		
					//Check if floating point constant and floating point type.
					else if (type == 'F') {
							
						assemblyInstruction = "subr ";
	
						try {
							val1F = Double.parseDouble(opVal1);
							val2F = Double.parseDouble(opVal2);
							isConstant = true;
						}catch(Exception e) {
							
						}
					}
		
					//If the expression is constant perform constant folding.
					if(isConstant && type == 'I') {
		
						this.constantFolding(val1I, val2I, '-');
						continue;
					}
					
					else if(isConstant && type == 'F') {
	
						this.constantFolding(val1F, val2F, '-');
						continue;
					}
		
					//Not constant attempt other construction of instruction.
					else {
								
						//Perform peep-hole optimizations and constant peep-hole optimization.
						
						targetRegister = null;
						operand = null;
						RegisterOperandPair regOp;
						
						regOp = this.peepholeReduction(targetRegister, operand, irInstructionElements, false);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
						
						regOp = this.TemporaryPeepholeReduction(targetRegister, operand, irInstructionElements, false);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
								
						if(targetRegister != null) {
										
							assemblyInstruction = assemblyInstruction.concat(operand + " " + targetRegister);
										
						}
						
						//If no optimization can be performed generate a standard instruction(s)
						else {
						
							targetRegister = "r" + this.registerCounter;
							
							this.assemblyCode.add("move " + irInstructionElements[1] + " " + targetRegister); 
							
							assemblyInstruction = assemblyInstruction.concat(irInstructionElements[2] + " " + targetRegister);
							
						}
					}
				}
				
				//Process an MUL IR instruction and apply optimizations.
				else if(irInstruction.contains("MUL")) {
							
					//Get the operands.
					String opVal1 = this.varValues.get(irInstructionElements[1]);
					String opVal2 = this.varValues.get(irInstructionElements[2]);
					
					//Used to check if constant for constant folding.
					boolean isConstant = false;
					
					//The assembly instruction to be built.	
					assemblyInstruction = "";
					
					//Get the operation type, int or real (FLOAT)
					char type = irInstruction.charAt(3);
		
					//Value of possible constants.
					Integer val1I = 0;
					Integer val2I = 0;
					Double val1F = 0.0;
					Double val2F = 0.0;
		
					//Check if integer constants and integer type.
					if(type == 'I') {
						
						assemblyInstruction = "muli ";
		
						try {
							val1I = Integer.parseInt(opVal1);
							val2I = Integer.parseInt(opVal2);
							isConstant = true;
						}catch(Exception e) {
						}	
					}
		
					//Check if floating point constant and floating point type.
					else if (type == 'F') {
							
						assemblyInstruction = "mulr ";
	
						try {
							val1F = Double.parseDouble(opVal1);
							val2F = Double.parseDouble(opVal2);
							isConstant = true;
						}catch(Exception e) {
							
						}
					}
		
					//If the expression is constant perform constant folding.
					if(isConstant && type == 'I') {
		
						this.constantFolding(val1I, val2I, '*');
						continue;
					}
					
					else if(isConstant && type == 'F') {
	
						this.constantFolding(val1F, val2F, '*');
						continue;
					}
		
					//Not constant attempt other construction of instruction.
					else {
								
						//Perform peep-hole optimizations and constant peep-hole optimization.
						
						targetRegister = null;
						operand = null;
						RegisterOperandPair regOp;
						
						regOp = this.peepholeReduction(targetRegister, operand, irInstructionElements, true);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
						
						regOp = this.TemporaryPeepholeReduction(targetRegister, operand, irInstructionElements, true);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
								
						if(targetRegister != null) {
										
							assemblyInstruction = assemblyInstruction.concat(operand + " " + targetRegister);
										
						}
						
						//If no optimization can be performed generate a standard instruction(s)
						else {
						
							targetRegister = "r" + this.registerCounter;
							
							this.assemblyCode.add("move " + irInstructionElements[1] + " " + targetRegister); 
							
							assemblyInstruction = assemblyInstruction.concat(irInstructionElements[2] + " " + targetRegister);
							
						}
					}
				}
				
				//Process an DIV IR instruction and apply optimizations.
				else if(irInstruction.contains("DIV")) {
							
					//Get the operands.
					String opVal1 = this.varValues.get(irInstructionElements[1]);
					String opVal2 = this.varValues.get(irInstructionElements[2]);
					
					//Used to check if constant for constant folding.
					boolean isConstant = false;
					
					//The assembly instruction to be built.	
					assemblyInstruction = "";
					
					//Get the operation type, int or real (FLOAT)
					char type = irInstruction.charAt(3);
		
					//Value of possible constants.
					Integer val1I = 0;
					Integer val2I = 0;
					Double val1F = 0.0;
					Double val2F = 0.0;
		
					//Check if integer constants and integer type.
					if(type == 'I') {
						
						assemblyInstruction = "divi ";
		
						try {
							val1I = Integer.parseInt(opVal1);
							val2I = Integer.parseInt(opVal2);
							isConstant = true;
						}catch(Exception e) {
						}	
					}
		
					//Check if floating point constant and floating point type.
					else if (type == 'F') {
							
						assemblyInstruction = "divr ";
	
						try {
							val1F = Double.parseDouble(opVal1);
							val2F = Double.parseDouble(opVal2);
							isConstant = true;
						}catch(Exception e) {
							
						}
					}
		
					//If the expression is constant perform constant folding.
					if(isConstant && type == 'I') {
		
						this.constantFolding(val1I, val2I, '/');
						continue;
					}
					
					else if(isConstant && type == 'F') {
	
						this.constantFolding(val1F, val2F, '/');
						continue;
					}
		
					//Not constant attempt other construction of instruction.
					else {
								
						//Perform peep-hole optimizations and constant peep-hole optimization.
						
						targetRegister = null;
						operand = null;		
						RegisterOperandPair regOp;
						
						regOp = this.peepholeReduction(targetRegister, operand, irInstructionElements, false);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
						
						regOp = this.TemporaryPeepholeReduction(targetRegister, operand, irInstructionElements, false);
						
						targetRegister = regOp.getRegister();
						operand = regOp.getOperand();
								
						if(targetRegister != null) {
										
							assemblyInstruction = assemblyInstruction.concat(operand + " " + targetRegister);
										
						}
						
						//If no optimization can be performed generate a standard instruction(s)
						else {
						
							targetRegister = "r" + this.registerCounter;
							
							this.assemblyCode.add("move " + irInstructionElements[1] + " " + targetRegister); 
							
							assemblyInstruction = assemblyInstruction.concat(irInstructionElements[2] + " " + targetRegister);
							
						}
						
					}
				}
				
				//Create and add the tiny instruction.
				this.assemblyCode.add(assemblyInstruction);
				
				String nextIRInstruction = this.IRCode.remove();
				
				String[] nextIRInstructionElements = nextIRInstruction.split(" ");
				
				this.assemblyCode.add("move " + targetRegister + " " + nextIRInstructionElements[2]);
				
				//Sets the register with its current new value and the variables computed value.
				this.registers.put(targetRegister, nextIRInstructionElements[2]);
				this.varValues.put(nextIRInstructionElements[2], "r"+this.registerCounter);
				this.registerCounter++;
			}
		}
			
		//Processing of instructions is over generate a tiny system call to exit the program.
		this.assemblyCode.add("sys halt");
	}
	
	//Converts a temporary ($T#) to a register (r#).
	private String convertTemporaryToReg(String temporary){
				
		return "r" + this.registerCounter;			
	}
	
	//Check if a value is in a given register.
	private boolean inRegister(String value) {
		
		return this.registers.containsValue(value);
	}
	
	//Given a value that exists in a register retrieve the register number it belongs to.
	private String getRegisterFromValue(String value) {
		
		for(String register: this.registers.keySet()) {
			
			if(this.registers.get(register).equals(value)) {
				return register;
			}
		}
		
		return null;
	}
	
	//Constant folding optimization for integers.
	private void constantFolding(Integer operand1, Integer operand2, char operation){

		Integer constantVal = null;

		switch (operation) {

			case '+':
				constantVal = operand1 + operand2;
				break;

			case '-':
				constantVal = operand1 - operand2;
				break;

			case '*':
				constantVal = operand1 * operand2;
				break;

			case '/':
				constantVal = operand1 / operand2;
				break;

		}
		
		//Generate the constant folding instructions.
		constantFoldInstruction(constantVal);
	}
	
	//Constant folding optimization for floats.
	private void constantFolding(Double operand1, Double operand2, char operation){

		Double constantVal = null;

		switch (operation) {

			case '+':
				constantVal = operand1 + operand2;
				break;

			case '-':
				constantVal = operand1 - operand2;
				break;

			case '*':
				constantVal = operand1 * operand2;
				break;

			case '/':
				constantVal = operand1 / operand2;
				break;

		}
		
		//Generate the constant folding instructions.
		constantFoldInstruction(constantVal);
	}
	
	//Generates instructions for a constant fold optimization.
	private void constantFoldInstruction(Number constantVal) {
		
		//Move constant into register for storing.
		this.assemblyCode.add("move " + constantVal + " " + "r" + this.registerCounter);
		
		String nextIRInstruction = this.IRCode.remove();
		
		String[] nextIRInstructionElements = nextIRInstruction.split(" ");
		
		//Move constant into memory for loading.
		this.assemblyCode.add("move " + "r" + this.registerCounter + " " + nextIRInstructionElements[2]);
		
		this.registers.put("r"+this.registerCounter, nextIRInstructionElements[2]);
		this.varValues.put(nextIRInstructionElements[2], constantVal.toString());
		
		
		this.registerCounter++;
	}
	
	//Peep-hole optimization for register reuse, containing variables.
	private RegisterOperandPair peepholeReduction(String targetRegister, String operand, String[] irInstructionElements, boolean isCom){
		
		//Reuse register with needed variable.
		if(this.inRegister(irInstructionElements[1])) {
								
			targetRegister = this.getRegisterFromValue(irInstructionElements[1]);
			operand = irInstructionElements[2];
			
			return new RegisterOperandPair(targetRegister, operand);
		}
		
		//Reuse second operand if possible, if commutative.
		else if(isCom && this.inRegister(irInstructionElements[2])) {
			
			targetRegister = this.getRegisterFromValue(irInstructionElements[2]);
			operand = irInstructionElements[1];
			
			return new RegisterOperandPair(targetRegister, operand);
		}
		
		return new RegisterOperandPair(targetRegister, operand);
		
	}
	
	//Peep-hole optimization for register reuse containing temporaries, along with constant operation optimization for temporaries.
	private RegisterOperandPair TemporaryPeepholeReduction(String targetRegister, String operand, String[] irInstructionElements, boolean isCom){
		
		if(irInstructionElements[1].contains("$T")){
			
			//For double constant operations use correct registers and reuse.
			if(targetRegister == null) {
				if(irInstructionElements[2].contains("$T")) {
					targetRegister = "r" + (this.registerCounter - 2);
					operand = "r" + (this.registerCounter - 1);
				}
				
				//Reuse register with constant.
				else {
					
					targetRegister = "r" + (this.registerCounter - 1);
					operand = irInstructionElements[2];
				}
			}
			
			//Use constant value stored in a register.
			else {
				operand = "r" + (this.registerCounter - 1);
			}
			
			return new RegisterOperandPair(targetRegister, operand);
		}
				
		//Check if second elements in a temporary.
		else if(irInstructionElements[2].contains("$T")){
			
			//If commutative reuse if not cannot reuse due to operation difference.
			if(isCom && targetRegister == null) {
				targetRegister = "r" + (this.registerCounter - 1);
				operand = irInstructionElements[1];
			}
			
			//Use constant value in register.
			else {
				operand = "r" + (this.registerCounter - 1);
			}
			
			return new RegisterOperandPair(targetRegister, operand);
		}
		
		return new RegisterOperandPair(targetRegister, operand);
	}
}
