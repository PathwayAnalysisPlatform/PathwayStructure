package no.uib.Model;

import gnu.trove.map.hash.TObjectByteHashMap;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapByteString {
    
    //<byte, byte[]>

    public String[] numberToCharacters;
    private TObjectByteHashMap<String> charactersToNumber;

    public BiMapByteString() {
        numberToCharacters = new String[9];
        charactersToNumber = new TObjectByteHashMap<>();
    }

    public BiMapByteString(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        numberToCharacters = new String[numElements];
        charactersToNumber = new TObjectByteHashMap<>(initialCapacity);
    }

    public void put(short num, String id) {
        numberToCharacters[num] = id;
        charactersToNumber.put(id, (byte)num);
    }

    public byte getNum(String id) {
        return charactersToNumber.get(id);
    }

    public String getId(byte num) {
        return numberToCharacters[num];
    }

}
