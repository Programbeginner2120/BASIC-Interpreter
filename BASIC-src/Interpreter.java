import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Interpreter {

    private StatementsNode abstractSyntaxTree;
    private HashMap<String, Integer> intMap;
    private HashMap<String, Float> floatMap;
    private HashMap<String, String> stringMap;
    private HashMap<String, Node> labeledStatementMap;
    private ArrayList dataContents; // will hold data of type String, float and int

    public Interpreter(StatementsNode ast) {
        this.abstractSyntaxTree = ast;
        this.intMap = new HashMap<>();
        this.floatMap = new HashMap<>();
        this.stringMap = new HashMap<>();
        this.labeledStatementMap = new HashMap<>();
        this.dataContents = new ArrayList();
    }

    public StatementsNode getAbstractSyntaxTree() {
        return abstractSyntaxTree;
    }

    public HashMap<String, Integer> getIntMap() {
        return intMap;
    }

    public HashMap<String, Float> getFloatMap() {
        return floatMap;
    }

    public HashMap<String, String> getStringMap() {
        return stringMap;
    }

    public HashMap<String, Node> getLabeledStatementMap() {
        return labeledStatementMap;
    }

    public ArrayList getDataContents(){
        return this.dataContents;
    }

    // initialize calls all of the optimization methods for assignment 8
    public void initialize(){
        this.findLabeledStatements();
        this.findForStatements();
        this.findDataStatements();
        this.setNextReferences();
    }

    // Interpret method used to cycle over StatementNodes in StatementsNode and interpret code line by line
    public void interpret(StatementNode currentStatement){
        if (currentStatement instanceof ReadNode)
            this.processReadNode((ReadNode) currentStatement);
        if (currentStatement instanceof AssignmentNode)
            this.processAssignmentNode((AssignmentNode) currentStatement);
        if (currentStatement instanceof InputNode)
            this.processInputNode((InputNode) currentStatement);
        if (currentStatement instanceof PrintNode)
            this.processPrintNode((PrintNode) currentStatement);
        if (currentStatement instanceof IfNode)
            this.processIfStatement((IfNode) currentStatement);
        if (currentStatement instanceof ForNode)
            this.processForStatement((ForNode) currentStatement);
        if (currentStatement instanceof NextNode) // when encountered set Next reference to ForNode reference
            currentStatement.setNext((StatementNode) ((NextNode) currentStatement).getNodeReference());
        if (currentStatement instanceof GosubNode)
            this.processGoSubStatement((GosubNode) currentStatement);
    }

    // processes GOSUB statements and processes matching RETURN statements
    private void processGoSubStatement(GosubNode goSubStatement){
        Stack<Node> goSubStack = new Stack<>();
        goSubStack.push(goSubStatement.getNext());
        String label = goSubStatement.getIdentifier().getVariableName();
        Node associatedNode = labeledStatementMap.get(label);
        if (associatedNode != null)
            goSubStatement.setNext((StatementNode) associatedNode);
        else
            throw new InvalidSymbolException("LABEL REFERENCED BY GOSUB UNDEFINED");
        StatementNode currentStatement = goSubStatement.getNext();
        while (!(currentStatement instanceof ReturnNode)){
            if (currentStatement.equals(goSubStatement))
                throw new InvalidSymbolException("FOUND REFERENCE TO GOSUB WITH NO RETURN STATEMENT");
            this.interpret(currentStatement);
            currentStatement = currentStatement.getNext();
        }
        goSubStatement.setNext((StatementNode) goSubStack.pop());
    }

    // Process for statements modifying the ForNode's and NextNode's general and special references accordingly
    private void processForStatement(ForNode forLoop){
        String varName = forLoop.getStartState().getVarNode().getVariableName();
        if (intMap.containsKey(varName))
            intMap.put(varName, intMap.get(varName) + forLoop.getStepValue().getIntegerValue());
        else
            intMap.put(varName, ((IntegerNode)forLoop.getStartState().getAssignmentVal()).getIntegerValue());
        int currentValue = intMap.get(varName);
        int endValue = forLoop.getEndValue().getIntegerValue();

        boolean continueLoop;
        if (forLoop.getStepValue().getIntegerValue() > 0)
            continueLoop = currentValue < endValue;
        else
            continueLoop = currentValue > endValue;

        if (!continueLoop){
            forLoop.setNext((StatementNode) forLoop.getNodeReference());
            intMap.remove(varName);
        }
    }

    // Processes IF statements and processes boolean expression using the processBooleanExpression method
    private void processIfStatement(IfNode ifStatement){
        boolean isValidBoolean = this.processBooleanExpression(ifStatement.getBooleanExpression());
        if (isValidBoolean){
            String label = ifStatement.getIdentifier().getVariableName();
            if (labeledStatementMap.containsKey(label)){
                StatementNode currentStatement = (StatementNode) labeledStatementMap.get(label);
                while (currentStatement != null && !currentStatement.equals(ifStatement)){
                    this.interpret(currentStatement);
                    currentStatement = currentStatement.getNext();
                }
                if (currentStatement == null)
                    ifStatement.setNext(null);
            }
            else
                throw new InvalidSymbolException("LABEL REFERENCED IN IF STATEMENT NEVER DECLARED");

        }
    }

    // Processes boolean expressions and calls upon helper method isTruthful to evaluate whether statement is
    // true or false
    private boolean processBooleanExpression(BooleanOperationNode booleanExpression){
        float leftHandValue = 0;
        float rightHandValue = 0;
        try{
            if (booleanExpression.getLeftExpressionNode() instanceof MathOpNode){
                boolean isIntOp = this.isIntegerOperation(booleanExpression.getLeftExpressionNode());
                leftHandValue = isIntOp ? this.evaluateIntMathOp(booleanExpression.getLeftExpressionNode()) :
                        this.evaluateFloatMathOp(booleanExpression.getLeftExpressionNode());
            }
            else{
                if (booleanExpression.getLeftExpressionNode() instanceof FloatNode)
                    leftHandValue = ((FloatNode) booleanExpression.getLeftExpressionNode()).getFloatValue();
                else if (booleanExpression.getLeftExpressionNode() instanceof VariableNode){
                    Object foundObject = intMap.get(((VariableNode) booleanExpression.getLeftExpressionNode()).
                            getVariableName()) != null ? intMap.get(((VariableNode) booleanExpression.
                            getLeftExpressionNode()).getVariableName()) : floatMap.
                            get(((VariableNode) booleanExpression.getLeftExpressionNode()).getVariableName());
                    if (foundObject == null)
                        throw new InvalidSymbolException("FOUND REFERENCE TO UNDECLARED VARIABLE");
                    else
                        leftHandValue = (float) foundObject;
                }
                else
                    leftHandValue = ((IntegerNode)booleanExpression.getLeftExpressionNode()).getIntegerValue();
            }
            if (booleanExpression.getRightExpressionNode() instanceof MathOpNode){
                boolean isIntOp = this.isIntegerOperation(booleanExpression.getRightExpressionNode());
                rightHandValue = isIntOp ? this.evaluateIntMathOp(booleanExpression.getRightExpressionNode()) :
                        this.evaluateFloatMathOp(booleanExpression.getRightExpressionNode());
            }
            else{
                if (booleanExpression.getRightExpressionNode() instanceof FloatNode)
                    rightHandValue = ((FloatNode) booleanExpression.getRightExpressionNode()).getFloatValue();
                else if (booleanExpression.getRightExpressionNode() instanceof VariableNode){
                    Object foundObject = intMap.get(((VariableNode) booleanExpression.getRightExpressionNode()).
                            getVariableName()) != null ? intMap.get(((VariableNode) booleanExpression.
                            getRightExpressionNode()).getVariableName()) : floatMap.
                            get(((VariableNode) booleanExpression.getRightExpressionNode()).getVariableName());
                    if (foundObject == null)
                        throw new InvalidSymbolException("FOUND REFERENCE TO UNDECLARED VARIABLE");
                    else
                        rightHandValue = (float) foundObject;
                }
                else
                    rightHandValue = ((IntegerNode)booleanExpression.getRightExpressionNode()).getIntegerValue();
            }
        }
        catch (ClassCastException ex){
            throw new InvalidSymbolException("FOUND INVALID BOOLEAN EXPRESSION WHILE PROCESSING IF STATEMENT");
        }

        return isTruthful(booleanExpression.getOperator().toString(), rightHandValue, leftHandValue);
    }

    // helper method for processBooleanExpression that returns truth value of boolean expression
    private boolean isTruthful(String operator, float rightValue, float leftValue){
        if (operator.equals("EQUALS"))
            return leftValue == rightValue;
        else if (operator.equals("GREATERTHANOREQUALTO"))
            return leftValue >= rightValue;
        else if (operator.equals("LESSTHANOREQUALTO"))
            return leftValue <= rightValue;
        else if (operator.equals("LESSTHAN"))
            return leftValue < rightValue;
        else if (operator.equals("GREATERTHAN"))
            return leftValue > rightValue;
        else if (operator.equals("NOTEQUALS"))
            return leftValue != rightValue;
        return false;
    }

    // Gets variable from map, compares types, throws exception if needed or moves on to next data item.
    private void processReadNode(ReadNode currentNode){
        List dataList = new ArrayList(); // Used to display read data
        List<VariableNode> variableNodes = currentNode.getVariableNodes();
        for (VariableNode varNode : variableNodes){
            String varName = varNode.getVariableName();
            Object dataNode; // Type Object since data can be of multiple types
            if (!dataContents.isEmpty())
                dataNode = dataContents.get(0);
            else
                throw new InvalidSymbolException("FOUND REFERENCE TO VARIABLE(S) WITHOUT NECESSARY DATA");
            if (varName.charAt(varName.length() - 1) == '$'){
                if (dataNode instanceof StringNode){
                    dataList.add(((StringNode) dataNode).getStringContents());
                    stringMap.put(varName, ((StringNode) dataNode).getStringContents());
                }

                else
                    throw new InvalidSymbolException("FOUND INCORRECT TYPE WHILE ATTEMPTING TO PROCESS STRING");
            }

            else if (varName.charAt(varName.length() - 1) == '%') {
                if (dataNode instanceof FloatNode){
                    dataList.add(((FloatNode) dataNode).getFloatValue());
                    floatMap.put(varName, ((FloatNode) dataNode).getFloatValue());
                }

                else
                    throw new InvalidSymbolException("FOUND INCORRECT TYPE WHILE ATTEMPTING TO PROCESS FLOAT");
            }
            else{
                if (dataNode instanceof IntegerNode){
                    dataList.add(((IntegerNode) dataNode).getIntegerValue());
                    intMap.put(varName, ((IntegerNode) dataNode).getIntegerValue());
                }

                else
                    throw new InvalidSymbolException("FOUND INCORRECT TYPE WHILE ATTEMPTING TO PROCESS INTEGER");
            }

            dataContents.remove(0);
        }
        System.out.println("Processed Data from READ: " + dataList.toString()); // Display processed data to user
    }

    // Processes the given AssignmentStatement and evaluates any expression or function call found
    private void processAssignmentNode(StatementNode currentNode){
        String variableName = ((AssignmentNode) currentNode).getVarNode().getVariableName();
        Node assignedValue = ((AssignmentNode) currentNode).getAssignmentVal();

        if (assignedValue instanceof MathOpNode) {
            try{
                Boolean isIntegerOperation = this.isIntegerOperation(assignedValue);
                if (isIntegerOperation) // use of helper method to determine whether int or float operation
                    assignedValue = new IntegerNode(this.evaluateIntMathOp(assignedValue)); // evaluates expression
                else
                    assignedValue = new FloatNode(this.evaluateFloatMathOp(assignedValue)); // evaluates expression
            }
            catch (ArithmeticException ex){
                System.out.println("DIVISION BY 0 ATTEMPTED, UNABLE TO EVALUATE EXPRESSION");
            }
        }

        if (assignedValue instanceof FunctionNode)
            assignedValue = this.processFunctionCall((FunctionNode) assignedValue);

        if (assignedValue instanceof StringNode)
            stringMap.put(variableName, ((StringNode) assignedValue).getStringContents());

        else if (assignedValue instanceof IntegerNode)
            intMap.put(variableName, ((IntegerNode) assignedValue).getIntegerValue());

        else if (assignedValue instanceof FloatNode)
            floatMap.put(variableName, ((FloatNode) assignedValue).getFloatValue());

    }

    //Recursive helper method for processAssignmentNode that determines whether to use integer
    // or floating point evaluation
    private boolean isIntegerOperation(Node operationNode){
        Node leftNode = null;
        Node rightNode = null;
        if (operationNode instanceof FloatNode)
            return false;
        if (operationNode instanceof IntegerNode)
            return true;
        if (operationNode instanceof VariableNode){
            Integer fromIntMap = intMap.get(((VariableNode) operationNode).getVariableName());
            Float fromFloatMap = floatMap.get(((VariableNode) operationNode).getVariableName());
            if (fromIntMap == null && fromFloatMap == null)
                throw new InvalidSymbolException("REFERENCE TO UNDECLARED VARIABLE " +
                        ((VariableNode) operationNode).getVariableName());
            if (fromIntMap == null)
                return false;
            return true;
        }
        if (operationNode instanceof MathOpNode){
            leftNode = ((MathOpNode) operationNode).getLeftNode();
            rightNode = ((MathOpNode) operationNode).getRightNode();
        }

        Boolean left = null;
        Boolean right = null;
        if (leftNode != null && rightNode != null){
            left = isIntegerOperation(leftNode);
            right = isIntegerOperation(rightNode);
        }


        if (!left || !right)
            return false;
        return true;
    }

    // Uses Java to print string parameter and then uses BufferedReader to process incoming data from user,
    // throws exception if there is a type mismatch
    private void processInputNode(StatementNode currentNode){
        List<Node> variableNodeList = ((InputNode) currentNode).getVariableNodes();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int i = 0;
        for (Node curr : variableNodeList){
            String response;
            if (curr instanceof StringNode)
                System.out.println(((StringNode) curr).getStringContents());
            else{
                System.out.print("Please enter value for " + ((VariableNode)variableNodeList.get(i)).
                        getVariableName() + ": ");
                try {
                    response = br.readLine();
                } catch (IOException e) {
                    throw new InvalidSymbolException("UNABLE TO PROCESS INPUT CORRECTLY FOR INPUT STATEMENT");
                }
                String varName = ((VariableNode) curr).getVariableName();
                if (varName.charAt(varName.length() - 1) == '$'){
                    try{
                        stringMap.put(varName, response);
                    }
                    catch (Exception ex){
                        throw new InvalidSymbolException("UNABLE TO PROCESS STRING DATA CORRECTLY");
                    }
                }
                else if (varName.charAt(varName.length() - 1) == '%'){
                    try{
                        floatMap.put(varName, Float.parseFloat(response));
                    }
                    catch (Exception ex){
                        throw new InvalidSymbolException("UNABLE TO PROCESS FLOAT DATA CORRECTLY");
                    }
                }
                else{
                    try{
                        intMap.put(varName, Integer.parseInt(response));
                    }
                    catch (Exception ex){
                        throw new InvalidSymbolException("UNABLE TO PROCESS INTEGER DATA CORRECTLY");
                    }
                }
            }
            i++;
        }
    }

    // Uses Java to print each of the referenced values, throws exception if variable referenced is not initialized
    private void processPrintNode(StatementNode currentNode){
        List<Node> printList = ((PrintNode) currentNode).getNodesToPrint();
        List resultsToPrint = new ArrayList();

        for (Node curr : printList){
            if (curr instanceof FunctionNode)
                resultsToPrint.add(this.processFunctionCall((FunctionNode) curr));
            else if (curr instanceof VariableNode){
                Object valueInMap = intMap.get(((VariableNode) curr).getVariableName());
                valueInMap = floatMap.get(((VariableNode) curr).getVariableName()) != null ? floatMap.get(((VariableNode)
                        curr).getVariableName()) : valueInMap;
                valueInMap = stringMap.get(((VariableNode) curr).getVariableName()) != null ? stringMap.get(((VariableNode)
                        curr).getVariableName()) : valueInMap;
                if (valueInMap != null)
                    resultsToPrint.add(valueInMap);
                else
                    throw new InvalidSymbolException("Variable referenced was never initialized");
            }
            else if (curr instanceof MathOpNode){
               Boolean isIntegerOperation = isIntegerOperation(curr);
               if (isIntegerOperation){
                   curr = new IntegerNode(this.evaluateIntMathOp(curr));
                   resultsToPrint.add(((IntegerNode) curr).getIntegerValue());
               }
               else{
                   curr = new FloatNode(this.evaluateFloatMathOp(curr));
                   resultsToPrint.add(((FloatNode) curr).getFloatValue());
               }
            }
            else
                resultsToPrint.add(curr);
        }
        System.out.println("processed data from PRINT: " + resultsToPrint.toString());
    }

    //Determines which function to call based upon function name, calls private function which does work
    private Node processFunctionCall(FunctionNode functionInvocation){
        String functionName = functionInvocation.getFunctionName();

        if (functionName.equals("RANDOM"))
            return processRandomFunctionCall();
        else if (functionName.equals("LEFT$"))
            return processLeft$FunctionCall(functionInvocation);
        else if (functionName.equals("RIGHT$"))
            return processRight$FunctionCall(functionInvocation);
        else if (functionName.equals("MID$"))
            return processMid$FunctionCall(functionInvocation);
        else if (functionName.equals("NUM$"))
            return processNum$FunctionCall(functionInvocation);
        else if (functionName.equals("VAL"))
            return processValIntFunctionCall(functionInvocation);
        else if (functionName.equals("VAL%"))
            return processValFloatFunctionCall(functionInvocation);
        else
            throw new InvalidSymbolException("UNKNOWN FUNCTION NAME FOUND WHILE PROCESSING");

    }

    // Recursively evaluates integer expressions, returns integers (precision is lost for floating point numbers)
    private int evaluateIntMathOp(Node currentNode){
        if (currentNode instanceof IntegerNode)
            return ((IntegerNode) currentNode).getIntegerValue();
        if (currentNode instanceof FloatNode)
            return (int)((FloatNode) currentNode).getFloatValue();
        if (currentNode instanceof VariableNode){
            Integer fromMap = null;
            if (!intMap.isEmpty()){
                fromMap = intMap.get( ((VariableNode) currentNode).getVariableName() );
                if (fromMap != null)
                    return fromMap;
            }
            throw new InvalidSymbolException("REFERENCE TO UNDECLARED VARIABLE " + ((VariableNode) currentNode).getVariableName());
        }

        int left = this.evaluateIntMathOp(((MathOpNode) currentNode).getLeftNode());
        int right = this.evaluateIntMathOp(((MathOpNode) currentNode).getRightNode());

        if (((MathOpNode) currentNode).getOperationType().equals(MathOpNode.OperationEnum.PLUS))
            return left + right;
        else if (((MathOpNode) currentNode).getOperationType().equals(MathOpNode.OperationEnum.MINUS))
            return left - right;
        else if (((MathOpNode) currentNode).getOperationType().equals(MathOpNode.OperationEnum.TIMES))
            return left * right;
        else
            return left / right;
    }

    // Recursively evaluates floating point expressions, returns float with as much precision as Java can handle
    private float evaluateFloatMathOp(Node currentNode){
        if (currentNode instanceof FloatNode)
            return ((FloatNode) currentNode).getFloatValue();
        if (currentNode instanceof IntegerNode)
            return (float) ((IntegerNode) currentNode).getIntegerValue();
        if (currentNode instanceof VariableNode){
            Float fromMap = null;
            if (!floatMap.isEmpty()){
                fromMap = floatMap.get( ((VariableNode) currentNode).getVariableName() );
                if (fromMap != null)
                    return fromMap;
            }
            if (!intMap.isEmpty() && fromMap == null){
                fromMap = (float) intMap.get( ((VariableNode) currentNode).getVariableName() );
                if (fromMap != null)
                    return fromMap;
            }
            throw new InvalidSymbolException("REFERENCE TO UNDECLARED VARIABLE " + ((VariableNode) currentNode).getVariableName());
        }

        float left = this.evaluateFloatMathOp(((MathOpNode) currentNode).getLeftNode());
        float right = this.evaluateFloatMathOp(((MathOpNode) currentNode).getRightNode());

        if (((MathOpNode) currentNode).getOperationType().equals(MathOpNode.OperationEnum.PLUS))
            return left + right;
        else if (((MathOpNode) currentNode).getOperationType().equals(MathOpNode.OperationEnum.MINUS))
            return left - right;
        else if (((MathOpNode) currentNode).getOperationType().equals(MathOpNode.OperationEnum.TIMES))
            return left * right;
        else
            return left / right;
    }

    // Processes RANDOM() function, returns generated value
    private IntegerNode processRandomFunctionCall(){
        Random r = new Random();
        return new IntegerNode(r.nextInt());
    }

    // Processes LEFT$() function call, throws exception for invalid numChars
    private StringNode processLeft$FunctionCall(FunctionNode function){
        List paramList = function.getParameterList();
        String originalString = ((StringNode) paramList.get(0)).getStringContents();
        int numChars = ((IntegerNode) paramList.get(1)).getIntegerValue();
        if (numChars > originalString.length() || numChars < 0)
            throw new InvalidSymbolException("STRING LENGTH " + originalString.length() +
                    " INSUFFICIENT FOR INT PARAMETER SIZE " + numChars);
        return new StringNode(originalString.substring(0, numChars));
    }

    // processes RIGHT$() function call, throws exception for invalid numChars
    private StringNode processRight$FunctionCall(FunctionNode function){
        List paramList = function.getParameterList();
        String originalString = ((StringNode) paramList.get(0)).getStringContents();
        int numChars = ((IntegerNode) paramList.get(1)).getIntegerValue();
        if (numChars > originalString.length() || numChars < 0)
            throw new InvalidSymbolException("STRING LENGTH " + originalString.length() +
                    " INSUFFICIENT FOR INT PARAMETER SIZE " + numChars);
        numChars = originalString.length() - numChars;
        return new StringNode(originalString.substring(numChars));
    }

    // processes MID$() function call, throws exception for invalid index or numChars
    private StringNode processMid$FunctionCall(FunctionNode function){
        List paramList = function.getParameterList();
        String originalString = ((StringNode) paramList.get(0)).getStringContents();
        int idx = ((IntegerNode) paramList.get(1)).getIntegerValue();
        int numChars = ((IntegerNode) paramList.get(2)).getIntegerValue();
        if (idx > originalString.length() - 1 || idx < 0)
            throw new InvalidSymbolException("STRING LENGTH " + originalString.length() +
                    " INSUFFICIENT FOR INDEX " + idx);
        if (numChars > originalString.length() - idx || numChars < 0)
            throw new InvalidSymbolException("STRING LENGTH " + originalString.length() +
                    " INSUFFICIENT FOR INT PARAMETER SIZE " + numChars);
        return new StringNode(originalString.substring(idx, (idx + numChars)));
    }

    // Processes NUM$() function call for ints and floats depending on type
    private StringNode processNum$FunctionCall(FunctionNode function){
        List paramList = function.getParameterList();
        if (paramList.get(0) instanceof IntegerNode){
            Integer integerVal = (((IntegerNode) paramList.get(0)).getIntegerValue());
            return new StringNode(integerVal.toString());
        }
        if (paramList.get(0) instanceof FloatNode){
            Float floatVal = (((FloatNode) paramList.get(0)).getFloatValue());
            return new StringNode(floatVal.toString());
        }
        return null;
    }

    // Processes VAL() function call for ints
    private IntegerNode processValIntFunctionCall(FunctionNode function){
        StringNode param1 = (StringNode) function.getParameterList().get(0);
        String stringVal = param1.getStringContents();
        return new IntegerNode(Integer.parseInt(stringVal));
    }

    // Processes VAL() function call for floats
    private FloatNode processValFloatFunctionCall(FunctionNode function){
        StringNode param1 = (StringNode) function.getParameterList().get(0);
        String stringVal = param1.getStringContents();
        return new FloatNode(Float.parseFloat(stringVal));
    }

    // finds labeledStatementNodes, replaces with StatementNode member in AST,
    // puts String member label and StatementNode in hashmap
    private void findLabeledStatements() {
        List<StatementNode> ast = abstractSyntaxTree.getStatementList();
        for (StatementNode statement : ast) {
            if (statement instanceof LabeledStatementNode) {
                labeledStatementMap.put(((LabeledStatementNode) statement).getLabel(), ((LabeledStatementNode) statement).getLabeledStatement());
                ast.set(ast.indexOf(statement), ((LabeledStatementNode) statement).getLabeledStatement());
            }
        }
    }

    // finds FOR and NEXT statements, matching them together via references and throwing exceptions when necessary -
    // a FOR statement's reference is null when there are no more statements to be executed after the adjacent NEXT statement
    private void findForStatements() {
        List<StatementNode> ast = abstractSyntaxTree.getStatementList();
        boolean foundForNode = false;
        ForNode forStatementRef = null;
        for (StatementNode statement : ast){
            if (statement instanceof ForNode){
                if (!foundForNode){
                    foundForNode = true;
                    forStatementRef = (ForNode) statement;
                }
                else
                    throw new InvalidSymbolException("Found new FOR statement without termination of prior FOR statement via NEXT statement");
        }

            if (statement instanceof NextNode){
                if (foundForNode){
                    Node afterNextRef = ast.size() - 1 > ast.indexOf(statement) ? ast.get(ast.indexOf(statement) + 1) : null;
                    ((NextNode) statement).setNodeReference(forStatementRef);
                    forStatementRef.setNodeReference(afterNextRef);
                    foundForNode = false;
                }
                else
                    throw new InvalidSymbolException("Found NEXT statement without any FOR statement");
            }
        }
        if (foundForNode)
            throw new InvalidSymbolException("FOUND FOR LOOP WITHOUT TERMINATING NEXT STATEMENT");
    }

    // Finds DATA statements, puts all contents of the statement into dataContents member, removes all DATA statements
    // at the end of iteration
    private void findDataStatements(){
        List<StatementNode> ast = abstractSyntaxTree.getStatementList();
        List<DataNode> dataToRemove = new ArrayList<>();
        for (StatementNode statement : ast) {
            if (statement instanceof DataNode) {
                dataContents.addAll(((DataNode) statement).getDataNodeList());
                dataToRemove.add((DataNode) statement);
            }
        }
        ast.removeAll(dataToRemove);
    }

    // Iterates through the AST and sets each next reference of type StatementNode to the next statement in the AST -
    // next is null for the last statement in the AST
    private void setNextReferences(){
        List<StatementNode> ast = abstractSyntaxTree.getStatementList();
        StatementNode nextStatement = null;
        for (StatementNode statement : ast) {
            nextStatement = ast.size() - 1 > ast.indexOf(statement) ? ast.get(ast.indexOf(statement) + 1) : null;
            statement.setNext(nextStatement);
        }
    }
}
