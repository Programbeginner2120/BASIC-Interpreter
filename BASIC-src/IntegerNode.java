public class IntegerNode extends Node{

    private int integerValue;

    public IntegerNode(int integerValue){
        this.integerValue = integerValue;
    }

    public int getIntegerValue(){
        return integerValue;
    }

    public String toString(){
        return Integer.toString(integerValue);
    }
}
