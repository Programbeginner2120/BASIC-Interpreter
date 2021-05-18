public abstract class StatementNode extends Node{

    private StatementNode next; // private member referring to the next StatementNode in the AST

    public StatementNode getNext() {  // Used in Interpreter to access next
        return next;
    }

    public void setNext(StatementNode next) { // Used in interpreter to set next
        this.next = next;
    }
}
