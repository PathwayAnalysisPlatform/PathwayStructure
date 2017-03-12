package no.uib.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class AdjacentNeighbor {

    private short num;
    private byte type;

    /**
     * Creates an AdjacentNeighbour specifying the number of neighbour and number of edge type.
     * 
     * @param num {short} Number of the neighbour.
     * @param type {byte} Number of the type.
     */
    public AdjacentNeighbor(short num, byte type) {
        this.num = num;
        this.type = type;
    }

    public short getNum() {
        return this.num;
    }

    public byte getType() {
        return this.type;
    }
}
