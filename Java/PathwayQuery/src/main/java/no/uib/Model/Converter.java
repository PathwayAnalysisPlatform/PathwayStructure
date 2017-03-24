package no.uib.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Converter {
    public static byte[] getByteArray(String str){
        byte[] result = new byte[str.length()];
        for(int I = 0; I < str.length(); I++){
            char c = str.charAt(I);
            result[I] = (byte)Character.getNumericValue(c);
        }
        return result;
    }  
    
    public static String getString(byte[] arr){
        String result = "";
        for(int I = 0; I < arr.length; I++){
            result += (char)arr[I];
        }
        return result;
    }
}
