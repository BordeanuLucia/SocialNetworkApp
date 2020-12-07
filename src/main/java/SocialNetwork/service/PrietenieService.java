package SocialNetwork.service;

import SocialNetwork.domain.*;
import SocialNetwork.repository.RepoException;
import SocialNetwork.repository.Repository;
import utils.Constants;
import utils.events.ChangeEventType;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observable;
import utils.observer.Observer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PrietenieService implements Observable<MessageTaskChangeEvent> {
    private Repository<Tuple<Long,Long>, Prietenie> repoFriendship;
    private Repository<Long,Utilizator> repoUser;
    private Repository<Tuple<Long,Long>, FriendRequest> repoRequest;
    Utilizator currentUser;
    private List<Observer<MessageTaskChangeEvent>> observers=new ArrayList<>();

    /**
     * creates a friendship
     * @param repoF - a repo for friendships based on their tuple
     * @param repoU - repo for users based on their id
     */
    public PrietenieService(Repository<Tuple<Long, Long>, Prietenie> repoF, Repository<Long, Utilizator> repoU, Repository<Tuple<Long, Long>, FriendRequest> repoR) {
        this.repoFriendship = repoF;
        this.repoUser = repoU;
        this.repoRequest = repoR;
        currentUser = null;
    }

    @Override
    public void addObserver(Observer<MessageTaskChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<MessageTaskChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(MessageTaskChangeEvent t) {
        observers.stream().forEach(x->x.update(t));
    }


    public void setCurrentUser(Long id){
        this.currentUser = repoUser.findOne(id);
    }

    /**
     *
     * @param id - user id for whom we want his friends
     * @return - all his friends
     */
    public List<Utilizator> allFriends(Long id){
        List<Utilizator> list = StreamSupport.stream(repoFriendship.findAll().spliterator(),false)
                .filter(x->{if(x.getId().getRight().equals(id) || x.getId().getLeft().equals(id)) return true; return false;})
                .map(x->{if(x.getId().getRight().equals(id)) return repoUser.findOne(x.getId().getLeft());
                            else return repoUser.findOne(x.getId().getRight());})
                .collect(Collectors.toList());
        return list;
    }


    /**
     *
     * @param id - user id for whom we want his friends
     * @param month1 - the month from witch we want the friendships
     * @return the specified friendships
     */
    public String friendshipMonth(Long id, String month1, int year){
        String month = month1.toUpperCase();
        List<String> months = Arrays.asList("JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER");
        if(!months.contains(month))
            throw new ServiceException("Month does not exist");
        List<Prietenie> list = new ArrayList<>();
        repoFriendship.findAll().forEach(x->{if(x.getId().getRight().equals(id) || x.getId().getLeft().equals(id) && x.getDate().getMonth().toString().equals(month) && x.getDate().getYear() == year) list.add(x) ;});
        String rez = list.stream()
                .map(x->{if(x.getId().getRight().equals(id)) return ""+repoUser.findOne(x.getId().getLeft()).getFirstName()+"|"+repoUser.findOne(x.getId().getLeft()).getLastName()+"|"+x.getDate().format(Constants.DATE_TIME_FORMATTER)+"\n";
                else return ""+repoUser.findOne(x.getId().getRight()).getFirstName()+"|"+repoUser.findOne(x.getId().getRight()).getLastName()+"|"+x.getDate().toString()+"\n";})
                .reduce("",(str,x)->str+x);
        return rez;
    }

    /**
     * adds friendship
     * @param id2 - long
     * @throws ServiceException
     *      if one or both users do not exist
     */
    public void addFriendship(Long id2) {
        Long id1 = currentUser.getId();
        Utilizator u2 = repoUser.findOne(id2);
        if(u2 == null)
            throw new ServiceException("Both users must exist");
        if(repoFriendship.findOne(new Tuple<>(id1, id2)) != null)
            throw new ServiceException("Friendship already exists");
        FriendRequest request = new FriendRequest();
        Tuple<Long, Long> tuple = new Tuple<>(id1, id2);
        Tuple<Long,Long> aux = new Tuple<>(id2, id1);
        request.setId(tuple);
        FriendRequest f = repoRequest.findOne(tuple);
        FriendRequest faux = repoRequest.findOne(aux);
        if(f == null && (faux==null || faux.getStatus().equals(Status.REJECTED))) {
            repoRequest.save(request);
            notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, null));
        }
        else{
            if(faux!=null) {
                if (faux.getStatus().equals(Status.PENDING))
                    throw new ServiceException("Friendship already sent to you");
                if (faux.getStatus().equals(Status.APPROVED))
                    throw new ServiceException("Friendship already exists");
            }
            if(f.getStatus().equals(Status.APPROVED))
                throw new ServiceException("Friendship already exists");
            if(f.getStatus().equals(Status.REJECTED)){
                if(f.getNumber() >= 2)
                    throw new ServiceException("Request already sent twice");
                request.setNumber(f.getNumber() + 1);
                request.setStatus(Status.PENDING);
                repoRequest.update(request);
                notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, null));
            }
            if(f.getStatus().equals(Status.PENDING))
                throw new ServiceException("Friendship already sent");
        }
    }

    /**
     * deletes a friendship
     * @param id1 - long
     * @throws ServiceException
     *      if one or both users do not exist
     */
    public void deleteFriendship(Long id1){
        Long id2 = currentUser.getId();
        Utilizator u1 = repoUser.findOne(id1);
        if(u1 == null)
            throw new ServiceException("Both users must exist");
        Tuple<Long,Long> tuple = new Tuple<>(id1, id2);
        repoFriendship.delete(tuple);
        try{
            repoRequest.delete(tuple);
        }catch (RepoException r){

        }
        try{
            repoRequest.delete(new Tuple<>(id2,id1));
        }catch (RepoException r){

        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventType.DELETE, u1));
    }

    /**
     * deletes all friendships of a user
     * @param id - long
     */
    public void deleteAllFriendships(Long id){
        for(Prietenie prietenie : repoFriendship.findAll()){
            if(prietenie.getId().getRight().equals(id)) {
                repoFriendship.delete(new Tuple<>(prietenie.getId().getLeft(),id));
            }
            if(prietenie.getId().getLeft().equals(id)) {
                repoFriendship.delete(new Tuple<>(prietenie.getId().getRight(),id));
            }
        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventType.DELETE, null));
    }

    public void deleteAllRequests(Long id){
        for(FriendRequest request : repoRequest.findAll()){
            if(request.getId().getRight().equals(id)) {
                repoRequest.delete(new Tuple<>(request.getId().getLeft(),id));
            }
            if(request.getId().getLeft().equals(id)) {
                repoRequest.delete(new Tuple<>(request.getId().getRight(),id));
            }
        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventType.DELETE, null));
    }

    public void deleteRequest(Utilizator user){
        for(FriendRequest fr : repoRequest.findAll()){
            if(fr.getId().getLeft() == currentUser.getId() && fr.getId().getRight() == user.getId() && fr.getStatus().equals(Status.PENDING)){
                FriendRequest aux = fr;
                aux.setStatus(Status.REJECTED);
                repoRequest.update(aux);
                notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, null));
                break;
            }
        }
    }

    /**
     * finds the number of communities
     * @return
     *      the number of communities
     */
    public int noCommunities(){
        int nr = 0;

        HashMap<Long,Utilizator> entities = new HashMap<>();
        for(Utilizator user : repoUser.findAll())
            entities.putIfAbsent(user.getId(), user);
        for(Prietenie friendships : repoFriendship.findAll()) {
            Utilizator u1 = entities.get(friendships.getId().getLeft());
            Utilizator u2 = entities.get(friendships.getId().getRight());
            u1.addFriend(u2);
            u2.addFriend(u1);
        }

        HashMap<Long,Boolean> colors = new HashMap<>();
        for(Utilizator user : entities.values())
            colors.putIfAbsent(user.getId(),false);
        while(colors.containsValue(false)){
            nr++;
            Long id = 0L;
            for(Map.Entry<Long,Boolean> entry : colors.entrySet())
                if(entry.getValue() == false){
                    entry.setValue(true);
                    id = entry.getKey();
                    break;
                }
            bfs(colors, id, entities);
        }
        return nr;
    }

    /**
     * implements a type of bfs algorithm
     * @param colors - must be a map of long and boolean
     * @param id - long
     * @param entities - map of user's id and users
     */
    private void bfs(Map<Long,Boolean> colors, Long id, Map<Long,Utilizator> entities){
        Utilizator source = entities.get(id);
        Queue<Utilizator> queue = new LinkedList<>();
        List<Utilizator> used = new ArrayList<>();
        queue.add(source);
        while(!queue.isEmpty()){
            Utilizator aux = queue.remove();
            used.add(aux);
            for(Utilizator u : aux.getFriends())
                if(!used.contains(u)){
                    queue.add(u);
                    colors.put(u.getId(),true);
                }
        }
    }

    /**
     * finds how long is the most talkative community
     * @return
     *      the length of the most talkative community
     */
    public int talkativeCommunity(){
        int max = 0, nr;

        HashMap<Long,Utilizator> entities = new HashMap<>();
        for(Utilizator user : repoUser.findAll())
            entities.putIfAbsent(user.getId(), user);
        for(Prietenie friendships : repoFriendship.findAll()) {
            Utilizator u1 = entities.get(friendships.getId().getLeft());
            Utilizator u2 = entities.get(friendships.getId().getRight());
            u1.addFriend(u2);
            u2.addFriend(u1);
        }

        for(Utilizator user : entities.values()) {
            List<Utilizator> list = new ArrayList<>();
            list.add(user);
            nr = longestRide(user, list, 0);
            if (nr > max)
                max = nr;
        }
        return max+1;
    }

    /**
     * finds the longest road from a node(user)
     * @param user - Utilizator
     * @param list - list of Utilizator
     * @param max - int
     * @return
     *      the length of the longest road from the user
     */
    private int longestRide(Utilizator user, List<Utilizator> list, int max){
        boolean bool = false;
        for(Utilizator friend : user.getFriends()) {
            if (!list.contains(friend)) {
                bool = true;
                break;
            }
        }
        if(!bool)
            return 0;
        for(Utilizator friend : user.getFriends()){
            if(!list.contains(friend)){
                list.add(friend);
                int nr = 1 + longestRide(friend, list, 0);
                if(nr > max)
                    max = nr;
                list.remove(friend);
            }
        }
        return max;
    }

    public String showFriendRequests(){
        String requests = "";
        for(FriendRequest request : repoRequest.findAll()){
            if(request.getStatus().equals(Status.PENDING) && request.getId().getRight() == currentUser.getId()) {
                requests+="Friendship from " + repoUser.findOne(request.getId().getLeft()).toStringId() + "\n";
            }
        }
        return requests;
    }

    /**
     *
     * @param id - user to who's request we answer
     * @param status - the status of the request
     */
    public void respondFriendship(Long id, Status status){
        if(repoUser.findOne(id) == null)
            throw new ServiceException("Friend does not exist");
        Tuple<Long,Long> tuple = new Tuple<>(id,currentUser.getId());
        FriendRequest request = repoRequest.findOne(tuple);
        if(request == null)
            throw new ServiceException("Request does not exist");
        if(request.getStatus().equals(Status.APPROVED) || request.getStatus().equals(Status.REJECTED))
            throw new ServiceException("Request already responded to");
        request.setStatus(status);
        repoRequest.update(request);
        if(status.equals(Status.APPROVED)){
            Prietenie prietenie = new Prietenie();
            prietenie.setId(tuple);
            repoFriendship.save(prietenie);
        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, null));
    }

    /**
     * gets all the friendships
     * @return
     *      all the friendships
     */
    public Iterable<Prietenie> getAllFriendships(){ return repoFriendship.findAll(); }

    public Iterable<FriendRequest> getAllRequests(){ return repoRequest.findAll(); }
}