package no.uib.Model;

import gnu.trove.map.hash.TObjectByteHashMap;
import java.util.ArrayList;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapByte {
    
    //<byte, byte[]>

    private ArrayList<byte[]> numberToCharacters;
    private TObjectByteHashMap<byte[]> charactersToNumber;

    public BiMapByte() {
        numberToCharacters = new ArrayList<>();
        charactersToNumber = new TObjectByteHashMap<>();
    }

    public BiMapByte(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        numberToCharacters = new ArrayList<>(numElements);
        charactersToNumber = new TObjectByteHashMap<>(initialCapacity);
    }

    public void put(byte num, byte[] id) {
        numberToCharacters.add(num, id);
        charactersToNumber.put(id, num);
    }

    public byte getNum(byte[] id) {
        return charactersToNumber.get(id);
    }

    public byte[] getId(byte num) {
        return numberToCharacters.get(num);
    }

    public String getStringId(byte num) {
        return new String(numberToCharacters.get(num));
    }

}
