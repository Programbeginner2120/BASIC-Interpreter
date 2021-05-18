public class BooleanOperationNode extends Node{

    private Node leftExpressionNode;
    private Node rightExpressionNode;
    TokenEnum operator;

    public BooleanOperationNode(Node leftExpressionNode, Node rightExpressionNode, TokenEnum operator){
        this.leftExpressionNode = leftExpressionNode;
        this.rightExpressionNode = rightExpressionNode;
        this.operator = operator;
    }

    public Node getLeftExpressionNode(){
        return leftExpressionNode;
    }

    public Node getRightExpressionNode() {
        return rightExpressionNode;
    }

    public TokenEnum getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BooleanOperationNode(");
        sb.append(leftExpressionNode);
        sb.append(", ");
        sb.append(rightExpressionNode);
        sb.append(", ");
        sb.append(operator);
        sb.append(")");
        return sb.toString();

    }
}
