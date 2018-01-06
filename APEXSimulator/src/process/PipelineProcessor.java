package process;

import components.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

public class PipelineProcessor { // All main processing of pipeline is being implemented in this class

	Map<Integer,Integer> memoryElements = null;
    private Map<Integer,Instruction> instructionMap = new LinkedHashMap<>();
    private Register registerObj = null;
    private Memory memoryObj = null;
    private Map<String, Instruction> pipeline = new LinkedHashMap<>();
    private Map<String, Integer> forwardedData = new LinkedHashMap<>();
    private LinkedList<String> outputDependency = new LinkedList<>();
    private List<Instruction> instructionSet = new ArrayList<>();
    private LinkedList<PRF> physicalRegisterList = new LinkedList<>();
    private LinkedList<IssueQueue> IssueQueueList = new LinkedList<>();
    private Queue<LSQ> LSQList = new LinkedList<>();
    private Queue<ReorderBuffer> reorderBufferList = new LinkedList<>();
    private final int noOfPR = 32;
    private int programCounter =  4000; // initialized program pointer to 4000
    private int PC = 4000;
    private int instructionCount = 1;
    private int simCycles = 0;
    private int simCycleCheckDiv = 0;
    private int simCycleCheckMul = 0;
    private int arithmaticResult = 0;
    private boolean fetchInstruction = true;
    private boolean fetchHalt = true;
    private boolean isHalt = false;
    private boolean isStalled = false;
    private int zeroFlag = 0;
    private int nonZeroFlag = 0;
    FileWriter fileWriter = null;
    PrintWriter printWriter = null;
    private int simMEMCycle = 0;
    
    public void initialization() {
		//System.out.println("start programCounter=>"+programCounter);
		for(Instruction instruction : instructionSet) {
			instruction.setProgramCounter(programCounter);
			instructionMap.put(programCounter, instruction);
			programCounter += 4;
		}
		//initializationDisplay();
		initPipeline();
		initRegistersAndMemory();
		initPhysicalRegisterFile();
	}    
   
	public void initPhysicalRegisterFile() {
		for(int i=0;i<32;i++) {
			physicalRegisterList.add(new PRF("P"+i, 0, false, false, false));
		}
	}

	public void initRegistersAndMemory() {
		registerObj = new Register();
		memoryObj = new Memory();
	}
	
	public void initPipeline() {
		pipeline.put("FETCH", null);
		pipeline.put("D/RF", null);
		pipeline.put("INTFU", null);
		pipeline.put("MULFU1", null);
		pipeline.put("MULFU2", null);
		pipeline.put("DFU1", null);
		pipeline.put("DFU2", null);
		pipeline.put("DFU3", null);
		pipeline.put("DFU4", null);
		pipeline.put("FRWD", null);
		pipeline.put("RET", null);
	}
	
	/*public void initializationDisplay() {
		for(Instruction instruction : instructionSet) {
			String printInst = getInstructionInfo(instruction);
		}
	}*/
    
	public String getInstructionInfo(Instruction instruction) { // this method returns all information of instruction
		
		String printInstruction = "";
		if(instruction != null) {
			String type = instruction.getOpcode();
			printInstruction = instruction.getProgramCounter()+":";
			printInstruction += type+",";
			
			if(instruction.getDestination() != null) {
				printInstruction += instruction.getDestination().getOperand()+",";
			}
			if(instruction.getSource1() != null) {
				printInstruction += instruction.getSource1().getOperand()+",";
			}
			if(instruction.getSource2() != null) {
				printInstruction += instruction.getSource2().getOperand()+",";
			}
			return "("+(instruction.getInstructionNo())+")"+printInstruction;
		}
		else {
			printInstruction = "[No Instruction]";
			return printInstruction;
		}
		
	}
	
	public String getIssuqQueueInstructions() {
		String ISQ="";
		if(!IssueQueueList.isEmpty()) {
			for(IssueQueue iq : IssueQueueList) {
				ISQ += iq.getInstruction().getInstructionNo()+",";
			}
		}
		else {
			return "EMPTY";
		}
		return ISQ.replaceAll(",$", "");
	}
	
	public void displayMemory() { //display memory blocks
		
		System.out.println("---------Memory---------");
		for(int i=0;i<1000;i++) {
			System.out.print("[");
			System.out.print(i+":");
			System.out.print(memoryObj.getMemoryBlocks().get(i));
			System.out.print("]");
			if(i % 15 == 0) {
				System.out.println("");
			}
		}
	}

	public void displayRegister() { // display registers and its contents
		//System.out.println(registerObj);
		//System.out.println("---------Registers---------"+registerObj.getRegisters());
		for(int i=0;i<registerObj.getREGISTERCOUNT();i++) {
			System.out.print("[");
			System.out.print("R"+i+":"+registerObj.getRegisters().get("R"+i));
			System.out.print("]");
		}
		System.out.println("");
	}
	
	public void simulate(int cycles) throws Exception {
		simCycles = 0;
		PC = 4000;
		fileWriter = new FileWriter("outputs.txt");
	    printWriter = new PrintWriter(fileWriter);
		while((cycles > simCycles) && (!instructionMap.isEmpty() && !allStagesCleared(PC)) && !isHalt) {
			Instruction instruction =  instructionMap.get(PC);
			fetchInstruction = true;
			retirement();
			//FrwdStage();
			//MEMStageClear();
			LSQ();
			EXEStage();
			Queue();
			DRFStage();
			FetchStage(instruction);
			displayPipeline(simCycles);
			simCycles++;
		}
	}
	
