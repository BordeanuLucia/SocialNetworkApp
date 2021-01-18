package domain;


import java.util.Objects;

public class Utilizator extends Entity<Long> {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String url;


    /**
     * creates an Utilizator based on two strings
     * @param firstName - string
     * @param lastName - string
     */
    public Utilizator(String firstName, String lastName, String username, String password, String email, String url) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.url = url;
    }

    public String getEmail() { return email; }

    public String getUsername() { return username;  }

    public String getPassword() { return password;  }

    public void setPassword(String password){ this.password = password; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public void setUsername(String username) { this.username = username; }

    public void setEmail(String email) { this.email = email; }

    public void setUrl(String url) { this.url = url; }

    public String getUrl() { return url; }

    @Override
    public String toString() { return username; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}