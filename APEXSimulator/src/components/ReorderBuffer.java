package components;

public class ReorderBuffer {
	Instruction  instruction;
	String instructionNo;
	String opcode;
	String dest;
	int value;
	boolean valid;
	
	public ReorderBuffer(Instruction instruction, String instructionNo, String opcode, String dest, int value, boolean valid) {
		this.instruction = instruction;
		this.instructionNo = instructionNo;
		this.opcode = opcode;
		this.dest = dest;
		this.value = value;
		this.valid = valid;
	}
	
	public ReorderBuffer() {
		
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}

	public String getInstructionNo() {
		return instructionNo;
	}

	public void setInstructionNo(String instructionNo) {
		this.instructionNo = instructionNo;
	}

	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

	public String getDest() {
		return dest;
	}
	
	public void setDest(String dest) {
		this.dest = dest;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	@Override
	public String toString() {
		return "ReorderBuffer [" + (instructionNo != null ? "instructionNo=" + instructionNo + ", " : "")
				+ (opcode != null ? "opcode=" + opcode + ", " : "") + (dest != null ? "dest=" + dest + ", " : "")
				+ "value=" + value + ", valid=" + valid + "]";
	}
			
}