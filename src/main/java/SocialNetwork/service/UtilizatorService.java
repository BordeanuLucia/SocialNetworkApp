package SocialNetwork.service;

import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.Repository;
import utils.events.ChangeEventType;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observable;
import utils.observer.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UtilizatorService implements Observable<MessageTaskChangeEvent> {
    private Repository<Long, Utilizator> repoUser;
    private Long primID;
    Utilizator currentUser;
    private List<Observer<MessageTaskChangeEvent>> observers=new ArrayList<>();

    @Override
    public void addObserver(Observer<MessageTaskChangeEvent> e) {
        observers.add(e);

    }

    @Override
    public void removeObserver(Observer<MessageTaskChangeEvent> e) {
        //observers.remove(e);
    }

    @Override
    public void notifyObservers(MessageTaskChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }
    /**
     *
     * @param repoUser - repo for users based on id
     */
    public UtilizatorService(Repository<Long, Utilizator> repoUser) {
        this.repoUser = repoUser;
        primID = findPrimID();
    }

    public Utilizator getCurrentUser(){
        return currentUser;
    }
    /**
     *
     * @return
     *      first id that is not associated with an user
     */
    private Long findPrimID(){
        Iterable<Utilizator> set = repoUser.findAll();
        Long aux = 1L;
        for(Utilizator u : set){
            if(u.getId().equals(aux))
                aux++;
            else
                break;
        }
        return aux;
    }

    /**
     * adda an user
     * @param first - string
     * @param last - string
     */
    public void addUtilizator(String first, String last, String password) {
        String username = "" + first + " " + last + "";
        Utilizator user = this.getUser(username,password);
        if(user == null) {
            Utilizator utilizator = new Utilizator(first, last, password);
            utilizator.setId(primID);
            repoUser.save(utilizator);
            primID = findPrimID();
            notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, utilizator));
        }
        else{
            throw new ServiceException("User already exists");
        }
    }

    /**
     *
     * @return
     *      an iterable of users
     */
    public Iterable<Utilizator> getAll(){
        return repoUser.findAll();
    }

    /**
     * finds all the users with a specific first and last name
     * @param name - string
     * @param lastName - string
     * @return
     *      a string with all the ids, first and last name for the specified strings
     */
    public String findByName(String name, String lastName){
        Iterable<Utilizator> list = this.getAll();
        String users = "";
        for(Utilizator u : list)
            if(u.getFirstName().equals(name) && u.getLastName().equals(lastName))
                users+=u.toStringId() + "\n";
        if(users.equals(""))
            throw new ServiceException("This name does not exist");
        return users;
    }

    /**
     * finds the ids of the users with a specific first and last name
     * @param name - string
     * @param lastName - string
     * @return
     *      a list with the ids of the proper users
     */
    public List<Long> findByNameIds(String name, String lastName) {
        Iterable<Utilizator> list = this.getAll();
        List<Long> ids = new ArrayList<>();
        for(Utilizator u : list){
            if(u.getFirstName().equals(name) && u.getLastName().equals(lastName))
                ids.add(u.getId());
        }
        return ids;
    }

    /**
     * deletes an user
     //     * @param id -  long
     //     * @param ids -  list of ids(long)
     */
    public void deleteUtilizator(){
        repoUser.delete(currentUser.getId());
        if(primID > currentUser.getId())
            primID = currentUser.getId();
        notifyObservers(new MessageTaskChangeEvent(ChangeEventType.DELETE, null));
    }

    /**
     * finds an user by its id
     * @param id - long
     * @return
     *      the user with the specified id
     */
    public Utilizator findOne(Long id){
        return repoUser.findOne(id);
    }

    public Utilizator getUser(String username, String password){
        String name, lastName;
        List<String> list = Arrays.stream(username.split(" ")).collect(Collectors.toList());
        if(list.size() < 2)
            throw new ServiceException("Invalid username");
        name = list.get(0);
        lastName = list.get(1);
        for (Utilizator x : repoUser.findAll()) {
            if (x.getFirstName().equals(name) && x.getLastName().equals(lastName) && x.getPassword().equals(password)) {
                return x;
            }
        }
        return null;
    }

    public Utilizator logIn(String username, String password){
        Utilizator user = this.getUser(username,password);
        if(user == null)
            throw new ServiceException("User does not exist");
        currentUser = user;
        return user;
    }

}