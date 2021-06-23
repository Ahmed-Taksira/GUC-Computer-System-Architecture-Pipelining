import java.io.File;
import java.io.IOException;
import java.util.*;

public class Architecture {

    RegisterFile registerFile;
    int pcRegister;
    MainMemory mainMemory;
    static int instructionCounter=1;

    public Architecture(String filename) throws Exception {
        registerFile = new RegisterFile();
        pcRegister = 0;
        mainMemory = new MainMemory(filename);
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

        Character type = switch (opcode) {
            case 0, 1, 9, 8 -> 'R';
            case 2, 3, 4, 5, 6, 10, 11 -> 'I';
            case 7 -> 'J';
            default -> null;
        };

        return new Instruction(pcRegister, opcode,r1,r2,r3,shamt,immediate,address,valueR1,valueR2,valueR3,type);

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
                    if(instruction.valueR1 != instruction.valueR2) {
                        pcRegister--;
                        pcRegister += 1 + instruction.immediate;
                    }
                    break;
                case 5: instruction.valueR1 = instruction.valueR2 & instruction.immediate; break;
                case 6: instruction.valueR1 = instruction.valueR2 | instruction.immediate; break;
                case 10:
                case 11: instruction.r1 = instruction.valueR2 + instruction.immediate - 1024; break;
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

    public void pipeline(){
        int n = mainMemory.numberOfInstructions;
        int x=0;
        int clk;
        int maxpipe=0;

        int decodearrival=0;
        int executearrival=0;
        int memoryarrival=0;
        int writebackarrival=0;
        int finisharrival=0;
        int skippingexecute=0;
        int decodearrivalplusone=0;
        int tempdecoding=0;

        Integer fetching=null;
        Instruction decoding=null;
        Instruction executing=null;
        Instruction memorying=null;
        Instruction writingbacking=null;

        int maxclocks=0;

        for(clk=1 ; clk<=(7+ ((n-1)*2))+maxclocks ; clk++){

            System.out.println("Clock Cycle = "+ clk);

            if(clk==finisharrival){
                maxpipe--;
                writingbacking=null;
            }

            if(clk==writebackarrival){
                writeBack(memorying);
                writingbacking=memorying;
                memorying=null;
                finisharrival=clk+1;
            }

            if(clk==memoryarrival){
                memory(executing);
                memorying=executing;
                executing=null;
                writebackarrival=clk+1;
            }

            if(clk==executearrival){

                if(decoding.opcode!=4 && decoding.opcode!=7)
                    execute(decoding);
                //execute(decoding);

                executing=decoding;
                decoding=null;
                if(executing.opcode==4 || executing.opcode==7)
                    skippingexecute=clk+1;
                memoryarrival=clk+2;
            }

            if(clk==decodearrivalplusone){
                decoding=decode(tempdecoding);
                tempdecoding=0;
            }

            if(clk==decodearrival){
                tempdecoding=fetching;
                decoding=decode(tempdecoding);
                fetching=null;
                executearrival=clk+2;
            }

            if(clk%2!=0 && pcRegister<mainMemory.instructionMemory.size() && maxpipe<=4){
                fetching=fetch();
                maxpipe++;
                decodearrival=clk+1;
                decodearrivalplusone=decodearrival+1;
            }

            System.out.println("PC  "+(pcRegister+1));
            System.out.println("Fetching = " + ((fetching==null)?"---":fetching));
            System.out.println("Decoding = " + ((decoding==null )?"---":decoding));
            System.out.println("Executing = " + ((executing==null)?"---":executing));
            System.out.println("Memory = " + ((memorying==null)?"---":memorying));
            System.out.println("Write Back = " + ((writingbacking==null)?"---":writingbacking));
            System.out.println("-------------------------------------------------------");

            if(clk==skippingexecute && executing!=null){

                if(executing.opcode==4 || executing.opcode==7){
                    int tempPc;
                    if(executing.opcode==4)
                        tempPc=pcRegister;
                    else
                         tempPc= pcRegister-2;

                    execute(executing);
                    System.out.println("IIIIIIDDDDD " +executing.id + "****PPPCCC   " +pcRegister);
                    if(executing.id<pcRegister-1)
                        x=n-pcRegister+1;
                    else
                        n+=pcRegister-executing.id-1;
                    if(tempPc!=pcRegister){
                    decoding=null;
                    fetching=null;
                    skippingexecute=0;
                    decodearrival=0;
                    executearrival=0;
                    decodearrivalplusone=0;
                    maxpipe-=2;
                    maxclocks=(7+ ((x-1)*2))-6;
                    if(executing.opcode==4)
                        pcRegister-=2;
                    }
                }
            }
        }

    }

    public static void main(String[] args) throws Exception {
        Architecture architecture = new Architecture("assemble.txt");
        architecture.pipeline();
        //Printings
    }

}
