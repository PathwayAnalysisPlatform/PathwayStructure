package no.uib.model;

import gnu.trove.map.hash.TObjectByteHashMap;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Bidirectional map data structure from byte integers to byte arrays. It is
 * implemented with two structures for every direction (byte -> byte[] or
 * byte[] -> byte). The ids are not stored as strings but as byte[] or
 * ByteBuffer depending on the direction. From byte to byte array there is an
 * array of byte[]. Because the maximum number of elements will not change
 * during the program. It is initialised once and stays the same. It provides
 * fast access to any position to get the associated byte[]. From byte[] to byte
 * a HashMap from the trove library is used, using a ByteBuffer as a wrapper that
 * provides the consistent equals and hashcode methods to verify if a key is
 * already contained in the map.
 *
 *
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapByteToByteArray {

    //<byte, byte[]>   //There is a byte number labels, and I want to translate them into byte[].
    public byte[][] byteToArray;   //Start with a number, goes to that array position and gets the byte[] stored there
    private TObjectByteHashMap<ByteBuffer> arrayToByte;   //Starts with a byte[] and translates that to a byte

    public BiMapByteToByteArray() {
        byteToArray = new byte[60][];
        arrayToByte = new TObjectByteHashMap<>(60);
    }

    public BiMapByteToByteArray(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        byteToArray = new byte[numElements][];
        arrayToByte = new TObjectByteHashMap<>(initialCapacity);
    }

    /**
     * Stores the string in the mapping at the first empty position available.
     * It is stored not as a string, but as a byte array with a byte number as
     * key.
     *
     * @param id {String} The identifier that will be translated into bytes and
     * will be given a byte number
     * @throws java.io.UnsupportedEncodingException
     */
    public void put(String id) throws UnsupportedEncodingException {
        if (!this.containsId(id)) {
            byte[] arr = id.getBytes("US-ASCII");
            byteToArray[arrayToByte.size()] = arr;
            arrayToByte.put(ByteBuffer.wrap(arr), (byte) arrayToByte.size());
        }
    }

    /**
     * Stores the string in the mapping at the position indicated by the num
     * paramenter. It is stored not as a string, but as a byte array with a
     * byte number as key.
     *
     * @param num {byte} The number to be associated with that string.
     * @param id {String} The identifier that will be translated into bytes and
     * will be associated to the byte number parameter.
     * @throws java.io.UnsupportedEncodingException
     */
    public void put(byte num, String id) throws UnsupportedEncodingException {
        if (!this.containsId(id)) {
            byte[] arr = id.getBytes("US-ASCII");
            byteToArray[num] = arr;
            arrayToByte.put(ByteBuffer.wrap(arr), num);
        }
    }

    /**
     * Get the byte number associated to the id. The id is internaly stored as
     * a byte[].
     *
     * @param id {String} The id that you are looking for.
     * @return The byte number associated to the id.
     * @throws UnsupportedEncodingException Because of the convertion from
     * string to byte array.
     */
    public byte getByte(String id) throws UnsupportedEncodingException {
        return arrayToByte.get(ByteBuffer.wrap(id.getBytes("US-ASCII")));
    }

    /**
     * Get the byte[] associated to the byte number specified in the parameter.
     *
     * @param num {byte} The key of the register that you are looking for.
     * @return The byte array
     */
    public byte[] getByteArray(byte num) {
        return byteToArray[num];
    }

    public String getString(byte num) {
        return new String(byteToArray[num]);
    }

    /**
     * Verify if an Id is stored in the BiMap or not. It checks only in the
 HashMap since the contents in the byteToArray array is the same but with
 inverted keys and valus.
     *
     *
     * @param id {String} The id that you want to check if is stored or not.
     * @return True if the string is stored in the BiMap, and false if not.
     * @throws UnsupportedEncodingException Because of the convertion from
     * string to byte array.
     */
    public boolean containsId(String id) throws UnsupportedEncodingException {
        return arrayToByte.contains(ByteBuffer.wrap(id.getBytes("US-ASCII")));
    }

    public int size() {
        return arrayToByte.size();
    }
}
