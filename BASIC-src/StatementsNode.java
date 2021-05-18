import java.util.List;

public class StatementsNode{

    private List<StatementNode> statementList;

    public StatementsNode(List<StatementNode> statementList){
        this.statementList = statementList;
    }

    public List<StatementNode> getStatementList(){
        return statementList;
    }

    public String toString(){
        return statementList.toString();
    }

}
