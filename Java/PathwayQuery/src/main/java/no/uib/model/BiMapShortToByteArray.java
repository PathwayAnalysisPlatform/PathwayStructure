package no.uib.model;

import gnu.trove.map.hash.TObjectShortHashMap;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Bidirectional map data structure from short integers to byte arrays. It is
 * implemented with two structures for every direction (short -> byte[] or
 * byte[] -> short). The ids are not stored as strings but as byte[] or
 * ByteBuffer depending on the direction. From short to byte array there is an
 * array of byte[]. Because the maximum number of elements will not change
 * during the program. It is initialised once and stays the same. It provides
 * fast access to any position to get the associated byte[]. From byte[] to short
 * a HashMap from the trove library is used, using a ByteBuffer as a wrapper that
 * provides the consistent equals and hashcode methods to verify if a key is
 * already contained in the map.
 *
 *
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapShortToByteArray {

    //<short, byte[]>   //There is a short number labels, and I want to translate them into byte[].
    public byte[][] shortToArray;   //Start with a number, goes to that array position and gets the byte[] stored there
    private TObjectShortHashMap<ByteBuffer> arrayToShort;   //Starts with a byte[] and translates that to a short

    public BiMapShortToByteArray() {
        shortToArray = new byte[21000][];
        arrayToShort = new TObjectShortHashMap<>(21000);
    }

    public BiMapShortToByteArray(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        shortToArray = new byte[numElements][];
        arrayToShort = new TObjectShortHashMap<>(initialCapacity);
    }

    /**
     * Stores the string in the mapping at the first empty position available.
     * It is stored not as a string, but as a byte array with a short number as
     * key.
     *
     * @param id {String} The identifier that will be translated into bytes and
     * will be given a short number
     * @throws java.io.UnsupportedEncodingException
     */
    public void put(String id) throws UnsupportedEncodingException {
        if (!this.containsId(id)) {
            byte[] arr = id.getBytes("US-ASCII");
            shortToArray[arrayToShort.size()] = arr;
            arrayToShort.put(ByteBuffer.wrap(arr), (short) arrayToShort.size());
        }
    }

    /**
     * Stores the string in the mapping at the position indicated by the num
     * paramenter. It is stored not as a string, but as a byte array with a
     * short number as key.
     *
     * @param num {short} The number to be associated with that string.
     * @param id {String} The identifier that will be translated into bytes and
     * will be associated to the short number parameter.
     * @throws java.io.UnsupportedEncodingException
     */
    public void put(short num, String id) throws UnsupportedEncodingException {
        if (!this.containsId(id)) {
            byte[] arr = id.getBytes("US-ASCII");
            shortToArray[num] = arr;
            arrayToShort.put(ByteBuffer.wrap(arr), num);
        }
    }

    /**
     * Get the short number associated to the id. The id is internaly stored as
     * a byte[].
     *
     * @param id {String} The id that you are looking for.
     * @return The short number associated to the id.
     * @throws UnsupportedEncodingException Because of the convertion from
     * string to byte array.
     */
    public short getShort(String id) throws UnsupportedEncodingException {
        return arrayToShort.get(ByteBuffer.wrap(id.getBytes("US-ASCII")));
    }

    /**
     * Get the byte[] associated to the short number specified in the parameter.
     *
     * @param num {short} The key of the register that you are looking for.
     * @return The byte array
     */
    public byte[] getByteArray(short num) {
        return shortToArray[num];
    }

    public String getString(short num) {
        return new String(shortToArray[num]);
    }

    /**
     * Verify if an Id is stored in the BiMap or not. It checks only in the
     * HashMap since the contents in the shortToArray array is the same but with
     * inverted keys and valus.
     *
     *
     * @param id {String} The id that you want to check if is stored or not.
     * @return True if the string is stored in the BiMap, and false if not.
     * @throws UnsupportedEncodingException Because of the convertion from
     * string to byte array.
     */
    public boolean containsId(String id) throws UnsupportedEncodingException {
        return arrayToShort.contains(ByteBuffer.wrap(id.getBytes("US-ASCII")));
    }

    public int size() {
        return arrayToShort.size();
    }
}
