package components;

public class Instruction {
	
	private int programCounter;//PC
	private String opcode="";// opcode string
	private String instructionType;// type of instruction
	private Operand source1,source2;// sources in instruction
	private Operand source1Orig,source2Orig;
	private Operand destination;// destination of instruction
	private Operand destinationOrig;
	private int src1,src2;// for setting values in sources in D/RF stage
	private int dest;
	private int memoryValue = 0;// setting memory value 
	private String instStage = "";
	private int literalValue; // setting literal value
	private boolean activeFlag = false;
	private String instructionNo = "";
	private boolean isStalledSource1 = false;
	private boolean isStalledSource2 = false;
	public Instruction() {
		
	}

	public Instruction(String opCode) {
		instructionType = instructionType(opCode);
		this.opcode = opCode;
		setOpcode(opCode);
	}

	public String instructionType(String opcode) {
		String type="";
		
		if(!"".equals(opcode)) {
			switch(opcode) {
			
			case "ADD":case "SUB":case "MUL":case "OR":case "AND":case "EXOR":case "DIV":
				type = "register2resiter";
				break;
			case "MOVC":
				type = "literal2register";
				break;
			case "LOAD":case "STORE":
				type = "memory";
				break;
			case "JUMP":
				type = "jump";
				break;
			case "BZ":case "BNZ":
				type = "branch";
				break;
			case "HALT":
				type="halt";
				break;
			case "JAL":case "BAL":
				type="jal";
				break;
			default: System.err.println("You've entered invalid opcode");
			}
		}
		return type;
	}
	
	public Operand getSource1Orig() {
		return source1Orig;
	}

	public void setSource1Orig(Operand source1Orig) {
		this.source1Orig = source1Orig;
	}

	public Operand getSource2Orig() {
		return source2Orig;
	}

	public void setSource2Orig(Operand source2Orig) {
		this.source2Orig = source2Orig;
	}

	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

	public int getProgramCounter() {
		return programCounter;
	}

	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter;
	}

	public String getInstructionType() {
		return instructionType;
	}

	public void setInstructionType(String instructionType) {
		this.instructionType = instructionType;
	}

	public Operand getSource1() {
		return source1;
	}

	public void setSource1(Operand source1) {
		this.source1 = source1;
	}

	public Operand getSource2() {
		return source2;
	}

	public void setSource2(Operand source2) {
		this.source2 = source2;
	}

	public Operand getDestination() {
		return destination;
	}

	public void setDestination(Operand destination) {
		this.destination = destination;
	}

	public int getLiteralValue() {
		return literalValue;
	}

	public void setLiteralValue(int literalValue) {
		this.literalValue = literalValue;
	}
	
	public int getSrc1() {
		return src1;
	}

	public void setSrc1(int src1) {
		this.src1 = src1;
	}

	public int getSrc2() {
		return src2;
	}

	public void setSrc2(int src2) {
		this.src2 = src2;
	}

	public int getDest() {
		return dest;
	}

	public void setDest(int dest) {
		this.dest = dest;
	}
	
	public int getMemoryValue() {
		return memoryValue;
	}

	public void setMemoryValue(int memoryValue) {
		this.memoryValue = memoryValue;
	}
	
	public String getInstStage() {
		return instStage;
	}

	public void setInstStage(String instStage) {
		this.instStage = instStage;
	}

	public boolean isActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(boolean activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getInstructionNo() {
		return instructionNo;
	}

	public void setInstructionNo(String instructionNo) {
		this.instructionNo = instructionNo;
	}
	
	public boolean isStalledSource1() {
		return isStalledSource1;
	}

	public void setStalledSource1(boolean isStalledSource1) {
		this.isStalledSource1 = isStalledSource1;
	}

	public boolean isStalledSource2() {
		return isStalledSource2;
	}

	public void setStalledSource2(boolean isStalledSource2) {
		this.isStalledSource2 = isStalledSource2;
	}
	
	public Operand getDestinationOrig() {
		return destinationOrig;
	}

	public void setDestinationOrig(Operand destinationOrig) {
		this.destinationOrig = destinationOrig;
	}

	@Override
	public String toString() {
		return "Instruction [programCounter=" + programCounter + ", "
				+ (opcode != null ? "opcode=" + opcode + ", " : "")
				+ (instructionType != null ? "instructionType=" + instructionType + ", " : "")
				+ (source1 != null ? "source1=" + source1 + ", " : "")
				+ (source2 != null ? "source2=" + source2 + ", " : "")
				+ (destination != null ? "destination=" + destination + ", " : "")
				+ (destinationOrig != null ? "destinationOrig=" + destinationOrig + ", " : "") + "src1=" + src1
				+ ", src2=" + src2 + ", dest=" + dest + ", memoryValue=" + memoryValue + ", "
				+ (instStage != null ? "instStage=" + instStage + ", " : "") + "literalValue=" + literalValue
				+ ", activeFlag=" + activeFlag + ", "
				+ (instructionNo != null ? "instructionNo=" + instructionNo + ", " : "") + "isStalledSource1="
				+ isStalledSource1 + ", isStalledSource2=" + isStalledSource2 + "]";
	}
	
}
