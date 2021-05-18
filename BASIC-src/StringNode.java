public class StringNode extends Node{

        private String stringContents;

        public StringNode(String stringContents){
            this.stringContents = stringContents;
        }

        public String getStringContents() {
            return stringContents;
        }

        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append(stringContents);
            return sb.toString();
        }

}
