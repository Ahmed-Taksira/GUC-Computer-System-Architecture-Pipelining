import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Architecture {

    RegisterFile registerFile;
    int pcRegister;
    MainMemory mainMemory;
    int clockCycle;

    public Architecture(){
        registerFile = new RegisterFile();
        pcRegister = 0;
        mainMemory = new MainMemory();
        clockCycle = 1;
    }

    public int fetch() {

        int instruction = mainMemory.instructionMemory.get(pcRegister);
        pcRegister++;
        return instruction;

    }

    public Instruction decode(int instructionValue) {


        int opcode = (instructionValue) >> 28;
        if(opcode < 0){
            opcode = opcode & 0b00000000000000000000000000001111;
        }
        int r1 = (instructionValue & 0b00001111100000000000000000000000) >> 23;
        int r2 = (instructionValue & 0b00000000011111000000000000000000) >> 18;
        int r3 = (instructionValue & 0b00000000000000111110000000000000) >> 13;
        int shamt = (instructionValue & 0b00000000000000000001111111111111);
        int immediate = (instructionValue & 0b00000000000000111111111111111111);
        int address = (instructionValue & 0b00001111111111111111111111111111);

        int valueR1 = registerFile.registers.get(r1).value;
        int valueR2 = registerFile.registers.get(r2).value;
        int valueR3 = registerFile.registers.get(r3).value;

        //System.out.println(opcode);
        Character type = switch (opcode) {
            case 0, 1, 9, 8 -> 'R';
            case 2, 3, 4, 5, 6, 10, 11 -> 'I';
            case 7 -> 'J';
            default -> null;
        };

        return new Instruction(opcode,r1,r2,r3,shamt,immediate,address,valueR1,valueR2,valueR3,type);

    }

    public void execute(Instruction instruction){

        if(instruction.type.equals('R')){
          switch (instruction.opcode){
              case 0: instruction.valueR1 = instruction.valueR2 + instruction.valueR3; break;
              case 1: instruction.valueR1 = instruction.valueR2 - instruction.valueR3; break;
              case 8: instruction.valueR1 = instruction.valueR2 << instruction.shamt; break;
              case 9: instruction.valueR1 = instruction.valueR2 >> instruction.shamt; break;
              default: break;
          }
        }
        else if(instruction.type.equals('I')){
            switch (instruction.opcode){
                case 2: instruction.valueR1 = instruction.valueR2 * instruction.immediate; break;
                case 3: instruction.valueR1 = instruction.valueR2 + instruction.immediate; break;
                case 4:
                    if(instruction.valueR1 != instruction.valueR2)
                        pcRegister += 1 + instruction.immediate;
                    break;
                case 5: instruction.valueR1 = instruction.valueR2 & instruction.immediate; break;
                case 6: instruction.valueR1 = instruction.valueR2 | instruction.immediate; break;
                case 10:
                //check if value or register index
                case 11: instruction.r1 = instruction.r2 + instruction.immediate - 1024; break;
                default: break;
            }
        }
        else {
            pcRegister = pcRegister & 0b11110000000000000000000000000000;
            pcRegister = pcRegister | instruction.address;
        }

    }

    public void memory(Instruction instruction){
        if(instruction.opcode == 10){
            instruction.valueR1 = mainMemory.dataMemory.get(instruction.r1);
        }
        else if(instruction.opcode == 11){
            mainMemory.dataMemory.set(instruction.r1, instruction.valueR1);
        }
    }

    public void writeBack(Instruction instruction){
        if(instruction.opcode != 4 && instruction.opcode != 7 && instruction.opcode != 11)
            registerFile.registers.get(instruction.r1).value = instruction.valueR1;
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
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | r3);
                    break;
                case "SUB":
                    opcode = 1 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    r3 = Integer.parseInt(line[3].substring(1)) << 13;
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | r3);
                    break;
                case "MULI":
                    opcode = 2 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "ADDI":
                    opcode = 3 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "BNE":
                    opcode = 4 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "ANDI":
                    opcode = 5 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "ORI":
                    opcode = 6 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "J":
                    opcode = 7 << 28;
                    address = Integer.parseInt(line[1]);
                    mainMemory.instructionMemory.add(opcode | address);
                    break;
                case "SLL":
                    opcode = 8 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    shamt = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | shamt);
                    break;
                case "SRL":
                    opcode = 9 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    shamt = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | shamt);
                    break;
                case "LW":
                    opcode = 10 << 28;
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | immediate);
                    break;
                case "SW":
                    opcode = 11 << 28;
                    //System.out.println(opcode);
                    r1 = Integer.parseInt(line[1].substring(1)) << 23;
                    r2 = Integer.parseInt(line[2].substring(1)) << 18;
                    immediate = Integer.parseInt(line[3]);
                    mainMemory.instructionMemory.add(opcode | r1 | r2 | immediate);
                    //System.out.println(opcode | r1 | r2 | immediate);
                    break;
                default: throw new Exception("Incorrect command!");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Architecture architecture = new Architecture();
        architecture.parser("src/assemble.txt");

        for (int i = 0; i < 6; i++){
            int instructionValue = architecture.fetch();
            Instruction instruction = architecture.decode(instructionValue);
            architecture.execute(instruction);
            architecture.memory(instruction);
            architecture.writeBack(instruction);

        }
        for (int i = 0; i < 5; i++)
            System.out.println(architecture.registerFile.registers.get(i).value);
        System.out.println("--------------------");
        for (int i = 0; i< 10; i++)
            System.out.println(architecture.mainMemory.dataMemory.get(i));
    }

}
