import java.util.ArrayList;

public class MainMemory {

    ArrayList<Integer> dataMemory;
    ArrayList<Integer> instructionMemory;

    public MainMemory(){
        dataMemory = new ArrayList<>(1024);
        instructionMemory = new ArrayList<>(1024);
        for(int i = 0; i < 1024; i++) {
            dataMemory.add(0);
            //instructionMemory.add(0);
        }
    }


}
