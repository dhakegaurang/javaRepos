package components;

import java.util.*;

public class Register {
	// register class to retrieve and access registers in pipeline
	private Map<String,Integer> registers = new LinkedHashMap<>();
	private Map<String, String> renameTable = new HashMap<>();
	private Map<String, String> renameTableD = new HashMap<>();
	private final int REGISTERCOUNT = 16;
	private Map<String, Boolean> activeDependency = new LinkedHashMap<>();
	private Map<String, Boolean> validMap = new LinkedHashMap<>();
	public Map<String, Boolean> getActiveDependency() {
		return activeDependency;
	}

	public void setActiveDependency(Map<String, Boolean> activeDependency) {
		this.activeDependency = activeDependency;
	}

	public Register() {
		initializeRegister();
	}
	
	public void initializeRegister() {
		for(int i=0;i<REGISTERCOUNT;i++) {
			registers.put("R"+i, 0);
			validMap.put("R"+i, false);		
		}
		validMap.put("ZF", false);
		validMap.put("NZF", false);
	}
	public boolean isRegister(String register) {
		
		return register.contains("R");
	}
	public Map<String, Integer> getRegisters() {
		return registers;
	}

	public void setRegisters(Map<String, Integer> registers) {
		this.registers = registers;
	}

	public int getREGISTERCOUNT() {
		return REGISTERCOUNT;
	}
	
	public Map<String, Boolean> getValidMap() {
		return validMap;
	}

	public void setValidMap(Map<String, Boolean> validMap) {
		this.validMap = validMap;
	}
	
	public Map<String, String> getRenameTable() {
		return renameTable;
	}

	public void setRenameTable(Map<String, String> renameTable) {
		this.renameTable = renameTable;
	}
	
	public Map<String, String> getRenameTableD() {
		return renameTableD;
	}

	public void setRenameTableD(Map<String, String> renameTableD) {
		this.renameTableD = renameTableD;
	}

	@Override
	public String toString() {
		return "Register [" + (registers != null ? "registers=" + registers + ", " : "")
				+ (renameTable != null ? "renameTable=" + renameTable + ", " : "") + "REGISTERCOUNT=" + REGISTERCOUNT
				+ ", " + (activeDependency != null ? "activeDependency=" + activeDependency + ", " : "")
				+ (validMap != null ? "validMap=" + validMap : "") + "]";
	}
	
}
