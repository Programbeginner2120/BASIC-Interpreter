import java.util.ArrayList;
import java.util.List;


public class Parser {

    private final List<Token> tokenArray;


    public Parser(List<Token> tokenArray) {
        this.tokenArray = tokenArray;
    }

    public Token matchAndRemove(TokenEnum tk) {  // standard matchAndRemove helper method from lecture
        if (tokenArray.size() > 0){  // make sure that tokenArray is populated to avoid null pointers
            Token compareToken = tokenArray.get(0);
            if (compareToken.getToken().equals(tk)){
                tokenArray.remove(compareToken);
                return compareToken;
            }
        }
        return null;
    }

    public Node expression() {
        Node currentNode;
        currentNode = this.term();
        if (currentNode != null)
            return getRightOfExpression(currentNode); // found expression, check for operator
        currentNode = this.functionInvocation(); // did not find expression, check for function invocation
        if (currentNode != null)
            return currentNode;
        return null;
    }

    private Node getRightOfExpression(Node leftNode){
        Token operatorToken = this.matchAndRemove(TokenEnum.PLUS); // checking for plus or minus operator
        operatorToken = ((operatorToken == null) ? this.matchAndRemove(TokenEnum.MINUS) : operatorToken);

        if (operatorToken == null)
            return leftNode;
        Node nextNode = term();
        if (nextNode == null)
            throw new InvalidSymbolException("Found a + or - but no term");

        MathOpNode.OperationEnum operationType = null;
        char operatorTokenSymbol = ' '; // placeholder for char
        Node operationNode;
        if (operatorToken.getToken().equals(TokenEnum.PLUS)){
            operationType = MathOpNode.OperationEnum.PLUS;
            operatorTokenSymbol = '+';
        }
        if (operatorToken.getToken().equals(TokenEnum.MINUS)){
            operationType = MathOpNode.OperationEnum.MINUS;
            operatorTokenSymbol = '-';
        }
        operationNode = new MathOpNode(operationType, operatorTokenSymbol, leftNode, nextNode);
        return getRightOfExpression(operationNode); // call itself to check for number
    }

        public Node term () {  // essential same process as expression()
            Node currentNode = this.factor();
            if (currentNode == null)
                return null;
            return getRightOfTerm(currentNode);
        }

        public Node getRightOfTerm(Node leftNode){
            Token operatorToken = this.matchAndRemove(TokenEnum.TIMES);
            operatorToken = ((operatorToken == null) ? this.matchAndRemove(TokenEnum.DIVIDE) : operatorToken);

            if (operatorToken == null)
                return leftNode;
            Node nextNode = factor();
            if (nextNode == null)
                throw new InvalidSymbolException("Found a * or / but no factor");

            MathOpNode.OperationEnum operationType = null;
            char operatorTokenSymbol = ' '; // placeholder for char
            Node operationNode;
            if (operatorToken.getToken().equals(TokenEnum.TIMES)){
                operationType = MathOpNode.OperationEnum.TIMES;
                operatorTokenSymbol = '*';
            }
            if (operatorToken.getToken().equals(TokenEnum.DIVIDE)){
                operationType = MathOpNode.OperationEnum.DIVIDE;
                operatorTokenSymbol = '/';
            }
            operationNode = new MathOpNode(operationType, operatorTokenSymbol, leftNode, nextNode);
            return getRightOfTerm(operationNode);
        }

        public Node factor (){
            Token matchedToken;
            matchedToken = this.matchAndRemove(TokenEnum.NUMBER);
            if (matchedToken != null){
                    if (matchedToken.getValueString().contains(".")) // check if number is float
                        return new FloatNode(Float.parseFloat(matchedToken.getValueString()));
                    else
                        return new IntegerNode(Integer.parseInt(matchedToken.getValueString()));
            }
            matchedToken = this.matchAndRemove(TokenEnum.IDENTIFIER); // check if variable
            if (matchedToken != null)
                return new VariableNode(matchedToken.getValueString());
            if (this.matchAndRemove(TokenEnum.LPAREN) != null){
                Node expressionCall = this.expression();
                if (expressionCall == null)
                    throw new InvalidSymbolException("Found left parentheses with no expression");
                if (this.matchAndRemove(TokenEnum.RPAREN) != null)
                    return expressionCall;
                throw new InvalidSymbolException("Found left parentheses and expression, though no right parentheses");
            }
            return null;
        }

