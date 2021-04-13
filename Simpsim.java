simport java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Simpsim {
    public static CPU cpu;
    public static boolean flag = false;
    public static void main(String[] args) {

        if(args.length == 0)
            System.out.println("USAGE: java Simpsim.java [options] <object file> [integer in value]");
        else
            parseArgs(args); 

    }

    private static void parseArgs(String[] args) {
        if(args[0].equals("-s")) {
            flag=true;
            if(args.length == 3)
                cpu = new CPU(args[1], args[2] );
            else
                cpu = new CPU(args[1] );

        }
        else {
            if(args.length==2)
                cpu = new CPU(args[0],args[1] );
            else
                cpu = new CPU(args[0]);
        }
      }

}

class CPU {
    int PC = 0, maxPC; //program counter
    protected Memory mem;
    protected Stack stack = new Stack();
    String portIn;
    public CPU(String mem) {
        this.mem = new Memory(mem);
        maxPC = this.mem.getMemorySize(); //FIX LATER
        startCycle();
    }
    public CPU(String mem, String input) {
        this.mem = new Memory(mem);
        maxPC=this.mem.getMemorySize();
        this.portIn = Integer.toHexString(Integer.parseInt(input) );
        startCycle();
    }

    private void startCycle() {
        boolean live;
        do {
            String opcode = fetch_and_decode(PC++).trim();
            live = executeInstruction(opcode);
        } while(PC < maxPC && live);
        if( Simpsim.flag ){
            System.out.println("Maximum stack depth "+stack.getDepth() );
        }
        stopCPU();
    }
    private String fetch_and_decode(int i) {
        //grabs hex code from memory
        String mem = this.mem.getDataAt(i);
        if(mem == null) {//temp fix
            return "0F00";
        }
        return mem;
    }

