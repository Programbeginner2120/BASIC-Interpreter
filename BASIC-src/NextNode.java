public class NextNode extends StatementNode{

    private VariableNode variable;
    private Node nodeReference;

    public NextNode(VariableNode variable){
        this.variable = variable;
    }

    public VariableNode getVariable() {
        return variable;
    }

    public Node getNodeReference(){ // Used in Interpreter to access nodeReference
        return nodeReference;
    }

    public void setNodeReference(Node nodeReference) { // Used in Interpreter to set nodeReference
        this.nodeReference = nodeReference;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NextNode(");
        sb.append(variable);
        sb.append(")");
        return sb.toString();
    }
}