        public StringNode string(){
            Token stringToken;
            stringToken = this.matchAndRemove(TokenEnum.STRING);
            if (stringToken != null)
                return new StringNode(stringToken.getValueString());
            return null;
        }

        public StatementsNode statements(){
            List <StatementNode> statementNodeList = new ArrayList<>();
            StatementNode parsedStatement = this.statement();
            while (parsedStatement != null){ // used to parse each line of text file
                if (tokenArray.size() != 0)
                    throw new InvalidSymbolException("Found illegal parameters after parsed statement");
                statementNodeList.add(parsedStatement);
                parsedStatement = this.statement();
            }
            return new StatementsNode(statementNodeList);
        }

        public StatementNode statement(){
            StatementNode currentNode;
            currentNode = this.labeledStatement(); // check if labeled statement
            if (currentNode != null)
                return currentNode;
            currentNode = this.assignment(); // check if assignment statement
            if (currentNode != null)
                return currentNode;
            currentNode = this.printStatement(); // check if PRINT statement
            if (currentNode != null)
                return currentNode;
            currentNode = this.read();  // check if READ
            if (currentNode != null)
                return currentNode;
            currentNode = this.data(); // check if DATA
            if (currentNode != null)
                return currentNode;
            currentNode = this.input(); // check if INPUT
            if (currentNode != null)
                return currentNode;
            currentNode = this.returnStatement(); // check if RETURN
            if (currentNode != null)
                return currentNode;
            currentNode = this.goSubStatement(); // check if GOSUB
            if (currentNode != null)
                return currentNode;
            currentNode = this.forStatement(); // check if FOR
            if (currentNode != null)
                return currentNode;
            currentNode = this.nextStatement(); // check if NEXT
            if (currentNode != null)
                return currentNode;
            currentNode = this.ifStatement(); // check if IF statement
            if (currentNode != null)
                return currentNode;
            return null;
        }

        // method to handle function invocations
        public FunctionNode functionInvocation(){
            FunctionNode generatedNode = null;
            Token functionToken = this.matchAndRemove(TokenEnum.FUNCTION);
            if (functionToken != null){
                String functionName = functionToken.getValueString();
                if (functionName.equals("RANDOM"))
                    generatedNode = randomFunctionParser(functionName);
                else if (functionName.equals("LEFT$") || functionName.equals("RIGHT$") || functionName.equals("MID$"))
                    generatedNode = charFunctionParser(functionName);
                else if (functionName.equals("NUM$"))
                    generatedNode = numFunctionParser(functionName);
                else if (functionName.equals("VAL") || functionName.equals("VAL%"))
                    generatedNode = valFunctionParser(functionName);
            }
            return generatedNode;
        }

        //Method to parse RANDOM
        public FunctionNode randomFunctionParser(String functionName){
            if (this.matchAndRemove(TokenEnum.LPAREN) != null){
                if (this.matchAndRemove(TokenEnum.RPAREN) != null)
                    return new FunctionNode(functionName);
                throw new InvalidSymbolException("Found function invocation without closing parenthesis");
            }
            throw new InvalidSymbolException("Found function invocation without opening parenthesis");
        }