	public void displayPipeline(int simCycles) {
		System.out.println("Rename Table:");
		if(!registerObj.getRenameTable().isEmpty()) {	
			for(Entry<String, String> e : registerObj.getRenameTable().entrySet()) { 
				System.out.println(e.getKey()+" : "+e.getValue());  
			}
		}
		else {
			System.out.println("[EMPTY]");
		}
		System.out.println(" ");
		
		System.out.println("IQ:");
		if(!IssueQueueList.isEmpty()) {
			for(IssueQueue iq : IssueQueueList) {
				System.out.println(getInstructionInfo(iq.getInstruction()));
			}
		}
		else {
			System.out.println("[EMPTY]");
		}
		System.out.println(" ");
		System.out.println("ROB:");
		int count = 1;
		String commit = "";
		if(!reorderBufferList.isEmpty()) {
			for(ReorderBuffer rob : reorderBufferList) {
				System.out.print(getInstructionInfo(rob.getInstruction()));
				if(rob.isValid() && (count == 1)) {
					commit = "<-COMMIT";
					System.out.print(commit);
				}
				if(rob.isValid() && (count == 2) && !"".equals(commit)) {
					System.out.print("<-COMMIT");
				}
				System.out.println("");
			count++;
			}
		}
		else {
			System.out.println("[EMPTY]"); 
		}
		System.out.println(" ");
		System.out.println("LSQ:");
		if(!LSQList.isEmpty()) {
			for(LSQ lsq : LSQList) {
				System.out.println(getInstructionInfo(lsq.getInstruction()));
			}
		}
		else {
			System.out.println("[EMPTY]"); 
		}
		
		System.out.println("Simlation Cycle : "+(simCycles+1));
		System.out.println("FETCH      "+getInstructionInfo(pipeline.get("FETCH")));
		System.out.println("D/RF      "+getInstructionInfo(pipeline.get("D/RF")));
		//System.out.println("IQ         ["+getIssuqQueueInstructions()+"]");
		System.out.println("INTFU      "+getInstructionInfo(pipeline.get("INTFU")));
		System.out.println("MULFU1      "+getInstructionInfo(pipeline.get("MULFU1")));
		System.out.println("MULFU2      "+getInstructionInfo(pipeline.get("MULFU2")));
		System.out.println("DFU1      "+getInstructionInfo(pipeline.get("DFU1")));
		System.out.println("DFU2      "+getInstructionInfo(pipeline.get("DFU2")));
		System.out.println("DFU3      "+getInstructionInfo(pipeline.get("DFU3")));
		System.out.println("DFU4      "+getInstructionInfo(pipeline.get("DFU4")));
		System.out.println("MEM      "+getInstructionInfo(pipeline.get("MEM")));
		System.out.println("*************************************************");
			
	}
	
	public boolean allStagesCleared(int pcValue) {
		removeFromROB();
		if(pcValue > 4000 && pipeline.get("RET") == null && pipeline.get("FRWD") == null && pipeline.get("FETCH") == null && pipeline.get("D/RF") == null && pipeline.get("INTFU") == null && pipeline.get("MULFU1") == null && pipeline.get("MULFU2") == null && pipeline.get("DFU1") == null && pipeline.get("DFU2") == null && pipeline.get("DFU3") == null && pipeline.get("DFU4") == null && IssueQueueList.isEmpty()) {
			if(pcValue > 4000 && !reorderBufferList.isEmpty()) {
				//System.out.println("allSTages Cleared INNI: "+reorderBufferList); 
				//removeFromROB();
				return false;
			}
			return true;
		}
		return false;
	}
	
	public void clearRetirement() {
		if(pipeline.get("RET") != null) {
			pipeline.put("RET", null);
		}
	}
	public void retirement() {
		Instruction instruction = null;
		
		if(pipeline.get("INTFU") != null) {
			instruction = pipeline.get("INTFU");
		}
		else if(pipeline.get("MULFU2") != null) {
			instruction = pipeline.get("MULFU2");
		}
		else if(pipeline.get("DFU4") != null) {
			instruction = pipeline.get("DFU4");
		}
		else if(pipeline.get("MEM") != null) {
			instruction = pipeline.get("MEM");
		}
		
		if(instruction != null) {
			//System.out.println("RET INS"+instruction.getInstructionNo()); 
			instruction.setInstStage("RET");
			operations(instruction,"RET");
			pipeline.put("RET", instruction);
			if(pipeline.get("INTFU") != null) {
				//LSQ();
				pipeline.put("INTFU", null);
			}
			if(pipeline.get("MULFU2") != null) {
				pipeline.put("MULFU2", null);
			}
			if(pipeline.get("DFU4") != null) {
				pipeline.put("DFU4", null);
			}
			if(pipeline.get("MEM") != null && simMEMCycle >= 3) {
				
				pipeline.put("MEM", null);
				simMEMCycle = 0;
			}
		}
		clearRetirement();
	}
	
	public void MEMStage(Instruction instruction) {
		if(pipeline.get("MEM") == null) {
			if("LOAD".equals(instruction.getOpcode()) || "STORE".equals(instruction.getOpcode())) {
				instruction.setInstStage("MEM");
				operations(instruction,"MEM");
				pipeline.put("MEM", instruction);
			}
		}
		simMEMCycle++;
	}

	public Queue<LSQ> insertLoadAt(LSQ lastlsq, int index) {
		Queue<LSQ> LSQListD = new LinkedList<>();
		int indexD = 0;
		boolean populateRest = false;
		LSQ lastlsq1 = null;
		for(LSQ lsq : LSQList) {
			if(index == indexD) {
				LSQListD.add(lastlsq);
				populateRest = true;
			}
			else if(index < indexD){
				LSQListD.add(lsq);
				
			}
			indexD++;
			if(populateRest) {
				LSQListD.add(lsq);
			}
			lastlsq1 = lsq;
		}
		LSQListD.remove(lastlsq1);
		return LSQListD;
	}
	public void LSQ() {
		if(!LSQList.isEmpty()) {
			LSQ lastlsq = null;
			for(LSQ lsqEach : LSQList) {
				lastlsq = lsqEach;
			}
			
			//if(LSQList.peek().isValid()) {
				LSQ lsq1 = LSQList.peek();
				//System.out.println("");
				if(lsq1.isValidAddress()) {
					MEMStage(lsq1.getInstruction()); 
					LSQList.remove(lsq1);
					return;
				}
				
			//}
			//Shifting load at position below the first store since it is valid, just bypassing that load might lead to starvation of previous stores if such valid loads keep coming
			if("LOAD".equals(lastlsq.getOpcode())) {
				//System.out.println("Last LOAD caought"); 
				if(lastlsq.isValid()) {
					//System.out.println("Last LOAD caought 2");
					boolean allStoreValid = true;
					for(LSQ lsqEach : LSQList) {
						if("STORE".equals(lsqEach.getOpcode())) {
							if(!lsqEach.isValid()) {
								allStoreValid = false;
								break;
							}
							else if(lsqEach.isValid() && lastlsq.isValid() && (lsqEach.getAddress() == lastlsq.getAddress())) {
								//System.out.println("Last LOAD caought 3");
								/*lastlsq.setDest(lsqEach.getDest());
								lastlsq.setValidDest(true);*/
								MEMStage(lastlsq.getInstruction());
								LSQList.remove(lastlsq);
							}
						}
					}
					if(allStoreValid) {
						//Find first store and put latest load below it
						//System.out.println("ALL VALID AHAH");
						int index = 0;
						for(LSQ lsqEach : LSQList) {
							if("STORE".equals(lsqEach.getOpcode())) {
								Queue<LSQ> temp = insertLoadAt(lastlsq,index);
								LSQList = temp;
							}
							index++;
						}
					}
				}
				
			}
			/*else {
				//Program order execution of LSQ
				System.out.println("IN ELSE QUEUE LOGIC"); 
				LSQ lsq = LSQList.peek();
				System.out.println("");
				if(lsq.isValidAddress()) {
					MEMStage(lsq.getInstruction()); 
					LSQList.remove(lsq);
				}
				
							
			}*/
		}
	}
	

