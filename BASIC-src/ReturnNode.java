public class ReturnNode extends StatementNode{

    // I'm honestly using the default constructor here since there are no members and a ReturnNode is just "RETURN"
    // on a line

    @Override
    public String toString() {
        return "ReturnNode(RETURN)";
    }
}
