package components;

public class LSQ {
	
	Instruction instruction;
	String opcode;
	int dest;
	String tagdest;
	boolean validDest;
	int valueSrc1;
	String tagSrc1;
	boolean validSrc1;
	int valueSrc2;
	int address;
	boolean validAddress;
	boolean valid;
	
	public LSQ() {}
	
	public Instruction getInstruction() {
		return instruction;
	}

	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public boolean isValidAddress() {
		return validAddress;
	}

	public void setValidAddress(boolean validAddress) {
		this.validAddress = validAddress;
	}

	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

	public int getDest() {
		return dest;
	}

	public void setDest(int dest) {
		this.dest = dest;
	}

	public String getTagdest() {
		return tagdest;
	}

	public void setTagdest(String tagdest) {
		this.tagdest = tagdest;
	}

	public boolean isValidDest() {
		return validDest;
	}

	public void setValidDest(boolean validDest) {
		this.validDest = validDest;
	}

	public int getValueSrc1() {
		return valueSrc1;
	}

	public void setValueSrc1(int valueSrc1) {
		this.valueSrc1 = valueSrc1;
	}

	public String getTagSrc1() {
		return tagSrc1;
	}

	public void setTagSrc1(String tagSrc1) {
		this.tagSrc1 = tagSrc1;
	}

	public boolean isValidSrc1() {
		return validSrc1;
	}

	public void setValidSrc1(boolean validSrc1) {
		this.validSrc1 = validSrc1;
	}

	public int getValueSrc2() {
		return valueSrc2;
	}

	public void setValueSrc2(int valueSrc2) {
		this.valueSrc2 = valueSrc2;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public String toString() {
		return "LSQ [" + (instruction != null ? "instruction=" + instruction + ", " : "")
				+ (opcode != null ? "opcode=" + opcode + ", " : "") + "dest=" + dest + ", "
				+ (tagdest != null ? "tagdest=" + tagdest + ", " : "") + "validDest=" + validDest + ", valueSrc1="
				+ valueSrc1 + ", " + (tagSrc1 != null ? "tagSrc1=" + tagSrc1 + ", " : "") + "validSrc1=" + validSrc1
				+ ", valueSrc2=" + valueSrc2 + ", address=" + address + ", validAddress=" + validAddress + ", valid="
				+ valid + "]";
	}
	
}
