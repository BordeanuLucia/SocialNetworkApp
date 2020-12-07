package SocialNetwork.domain;

import utils.Constants;

import java.time.LocalDateTime;



public class Prietenie extends Entity<Tuple<Long,Long>> {

    LocalDateTime date;

    public Prietenie() {
        date = LocalDateTime.now();
    }

    /**
     * finds when the friendship was created
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * sets the date
     * @param d - LocalDateTime the date to be set to
     */
    public void setDate(LocalDateTime d){
        date = d;
    }

    /**
     * creates a string with the friendships information
     * @param u1 - Utilizator from the friendship
     * @param u2 - Utilizator from the friendship
     * @return
     *      String with friendship's information
     */
    public String toString(Utilizator u1, Utilizator u2){
        return "Friendship between " + u1.toString() + " and " + u2.toString() + " made in " + date.format(Constants.DATE_TIME_FORMATTER);
    }

    /**
     * finds whether two friendships are equal
     * @param o - Object to be compared to
     * @return
     *      true if the two are equal
     *      false otherwise
     */
    @Override
    public boolean equals(Object o){
        if(this == o)
            return true;
        if(!(o instanceof Prietenie))
            return false;
        Prietenie ot = (Prietenie) o;
        return (ot.getId().equals(this.getId()));
    }
}