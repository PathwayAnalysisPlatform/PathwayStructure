package no.uib.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class AdjacentNeighbor implements Comparable<AdjacentNeighbor> {

    private short num;
    private byte type;

    /**
     * Creates an AdjacentNeighbour specifying the number of neighbour and
     * number of edge type.
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

    /**
     * Compates two adjacent neighbours for ordering.
     *
     * @param otherEdge
     * @return Return -1 if the other edge comes before than this in the
     * ordering. Return 1 if the other edge comes after. Returns 0 if the edges
     * are the same.
     */
    @Override
    public int compareTo(AdjacentNeighbor otherEdge) {
        if (this.num < otherEdge.num) {
            return -1;
        } else if (this.num > otherEdge.num) {
            return 1;
        } else {
            if (this.type < otherEdge.type) {
                return -1;
            } else if (this.type > otherEdge.type) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(Object otherEdge) {
        if (otherEdge instanceof AdjacentNeighbor) {
            AdjacentNeighbor n = (AdjacentNeighbor) otherEdge;
            if (this.num == n.num && this.type == n.type)
            {
                return true;
            }
        }
        return false;
    }
}