	public void EXEStage() {
		if(!IssueQueueList.isEmpty()) {
			Iterator<IssueQueue> iterator = IssueQueueList.iterator();
			while(iterator.hasNext()) {
				IssueQueue iq = iterator.next();
				//System.out.print(iq.getInstruction().getInstructionNo()+"|size:"+IssueQueueList.size());
				//System.out.println("Pending INst:"+iq.getInstruction().getInstructionNo()+"|Staus:"+iq.isValid());
				if(iq.isValid() && !iq.isIssued()) {
					
					Instruction instruction = iq.getInstruction();
					if(instruction.getProgramCounter() == 4028) {
						//System.out.println("Start");
					}
					if("LOAD".equalsIgnoreCase(instruction.getOpcode()) || "STORE".equalsIgnoreCase(instruction.getOpcode()) || "BAL".equalsIgnoreCase(instruction.getOpcode()) || "JAL".equalsIgnoreCase(instruction.getOpcode()) || "JUMP".equals(instruction.getOpcode()) || "BZ".equals(instruction.getOpcode()) || "BNZ".equals(instruction.getOpcode()) || "ADD".equals(instruction.getOpcode()) || "SUB".equals(instruction.getOpcode()) || "MOVC".equals(instruction.getOpcode()) || "EXOR".equals(instruction.getOpcode()) || "AND".equals(instruction.getOpcode()) || "OR".equals(instruction.getOpcode())) {
						if(pipeline.get("INTFU") == null) {
							//System.out.println("GOING IN INTFU:"+instruction.getInstructionNo()); 
							iq.setIssued(true);
							INTFU(instruction);
							/*IssueQueueList.remove(iq);*/iterator.remove();
							//System.out.println("IQ Status:");
							for(IssueQueue iqs : IssueQueueList) {
								//System.out.print(iqs.getInstruction().getInstructionNo()+"|size:"+IssueQueueList.size());	
							}
							continue;
						}
					}
					if("MUL".equalsIgnoreCase(instruction.getOpcode())) {
						if(pipeline.get("MULFU1") == null) {
							iq.setIssued(true);
							MULFU1(instruction);
							/*IssueQueueList.remove(iq);*/iterator.remove();
							continue;
						}
					}
					if("DIV".equalsIgnoreCase(instruction.getOpcode())) {
						if(pipeline.get("DFU1") == null) {
							iq.setIssued(true);
							DFU1(instruction);
							/*IssueQueueList.remove(iq);*/iterator.remove();
							continue;
						}
					}
				}			
			}
		}
		MULFU2();
		DFU4();
		DFU3();
		DFU2();
	}
	
	public void DFU4() {
		Instruction instruction = pipeline.get("DFU3");
		if(instruction != null) { 
			instruction.setInstStage("DFU4");
			operations(instruction,"EXE");
			pipeline.put("DFU4", instruction);
			pipeline.put("DFU3", null);			
		}
		
	}
	
	public void DFU3() {
		Instruction instruction = pipeline.get("DFU2");
		//System.out.println("DFU3 me:"+instruction);
		if(instruction != null) { 
			instruction.setInstStage("DFU3");
			pipeline.put("DFU3", instruction);
			pipeline.put("DFU2", null);			
		}
		
	}
	
	public void DFU2() {
		//System.out.println("simCycleCheckDiv simCycles"+simCycleCheckDiv+" "+simCycles);
		Instruction instruction = pipeline.get("DFU1");
		if(instruction != null && simCycleCheckDiv != simCycles) { 
			instruction.setInstStage("DFU2");
			pipeline.put("DFU2", instruction);
			pipeline.put("DFU1", null);	
		}
		
	}
	
	public void DFU1(Instruction instruction) {
		//System.out.println("IN DFU1---->");
		if("DIV".equalsIgnoreCase(instruction.getOpcode())) {
			instruction.setInstStage("DFU1");
			pipeline.put("DFU1", instruction);
			pipeline.put("IQ", null);
			simCycleCheckDiv = simCycles;
		}
	}
	
	public void MULFU2() {
		Instruction instruction = pipeline.get("MULFU1");
		if(instruction != null && simCycleCheckMul != simCycles) {
			if(pipeline.get("MULFU1") != null) { 
				instruction.setInstStage("MULFU2");
				operations(instruction,"EXE");
				pipeline.put("MULFU2", instruction);
				pipeline.put("MULFU1", null);	
				//dependencyCheck(instruction);
			}	
		}
	}

	public void MULFU1(Instruction instruction) {
		//System.out.println("IN MULFU1---->");
		if("MUL".equalsIgnoreCase(instruction.getOpcode())) {
			instruction.setInstStage("MULFU1");
			pipeline.put("MULFU1", instruction);
			pipeline.put("IQ", null);
			simCycleCheckMul = simCycles;
		}
		
	}

