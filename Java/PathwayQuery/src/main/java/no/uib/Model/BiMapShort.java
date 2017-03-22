package no.uib.Model;

import gnu.trove.map.hash.TObjectShortHashMap;
import java.util.ArrayList;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapShort {
    
    //<short, byte[]>

    public ArrayList<byte[]> numberToCharacters;
    private TObjectShortHashMap<byte[]> charactersToNumber;

    public BiMapShort() {
        numberToCharacters = new ArrayList<>();
        charactersToNumber = new TObjectShortHashMap<>();
    }

    public BiMapShort(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        numberToCharacters = new ArrayList<>(numElements);
        charactersToNumber = new TObjectShortHashMap<>(initialCapacity);
    }

    public void put(short num, byte[] id) {
        numberToCharacters.add(num, id);
        charactersToNumber.put(id, num);
    }

    public short getNum(byte[] id) {
        return charactersToNumber.get(id);
    }

    public byte[] getId(short num) {
        return numberToCharacters.get(num);
    }

    public String getStringId(short num) {
        return new String(numberToCharacters.get(num));
    }

}