        //Method to parse LEFT$, RIGHT$, MID$
        public FunctionNode charFunctionParser(String functionName){
            List <Node> parameterList = new ArrayList<>();
            if (this.matchAndRemove(TokenEnum.LPAREN) != null) {
                StringNode stringParam = string();
                if (stringParam != null) {
                    parameterList.add(stringParam);
                    if (this.matchAndRemove(TokenEnum.COMMA) != null){
                        IntegerNode intParam;
                        IntegerNode secondIntParam;
                        try {
                            intParam = (IntegerNode) expression(); // Since second param needs to be int from expression call
                            if (intParam != null)
                                parameterList.add(intParam);
                            else
                                throw new InvalidSymbolException("Invalid type found when parsing int parameter");
                            if (functionName.equals("MID$")){
                                if (this.matchAndRemove(TokenEnum.COMMA) != null){
                                    secondIntParam = (IntegerNode) expression();
                                    if (secondIntParam != null)
                                        parameterList.add(secondIntParam);
                                    else
                                        throw new InvalidSymbolException("Found function invocation without required int parameter");
                                }
                                else
                                    throw new InvalidSymbolException("Did not find comma separating parameters");
                            }
                        }
                        catch (ClassCastException ex) {
                            throw new InvalidSymbolException("Invalid type found when parsing int parameter");
                        }
                        if (this.matchAndRemove(TokenEnum.RPAREN) != null)
                            return new FunctionNode(functionName, parameterList);
                        throw new InvalidSymbolException("Found function invocation without closing parenthesis");
                    }
                    throw new InvalidSymbolException("Did not find comma separating parameters");
                }
                throw new InvalidSymbolException("Found function invocation without required String parameter");
            }
            throw new InvalidSymbolException("Found function invocation without opening parenthesis");
        }

        //Method to parse NUM$
        public FunctionNode numFunctionParser(String functionName){
            List <Node> parameterList = new ArrayList<>();
            if (this.matchAndRemove(TokenEnum.LPAREN) != null){
                Node functionParameter = expression();
                if (functionParameter != null && !(functionParameter instanceof VariableNode)){
                    parameterList.add(functionParameter);
                    if (this.matchAndRemove(TokenEnum.RPAREN) != null)
                        return new FunctionNode(functionName, parameterList);
                    throw new InvalidSymbolException("Found function invocation without closing parenthesis");
                }
                throw new InvalidSymbolException("Found function invocation without required parameter");
            }
            throw new InvalidSymbolException("Found function invocation without opening parenthesis");
        }

        //Method to parse VAL, VAL%
        public FunctionNode valFunctionParser(String functionName){
            List <Node> parameterList = new ArrayList<>();
            if (this.matchAndRemove(TokenEnum.LPAREN) != null){
                Node functionParameter = string();
                if (functionParameter != null){
                    parameterList.add(functionParameter);
                    if (this.matchAndRemove(TokenEnum.RPAREN) != null)
                        return new FunctionNode(functionName, parameterList);
                    throw new InvalidSymbolException("Found function invocation without closing parenthesis");
                }
                throw new InvalidSymbolException("Found function invocation without required String parameter");
            }
            throw new InvalidSymbolException("Found function invocation without opening parenthesis");
        }

        // Method to parse constructed if statements in the form LPAREN Boolean Operation RPAREN
        public IfNode ifStatement(){
            BooleanOperationNode generatedOperation;
            if (this.matchAndRemove(TokenEnum.IF) != null){
                generatedOperation = booleanExpression();
                if (generatedOperation != null){
                    if (this.matchAndRemove(TokenEnum.THEN) != null){
                        try{
                            VariableNode parsedIdentifier;
                            parsedIdentifier = (VariableNode) expression();
                            if (parsedIdentifier != null)
                                return new IfNode(generatedOperation, parsedIdentifier);
                            throw new InvalidSymbolException("Failed to find identifier when parsing IF statement");
                        }
                        catch (ClassCastException ex){
                            throw new InvalidSymbolException("Found illegal parameters while parsing statement");
                        }
                        }
                    throw new InvalidSymbolException("Failed to find THEN keyword when parsing IF statement");
                    }
                throw new InvalidSymbolException("IF statement missing boolean expression");
            }
            return null;
        }

