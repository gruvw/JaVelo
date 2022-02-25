package ch.epfl.javelo.data;

/**
 * Set of OpenStreetMap attributes (record).
 *
 * @author Lucas Jung (324724)
 */
public record AttributeSet(long bits) {

    /**
     * @param bits binary vector representing the content of the set: bit at index b is 1 if and
     *        only if the attribute b is in the set
     * @throws IllegalArgumentException if a bit in <code>bits</code> is set to 1 but does not
     *         correspond to any OSM attribute
     */
    public AttributeSet {
        // TODO
    }

    /**
     * @param attributes list of attributes to put in the set
     * @return the set corresponding to the given attributes
     */
    public static AttributeSet of(Attribute... attributes) {
        // TODO
    }

    /**
     * @param attribute the checked attribute
     * @return true if this set contains the given attribute, false otherwise
     */
    public boolean contains(Attribute attribute) {
        // TODO
    }

    /**
     * @param that the other set to intersect with
     * @return true if the intersection of this set with the given one <code>that</code> is not
     *         empty, false otherwise
     */
    public boolean intersects(AttributeSet that) {
        return (this.bits & that.bits) != 0;
    }

    @Override
    public String toString() {
        // TODO
    }

}
