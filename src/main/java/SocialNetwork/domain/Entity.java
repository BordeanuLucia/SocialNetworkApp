package SocialNetwork.domain;

import java.io.Serializable;

public class Entity<ID> implements Serializable {

    private static final long serialVersionUID = 7331115341259248461L;
    private ID id;

    /**
     * gets the id of an entity
     * @return
     *      ID the id requested
     */
    public ID getId() {
        return id;
    }

    /**
     * sets the id
     * @param id - ID the id to be set to
     */
    public void setId(ID id) {
        this.id = id;
    }
}