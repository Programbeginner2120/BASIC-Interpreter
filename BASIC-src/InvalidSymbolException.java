public class InvalidSymbolException extends RuntimeException{

    public InvalidSymbolException(String message){
        super(message); // custom checked exception to inhibit illegal input
    }
}
