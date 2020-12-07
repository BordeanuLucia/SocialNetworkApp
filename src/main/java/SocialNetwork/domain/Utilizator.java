package SocialNetwork.domain;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Utilizator extends Entity<Long> {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private List<Utilizator> friends;

    /**
     * creates an Utilizator based on two strings
     * @param firstName - string
     * @param lastName - string
     */
    public Utilizator(String firstName, String lastName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = firstName+" "+lastName;
        this.password = password;
        friends = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * finds the first name of an user
     * @return
     *      string containing the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * set the first name of a user
     * @param firstName - string with the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * finds the last name of an user
     * @return
     *      string containing the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * set the last name of a user
     * @param lastName - string with the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * gets the friends of a user
     * @return
     *      list of users with the friends
     */
    public List<Utilizator> getFriends() {
        return friends;
    }

    /**
     * add a user to the user's list of friends
     * @param user - Utilizator to be added
     */
    public void addFriend(Utilizator user) {
        if (!friends.contains(user))
            friends.add(user);
    }

    /**
     * removes a user from the user's list of friends
     * @param user - Utilizator to be removed
     */
    public void removeFriend(Utilizator user) {
        friends.remove(user);
    }

    /**
     * removed a friend with a certain id
     * @param id - long the id of the removed friend
     */
    public void removeFriendById(Long id){
        for(Utilizator user : friends){
            if(user.getId().equals(id)) {
                friends.remove(user);
                break;
            }
        }
    }
    /**
     * transforms a user in a string with its information
     * @return
     *      String with user's information
     */
    @Override
    public String toString() { return firstName + " " + lastName; }

    public String toStringId(){
        return this.getId() + " " + this.toString();
    }
    /**
     * finds if an object is equal to the user
     * @param o - Object to be compared to
     * @return
     *      true if the users are equals
     *      false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return this.getId().equals(that.getId());
    }

    /**
     * finds the hashcode of a user
     * @return
     *      int representing the hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());//(getFirstName(), getLastName());
    }
}