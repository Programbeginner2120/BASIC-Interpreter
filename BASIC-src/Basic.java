import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Basic {
    public static void main(String[] args) throws IOException, InvalidSymbolException {


        if (args.length != 1){
            System.out.println("invalid number of arguments, terminating program...");
            System.exit(0);
        }
        else
            System.out.println("Correct number of arguments passed");

        try {

//            File myFile = new File("assignment-10-tests.txt");
//            Path path = Paths.get(String.valueOf(myFile));
            Path path = Paths.get(args[0]); // getting the file path sent through via argument
            List<String> lines = Files.readAllLines(path); // using the readAllLines method to read file


//            List<Token> tokenArray = new ArrayList<>(); // Used in previous assignments, was temporary
            ArrayList<StatementNode> statementList = new ArrayList<>(); // will be passed into StatementsNode to create AST
            int idx = 0;
            while (idx < lines.size()) {
                String currentLine = lines.get(idx);
                try {
                    Lexer currentLexer = new Lexer(); // Lexer object instantiated & lex method called for each line
                    List<Token> elementsInline = currentLexer.lex(currentLine);
                    Parser currentParser = new Parser(elementsInline);
                    StatementsNode currentStatementsNode = currentParser.parse();
                    statementList.addAll(currentStatementsNode.getStatementList());
//                    System.out.println(statementList.get(statementList.size() - 1));
//                    tokenArray.addAll(elementsInline); // Used in previous assignments, was temporary
//                    System.out.println(currentLexer.toString()); // Used in previous assignments, was temporary
                    idx++;
                } catch (InvalidSymbolException ex) {
                    System.out.println(ex.getMessage());
                    idx++;
                }
            }

            //System.out.println("Creation of AST and initial output:");
            StatementsNode abstractSyntaxTree = new StatementsNode(statementList);
            //System.out.println(abstractSyntaxTree.getStatementList().toString());

            // Instantiation of Interpreter class and call to initialize method
            Interpreter basicInterpreter = new Interpreter(abstractSyntaxTree);
            basicInterpreter.initialize();

            StatementNode currentStatement; // StatementNode currentStatement used for iteration
            currentStatement = abstractSyntaxTree.getStatementList().get(0); // access to first element in AST
            while (currentStatement != null){
                basicInterpreter.interpret(currentStatement);
                currentStatement = currentStatement.getNext(); // Loops until currentStatement.getNext() is null
            }


//             System.out.println(tokenArray.toString()); // // Used in previous assignments, was temporary
        }
        catch (InvalidSymbolException ex){
            System.out.println(ex.getMessage());
        }
        catch (ArrayIndexOutOfBoundsException ex){ // Catches exception from get method if no file is passed through
            System.out.println("ArrayIndexOutOfBoundsException encountered: " + ex.getMessage());
            System.out.println("Terminating program due to encountered exception...");
        }
        catch (IOException ex){ // In the case that IOException occurs
            System.out.println("IOException encountered, terminating program...");
        }
    }
}
