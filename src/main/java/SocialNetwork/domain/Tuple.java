package SocialNetwork.domain;

import java.util.Objects;


/**
 * Define a Tuple o generic type entities
 * @param <E1> - tuple first entity type
 * @param <E2> - tuple second entity type
 */
public class Tuple<E1, E2> {
    private E1 e1;
    private E2 e2;

    /**
     * creates a Tuple
     * @param e1 - E1
     * @param e2 - E2
     */
    public Tuple(E1 e1, E2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    /**
     * finds the left element of the tuple
     * @return
     *      E1 the left element
     */
    public E1 getLeft() {
        return e1;
    }

    /**
     * sets the left element of the tuple
     * @param e1 - E1 what the element will be set to
     */
    public void setLeft(E1 e1) {
        this.e1 = e1;
    }

    /**
     * finds the right element of the tuple
     * @return
     *      E2 the right element
     */
    public E2 getRight() {
        return e2;
    }

    /**
     * sets the right element of the tuple
     * @param e2 - E2 what the element will be set to
     */
    public void setRight(E2 e2) {
        this.e2 = e2;
    }

    /**
     * makes a string with the tuple's information
     * @return
     *      string with tuple's information
     */
    @Override
    public String toString() {
        return "" + e1 + "," + e2;

    }

    /**
     * finds if the tuple is equal to another object
     * @param obj - Object to be compared to
     * @return
     *      true if the two are equal
     *      false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!(obj instanceof Tuple))
            return false;
        return (this.e1.equals(((Tuple) obj).e1) && this.e2.equals(((Tuple) obj).e2));
    }

    /**
     * finds the hashcode of the tuple
     * @return
     *      int with the hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(e1, e2);
    }
}