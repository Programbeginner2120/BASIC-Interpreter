import java.util.List;

public class InputNode extends StatementNode{

    private List<Node> inputListNodes;

    public InputNode(List<Node> inputListNodes){
        this.inputListNodes = inputListNodes;
    }

    public List<Node> getVariableNodes(){
        return inputListNodes;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("InputNode(");
        sb.append(inputListNodes.toString());
        sb.append(")");
        return sb.toString();
    }
}
