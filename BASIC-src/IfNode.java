public class IfNode extends StatementNode{

    private BooleanOperationNode booleanExpression;
    private VariableNode identifier;

    public IfNode(BooleanOperationNode booleanExpression, VariableNode identifier){
        this.booleanExpression = booleanExpression;
        this.identifier = identifier;
    }

    public BooleanOperationNode getBooleanExpression() {
        return booleanExpression;
    }

    public VariableNode getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("IfNode(");
        sb.append(booleanExpression);
        sb.append(", ");
        sb.append(identifier);
        sb.append(")");
        return sb.toString();
    }

}
