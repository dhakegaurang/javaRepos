package main;

import java.util.*;

import process.InstructionGetter;
import process.PipelineProcessor;

public class MainSimulator {

	public static void main(String[] args) throws Exception{// main class of pipeline to accept inputs from user
		
		String inputFileName = "inputFile.txt";//args[0];
		Scanner scr = null;
		scr = new Scanner(System.in);
		String choiceInput = "N";
		if(inputFileName != null && !"".equals(inputFileName)) {
			//MainProcessor processor = new MainProcessor();
			PipelineProcessor processor = new PipelineProcessor();
			InstructionGetter instructionGetter = new InstructionGetter(inputFileName);
			instructionGetter.getInstuction();
			processor.setInstructionSet(instructionGetter.getInstructions());
			processor.initialization();
			
			do {
				System.out.println("Enter number of cycles to execute the pipeline");
				int cycles = scr.nextInt();
				processor.simulate(cycles);
				System.out.println("\nDo you want to continue (Y|N)");
				scr = new Scanner(System.in);
				choiceInput = scr.nextLine();
			}while(!"N".equals(choiceInput));
			
			System.out.println("\n\nSimulation terminated by user...");
		}
		else {
			System.out.println("Invalid file or path...kindly check again");
		}

	}

}