    private boolean executeInstruction(String opcode) {
        int opValue = Integer.parseInt(opcode, 16); //decimal value of opcode
        char[] opCodeInChar = opcode.toCharArray();
        //last three is in hex
        //last three parsed in decimal 
        String last_three = "0"+String.valueOf(opCodeInChar[1])+String.valueOf(opCodeInChar[2])+String.valueOf(opCodeInChar[3]);
        int last_three_parsed = Integer.parseInt(last_three, 16);
        if(opcode.equals("0000") )
            return true;
        else if(opcode.equals("0F00") ) {
            return false;
        }
        else if(opValue >= 4096 && opValue < 8192 ) { 

            stack.push(last_three);  }//I -> top
        else if(opValue >= 8192 && opValue < 12288) {
            //System.out.println("mem[a] is "+mem.getDataAt(last_three_parsed) );
            stack.push(mem.getDataAt(last_three_parsed) );
        } //mem[A]->top
        else if (opValue>= 12288 &&  opValue < 16384) {
            mem.setDataAt(last_three_parsed, stack.pop() );
        }//top->mem[A]
        else if (opValue>= 16384 &&  opValue < 20480) {
            PC = last_three_parsed;
        } // A -> PC
        else if (opValue>= 20480 &&  opValue < 24576) {
            if(stack.pop().equals("0000") )
                PC=last_three_parsed;
        } //A -> PC if top ==0
        else if (opValue>= 24576 &&  opValue < 28672) {
            if( !stack.pop().equals("0000") )
                PC=last_three_parsed;
        } //A -> PC if top != 0
        else if (opValue>= 53248 &&  opValue < 57344) {
            stack.push(portIn);
            //System.out.println("is"+portIn );
             } //in[port p] -> top
        else if (opValue>= 57344 &&  opValue < 61440) {
            System.out.println( Integer.parseInt(stack.pop(), 16) );
        }//top -> out[port P]
        else if(opValue == 61440) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            stack.push( formatHex(Integer.toHexString(next + top)));
        } //next + top -> top F000
        else if(opValue == 61441) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            stack.push( formatHex(Integer.toHexString(next - top)));
        } //next - top -> top F001
        else if(opValue == 61442) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            stack.push( formatHex(Integer.toHexString(next * top)));
        } //next * top -> top F002
        else if(opValue == 61443) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            stack.push( formatHex(Integer.toHexString(next % top)));
        } //next % top -> top F003
        else if(opValue == 61444) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            //System.out.println("--------"+formatHex(next<<top) );
            stack.push(formatHex(Integer.toHexString(next<<top)) );
        } //next << top -> top F004
        else if(opValue == 61445) {
            int top = Integer.parseInt(stack.pop(), 16);
            String temp = stack.pop();
            int next = Integer.parseInt(temp, 16);
            stack.push(formatHex(Integer.toHexString(next>>top) ) );
        } //next >> top -> top F005
        else if(opValue == 61450) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            stack.push(formatHex(Integer.toHexString(next&top)) );
        } //next & top -> top F00A
        else if(opValue == 61451) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            //System.out.println("--------"+formatHex(next|top) );
            stack.push(formatHex(Integer.toHexString(next|top)) );
        } //next | top -> top F00B
        else if(opValue == 61452) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            //System.out.println("--------"+formatHex(next^top) );
            stack.push(formatHex(Integer.toHexString(next^top)) );
        } // next ^ top -> top F00C
        else if(opValue == 61456) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            if( next == top )
                stack.push("0001");
            else
                stack.push("0000");
        } //next == top -> top F010
        else if(opValue == 61457) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            if( next != top )
                stack.push("0001");
            else
                stack.push("0000");
        } //next != top ->top F011
        else if(opValue == 61458) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            if( next >= top )
                stack.push("0001");
            else
                stack.push("0000");
        } //next >= top -> top F012
        else if(opValue == 61459) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            if( next <= top )
                stack.push("0001");
            else
                stack.push("0000");
        } //next <= top -> top F013
        else if(opValue == 61460) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            if( next > top )
                stack.push("0001");
            else
                stack.push("0000");
        } //next > top -> top F014
        else if(opValue == 61461) {
            int top = Integer.parseInt(stack.pop(), 16);
            int next = Integer.parseInt(stack.pop(), 16);
            if( next < top )
                stack.push("0001");
            else
                stack.push("0000");
        } //next < top -> top F015
        else if(opValue == 61446) {
            int top = Integer.parseInt(stack.pop(), 16);
            stack.push( formatHex(-1 * top) );
        } //-top -> top (unary minus) F006
        else if(opValue == 61453) {
            int top=Integer.parseInt(stack.pop(), 16);
            stack.push(formatHex(~top) );
        } //~top -> top (bitwise inversion) F00D
        return true;
    }
    private String formatHex(String hex) {
        char[] hexChar = hex.toCharArray();
        if(hexChar.length == 1) {
            char[] temp = {'0','0','0',hexChar[0]};
            return String.valueOf(temp);
        }
        else if(hexChar.length ==2) {
            char[] temp = {'0','0',hexChar[0],hexChar[1] };
            return String.valueOf(temp);
        }
        else if(hexChar.length == 3) {
            char[] temp = {'0',hexChar[0],hexChar[1]};
            return String.valueOf(temp);
        }
        return hex;
    }
    private String formatHex(int decimal) {
        //takes in an int and returns last 4 binary 
        //horrible but works
        char[] bin = Integer.toBinaryString(decimal).toCharArray();
        //System.out.println("bin is "+String.valueOf(bin) +" length:"+bin.length);
        while(bin.length< 4) {
            for(int i =4-bin.length; i<4;i++){
                bin = ("0"+ String.valueOf(bin)).toCharArray();
            }
        }
        //System.out.println("bin is "+String.valueOf(bin));
        int len = bin.length -1;
        char[] temp = {bin[len-3], bin[len-2], bin[len-1], bin[len] };
        return formatHex(Integer.toHexString(Integer.parseInt(String.valueOf(temp), 2 )));
    }
    private void stopCPU() {
        System.exit(0);
    }
}

class Memory {
    static String[] memory = new String[4096];
    File file;
    public Memory() {
        this.file = new File("src/test.out");
    }
    public Memory(String filename) {
        this.file = new File(filename);
        setUp();
    }
    private void setUp() {
        try {
            Scanner myReader = new Scanner(this.file);
            int counter=0;
            while( myReader.hasNextLine() ) {
                String st = myReader.nextLine();
                if(st.equals("v2.0 raw") )
                    continue;
                this.memoryOp(st,counter++);
            }
            myReader.close();
        } catch(FileNotFoundException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
        /* for(String s: memory)
            System.out.println(s); */
    }

    private void memoryOp(String st,int i) {
        memory[i] = st;
    }
    public int getMemorySize() { return memory.length; }
    public String getDataAt(int index) {return memory[index]; }
    public void setDataAt(int index, String data) { memory[index] = data;}
    //just for test fix this
    public String sendToCPU(String st) {
        return st;
    }

}
class Stack {

    private static class Node {
        private String data;
        private Node next;
        private Node(String data) {
            this.data = data;
        }
    }
    private int depth =0;
    private Node head;
    private int size = 0;

    public boolean isEmpty() {
        return head == null;
    }

    public String peek() {
        if (head != null) {
            return head.data;
        }
        return "";
    }

    public void push(String data) {
        Node node = new Node(data);
        node.next = head;
        head = node;
        size++;
        if(size>depth)
            depth = size;
    }
    public int getDepth() {
        return depth;
    }
    public String pop() {
        if(head != null) {
            String temp = head.data;
            head = head.next;
            size--;
            return temp;
        }
        return null;
    }

    public int size() {
        return size;
    }

}
