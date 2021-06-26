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

        String statement = "";
        switch (opcode){
            case 0: statement = "Instruction: ADD R" + r1 + " R" + r2 + " R" + r3; break;
            case 1: statement = "Instruction: SUB R" + r1 + " R" + r2 + " R" + r3; break;
            case 2: statement = "Instruction: MULI R" + r1 + " R" + r2 + " " + immediate; break;
            case 3: statement = "Instruction: ADDI R" + r1 + " R" + r2 + " " + immediate; break;
            case 4: statement = "Instruction: BNE R" + r1 + " R" + r2 + " " + immediate; break;
            case 5: statement = "Instruction: ANDI R" + r1 + " R" + r2 + " " + immediate; break;
            case 6: statement = "Instruction: ORI R" + r1 + " R" + r2 + " " + immediate; break;
            case 7: statement = "Instruction: J " + address; break;
            case 8: statement = "Instruction: SLL R" + r1 + " R" + r2 + " " + shamt; break;
            case 9: statement = "Instruction: SRL R" + r1 + " R" + r2 + " " + shamt; break;
            case 10: statement = "Instruction: LW R" + r1 + " R" + r2 + " " + immediate; break;
            case 11: statement = "Instruction: SW R" + r1 + " R" + r2 + " " + immediate; break;
            default: break;
        }

        return new Instruction(pcRegister, opcode,r1,r2,r3,shamt,immediate,address,valueR1,valueR2,valueR3,type,statement);

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
                        pcRegister = instruction.id-1+ 1 +instruction.immediate;
                    }
                    break;
                case 5: instruction.valueR1 = instruction.valueR2 & instruction.immediate; break;
                case 6: instruction.valueR1 = instruction.valueR2 | instruction.immediate; break;
                case 10: break;
                case 11: instruction.r1 = instruction.valueR2 + instruction.immediate - 1024; break;
                default: break;
            }
        }
        else {
            pcRegister = pcRegister & 0b11110000000000000000000000000000;
            pcRegister = pcRegister | instruction.address;
        }
        if(instruction.r1==0)
            instruction.valueR1=0;

    }

    public void memory(Instruction instruction){
        if(instruction.opcode == 10){
            instruction.valueR1 = mainMemory.dataMemory.get(instruction.r1);
        }
        else if(instruction.opcode == 11){
            mainMemory.dataMemory.set(instruction.r1, instruction.valueR1);
            System.out.println("Data Memory Block " + instruction.r1 + ": " + mainMemory.dataMemory.get(instruction.r1));
        }
    }

    public void writeBack(Instruction instruction){
        if(instruction.opcode != 4 && instruction.opcode != 7 && instruction.opcode != 11){
            registerFile.registers.get(instruction.r1).value = instruction.valueR1;
            System.out.println("Register " + instruction.r1 + ": " + registerFile.registers.get(instruction.r1).value);
        }
    }

    public void pipeline(){
        int n = mainMemory.numberOfInstructions;
        int clk;
        int maxpipe=0;

        int decodearrival=0;
        int executearrival=0;
        int memoryarrival=0;
        int writebackarrival=0;
        int finisharrival=0;
        int decodearrivalplusone=0;
        int executearrivalplusone=0;
        int tempdecoding=0;
        int jumpingPC = 0;
        int oldPC = 0;
        boolean weJumping = false;
        boolean dropOne = false;

        // pointers
        Integer fetching=null;
        Instruction decoding=null;
        Instruction executing=null;
        Instruction memorying=null;
        Instruction writingbacking=null;



        for(clk=1 ; clk<=(7+ ((n-1)*2)); clk++){

            System.out.println("Clock Cycle = "+ clk);

            // Finishing the instruction
            if(clk==finisharrival){
                maxpipe--;
                writingbacking=null;
            }
            // Writing_Back Stage
            if(clk==writebackarrival){
                writeBack(memorying);
                writingbacking=memorying;
                memorying=null;
                finisharrival=clk+1;
            }
            // Memory Stage
            if(clk==memoryarrival){
                memory(executing);
                memorying=executing;
                executing=null;
                writebackarrival=clk+1;
            }

            // Executing in second clk
            if (clk==executearrivalplusone){

                if(executing.opcode==4 || executing.opcode==7){
                    weJumping = true;
                    oldPC=pcRegister;
                }
                execute(executing);

                if (oldPC == pcRegister && executing.opcode==7)
                    dropOne = true;
                if (oldPC == pcRegister && executing.opcode==4){
                    if (executing.valueR1 != executing.valueR2)
                        dropOne= true;
                }
                if (oldPC==pcRegister+1 && weJumping)
                    pcRegister=oldPC;

            }

            // First executing clk
            if(clk==executearrival){
                executing=decoding;
                decoding=null;
                memoryarrival=clk+2;
                executearrivalplusone= executearrival+1;
            }

            // Second decoding clk
            if(clk==decodearrivalplusone){
                decoding=decode(tempdecoding);
                tempdecoding=0;
            }

            // First decoding clk
            if(clk==decodearrival){
                tempdecoding=fetching;
                decoding=decode(tempdecoding);
                if(decoding.opcode==4 || decoding.opcode==7)
                    jumpingPC = pcRegister-1;
                fetching=null;
                executearrival=clk+2;
                decodearrivalplusone=decodearrival+1;
            }

            // Fetching Stage
            if(clk%2!=0 && pcRegister<mainMemory.instructionMemory.size() && maxpipe<=4){
                fetching=fetch();
                maxpipe++;
                decodearrival=clk+1;

            }
            // To end of we finished the instructions :)
            if (fetching==null&&decoding==null&&executing==null&&memorying==null&&writingbacking==null){
                n=clk;
                break;
            }

            // printing :)
            System.out.println("PC  " + (pcRegister));
            System.out.println("Fetching = " + ((fetching==null)?"---":fetching));
            System.out.println("Decoding = " + ((decoding==null)?"---":decoding));
            System.out.println("Executing = " + ((executing==null)?"---":executing));
            System.out.println("Memory = " + ((memorying==null)?"---":memorying));
            System.out.println("Write Back = " + ((writingbacking==null)?"---":writingbacking));
            System.out.println("-------------------------------------------------------");

            // to null the instructions we are dropping
            if(weJumping && executing!=null){
                weJumping=false;
                if(jumpingPC>pcRegister)
                    n = n + n;
                if(oldPC!=pcRegister-1 && oldPC!=pcRegister){
                    decoding=null;
                    fetching=null;
                    decodearrival=0;
                    decodearrivalplusone=0;
                    executearrival=0;
                    executearrivalplusone=0;
                    maxpipe-=2;
                    jumpingPC=0;
                    oldPC=0;
                    pcRegister--;
                }
                if(dropOne){
                    decoding=null;
                    decodearrivalplusone=0;
                    executearrival=0;
                    executearrivalplusone=0;
                    maxpipe-=1;
                    jumpingPC=0;
                    oldPC=0;
                    dropOne= false;
                }
            }//end of nulling

        }//end of looping

        for(int i = 0; i < registerFile.registers.size(); i++){
            System.out.println("Register: " + registerFile.registers.get(i).name + "  " + " Value: " + registerFile.registers.get(i).value);
        }

        for(int i = 0; i < mainMemory.dataMemory.size(); i++){
            System.out.println("Data Memory Block " + i + ": " + mainMemory.dataMemory.get(i));
        }
        for(int i = 0; i < mainMemory.instructionMemory.size(); i++){
            System.out.println("Instruction Memory Block " + i + ": " + mainMemory.instructionMemory.get(i));
        }
    }// end of method

    public static void main(String[] args) throws Exception {
        Architecture architecture = new Architecture("assemble.txt");
        architecture.pipeline();
        //Printings
    }

}
