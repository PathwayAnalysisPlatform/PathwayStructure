package no.uib.model;

import gnu.trove.map.hash.TObjectShortHashMap;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapShortString {
    
    //<short, byte[]>   //There is a short number labels, and I want to translate them into byte[].

    public String[] numberToCharacters;
    private TObjectShortHashMap<String> charactersToNumber;

    public BiMapShortString() {
        numberToCharacters = new String[21000];
        charactersToNumber = new TObjectShortHashMap<>();
    }

    public BiMapShortString(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        numberToCharacters = new String[numElements];
        charactersToNumber = new TObjectShortHashMap<>(initialCapacity);
    }

    public void put(short num, String id) {
        numberToCharacters[num] = id;
        charactersToNumber.put(id, num);
    }

    public short getNum(String id) {
        return charactersToNumber.get(id);
    }

    public String getId(short num) {
        return numberToCharacters[num];
    }

    public boolean containsId(String id){
        return charactersToNumber.contains(id);
    }
}