	public void INTFU(Instruction instruction) {
		//System.out.println("IN INTFU---->"+pipeline.get("IQ")+"||"+"|"+("BAL".equalsIgnoreCase(instruction.getOpcode()))+"|"+instruction);
		if(instruction != null) {
			if(pipeline.get("IQ") != null || pipeline.get("INTFU") == null) {
				if("LOAD".equalsIgnoreCase(instruction.getOpcode()) || "STORE".equalsIgnoreCase(instruction.getOpcode()) || "BAL".equalsIgnoreCase(instruction.getOpcode()) || "JAL".equalsIgnoreCase(instruction.getOpcode()) || "JUMP".equals(instruction.getOpcode()) || "BZ".equals(instruction.getOpcode()) || "BNZ".equals(instruction.getOpcode()) || "ADD".equals(instruction.getOpcode()) || "SUB".equals(instruction.getOpcode()) || "MOVC".equals(instruction.getOpcode()) || "LOAD".equals(instruction.getOpcode()) || "STORE".equals(instruction.getOpcode()) || "EXOR".equals(instruction.getOpcode()) || "AND".equals(instruction.getOpcode()) || "OR".equals(instruction.getOpcode())) {
					
					instruction.setInstStage("INTFU");
					operations(instruction,"EXE");
					pipeline.put("INTFU", instruction);
					pipeline.put("IQ", null);	
				}
				
			}
		}
		/*for(IssueQueue iq : IssueQueueList) {
			
		}*/
	}

	public void Queue() {
		if(pipeline.get("D/RF") != null) {
			
			Instruction instruction = pipeline.get("D/RF");
			String opcode = instruction.getOpcode();
			instruction.setInstStage("IQ");
			operations(instruction, "IQ");
			pipeline.put("IQ", instruction);
			pipeline.put("D/RF", null);
			
		}
		
	}
	
	public void DRFStage() {
		if(pipeline.get("D/RF") == null && pipeline.get("FETCH") != null) {
			Instruction instruction = pipeline.get("FETCH");
			instruction.setInstStage("D/RF");
			operations(instruction,"D/RF");
			pipeline.put("D/RF", instruction);
			pipeline.put("FETCH", null);
		}
		else {
			if(pipeline.get("D/RF") != null) {
				Instruction instruction = pipeline.get("D/RF");
				operations(instruction,"D/RF");
			}
		}
	}
	
	public void renaming(Instruction instruction) {
		if(instruction != null) {
			if(instruction.getDestination() != null) {
				for(int i=0;i<physicalRegisterList.size();i++) {
					if(!physicalRegisterList.get(i).isAllocated() && !physicalRegisterList.get(i).isRenamed()) {
						registerObj.getRenameTable().put(physicalRegisterList.get(i).getPhysicalRegister(),instruction.getDestination().getOperand());
						registerObj.getRenameTableD().put(instruction.getDestination().getOperand(), physicalRegisterList.get(i).getPhysicalRegister());
						physicalRegisterList.get(i).setValue(-9655);
						physicalRegisterList.get(i).setStatus(false);
						physicalRegisterList.get(i).setAllocated(true);
						physicalRegisterList.get(i).setRenamed(true);
						instruction.getDestination().setOperand(physicalRegisterList.get(i).getPhysicalRegister());
						break;
					}
				}
				
			}
		}
	}
	
	public void FetchStage(Instruction instruction) {
		if(pipeline.get("FETCH") == null && fetchInstruction && fetchHalt) {
			pipeline.put("FETCH", instruction);
			PC += 4;
		}
	}
	
