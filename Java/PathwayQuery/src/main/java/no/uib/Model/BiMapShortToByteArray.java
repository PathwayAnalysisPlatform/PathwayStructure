package no.uib.Model;

import gnu.trove.map.hash.TObjectShortHashMap;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapShortToByteArray {
    
    //<short, byte[]>

    public byte[][] numberToCharacters;
    private TObjectShortHashMap<byte[]> charactersToNumber;

    public BiMapShortToByteArray() {
        numberToCharacters = new byte[21000][];
        charactersToNumber = new TObjectShortHashMap<>();
    }

    public BiMapShortToByteArray(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        numberToCharacters = new byte[numElements][];
        charactersToNumber = new TObjectShortHashMap<>(initialCapacity);
    }

    public void put(short num, byte[] id) {
        numberToCharacters[num] = id;
        charactersToNumber.put(id, num);
    }

    public short getNum(byte[] id) {
        return charactersToNumber.get(id);
    }

    public byte[] getId(short num) {
        return numberToCharacters[num];
    }

    public String getStringId(short num) {
        return new String(numberToCharacters[num]);
    }

}
