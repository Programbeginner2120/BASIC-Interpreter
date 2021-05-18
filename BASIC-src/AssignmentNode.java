public class AssignmentNode extends StatementNode{

    private VariableNode varNode;
    private Node assignmentValue;

    public AssignmentNode(VariableNode varNode, Node assignmentValue){
        this.varNode = varNode;
        this.assignmentValue = assignmentValue;
    }

    public VariableNode getVarNode() {
        return varNode;
    }

    public Node getAssignmentVal() {
        return assignmentValue;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("AssignmentNode(");
        sb.append(varNode.toString());
        sb.append(" = ");
        sb.append(assignmentValue);
        sb.append(")");
        return sb.toString();
    }

}