	public void forwarding(Instruction inst, String pRegister, int value) {
		
		physicalRegisterList.forEach(prf->{
			if(pRegister.equals(prf.getPhysicalRegister())){ 
				prf.setValue(value);prf.setStatus(true);
			}
		});
		
		for(ReorderBuffer rob : reorderBufferList) {
			String archReg = "";
			for(Entry<String, String> entry : registerObj.getRenameTable().entrySet()) {
				if(entry.getKey().equals(pRegister)) {
					archReg = entry.getValue();
					//break;
				}
			}
			
			if(archReg.equals(rob.getDest()) && (rob.getInstructionNo() == inst.getInstructionNo())) {
				//System.out.println("ArchReg:"+archReg+"|"+value+"|reb Dest:"+rob.getDest());
				rob.setValid(true);
				rob.setValue(value);
			}
		}
		
		for(LSQ lsq : LSQList){
			/*System.out.println("PREg:"+pRegister+"|lsqtagdest:"+lsq.getTagdest()+"|lsq.isValidDest:"+lsq.isValidDest());
			System.out.println("PREg:"+pRegister+"|lsq.getTagSrc1:"+lsq.getTagSrc1()+"|lsq.isValidSrc1:"+lsq.isValidSrc1());
			System.out.println("PREg:"+pRegister+"| isValidAddr:"+lsq.isValidAddress());  */
			if(pRegister.equals(lsq.getTagdest())) {
				lsq.setDest(value);
				lsq.setValidDest(true);
			}
			if(pRegister.equals(lsq.getTagSrc1())) {
				lsq.setValueSrc1(value);
				lsq.setValidSrc1(true);
			}
			if(lsq.isValidAddress()) {
				lsq.setValid(true);
			}
		}
		
		IssueQueue iqRemove = null;
		for(IssueQueue iq : IssueQueueList){
			//System.out.print("OK=>"+iq.getInstruction().getInstructionNo()+"<=|"); 
			if(iq != null) {
				if(pRegister.equals(iq.getTagsrc1())) {
					iq.setValuesrc1(value);
					iq.setValidsrc1(true);
				}
			
				if(pRegister.equals(iq.getTagsrc2())) {
					iq.setValuesrc2(value);
					iq.setValidsrc2(true);
				}
				if(pRegister.equals(iq.getDestTag())) { 
					iq.setDestValue(value);
					iq.setValidDest(true);
				}
				/*if(iq.isValidDest() && iq.isValidsrc1()) {
					iq.setValid(true);
				}*/
				if("STORE".equals(iq.getInstruction().getOpcode())) {
					//System.out.println("STORE wala love:P ==> "); 
					if(pRegister.equals(iq.getDestTag())) {
						iq.setDestValue(value);
						iq.setValidDest(true);
					}
					if(pRegister.equals(iq.getTagsrc1())) {
						iq.setValuesrc1(value);
						iq.setValidsrc1(true);
					}
					if(iq.isValidDest() && iq.isValidsrc1()) {
						iq.setValid(true);
					}
				}
				if(iq.isValidsrc1() && iq.isValidsrc2()) {
					iq.setValid(true);
					iqRemove = iq;
				}
				
				//System.out.println(inst.getInstructionNo()+"| DestValid:"+iq.isValidDest()+"| src1valid:"+iq.isValidsrc1());
				
			}
		}
		
	}
	public void removeFromROB() {
		//System.out.println("IN removeFromROB()"); 
		if(!reorderBufferList.isEmpty()) {
			ReorderBuffer rob = reorderBufferList.peek();
			if(rob != null && rob.isValid()) {
				registerObj.getRegisters().put(rob.getDest(), rob.getValue());
				//System.out.println("ROB BEING REMOVED:"+rob.getInstructionNo()); 
				reorderBufferList.remove(rob);
			}
		
			ReorderBuffer rob2 = reorderBufferList.peek();
			if(rob2 != null && rob2.isValid()) {
				registerObj.getRegisters().put(rob2.getDest(), rob2.getValue());
				////System.out.println("ROB BEING REMOVED:"+rob.getInstructionNo());
				reorderBufferList.remove(rob2);
			}
		}
		
	}
	public void operations(Instruction instruction, String operation) {
		if("RET".equals(operation)) {
			String opCode = "";
			if(instruction != null)
				opCode = instruction.getOpcode();
			switch(opCode) {	// applying switch cases for different opcodes
				case "ADD":case "SUB":case "MUL":case "OR":case "AND":case "EXOR":case "DIV":case "MOVC":
					//System.out.println(reorderBufferList);
					removeFromROB();
					physicalRegisterList.forEach((object) -> {
					    if(instruction.getDestination().getOperand().equals(object.getPhysicalRegister())) {
					    	object.setStatus(true);
					    	object.setAllocated(false);
					    	object.setRenamed(true);
					    	Iterator<Map.Entry<String,String>> iter = registerObj.getRenameTable().entrySet().iterator();
					    	while (iter.hasNext()) {
					    	    Map.Entry<String,String> entry = iter.next();
					    	    if(entry.getKey().equals(object.getPhysicalRegister()) && entry.getValue().equals(instruction.getDestinationOrig().getOperand())){
					    	       // iter.remove();
					    	        //registerObj.getRenameTableD().remove(instruction.getDestinationOrig().getOperand());
					    	        //System.out.println("RenameD"+registerObj.getRenameTableD());
					    	    }
					    	    
					    	}
					    }
					});
					
					break;
				case "LOAD":
					break;
				case "STORE":
					break;
				case "BZ":
					break;
				case "BNZ":
					break;
				case "JUMP":
					break;
				case "JAL": case "BAL":
					break;
				default:
					break;
			}
		}
		else if("LSQ".equals(operation)) {
			
		}
		else if("IQ".equals(operation)) {
			String opCode = instruction.getOpcode();
			switch(opCode) {// applying switch cases for different opcodes
				case "ADD":case "SUB":case "MUL":case "OR":case "AND":case "EXOR":case "DIV":
					IssueQueue iq = new IssueQueue();
					for(PRF prf : physicalRegisterList) {
						//checking whether valid value is right
						if(instruction.getSource1().getOperand().startsWith("R")) { 
							iq.setValidsrc1(true);
							iq.setTagsrc1("");
							iq.setValuesrc1(instruction.getSrc1());
							
						}
						else if(prf.getPhysicalRegister().equals(instruction.getSource1().getOperand())){
							iq.setValidsrc1(prf.isStatus());
							iq.setTagsrc1(prf.getPhysicalRegister());
							iq.setValuesrc1(prf.getValue());
						}
						
						if(instruction.getSource2().getOperand().startsWith("R")) {
							iq.setValidsrc2(true);
							iq.setTagsrc2("");
							iq.setValuesrc2(instruction.getSrc2());
						}
						else if(prf.getPhysicalRegister().equals(instruction.getSource2().getOperand())){
							iq.setValidsrc2(prf.isStatus());
							iq.setTagsrc2(prf.getPhysicalRegister());
							iq.setValuesrc2(prf.getValue());
						}
					}
					iq.setDest(instruction.getDestination().getOperand()); 
					if(iq.isValidsrc1() && iq.isValidsrc2()) {
						iq.setValid(true);
					}
					else {
						iq.setValid(false);
					}
					iq.setIssued(false);iq.setInstruction(instruction);iq.setInstructionNo(instruction.getInstructionNo());
					//System.out.println(iq); 
					IssueQueueList.add(iq);
					ReorderBuffer rob = new ReorderBuffer();
					rob.setInstruction(instruction);rob.setInstructionNo(instruction.getInstructionNo());rob.setOpcode(instruction.getOpcode());rob.setDest(instruction.getDestinationOrig().getOperand());rob.setValue(-9865);rob.setValid(false);
					reorderBufferList.add(rob);
					break;
				case "MOVC":
					reorderBufferList.add(new ReorderBuffer(instruction,instruction.getInstructionNo(),instruction.getOpcode(), instruction.getDestinationOrig().getOperand(), -9865, false));
					IssueQueueList.add(new IssueQueue(instruction.getInstructionNo(), instruction, true, instruction.getOpcode(), instruction.getDestination().getOperand(), true, instruction.getDestination().getOperand(), instruction.getSrc1(), true, instruction.getDestination().getOperand(), -98776, false));
					break;
				case "LOAD":
					IssueQueue iqLoad = new IssueQueue();
					LSQ lsqLoad = new LSQ();
					lsqLoad.setOpcode("LOAD");
					//System.out.println("Ins s1:"+instruction.getProgramCounter()+"|"+instruction.getSource1().getOperand()); 
					//System.out.println("Ins s2:"+instruction.getProgramCounter()+"|"+instruction.getSource2().getOperand());
					for(PRF prf : physicalRegisterList) {
						//checking whether valid value is right
						if(instruction.getSource1().getOperand().startsWith("R")) { 
							iqLoad.setValidsrc1(true);
							iqLoad.setTagsrc1("");
							iqLoad.setValuesrc1(instruction.getSrc1());
							lsqLoad.setValueSrc1(instruction.getSrc1());
							lsqLoad.setValidSrc1(true);
							lsqLoad.setTagSrc1(""); 
 						}
						else if(prf.getPhysicalRegister().equals(instruction.getSource1().getOperand())){
							iqLoad.setValidsrc1(prf.isStatus());
							iqLoad.setTagsrc1(prf.getPhysicalRegister());
							iqLoad.setValuesrc1(prf.getValue());
							lsqLoad.setValueSrc1(prf.getValue());
							lsqLoad.setValidSrc1(prf.isStatus());
							lsqLoad.setTagSrc1(prf.getPhysicalRegister());
						}
						
						if(instruction.getSource2().getOperand() != null) {
							iqLoad.setValuesrc2(instruction.getSrc2());
							lsqLoad.setValueSrc2(instruction.getSrc2());
							iqLoad.setValidsrc2(true); 
						}
					}
					
					iqLoad.setDest(instruction.getDestination().getOperand());
								
					if(iqLoad.isValidsrc1() && iqLoad.isValidsrc2()) {
						iqLoad.setValid(true);
					}
					else {
						iqLoad.setValid(false);
					}
					iqLoad.setIssued(false);iqLoad.setInstruction(instruction);iqLoad.setInstructionNo(instruction.getInstructionNo());
					//System.out.println(iqLoad); 
					IssueQueueList.add(iqLoad);
					ReorderBuffer robLoad = new ReorderBuffer();
					robLoad.setInstruction(instruction);robLoad.setInstructionNo(instruction.getInstructionNo());robLoad.setOpcode(instruction.getOpcode());robLoad.setDest(instruction.getDestinationOrig().getOperand());robLoad.setValue(-9865);robLoad.setValid(false);
					reorderBufferList.add(robLoad);
					
					lsqLoad.setDest(-9897);
					lsqLoad.setValidDest(false);
					lsqLoad.setTagdest(instruction.getDestination().getOperand());
					lsqLoad.setValid(false);
					if(lsqLoad.isValidDest() && lsqLoad.isValidSrc1())
						lsqLoad.setValid(true);
					else
						lsqLoad.setValid(false);
					lsqLoad.setInstruction(instruction); 
					LSQList.add(lsqLoad);
					break;
					
				case "STORE":
					IssueQueue iqStore = new IssueQueue();
					LSQ lsqStore = new LSQ();
					lsqStore.setOpcode("STORE");
					//System.out.println("Ins s1:"+instruction.getProgramCounter()+"|"+instruction.getSource1().getOperand()); 
					//System.out.println("Ins s2:"+instruction.getProgramCounter()+"|"+instruction.getSource2().getOperand()); 
					for(PRF prf : physicalRegisterList) {
						//checking whether valid value is right
						if(instruction.getDestination().getOperand().startsWith("R")) { 
							/*iqStore.setValidsrc1(true);
							iqStore.setTagsrc1("");
							iqStore.setValuesrc1(registerObj.getRegisters().get(instruction.getDestination().getOperand()));*/
							iqStore.setValidDest(true);
							iqStore.setDestTag("");
							iqStore.setDestValue(registerObj.getRegisters().get(instruction.getDestination().getOperand())); 
							lsqStore.setValidDest(true);
							lsqStore.setTagdest("");
							lsqStore.setDest(registerObj.getRegisters().get(instruction.getDestination().getOperand())); 
						}
						else if(prf.getPhysicalRegister().equals(instruction.getDestination().getOperand())){
							/*iqStore.setValidsrc1(prf.isStatus());
							iqStore.setTagsrc1(prf.getPhysicalRegister());
							iqStore.setValuesrc1(prf.getValue());*/
							iqStore.setValidDest(prf.isStatus());
							iqStore.setDestTag(prf.getPhysicalRegister());
							iqStore.setDestValue(prf.getValue());
							lsqStore.setValidDest(prf.isStatus());
							lsqStore.setTagdest(prf.getPhysicalRegister());
							lsqStore.setDest(prf.getValue());
						}
						
						if(instruction.getSource1().getOperand().startsWith("R")) { 
							/*iqStore.setValidsrc2(true);
							iqStore.setTagsrc2("");
							iqStore.setValuesrc2(registerObj.getRegisters().get(instruction.getSource1().getOperand()));*/
							iqStore.setValidsrc1(true);
							iqStore.setTagsrc1("");
							iqStore.setValuesrc1(registerObj.getRegisters().get(instruction.getSource1().getOperand()));
							lsqStore.setValidSrc1(true);
							lsqStore.setTagSrc1("");
							lsqStore.setValueSrc1(registerObj.getRegisters().get(instruction.getSource1().getOperand()));
						}
						else if(prf.getPhysicalRegister().equals(instruction.getSource1().getOperand())) {
							iqStore.setValidsrc1(prf.isStatus());
							iqStore.setTagsrc1(prf.getPhysicalRegister()); 
							iqStore.setValuesrc1(prf.getValue());
						}
							
						if(prf.getPhysicalRegister().equals(instruction.getSource2().getOperand())){
							lsqStore.setValueSrc2(prf.getValue());	
						}
						iqStore.setValidsrc2(true); 
						
					}
					 
					if(iqStore.isValidsrc1() && iqStore.isValidDest()) {
						iqStore.setValid(true);
					}
					else {
						iqStore.setValid(false);
					}
					
					iqStore.setIssued(false);iqStore.setInstruction(instruction);iqStore.setInstructionNo(instruction.getInstructionNo());
					//System.out.println(iqStore); 
					IssueQueueList.add(iqStore);
					
					reorderBufferList.add(new ReorderBuffer(instruction, instruction.getInstructionNo(), instruction.getOpcode(), instruction.getSource1Orig().getOperand(), -9865, false));
					lsqStore.setInstruction(instruction); 
					LSQList.add(lsqStore);
					
					break;
				case "BZ":
					break;
				case "BNZ":
					break;
				case "JUMP":
					break;
				case "JAL": case "BAL":
					break;
				default:
					break;
			}
		}
		else if("D/RF".equals(operation)) {
			String opCode = instruction.getOpcode();
			switch(opCode) {// applying switch cases for different opcodes
			case "ADD":case "SUB":case "MUL":case "OR":case "AND":case "EXOR":case "DIV":
				
				if(instruction != null) {
					
					if(instruction.getSource1() != null && !instruction.getSource1().isLiteral()) {
						if(registerObj.getRenameTableD().get(instruction.getSource1().getOperand()) != null) {
							String latestPR = "";
							for(Entry<String, String> entry : registerObj.getRenameTable().entrySet()) {
								if(instruction.getSource1().getOperand().equals(entry.getValue())) { 
									latestPR = entry.getKey();
								}
							}
							instruction.getSource1().setOperand(latestPR); 
						} 
						
						else if(instruction.getSource1().getOperand().startsWith("R")) {
							instruction.setSrc1(registerObj.getRegisters().get(instruction.getSource1().getOperand()));
							//System.out.println("if sources are not present in rename table/PRF 1 | src1:"+instruction.getSrc1());
						}
					}
					if(instruction.getSource2() != null && !instruction.getSource2().isLiteral()) {
						if(registerObj.getRenameTableD().get(instruction.getSource2().getOperand()) != null) {
							String latestPR = "";
							for(Entry<String, String> entry : registerObj.getRenameTable().entrySet()) {
								if(instruction.getSource2().getOperand().equals(entry.getValue())) { 
									latestPR = entry.getKey();
								}
							}
							instruction.getSource2().setOperand(latestPR);
						}
						else if(instruction.getSource2().getOperand().startsWith("R")) {
							instruction.setSrc2(registerObj.getRegisters().get(instruction.getSource2().getOperand()));
							//System.out.println("if sources are not present in rename table/PRF 1 | src1:"+instruction.getSrc2());
						}
					}
					if(instruction.getDestination() != null) {
						renaming(instruction);
					}
				}
				
				break;
			case "MOVC":
				if(instruction.getSource1() != null) {
					instruction.setSrc1(Integer.parseInt(instruction.getSource1().getOperand()));
				}
				renaming(instruction);
				break;
				
			case "LOAD":
				if(instruction != null) {
					if(instruction.getSource1() != null && !instruction.getSource1().isLiteral()) {
						if(registerObj.getRenameTableD().get(instruction.getSource1().getOperand()) != null) {
							instruction.getSource1().setOperand(registerObj.getRenameTableD().get(instruction.getSource1().getOperand()));
						} 
						
						else if(instruction.getSource1().getOperand().startsWith("R")) {
							instruction.setSrc1(registerObj.getRegisters().get(instruction.getSource1().getOperand()));
							//System.out.println("if sources are not present in rename table/PRF 1 | src1:"+instruction.getSrc1());
						}
					}
					if(instruction.getSource2() != null) {
						instruction.setSrc2(Integer.parseInt(instruction.getSource2().getOperand()));
					}
					if(instruction.getDestination() != null) {
						renaming(instruction);
					}
				}
				break;
			case "STORE":
				if(instruction != null) {
					if(instruction.getDestination() != null  && !instruction.getDestination().isLiteral()) {
						/*renaming(instruction);*/
						if(registerObj.getRenameTableD().get(instruction.getDestination().getOperand()) != null) {
							instruction.getDestination().setOperand(registerObj.getRenameTableD().get(instruction.getDestination().getOperand()));
						}
						else if(instruction.getDestination().getOperand().startsWith("R")) {
							instruction.setDest(registerObj.getRegisters().get(instruction.getDestination().getOperand()));
							//System.out.println("if sources are not present in rename table/PRF 1 | src1:"+instruction.getDest());
						}
					}

					if(instruction.getSource1() != null && !instruction.getSource1().isLiteral()) {
						if(registerObj.getRenameTableD().get(instruction.getSource1().getOperand()) != null) {
							instruction.getSource1().setOperand(registerObj.getRenameTableD().get(instruction.getSource1().getOperand()));
						}
						else if(instruction.getSource1().getOperand().startsWith("R")) {
							instruction.setSrc1(registerObj.getRegisters().get(instruction.getSource1().getOperand()));
							//System.out.println("if sources are not present in rename table/PRF 1 | src1:"+instruction.getSrc1());
						}
					}
					if(instruction.getSource2() != null && instruction.getSource2().isLiteral()) {
						instruction.setSrc2(Integer.parseInt(instruction.getSource2().getOperand()));
					}
				}
				break;
			case "JUMP":
				if(instruction.getSource1() != null && instruction.getSource2() != null) {
					//System.out.println("fgxf="+instruction.getSource1().getOperand()+"--"+registerObj.getRegisters().get(instruction.getSource1().getOperand())+"--"+registerObj.getValidMap().get(instruction.getSource1().getOperand()));
					if(registerObj.getValidMap().get(instruction.getSource1().getOperand())) {
						instruction.setSrc1(registerObj.getRegisters().get(instruction.getSource1().getOperand()));
					}
					else if(forwardedData.containsKey(instruction.getSource1().getOperand())) {
						instruction.setSrc1(forwardedData.get(instruction.getSource1().getOperand()));
						//System.out.println("D/RF stage src1=>"+instruction.getSrc1());
					}
					else {
						isStalled = true;
					}
					instruction.setSrc2(Integer.parseInt(instruction.getSource2().getOperand()));
					//System.out.println("D/RF stage src1+src2=>"+instruction.getSrc1()+"--"+instruction.getSrc2());
				}
				//System.out.println("src1+src2=>"+instruction.getSrc1()+"--"+instruction.getSrc2()+"--"+forwardedData.containsKey(instruction.getSource1().getOperand()));
				
				break;
			case "BZ":
				if(instruction.getSource1() != null)
					instruction.setSrc1(Integer.parseInt(instruction.getSource1().getOperand()));
				break;
			case "BNZ":
				
				if(instruction.getSource1() != null)
					instruction.setSrc1(Integer.parseInt(instruction.getSource1().getOperand()));
				
				break;
			case "HALT":
				fetchHalt = false;
				
				break;
			case "JAL":case "BAL":
				if(instruction.getSource1() != null && instruction.getSource2() != null) {
					//System.out.println("JAL DRF INIT=>"+(instruction.getSource1().getOperand()));
					
					if(registerObj.getValidMap().get(instruction.getSource1().getOperand())) {
						//System.out.println("JAL valid only 1=>"+registerObj.getRegisters().get(instruction.getSource1().getOperand()));
						instruction.setSrc1(registerObj.getRegisters().get(instruction.getSource1().getOperand()));
						instruction.setStalledSource1(false);
					}
					else if(forwardedData.containsKey(instruction.getSource1().getOperand())) {
						//System.out.println("JAL forwarded only 1=>"+forwardedData.get(instruction.getSource1().getOperand()));
						instruction.setSrc1(forwardedData.get(instruction.getSource1().getOperand()));
						instruction.setStalledSource1(false);
					}
					else {
						//System.out.println("isstalled=>1");
						instruction.setStalledSource1(true);
					}
					instruction.setSrc2(Integer.parseInt(instruction.getSource2().getOperand()));
					instruction.setStalledSource2(false);
				}
				
				break;
			default: System.out.println("Invalid operation case in D/RF Stage"); break;
			
			}
		}
		else if("EXE".equals(operation)) {
				
				String opCode = instruction.getOpcode();
				IssueQueue iqObject = null;
				for(IssueQueue iq : IssueQueueList) {
					if(instruction.getInstructionNo() == iq.getInstructionNo() && iq.isIssued()){
						iqObject = iq;
						break;
					}
				}
				switch(opCode) {
				case "ADD":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							arithmaticResult = iqObject.getValuesrc1() + iqObject.getValuesrc2();
							instruction.setDest(arithmaticResult);
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					//break;
				
				case "SUB":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							arithmaticResult = iqObject.getValuesrc1() - iqObject.getValuesrc2();
							instruction.setDest(arithmaticResult);
						}	
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					
				
				case "MUL":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							arithmaticResult = iqObject.getValuesrc1() * iqObject.getValuesrc2();
							instruction.setDest(arithmaticResult);
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					//break;
				case "DIV":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							arithmaticResult = iqObject.getValuesrc1() / iqObject.getValuesrc2();
							instruction.setDest(arithmaticResult);
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					
				case "OR":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							arithmaticResult = iqObject.getValuesrc1() | iqObject.getValuesrc2();
							instruction.setDest(arithmaticResult);
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					
				case "AND":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							arithmaticResult = iqObject.getValuesrc1() & iqObject.getValuesrc2();
							instruction.setDest(arithmaticResult);
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					//break;
				case "EXOR":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							arithmaticResult = iqObject.getValuesrc1() ^ iqObject.getValuesrc2();
							instruction.setDest(arithmaticResult);
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					//break;
				case "MOVC":
					if(iqObject != null) {
						if(iqObject.isValid()) {
							instruction.setDest(iqObject.getValuesrc1());
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest());
					return;
					//break;
				case "LOAD":
					instruction.setDest((instruction.getSrc1() + instruction.getSrc2())/4);
					for(LSQ lsq : LSQList) {
						if(lsq.getInstruction().getInstructionNo() == instruction.getInstructionNo() && lsq.getOpcode().equals(instruction.getOpcode())) {
							lsq.setAddress(instruction.getDest());
							lsq.setValidAddress(true); 
						}
					}
					forwarding(instruction, instruction.getDestination().getOperand(), instruction.getDest()); 
					//System.out.println("EXE LOAD"+instruction.getDest());
					break;
				case "STORE":
					instruction.setSrc1((instruction.getSrc1() + instruction.getSrc2())/4);
					for(LSQ lsq : LSQList) {
						if(lsq.getInstruction().getInstructionNo() == instruction.getInstructionNo() && lsq.getOpcode().equals(instruction.getOpcode())) {
							lsq.setAddress(instruction.getSrc1());
							lsq.setValidAddress(true); 
						}
					}
					
					//System.out.println("EXE STORE:"+instruction.getSrc1());
					break;
				case "JUMP":
					instruction.setDest(instruction.getSrc1() + instruction.getSrc2());
					flushFetchAndDecodeStages();
					PC = instruction.getDest();
					fetchInstruction = false;
					break;
				case "BZ":
					if(arithmaticResult == 0) {
						zeroFlag = 0;
						flushFetchAndDecodeStages();
						fetchInstruction = false;
						PC = PC + instruction.getSrc1();
						//System.out.println("updated PC=>"+PC);
					}
					break;
				case "BNZ":
					if(arithmaticResult != 0) {
						nonZeroFlag = 0;
						flushFetchAndDecodeStages();
						fetchInstruction = false;
						PC = PC + instruction.getSrc1();
					}
					break;
				case "HALT":
					flushFetchAndDecodeStages();
					break;
				case "JAL":case "BAL":
					instruction.setDest(PC+4);
					flushFetchAndDecodeStages();
					PC = instruction.getSrc1() + instruction.getSrc2();
					fetchInstruction = false;
					break;
				default: System.out.println("Invalid operation case in EXE Stage");
				}			
		}
		else if("MEM".equals(operation)) {
			String opCode = instruction.getOpcode();
			
			switch(opCode) {
			case "LOAD":
				/*int location = instruction.getDest()/4;
				instruction.setMemoryValue(memoryObj.getMemoryBlocks().get(location));
				forwarding(instruction,instruction.getDestination().getOperand(), instruction.getMemoryValue());*/
				break;
			case "STORE":
				/*instruction.setMemoryValue(instruction.getSrc1());*/
				break;
			}
			
		}
				
	}

