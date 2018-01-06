package components;

public class Operand {
	// this is actual operand in the instruction
	private String operand;
	private boolean isLiteral = false;
	public Operand(String operand) {
		this.operand = operand;
	}

	public String getOperand() {
		return operand;
	}

	public void setOperand(String operand) {
		this.operand = operand;
	}
	
	public boolean isLiteral() {
		
		return !operand.startsWith("R");
	}

	public void setLiteral(boolean isLiteral) {
		this.isLiteral = isLiteral;
	}

	@Override
	public String toString() {
		return "Operand [" + (operand != null ? "operand=" + operand + ", " : "") + "isLiteral=" + isLiteral + "]";
	}
	
}