        // Method to parse boolean expressions in the form expression operator expression
        public BooleanOperationNode booleanExpression(){
            Node leftNode;
            Node rightNode;
            Token operatorToken;

            leftNode = expression();
            if (leftNode != null){
                operatorToken = retrieveOperator();
                if (operatorToken != null){
                    rightNode = expression();
                    if (rightNode != null)
                        return new BooleanOperationNode(leftNode, rightNode, operatorToken.getToken());
                    throw new InvalidSymbolException("Right hand expression not found");
                }
                throw new InvalidSymbolException("Operator not found");
            }
            return null;
        }

        // helper method to check for allowed operators in boolean expression
        private Token retrieveOperator() {
            TokenEnum[] tkEnumArray = {TokenEnum.GREATERTHAN, TokenEnum.GREATERTHANOREQUALTO, TokenEnum.LESSTHAN,
                    TokenEnum.LESSTHANOREQUALTO, TokenEnum.NOTEQUALS, TokenEnum.EQUALS};

            Token operatorToken;
            for (TokenEnum tkEnum : tkEnumArray) {
                operatorToken = this.matchAndRemove(tkEnum);
                if (operatorToken != null)
                    return operatorToken;
            }
            return null;
        }

        public LabeledStatementNode labeledStatement(){
        Token labelToken = this.matchAndRemove(TokenEnum.LABEL);
            StatementNode generatedNode;
            if (labelToken != null){
                generatedNode = this.statement();
                if (generatedNode == null)
                    throw new InvalidSymbolException("Found label with no following statement");
                return new LabeledStatementNode(labelToken.getValueString(), generatedNode);
            }
            return null;
        }

        public ReturnNode returnStatement(){
            Token returnToken = this.matchAndRemove(TokenEnum.RETURN);
            if (returnToken != null){
                if (!tokenArray.isEmpty()) // if there is anything that hasn't been parsed to the right
                    throw new InvalidSymbolException("Found text to the right of RETURN statement where there" +
                            " should be none");
                return new ReturnNode();
            }
            return null;
        }

        public GosubNode goSubStatement(){
            Token goSubToken = this.matchAndRemove(TokenEnum.GOSUB);
            VariableNode identifier;
            if (goSubToken != null){
                try{
                    identifier = (VariableNode) this.expression(); // try - catch since needs to return VariableNode
                    if (identifier == null)
                        throw new InvalidSymbolException("Found GOSUB without reference to subroutine");
                    return new GosubNode(identifier);
                }
                catch (ClassCastException ex){
                    throw new InvalidSymbolException("Found illegal parameter while parsing statement");
                }
            }
            return null;
        }

        public ForNode forStatement(){
            Token forToken = this.matchAndRemove(TokenEnum.FOR);
            VariableNode forVariable;
            IntegerNode startValue;
            IntegerNode endValue;
            IntegerNode stepValue;
            if (forToken != null){
                try{
                    forVariable = (VariableNode) this.expression();
                    if (forVariable != null){
                        if (this.matchAndRemove(TokenEnum.EQUALS) == null)
                            throw new InvalidSymbolException("Found FOR with no equal sign");
                        startValue = (IntegerNode) this.expression(); // try - catch for multiple IntegerNode casts
                        if (startValue != null){
                            if (matchAndRemove(TokenEnum.TO) == null)
                                throw new InvalidSymbolException("Found FOR with no TO statement");
                            endValue = (IntegerNode) this.expression();
                            if (endValue != null){
                                if (matchAndRemove(TokenEnum.STEP) != null){
                                    stepValue = (IntegerNode) this.expression();
                                    if (stepValue != null)
                                        return new ForNode(new AssignmentNode(forVariable, startValue), endValue
                                                , stepValue);
                                    throw new InvalidSymbolException("Found STEP with no value afterwards");
                                }
                                return new ForNode(new AssignmentNode(forVariable, startValue), endValue);
                            }
                            throw new InvalidSymbolException("Found FOR with no limit value");
                        }
                        throw new InvalidSymbolException("Found FOR with no initial value");
                    }
                    throw new InvalidSymbolException("Found FOR with no assignment statement");
                }
                catch (ClassCastException ex){
                    throw new InvalidSymbolException("Found illegal parameter while parsing");
                }
            }
            return null;
        }

