public class FloatNode extends Node{

    private float floatValue;

    public FloatNode(float floatValue){
        this.floatValue = floatValue;
    }

    public float getFloatValue(){
        return floatValue;
    }

    public String toString(){
        return Float.toString(floatValue);
    }
}
