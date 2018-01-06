package components;

public class IQInstruction {
	private boolean status;
	private boolean src1;
	private boolean src2;
	private Instruction instruction;
	
	public IQInstruction(Instruction instruction) {
		this.instruction = instruction;
		status = false;
		src1 = false;
		src2 = false;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isSrc1() {
		return src1;
	}

	public void setSrc1(boolean src1) {
		this.src1 = src1;
	}

	public boolean isSrc2() {
		return src2;
	}

	public void setSrc2(boolean src2) {
		this.src2 = src2;
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public void setInstruction(Instruction instruction) {
		this.instruction = instruction;
	}

	@Override
	public String toString() {
		return "IQInstruction [status=" + status + ", src1=" + src1 + ", src2=" + src2 + ", "
				+ (instruction != null ? "instruction=" + instruction : "") + "]";
	}
}
