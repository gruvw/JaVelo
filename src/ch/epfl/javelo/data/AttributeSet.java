package ch.epfl.javelo.data;

import java.util.StringJoiner;
import ch.epfl.javelo.Preconditions;

/**
 * Set of OpenStreetMap attributes. (record)
 *
 * @param bits binary vector representing the content of the set: bit at index b is 1 if and only if
 *             the attribute b is in the set
 *
 * @author Lucas Jung (324724)
 */
public record AttributeSet(long bits) {

    /**
     * Constructor for an attribute set.
     *
     * @throws IllegalArgumentException if a bit in {@code bits} is set to 1 but does not correspond
     *                                  to any OSM attribute
     */
    public AttributeSet {
        Preconditions.checkArgument(bits >> Attribute.COUNT == 0);
    }

    /**
     * Creates an attribute set with all the attributes in argument.
     *
     * @param attributes list of attributes to put in the set
     * @return the set corresponding to the given attributes
     */
    public static AttributeSet of(Attribute... attributes) {
        long bits = 0;
        for (Attribute attribute : attributes)
            bits |= 1L << attribute.ordinal();
        return new AttributeSet(bits);
    }

    /**
     * Checks if the given attribute is contained inside the attribute set.
     *
     * @param attribute the checked attribute
     * @return true if this set contains the given attribute, false otherwise
     */
    public boolean contains(Attribute attribute) {
        return (bits & (1L << attribute.ordinal())) != 0;
    }

    /**
     * Checks if the intersection between the current set and the given set {@code that} is not
     * empty.
     *
     * @param that the other set to intersect with
     * @return true if the intersection of this set with the given one is not empty, false otherwise
     */
    public boolean intersects(AttributeSet that) {
        return (this.bits & that.bits) != 0;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Attribute attribute : Attribute.values())
            if (contains(attribute))
                joiner.add(attribute.keyValue());
        return joiner.toString();
    }

}
