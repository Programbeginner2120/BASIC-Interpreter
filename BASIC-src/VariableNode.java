public class VariableNode extends Node{

    private String variableName;

    public VariableNode(String variableName){
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("VariableNode(");
        sb.append(variableName);
        sb.append(")");
        return sb.toString();
    }

}
