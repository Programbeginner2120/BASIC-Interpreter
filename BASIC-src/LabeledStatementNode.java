public class LabeledStatementNode extends StatementNode{

    private String label;
    private StatementNode labeledStatement;

    public LabeledStatementNode(String label, StatementNode labeledStatement){
        this.label = label;
        this.labeledStatement = labeledStatement;
    }

    public String getLabel() {
        return label;
    }

    public StatementNode getLabeledStatement(){
        return labeledStatement;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LabeledStatementNode(");
        sb.append(this.label);
        sb.append(": ");
        sb.append(labeledStatement);
        sb.append(")");
        return sb.toString();
    }
}
