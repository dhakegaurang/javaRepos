package process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;

import components.Instruction;
import components.Operand;

public class InstructionGetter {

	File inputFileObj;
	List<Instruction> instructions = null;
	public InstructionGetter(String inputFileName) {
		inputFileObj = new File(inputFileName);
	}
	
	public void getInstuction() {
		try {
			Scanner scr = new Scanner(inputFileObj);
			int instructionNo = 0;
			instructions = new ArrayList<>();
			// Retrieving all instruction in loop and preparing instruction object in inserting in list of instructions
			while(scr.hasNextLine()) {
				String[] splitInst = scr.nextLine().split(",");
				Instruction instruction = null;
				instruction = new Instruction(splitInst[0].trim());
				if("register2resiter".equalsIgnoreCase(instruction.getInstructionType())) {
					instruction.setDestination(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setDestinationOrig(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setSource1(new Operand(splitInst[2].replace(",", "").replace("#", "").trim()));
					instruction.setSource2(new Operand(splitInst[3].replace(",", "").replace("#", "").trim()));
				}
				else if("literal2register".equalsIgnoreCase(instruction.getInstructionType())) {
					instruction.setDestination(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setDestinationOrig(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setSource1(new Operand(splitInst[2].replace(",", "").replace("#", "").trim()));
				}
				else if("memory".equalsIgnoreCase(instruction.getInstructionType())) {
					instruction.setDestination(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setDestinationOrig(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setSource1(new Operand(splitInst[2].replace(",", "").replace("#", "").trim()));
					instruction.setSource2(new Operand(splitInst[3].replace(",", "").replace("#", "").trim()));
					instruction.setSource1Orig(new Operand(splitInst[2].replace(",", "").replace("#", "").trim()));
					instruction.setSource2Orig(new Operand(splitInst[3].replace(",", "").replace("#", "").trim()));
				}
				else if("branch".equalsIgnoreCase(instruction.getInstructionType())) {
					instruction.setSource1(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
				}
				else if("halt".equalsIgnoreCase(instruction.getInstructionType())) {
					instruction.setSource1(null);
					instruction.setSource2(null);
				}
				else if("jump".equalsIgnoreCase(instruction.getInstructionType())) {
					instruction.setSource1(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setSource2(new Operand(splitInst[2].replace(",", "").replace("#", "").trim()));
				}
				else if("jal".equalsIgnoreCase(instruction.getInstructionType())) {
					instruction.setDestination(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setDestinationOrig(new Operand(splitInst[1].replace(",", "").replace("#", "").trim()));
					instruction.setSource1(new Operand(splitInst[2].replace(",", "").replace("#", "").trim()));
					instruction.setSource2(new Operand(splitInst[3].replace(",", "").replace("#", "").trim()));
				}
				//System.out.println("Instruction Objects=>");
				//System.out.println(instruction.toString());
				instruction.setInstructionNo("I"+instructionNo++);
				instructions.add(instruction);
				
			}
		}
		catch(FileNotFoundException e) {
			System.out.println("Invalid path has been passed...File not found on the given file location");
		}
	}

	public File getInputFileObj() {
		return inputFileObj;
	}

	public void setInputFileObj(File inputFileObj) {
		this.inputFileObj = inputFileObj;
	}

	public List<Instruction> getInstructions() {
		return instructions;
	}

	public void setInstructions(List<Instruction> instructions) {
		this.instructions = instructions;
	}
	

}