        public NextNode nextStatement(){
            Token generatedToken = this.matchAndRemove(TokenEnum.NEXT);
            VariableNode varNode;
            if (generatedToken != null){
                try{
                    varNode = (VariableNode) this.expression(); // try - catch for since needs to return VariableNode
                    if (varNode != null)
                        return new NextNode(varNode);
                    throw new InvalidSymbolException("Found keyword NEXT with no variable afterward");
                }
                catch (ClassCastException ex){
                    throw new InvalidSymbolException("Found illegal parameter while parsing");
                }
            }
            return null;
        }

        public PrintNode printStatement(){
            List <Node> printNodeList = new ArrayList<>();
            PrintNode generatedNode = this.printList(printNodeList);
            if (generatedNode != null){
                if (tokenArray.size() != 0) // if any lexeme(s) remain in line that haven't been parsed
                    throw new InvalidSymbolException("Found illegal parameter(s) while parsing");
                return generatedNode;
            }
            return null;
        }

        public PrintNode printList(List <Node> printNodeList){
            if (this.matchAndRemove(TokenEnum.PRINT) != null){
                Node currentNode;
                currentNode = this.expression();
                currentNode = currentNode != null ? currentNode : this.string(); // Use ternary if statement to get Node
                if (currentNode != null){
                    printNodeList.add(currentNode);
                    while (hasCommaToken()){ // loop through comma separated list
                        currentNode = this.expression();
                        currentNode = currentNode != null ? currentNode : this.string();
                        if (currentNode != null)
                            printNodeList.add(currentNode);
                        else
                            throw new InvalidSymbolException("Found comma with invalid print parameter");
                    }
                    return new PrintNode(printNodeList);
                }
            }
            if (hasCommaToken()) // If there is a PRINT statement with just a comma
                throw new InvalidSymbolException("Found comma with invalid print parameter");
            return null;
        }

            public ReadNode read(){
                List <VariableNode> readListNodes = new ArrayList<>();
                ReadNode generatedNode = this.readList(readListNodes);
                if (generatedNode != null){
                    if (tokenArray.size() != 0) // if any lexeme(s) remain in line that haven't been parsed
                        throw new InvalidSymbolException("Found illegal parameter(s) while parsing");
                    return generatedNode;
                }
                return null;
            }

            public ReadNode readList(List <VariableNode> readListNodes) {
                if (this.matchAndRemove(TokenEnum.READ) != null) {
                    Node currentNode;
                    currentNode = this.expression();
                    if (currentNode != null) {
                        try {
                            readListNodes.add((VariableNode) currentNode);
                            while (hasCommaToken()) { // loop through comma separated list
                                currentNode = this.expression();
                                if (currentNode != null)
                                    readListNodes.add((VariableNode) currentNode);
                                else
                                    throw new InvalidSymbolException("Found comma with invalid read parameter");
                            }
                            return new ReadNode(readListNodes);
                        }
                        catch (ClassCastException ex){ // If not a VariableNode, throw exception and stop parsing
                            throw new InvalidSymbolException("Found invalid parameter while parsing statement");
                    }
                }
            }
                if (hasCommaToken()) // If there is a PRINT statement with just a comma
                    throw new InvalidSymbolException("Found comma with invalid read parameter");
                return null;
            }

