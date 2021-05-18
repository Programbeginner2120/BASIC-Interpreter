public class ForNode extends StatementNode{

    private AssignmentNode startState;
    private IntegerNode endValue;
    private IntegerNode stepValue;
    private Node nodeReference;

    public ForNode(AssignmentNode startState, IntegerNode endValue, IntegerNode stepValue){
        this.startState = startState;
        this.endValue = endValue;
        this.stepValue = stepValue;
    }

    public ForNode(AssignmentNode startState, IntegerNode endValue){
        this.startState = startState;
        this.endValue = endValue;
        this.stepValue = new IntegerNode(1);
    }

    public AssignmentNode getStartState() {
        return startState;
    }

    public IntegerNode getEndValue() {
        return endValue;
    }

    public IntegerNode getStepValue() {
        return stepValue;
    }

    public Node getNodeReference() { // Used in Interpreter to access nodeReference
        return nodeReference;
    }

    public void setNodeReference(Node nodeReference) { // Used in Interpreter to set nodeReference
        this.nodeReference = nodeReference;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ForNode(");
        sb.append(startState);
        sb.append(" ");
        sb.append(TokenEnum.TO.toString());
        sb.append(" ");
        sb.append(endValue);
        sb.append(" ");
        sb.append(TokenEnum.STEP.toString());
        sb.append(" ");
        sb.append(stepValue);
        sb.append(")");
        return sb.toString();
    }
}