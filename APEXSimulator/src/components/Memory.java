package components;

import java.util.*;

public class Memory {
	//memory class to store and retrieve memory contents
	private final int MEMORY = 4000;
	private final int MEMORYSIZE = 4;
	private Map<Integer,Integer> memoryBlocks = new LinkedHashMap<>();
	
	public Memory() {
		initializeMemory();
	}
	
	public void initializeMemory() {
		for(int i=0;i<MEMORY/MEMORYSIZE;i+=1) {
			memoryBlocks.put(i, 0);
		}
	}
	
	public Map<Integer, Integer> getMemoryBlocks() {
		return memoryBlocks;
	}
	
	public void setMemoryBlocks(Map<Integer, Integer> memoryBlocks) {
		this.memoryBlocks = memoryBlocks;
	}
	
	public int getMEMORY() {
		return MEMORY;
	}
	
	public int getMEMORYSIZE() {
		return MEMORYSIZE;
	}
	
	@Override
	public String toString() {
		return "Memory [MEMORY=" + MEMORY + ", MEMORYSIZE=" + MEMORYSIZE + ", "
				+ (memoryBlocks != null ? "memoryBlocks=" + memoryBlocks : "") + "]";
	}
	
}