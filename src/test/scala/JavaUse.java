import moe.roselia.NaiveJSON.*;


public class JavaUse {
    private static void useJSON(){
        JSON a = package$.MODULE$.parseJSON_$bang$bang("{\"a\": 1, \"b\": 2, \"c\": [1,2,3,4,5,6]}");
        System.out.println(a.format());
    }

    private static void howToUse(){
        System.out.println("Seemed no way");
    }

    public static void main(String[] args){
        useJSON();
        howToUse();
    }
}
