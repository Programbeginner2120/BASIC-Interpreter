import java.util.List;

public class ReadNode extends StatementNode{

    private List<VariableNode> variableNodes;

    public ReadNode(List<VariableNode> variableNodes){
        this.variableNodes = variableNodes;
    }

    public List<VariableNode> getVariableNodes(){
        return variableNodes;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("ReadNode(");
        sb.append(variableNodes.toString());
        sb.append(")");
        return sb.toString();
    }
}
