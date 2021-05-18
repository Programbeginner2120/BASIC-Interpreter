import java.util.List;

public class PrintNode extends StatementNode{

    private List<Node> nodesToPrint;

    public PrintNode(List<Node> nodesToPrint){
        this.nodesToPrint = nodesToPrint;
    }

    public List<Node> getNodesToPrint(){
        return nodesToPrint;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("PrintNode(");
        sb.append(nodesToPrint.toString());
        sb.append(")");
        return sb.toString();
    }

}
