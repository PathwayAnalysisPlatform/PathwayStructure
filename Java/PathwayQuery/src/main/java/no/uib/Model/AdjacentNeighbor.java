/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.Model;

/**
 *
 * @author Luis Francisco Hernández Sánchez
 */
public class AdjacentNeighbor {

    private short num;
    private byte type;

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
    
    public enum EdgeTypes{
        ComplexNeighbor {
            public String toString() {
                return "cn";
            }
        }
    }
}
