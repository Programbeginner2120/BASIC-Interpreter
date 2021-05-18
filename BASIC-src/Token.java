public class Token {


    private TokenEnum token;
    private final String valueString;

    public Token(TokenEnum token, String valueString){ // Token constructor for tokens of enum NUMBER
        this.token = token;
        this.valueString = valueString;
    }

    public Token(TokenEnum token){ // Token constructor for tokens enums that are operators/symbols
        this.token = token;
        valueString = null; // initializing valueString as null, not needed here
    }


    public String getValueString(){
        return this.valueString;
    }

    public TokenEnum getToken(){
        return this.token;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(token);
        return sb.toString();
    }
}