	public void flushFetchAndDecodeStages() { // flushing fetch and D/RF stages for BZ/BNZ/JUMP/JAL
		pipeline.put("FETCH", null);
		pipeline.put("D/RF", null);
		
	}

	public Register getRegisterObj() {
		return registerObj;
	}

	public void setRegisterObj(Register registerObj) {
		this.registerObj = registerObj;
	}

	public Memory getMemoryObj() {
		return memoryObj;
	}

	public void setMemoryObj(Memory memoryObj) {
		this.memoryObj = memoryObj;
	}

	public Map<String, Instruction> getPipeline() {
		return pipeline;
	}

	public void setPipeline(Map<String, Instruction> pipeline) {
		this.pipeline = pipeline;
	}

	public int getProgramCounter() {
		return programCounter;
	}

	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter;
	}

	public Map<Integer, Integer> getMemoryElements() {
		return memoryElements;
	}
	public void setMemoryElements(Map<Integer, Integer> memoryElements) {
		this.memoryElements = memoryElements;
	}
	public Map<Integer, Instruction> getInstructionMap() {
		return instructionMap;
	}
	public void setInstructionMap(Map<Integer, Instruction> instructionMap) {
		this.instructionMap = instructionMap;
	}
	public List<Instruction> getInstructionSet() {
		return instructionSet;
	}
	public void setInstructionSet(List<Instruction> instructionSet) {
		this.instructionSet = instructionSet;
	}

}