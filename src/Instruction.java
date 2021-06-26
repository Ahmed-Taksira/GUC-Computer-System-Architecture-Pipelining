public class Instruction {

    int id;
    int opcode;
    int r1;
    int r2;
    int r3;
    int shamt;
    int immediate;
    int address;
    int valueR1;
    int valueR3;
    int valueR2;
    Character type;
    String statement;

    public Instruction(int id,int opcode, int r1, int r2, int r3, int shamt, int immediate, int address, int valueR1, int valueR2, int valueR3, Character type, String statement) {
        this.id=id;
        this.opcode = opcode;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        this.shamt = shamt;
        this.immediate = immediate;
        this.address = address;
        this.valueR1 = valueR1;
        this.valueR3 = valueR3;
        this.valueR2 = valueR2;
        this.type = type;
        this.statement = statement;
    }

    @Override
    public String toString() { return statement + " --- " + "Instruction "+ id; }
}
