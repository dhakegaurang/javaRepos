package components;

public class IssueQueue {
	
	String instructionNo;
	Instruction instruction;
	boolean valid;
	String opcode;
	String dest;
	String destTag;
	boolean validDest = false;
	int destValue;
	boolean validsrc1;
	String tagsrc1;
	int valuesrc1;
	boolean validsrc2;
	String tagsrc2;
	int valuesrc2;
	boolean issued;
	
	public IssueQueue(String instructionNo, Instruction instruction, boolean valid, String opcode, String dest,
			String destTag, boolean validDest, int destValue, boolean validsrc1, String tagsrc1, int valuesrc1,
			boolean validsrc2, String tagsrc2, int valuesrc2, boolean issued) {
		this.instructionNo = instructionNo;
		this.instruction = instruction;
		this.valid = valid;
		this.opcode = opcode;
		this.dest = dest;
		this.destTag = destTag;
		this.validDest = validDest;
		this.destValue = destValue;
		this.validsrc1 = validsrc1;
		this.tagsrc1 = tagsrc1;
		this.valuesrc1 = valuesrc1;
		this.validsrc2 = validsrc2;
		this.tagsrc2 = tagsrc2;
		this.valuesrc2 = valuesrc2;
		this.issued = issued;
	}
	public IssueQueue(String instructionNo, Instruction instruction, boolean valid, String opcode, String dest, boolean validsrc1, String tagsrc1, int valuesrc1, boolean validsrc2, String tagsrc2, int valuesrc2, boolean issued) {
		this.instructionNo = instructionNo;
		this.instruction = instruction;
		this.valid = valid;
		this.opcode = opcode;
		this.dest = dest;
		this.validsrc1 = validsrc1;
		this.tagsrc1 = tagsrc1;
		this.valuesrc1 = valuesrc1;
		this.validsrc2 = validsrc2;
		this.tagsrc2 = tagsrc2;
		this.valuesrc2 = valuesrc2;
		this.issued = issued;
	}
	public IssueQueue() {}
	public String getInstructionNo() {
		return instructionNo;
	}
	public void setInstructionNo(String instructionNo) {
		this.instructionNo = instructionNo;
	}
	public Instruction getInstruction() {
		return instruction;
	}
	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
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
	public String getDestTag() {
		return destTag;
	}
	public void setDestTag(String destTag) {
		this.destTag = destTag;
	}
	public boolean isValidDest() {
		return validDest;
	}
	public void setValidDest(boolean validDest) {
		this.validDest = validDest;
	}
	public int getDestValue() {
		return destValue;
	}
	public void setDestValue(int destValue) {
		this.destValue = destValue;
	}
	public boolean isValidsrc1() {
		return validsrc1;
	}
	public void setValidsrc1(boolean validsrc1) {
		this.validsrc1 = validsrc1;
	}
	public String getTagsrc1() {
		return tagsrc1;
	}
	public void setTagsrc1(String tagsrc1) {
		this.tagsrc1 = tagsrc1;
	}
	public int getValuesrc1() {
		return valuesrc1;
	}
	public void setValuesrc1(int valuesrc1) {
		this.valuesrc1 = valuesrc1;
	}
	public boolean isValidsrc2() {
		return validsrc2;
	}
	public void setValidsrc2(boolean validsrc2) {
		this.validsrc2 = validsrc2;
	}
	public String getTagsrc2() {
		return tagsrc2;
	}
	public void setTagsrc2(String tagsrc2) {
		this.tagsrc2 = tagsrc2;
	}
	public int getValuesrc2() {
		return valuesrc2;
	}
	public void setValuesrc2(int valuesrc2) {
		this.valuesrc2 = valuesrc2;
	}
	public boolean isIssued() {
		return issued;
	}
	public void setIssued(boolean issued) {
		this.issued = issued;
	}
	@Override
	public String toString() {
		return "IssueQueue [" + (instructionNo != null ? "instructionNo=" + instructionNo + ", " : "")
				+ (instruction != null ? "instruction=" + instruction + ", " : "") + "valid=" + valid + ", "
				+ (opcode != null ? "opcode=" + opcode + ", " : "") + (dest != null ? "dest=" + dest + ", " : "")
				+ (destTag != null ? "destTag=" + destTag + ", " : "") + "validDest=" + validDest + ", destValue="
				+ destValue + ", validsrc1=" + validsrc1 + ", " + (tagsrc1 != null ? "tagsrc1=" + tagsrc1 + ", " : "")
				+ "valuesrc1=" + valuesrc1 + ", validsrc2=" + validsrc2 + ", "
				+ (tagsrc2 != null ? "tagsrc2=" + tagsrc2 + ", " : "") + "valuesrc2=" + valuesrc2 + ", issued=" + issued
				+ "]";
	}
	
}
