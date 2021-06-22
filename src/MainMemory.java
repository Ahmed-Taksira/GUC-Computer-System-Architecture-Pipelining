import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class MainMemory {

    ArrayList<Integer> dataMemory;
    ArrayList<Integer> instructionMemory;
    int numberOfInstructions=0;

    public MainMemory(String filename) throws Exception {
        dataMemory = new ArrayList<>(1024);
        instructionMemory = new ArrayList<>(1024);

        for(int i = 0; i < 1024; i++) {
            dataMemory.add(0);
            //instructionMemory.add(0);
        }
        parser("src/"+ filename);
    }

    public void parser(String fileName) throws Exception {
        File file = new File(fileName);
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            String[] line = reader.nextLine().split(" ");
            int opcode;
            int r1;
            int r2;
            int r3;
            int immediate;
            int shamt;
            int address;
            switch(line[0]){
                case "ADD":
                    opcode = 0;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    r3 = Integer.parseInt(line[3].substring(1)) << 13;
                    instructionMemory.add(opcode | r1 | r2 | r3);
                    break;
                case "SUB":
                    opcode = 1 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    r3 = Integer.parseInt(line[3].substring(1)) << 13;
                    instructionMemory.add(opcode | r1 | r2 | r3);
                    break;
                case "MULI":
                    opcode = 2 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "ADDI":
                    opcode = 3 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "BNE":
                    opcode = 4 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "ANDI":
                    opcode = 5 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "ORI":
                    opcode = 6 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "J":
                    opcode = 7 << 28;
                    address = Integer.parseInt(line[1]);
                    instructionMemory.add(opcode | address);
                    break;
                case "SLL":
                    opcode = 8 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    shamt = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | shamt);
                    break;
                case "SRL":
                    opcode = 9 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    shamt = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | shamt);
                    break;
                case "LW":
                    opcode = 10 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "SW":
                    opcode = 11 << 28;
                    //System.out.println(opcode);
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    instructionMemory.add(opcode | r1 | r2 | immediate);
                    //System.out.println(opcode | r1 | r2 | immediate);
                    break;
                default: throw new Exception("Incorrect command!");
            }
            numberOfInstructions++;
        }
    }


}
