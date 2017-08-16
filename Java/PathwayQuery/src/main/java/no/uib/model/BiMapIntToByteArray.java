package no.uib.model;

import gnu.trove.map.hash.TObjectIntHashMap;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bidirectional map data structure from int integers to byte arrays. It is
 * implemented with two structures for every direction (int -> byte[] or
 * byte[] -> int). The ids are not stored as strings but as byte[] or
 * ByteBuffer depending on the direction. From int to byte array there is an
 * array of byte[]. Because the maximum number of elements will not change
 * during the program. It is initialised once and stays the same. It provides
 * fast access to any position to get the associated byte[]. From byte[] to int
 * a HashMap from the trove library is used, using a ByteBuffer as a wrapper that
 * provides the consistent equals and hashcode methods to verify if a key is
 * already contained in the map.
 *
 *
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class BiMapIntToByteArray {

    //<int, byte[]>   //There is a int number labels, and I want to translate them into byte[].
    public byte[][] intToArray;   //Start with a number, goes to that array position and gets the byte[] stored there
    private TObjectIntHashMap<ByteBuffer> arrayToInt;   //Starts with a byte[] and translates that to a int

    public BiMapIntToByteArray() {
        intToArray = new byte[21000][];
        arrayToInt = new TObjectIntHashMap<>(21000);
    }

    public BiMapIntToByteArray(int numElements) {

        // Calculate the initial capacity so that it never has to resize the ArrayList
        int initialCapacity = (int) (numElements * 1.4);

        intToArray = new byte[numElements][];
        arrayToInt = new TObjectIntHashMap<>(initialCapacity);
    }

    /**
     * Stores the string in the mapping at the first empty position available.
     * It is stored not as a string, but as a byte array with a int number as
     * key.
     *
     * @param id {String} The identifier that will be translated into bytes and
     * will be given a int number
     * @throws java.io.UnsupportedEncodingException
     */
    public void put(String id)  {
        try {
            if (!this.containsId(id)) {
                byte[] arr = id.getBytes("US-ASCII");
                
                intToArray[arrayToInt.size()] = arr;
                arrayToInt.put(ByteBuffer.wrap(arr), arrayToInt.size());
            }
        } 
//catch(IndexOutOfBoundsException ex){
//            System.out.println(id);
//        }
catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BiMapIntToByteArray.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Stores the string in the mapping at the position indicated by the num
     * paramenter. It is stored not as a string, but as a byte array with a
     * int number as key.
     *
     * @param num {int} The number to be associated with that string.
     * @param id {String} The identifier that will be translated into bytes and
     * will be associated to the int number parameter.
     * @throws java.io.UnsupportedEncodingException
     */
    public void put(int num, String id) throws UnsupportedEncodingException {
        if (!this.containsId(id)) {
            byte[] arr = id.getBytes("US-ASCII");
            intToArray[num] = arr;
            arrayToInt.put(ByteBuffer.wrap(arr), num);
        }
    }

    /**
     * Get the int number associated to the id. The id is internaly stored as
     * a byte[].
     *
     * @param id {String} The id that you are looking for.
     * @return The int number associated to the id.
     * @throws UnsupportedEncodingException Because of the convertion from
     * string to byte array.
     */
    public int getInt(String id) {
        try {
            return arrayToInt.get(ByteBuffer.wrap(id.getBytes("US-ASCII")));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BiMapIntToByteArray.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    /**
     * Get the byte[] associated to the int number specified in the parameter.
     *
     * @param num {int} The key of the register that you are looking for.
     * @return The byte array
     */
    public byte[] getByteArray(int num) {
        return intToArray[num];
    }

    public String getString(int num) {
        return new String(intToArray[num]);
    }

    /**
     * Verify if an Id is stored in the BiMap or not. It checks only in the
     * HashMap since the contents in the intToArray array is the same but with
     * inverted keys and values.
     *
     *
     * @param id {String} The id that you want to check if is stored or not.
     * @return True if the string is stored in the BiMap, and false if not.
     * @throws UnsupportedEncodingException Because of the conversion from
     * string to byte array.
     */
    public boolean containsId(String id) throws UnsupportedEncodingException {
        return arrayToInt.contains(ByteBuffer.wrap(id.getBytes("US-ASCII")));
    }

    public int size() {
        return arrayToInt.size();
    }
}
