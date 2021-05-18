import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lexer {

    private final List<Token> tokenArray;
    private final HashMap <String, TokenEnum> tokenEnumHashMap; // These don't get mutated later, so made them final

    public Lexer() {
        tokenArray = new ArrayList<>();
        tokenEnumHashMap = new HashMap<>();
        this.populateHashMap(tokenEnumHashMap);
    }

    private void populateHashMap(HashMap <String, TokenEnum> tokenEnumHashMap){
        tokenEnumHashMap.put("PRINT", TokenEnum.PRINT); // All tokens as of current assignment in hashmap
        tokenEnumHashMap.put("GOSUB", TokenEnum.GOSUB);
        tokenEnumHashMap.put("RETURN", TokenEnum.RETURN);
        tokenEnumHashMap.put("FOR", TokenEnum.FOR);
        tokenEnumHashMap.put("NEXT", TokenEnum.NEXT);
        tokenEnumHashMap.put("TO", TokenEnum.TO);
        tokenEnumHashMap.put("STEP", TokenEnum.STEP);
        tokenEnumHashMap.put("IF", TokenEnum.IF);
        tokenEnumHashMap.put("RANDOM", TokenEnum.FUNCTION);
        tokenEnumHashMap.put("LEFT$", TokenEnum.FUNCTION);
        tokenEnumHashMap.put("RIGHT$", TokenEnum.FUNCTION);
        tokenEnumHashMap.put("MID$", TokenEnum.FUNCTION);
        tokenEnumHashMap.put("NUM$", TokenEnum.FUNCTION);
        tokenEnumHashMap.put("VAL", TokenEnum.FUNCTION);
        tokenEnumHashMap.put("VAL%", TokenEnum.FUNCTION);
        tokenEnumHashMap.put("THEN", TokenEnum.THEN);
    }

    public List<Token> lex(String currentString){
        int state = 1;
        String currentCharacter;
        TokenEnum tokenType = null;
        TokenEnum previousTokenType = null; // TokenEnum used for minus vs. negative number logic
        String valueString = ""; // String that will hold the value when TokenEnum is of type NUMBER
        boolean stringIsOpen = false; // boolean flag for STRING to make logic easier
        Token generatedToken;
        int i = 0;
        while (i < currentString.length()) {
            currentCharacter = Character.toString(currentString.charAt(i)); // matches requires a String as parameter
            switch (state){
                case 1:
                    if (currentCharacter.matches("[0-9]") || currentCharacter.equals(".")) {
                        tokenType = TokenEnum.NUMBER;
                        state = 2;
                        valueString += currentCharacter;
                        if (currentCharacter.equals(".")){
                            state = 3; // go to number that has a decimal state
                            tokenType = TokenEnum.NUMBER;
                            valueString += currentCharacter;
                        }
                        i++;
                        if (i > currentString.length() - 1){ // Go right to creation state since end of line
                            i--;
                            state = 9;
                        }
                    }
                    else if (currentCharacter.equals("+")){
                        tokenType = TokenEnum.PLUS;
                        state = 9;
                    }
                    else if (currentCharacter.equals("-")){
                        tokenType = TokenEnum.MINUS;
                        if (i == currentString.length() - 1){
                            state = 9;
                            break;
                        }
                        state = 2; // Goes to state 2 to see if it is a MINUS or negative number
                        i++;
                    }
                    else if (currentCharacter.equals("*")){
                        tokenType = TokenEnum.TIMES;
                        state = 9;
                    }
                    else if (currentCharacter.equals("/")){
                        tokenType = TokenEnum.DIVIDE;
                        state = 9;
                    }
                    else if (currentCharacter.equals(">")){
                        tokenType = TokenEnum.GREATERTHAN;
                        state = 4; // Goes to state 4 to see if it is >=
                        i++;
                        if (i > currentString.length() - 1){
                            i--;
                            state = 9;
                        }

                    }
                    else if (currentCharacter.equals("<")){
                        tokenType = TokenEnum.LESSTHAN;
                        state = 5; // // Goes to state 2 to see if it is <= or <>
                        i++;
                        if (i > currentString.length() - 1){
                            i--;
                            state = 9;
                        }

                    }
                    else if (currentCharacter.equals("=")){
                        tokenType = TokenEnum.EQUALS;
                        state = 9;
                    }
                    else if (currentCharacter.equals("(")){
                        tokenType = TokenEnum.LPAREN;
                        state = 9;
                    }
                    else if (currentCharacter.equals(")")){
                        tokenType = TokenEnum.RPAREN;
                        state = 9;
                    }
                    else if (currentCharacter.equals("\"")){
                        tokenType = TokenEnum.STRING;
                        stringIsOpen = true;
                        state = 6;
                        if (i == currentString.length() - 1){
                            throw new InvalidSymbolException("Invalid input entered, continuing to next line");
                        }
                        i++;

                    }
                    else if (Character.isAlphabetic(currentString.charAt(i)))
                    {
                        if (!stringIsOpen){
                            tokenType = TokenEnum.IDENTIFIER;
                            state = 7;
                            break;
                        }
                        valueString += currentCharacter;
                        if (i == currentString.length() - 1){
                            state = 9;
                            break;
                        }
                        i++;
                    }
                    else if (currentCharacter.matches("[ ]+")){
                        if (tokenType == null){ // just ignore space totally
                            state = 1;
                            i++;
                        }
                        else
                            state = 9; // create current TokenEnum, then move on
                    }
                    else if (currentCharacter.equals(",")){
                        tokenType = TokenEnum.COMMA;
                        state = 9;
                    }

                    else
                        throw new InvalidSymbolException("Invalid input entered, continuing to next line");

                    break;
                case 2:
                    if (currentCharacter.matches("[0-9]") || currentCharacter.equals(".")) {
                        if (tokenType != null && tokenType.equals(TokenEnum.MINUS) &&
                                (previousTokenType == null || !previousTokenType.equals(TokenEnum.NUMBER))){
                            valueString += "-"; // if statement used to check if negative number should be made
                            valueString += currentCharacter;
                            tokenType = TokenEnum.NUMBER;
                            i++;
                            if (i > currentString.length() - 1){
                                i--;
                                state = 9;
                            }
                            break;
                        }
                        else if (tokenType != null && tokenType.equals(TokenEnum.MINUS)){
                            i--; // if not negative number, make a MINUS token and move on
                            state = 9;
                            break;
                        }
                        if (currentCharacter.matches("[0-9]")){
                            valueString += currentCharacter;
                            tokenType = TokenEnum.NUMBER;
                            i++;
                        }
                        if (currentCharacter.equals(".")){
                            state = 3;
                            tokenType = TokenEnum.NUMBER;
                            valueString += currentCharacter;
                            i++;
                        }
                        if (i > currentString.length() - 1){
                            i--;
                            state = 9;
                        }
                    }
                    else{
                        i--;
                        state = 9;
                    }
                    break;
                case 3:
                    if (currentCharacter.matches("[0-9]")){
                        valueString += currentCharacter;
                        i++;
                        if (i > currentString.length() - 1){
                            i--;
                            state = 9;
                        }
                    }
                    else if (currentCharacter.equals(".")) // No more decimals since one is present
                        throw new InvalidSymbolException("Invalid input entered, continuing to next line");
                    else{
                        i--;
                        state = 9;
                    }
                    break;
                case 4:
                    if (currentCharacter.equals("="))
                        tokenType = TokenEnum.GREATERTHANOREQUALTO;
                    else if (i > 0)
                        i--;
                    state = 9;
                    break;
                case 5:
                    if (currentCharacter.equals("="))
                        tokenType = TokenEnum.LESSTHANOREQUALTO;
                    else if (currentCharacter.equals(">"))
                        tokenType = TokenEnum.NOTEQUALS;
                    else
                        i--;
                    state = 9;
                    break;
                case 6:

                    if (i == currentString.length() - 1 && !currentCharacter.equals("\"")) // if no close quotes
                        throw new InvalidSymbolException("Invalid input entered, continuing to next line");

                    if (currentCharacter.equals("\"")){
                        stringIsOpen = false;
                        state = 9;
                        break;
                    }
                    else{
                        valueString += currentCharacter;
                        i++;
                        break;
                    }

                case 7:
                    if (currentCharacter.equals("$") || currentCharacter.equals("%")){
                        valueString += currentCharacter;
                        if (i == currentString.length() - 1){
                            state = 9;
                            i--;
                        }
                        else
                            state = 8;
                        i++;
                        break;
                    }
                    if (currentCharacter.equals(":")){
                        tokenType = TokenEnum.LABEL;
                        state = 9;
                        break;
                    }
                    if (Character.isAlphabetic(currentString.charAt(i))){
                        valueString += currentCharacter;
                        if (i == currentString.length() - 1){
                                state = 9;
                                break;
                            }
                        i++;
                    }

                    else{
                        state = 9;
                        i--;
                    }
                    break;
                case 8:
                    if (currentCharacter.equals(":"))
                        tokenType = TokenEnum.LABEL;
                    else
                        i--;
                    state = 9;
                    break;
                case 9:
                    if (tokenType.equals(TokenEnum.NUMBER) || tokenType.equals(TokenEnum.IDENTIFIER)
                            || tokenType.equals(TokenEnum.LABEL)) {
                        if (valueString.charAt(valueString.length() - 1) == '.')
                            throw new InvalidSymbolException("Invalid input entered, continuing to next line");
                        if (valueString.equals("READ") || valueString.equals("DATA") || valueString.equals("INPUT"))
                            tokenType = TokenEnum.valueOf(valueString);
                        if (tokenEnumHashMap.containsKey(valueString))
                            tokenType = tokenEnumHashMap.get(valueString);
                        generatedToken = new Token(tokenType, valueString);
                        tokenArray.add(generatedToken);
                        valueString = "";
                    }
                    else if (tokenType.equals(TokenEnum.STRING)){
                        if (stringIsOpen)
                            throw new InvalidSymbolException("Invalid input entered, continuing to next line");
                        generatedToken = new Token(tokenType, valueString);
                        tokenArray.add(generatedToken);
                        valueString = "";
                    }
                    else{
                        generatedToken = new Token(tokenType);
                        tokenArray.add(generatedToken);
                    }
                    valueString = ""; // setting everything back to initial conditions
                    previousTokenType = tokenType;
                    tokenType = null;
                    state = 1;
                    i++;
                    break;
            }
        }

        return tokenArray;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokenArray.size(); i++){
            sb.append(tokenArray.get(i));
            sb.append(' ');
        }

        sb.append("endOfLine");
        return sb.toString();
    }
}