package no.uib.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class Converter {
    public static byte[] getByteArray(String str){
        byte[] result = new byte[str.length()];
        for(int I = 0; I < str.length(); I++){
            result[I] = (byte)str.charAt(I);
        }
        return result;
    }  
}
