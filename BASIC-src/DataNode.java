import java.util.List;

public class DataNode extends StatementNode{

    private List<Node> dataNodeList;

    public DataNode(List<Node> dataNodeList){
        this.dataNodeList = dataNodeList;
    }

    public List<Node> getDataNodeList(){
        return dataNodeList;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("DataNode(");
        sb.append(dataNodeList.toString());
        sb.append(")");
        return sb.toString();
    }

}
