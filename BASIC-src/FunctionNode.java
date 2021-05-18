import java.util.List;

public class FunctionNode extends Node{

    private String functionName;
    private List<Node> parameterList;

    public FunctionNode(String functionName, List<Node> parameterList){
        this.functionName = functionName;
        this.parameterList = parameterList;
    }

    public FunctionNode(String functionName){
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Node> getParameterList() {
        return parameterList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FunctionNode(");
        sb.append(functionName);
        sb.append("(");
        if (parameterList != null)
            sb.append(parameterList);
        sb.append(")");
        sb.append(")");
        return sb.toString();
    }

}
