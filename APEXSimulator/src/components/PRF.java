package components;

public class PRF {
	
	String physicalRegister;
	int value;
	boolean status;
	boolean allocated;
	boolean renamed;
	
	public PRF(String physicalRegister, int value, boolean status, boolean allocated, boolean renamed) {
		this.physicalRegister = physicalRegister;
		this.value = value;
		this.status = status;
		this.allocated = allocated;
		this.renamed = renamed;
	}

	public String getPhysicalRegister() {
		return physicalRegister;
	}

	public void setPhysicalRegister(String physicalRegister) {
		this.physicalRegister = physicalRegister;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isAllocated() {
		return allocated;
	}

	public void setAllocated(boolean allocated) {
		this.allocated = allocated;
	}

	public boolean isRenamed() {
		return renamed;
	}

	public void setRenamed(boolean renamed) {
		this.renamed = renamed;
	}

	@Override
	public String toString() {
		return "PRF [" + (physicalRegister != null ? "physicalRegister=" + physicalRegister + ", " : "") + "value="
				+ value + ", status=" + status + ", allocated=" + allocated + ", renamed=" + renamed + "]";
	}
	
}
