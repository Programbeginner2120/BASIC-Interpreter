public class GosubNode extends StatementNode{

    private VariableNode identifier;

    public GosubNode(VariableNode identifier){
        this.identifier = identifier;
    }

    public VariableNode getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GoSubNode(");
        sb.append(identifier);
        sb.append(")");
        return sb.toString();
    }
}