            public DataNode data(){
                List <Node> dataListNodes = new ArrayList<>();
                DataNode generatedNode = this.dataList(dataListNodes);
                if (generatedNode != null){
                    if (tokenArray.size() != 0) // if any lexeme(s) remain in line that haven't been parsed
                        throw new InvalidSymbolException("Found illegal parameter(s) while parsing");
                    return generatedNode;
                }
                return null;
            }

            public DataNode dataList(List <Node> dataListNodes){
                if (this.matchAndRemove(TokenEnum.DATA) != null) {
                    Node currentNode;
                    currentNode = this.expression();
                    currentNode = currentNode != null && (currentNode instanceof FloatNode || currentNode
                            instanceof IntegerNode) ? currentNode : this.string();
                    if (currentNode != null) {
                            dataListNodes.add(currentNode);
                            while (hasCommaToken()) { // loop through comma separated list
                                currentNode = this.expression();
                                currentNode = currentNode != null && (currentNode instanceof FloatNode || currentNode
                                        instanceof IntegerNode) ? currentNode : this.string(); // Used to get correct Node
                                if (currentNode != null)
                                    dataListNodes.add(currentNode);
                                else
                                    throw new InvalidSymbolException("Found comma with invalid data parameter");
                            }
                            return new DataNode(dataListNodes);
                    }
                }
                if (hasCommaToken()) // If there is a PRINT statement with just a comma
                    throw new InvalidSymbolException("Found comma with invalid data parameter");
                return null;
            }

            public InputNode input(){
                List <Node> inputListNodes = new ArrayList<>();
                InputNode generatedNode = this.inputList(inputListNodes);
                if (generatedNode != null){
                    if (tokenArray.size() != 0) // if any lexeme(s) remain in line that haven't been parsed
                        throw new InvalidSymbolException("Found illegal parameter(s) while parsing");
                    return generatedNode;
                }
                return null;
            }

            public InputNode inputList(List <Node> inputListNodes){
                if (this.matchAndRemove(TokenEnum.INPUT) != null) {
                    Node currentNode;
                    currentNode = this.expression();
                    currentNode = currentNode != null && currentNode instanceof VariableNode ?
                            currentNode : this.string();
                    if (currentNode != null) {
                            inputListNodes.add(currentNode);
                            try{
                                while (hasCommaToken()) { // loop through comma separated list
                                    currentNode = this.expression();
                                    if (currentNode != null)
                                        inputListNodes.add((VariableNode) currentNode);
                                    else
                                        throw new InvalidSymbolException("Found comma with invalid input parameter");
                                }
                                return new InputNode(inputListNodes);
                            }
                            catch (ClassCastException ex){ // If not a VariableNode, throw exception and stop parsing
                                throw new InvalidSymbolException("Found invalid parameter while parsing statement");
                            }
                    }
                }
                if (hasCommaToken()) // If there is a PRINT statement with just a comma
                    throw new InvalidSymbolException("Found comma with invalid input parameter");
                return null;
            }

        public boolean hasCommaToken(){ // boolean helper method to detect presence of comma token in printList
            return this.matchAndRemove(TokenEnum.COMMA) != null;
        }

        public AssignmentNode assignment(){
            Token variableName;
            Node expression;
            variableName = this.matchAndRemove(TokenEnum.IDENTIFIER);
            if (variableName == null)
                return null;
            if (this.matchAndRemove(TokenEnum.EQUALS) == null)
                throw new InvalidSymbolException("Found variable but no assignment statement");
            expression = this.expression();
            Token foundStringToken = this.matchAndRemove(TokenEnum.STRING); // used so that assignmentNode can also have string data
            expression = foundStringToken != null ? new StringNode(foundStringToken.getValueString()) : expression;
            if (expression != null)
                return new AssignmentNode(new VariableNode(variableName.getValueString()), expression);
            throw new InvalidSymbolException("Found variable and equals but no assignment");
        }

        public StatementsNode parse () {
            return this.statements(); // call to statements, process of parsing begins
        }
    }