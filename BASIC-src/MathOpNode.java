public class MathOpNode extends Node{

    public enum OperationEnum{
        PLUS, MINUS, TIMES, DIVIDE
    }

    private OperationEnum operationType;
    private final char operationCharacter;
    private Node leftNode;
    private Node rightNode;

    public MathOpNode(OperationEnum operationType, char operationCharacter, Node leftNode, Node rightNode){
        this.operationType = operationType;
        this.operationCharacter = operationCharacter;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public OperationEnum getOperationType(){
        return operationType;
    }

    public Node getLeftNode(){
        return leftNode;
    }

    public Node getRightNode(){
        return rightNode;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("MathNode(");
        sb.append(this.operationCharacter);
        sb.append(",");
        sb.append(this.leftNode);
        sb.append(",");
        sb.append(this.rightNode);
        sb.append(")");
        return sb.toString();
    }
}
