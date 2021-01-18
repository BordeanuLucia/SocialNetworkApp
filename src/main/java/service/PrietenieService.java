package service;

import config.ApplicationContext;
import domain.*;
import repository.FriendshipDBRepository;
import repository.RepoException;
import repository.RequestDBRepository;
import repository.UserDBRepository;
import repository.paging.Page;
import repository.paging.PageImplementation;
import repository.paging.Pageable;
import repository.paging.PageableImplementation;
import utils.events.ChangeEventEntity;
import utils.events.ChangeEventType;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observable;
import utils.observer.Observer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PrietenieService implements Observable<MessageTaskChangeEvent> {
    private FriendshipDBRepository repoFriendship;
    private UserDBRepository repoUser;
    private RequestDBRepository repoRequest;
    Utilizator currentUser = null;
    private List<Observer<MessageTaskChangeEvent>> observers = new ArrayList<>();

    /**
     * creates a friendship
     *
     * @param repoF - a repo for friendships based on their tuple
     * @param repoU - repo for users based on their id
     */
    public PrietenieService(FriendshipDBRepository repoF, UserDBRepository repoU, RequestDBRepository repoR) {
        this.repoFriendship = repoF;
        this.repoUser = repoU;
        this.repoRequest = repoR;
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
        observers.stream().forEach(x -> x.update(t));
    }

    public void setCurrentUser(Utilizator u) {
        this.currentUser = u;
    }

    public Iterable<Prietenie> getAllFriendships() {
        return repoFriendship.findAll();
    }

    public Iterable<FriendRequest> getAllRequests() {
        return repoRequest.findAll();
    }

    public FriendRequest findRequest(Long id1, Long id2) {
        return repoRequest.findOne(new Tuple<>(id1, id2));
    }

    public Prietenie findFriendship(Long id1, Long id2) {
        return repoFriendship.findOne(new Tuple<>(id1, id2));
    }

    /**
     * adds friendship
     *
     * @param id2 - long
     * @throws ServiceException if one or both users do not exist
     */
    public void addFriendshipRequest(Long id2) {
//        Long id1 = currentUser.getId();
//        Utilizator u2 = repoUser.findOne(id2);
//        if (u2 == null)
//            throw new ServiceException("Both users must exist");
//        if (repoFriendship.findOne(new Tuple<>(id1, id2)) != null)
//            throw new ServiceException("Friendship already exists");
        Tuple<Long, Long> tuple = new Tuple<>(currentUser.getId(), id2);
        Tuple<Long, Long> aux = new Tuple<>(id2, currentUser.getId());
        FriendRequest f = repoRequest.findOne(tuple);
        FriendRequest faux = repoRequest.findOne(aux);
        FriendRequest request = new FriendRequest();
        request.setId(tuple);
        if (f == null && (faux == null || faux.getStatus().equals(Status.REJECTED))) {
            repoRequest.save(request);
            notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.REQUEST, ChangeEventType.ADD));
        } else {
            if (faux != null) {
                if (faux.getStatus().equals(Status.PENDING))
                    throw new ServiceException("Friendship already sent to you");
                if (faux.getStatus().equals(Status.APPROVED))
                    throw new ServiceException("Friendship already exists");
            }
            if (f.getStatus().equals(Status.APPROVED))
                throw new ServiceException("Friendship already exists");
            if (f.getStatus().equals(Status.REJECTED)) {
                if (f.getNoOfRequestsSent() >= 2)
                    throw new ServiceException("Request already sent twice");
                request.setNoOfRequestsSent(f.getNoOfRequestsSent() + 1);
                repoRequest.update(request);
                notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.REQUEST, ChangeEventType.UPDATE));
            }
            if (f.getStatus().equals(Status.PENDING))
                throw new ServiceException("Friendship already sent");
        }
    }

    /**
     * deletes a friendship
     * usuful when unfriending
     *
     * @param id1 - long
     * @throws ServiceException if one or both users do not exist
     */
    public void deleteFriendship(Long id1) {
        Long id2 = currentUser.getId();
//        Utilizator u1 = repoUser.findOne(id1);
//        if (u1 == null)
//            throw new ServiceException("Both users must exist");
        Tuple<Long, Long> tuple = new Tuple<>(id1, id2);
        try {
            repoFriendship.delete(tuple);
        } catch (RepoException r) {
        }
        try {
            repoRequest.delete(tuple);
        } catch (RepoException r) {
        }
        try {
            repoRequest.delete(new Tuple<>(id2, id1));
        } catch (RepoException r) { }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.FRIENDSHIP, ChangeEventType.DELETE));
    }

    /**
     * deletes all friendships of current user
     * useful when deleting an account
     */
    public void deleteAllFriendships() {
        repoFriendship.deleteAllFriendships(currentUser.getId());
        repoRequest.deleteAllFriendshipRequests(currentUser.getId());
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.FRIENDSHIP, ChangeEventType.DELETE));
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.REQUEST, ChangeEventType.DELETE));
    }

    /**
     * deleting a request means changing it from pending to rejected
     */
    public void deleteRequest(Utilizator user) {
        try {
            repoRequest.updateRequestStatus(new Tuple<>(currentUser.getId(), user.getId()), Status.REJECTED);
        } catch (RepoException r) {
        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.REQUEST, ChangeEventType.UPDATE));
    }

    /**
     * @param id     - user to who's request we answer
     * @param status - the status of the request
     */
    public void respondFriendship(Long id, Status status) {
//        if (repoUser.findOne(id) == null)
//            throw new ServiceException("Friend does not exist");
        Tuple<Long, Long> tuple = new Tuple<>(id, currentUser.getId());
        FriendRequest request = repoRequest.findOne(tuple);
        if (request == null)
            throw new ServiceException("Request does not exist");
        if (request.getStatus().equals(Status.APPROVED) || request.getStatus().equals(Status.REJECTED))
            throw new ServiceException("Request already responded to");
        repoRequest.updateRequestStatus(request.getId(), status);
        if (status.equals(Status.APPROVED)) {
            Prietenie prietenie = new Prietenie();
            prietenie.setId(tuple);
            repoFriendship.save(prietenie);
            notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.FRIENDSHIP, ChangeEventType.ADD));
        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.REQUEST, ChangeEventType.UPDATE));
    }

    public List<Prietenie> friendsBetweenDates(LocalDate date1, LocalDate date2) {
        //TODO de modificat dupa ce facem Date in baza de date -------------------------------------------------------------------------
        List<Prietenie> prietenieList = StreamSupport.stream(this.getAllFriendships().spliterator(), false)
                .filter(x -> {
                    if ((x.getId().getLeft().equals(currentUser.getId()) || x.getId().getRight().equals(currentUser.getId()))
                            && (x.getDate().toLocalDate().isAfter(date1) || x.getDate().toLocalDate().equals(date1))
                            && (x.getDate().toLocalDate().isBefore(date2) || x.getDate().toLocalDate().equals(date2)))
                        return true;
                    return false;
                })
                .collect(Collectors.toList());
        return prietenieList;
    }

    private int page = -1;
    private int size = 7;

    public List<Prietenie> getFriendshipsOnPage(int page) {
        this.page = page;
        Pageable pageable = new PageableImplementation(page, this.size);
        Page<Prietenie> userPage = repoFriendship.findAll(pageable);
        return userPage.getContent().collect(Collectors.toList());
    }

    public List<Prietenie> getNextFriendships() {
        this.page++;
        return getFriendshipsOnPage(this.page);
    }


    public List<Utilizator> getUsersFriendsOnPage(int page) {
        this.page = page;
        Pageable pageable = new PageableImplementation(page, this.size);
        Page<Utilizator> withFriendsPage = new PageImplementation<>(pageable, getFriendsPage(page).stream());
        return withFriendsPage.getContent().collect(Collectors.toList());
    }


    private List<Utilizator> getFriendsPage(int page) {
        List<Utilizator> friends = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships where id1="
                     + currentUser.getId().toString() + " OR id2=" + currentUser.getId().toString() + " LIMIT 12"
                     + " OFFSET " + 12 * page);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");

                if (id1.equals(currentUser.getId()))
                    friends.add(repoUser.findOne(id2));
                if (id2.equals(currentUser.getId()))
                    friends.add(repoUser.findOne(id1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public int numberOfFriends() {
        return repoFriendship.numberOfFriends(currentUser.getId()) / size + 1;
    }
}


//
//    /**
//     *
//     * @param id - user id for whom we want his friends
//     * @return - all his friends
//     */
//    public List<Utilizator> allFriends(Long id){
//        List<Utilizator> list = StreamSupport.stream(repoFriendship.findAll().spliterator(),false)
//                .filter(x->{if(x.getId().getRight().equals(id) || x.getId().getLeft().equals(id)) return true; return false;})
//                .map(x->{if(x.getId().getRight().equals(id)) return repoUser.findOne(x.getId().getLeft());
//                            else return repoUser.findOne(x.getId().getRight());})
//                .collect(Collectors.toList());
//        return list;
//    }
//

//
//    /**
//     *
//     * @param id - user id for whom we want his friends
//     * @param month1 - the month from witch we want the friendships
//     * @return the specified friendships
//     */
//    public String friendshipMonth(Long id, String month1, int year){
//        String month = month1.toUpperCase();
//        List<String> months = Arrays.asList("JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER");
//        if(!months.contains(month))
//            throw new ServiceException("Month does not exist");
//        List<Prietenie> list = new ArrayList<>();
//        repoFriendship.findAll().forEach(x->{if(x.getId().getRight().equals(id) || x.getId().getLeft().equals(id) && x.getDate().getMonth().toString().equals(month) && x.getDate().getYear() == year) list.add(x) ;});
//        String rez = list.stream()
//                .map(x->{if(x.getId().getRight().equals(id)) return ""+repoUser.findOne(x.getId().getLeft()).getFirstName()+"|"+repoUser.findOne(x.getId().getLeft()).getLastName()+"|"+x.getDate().format(Constants.DATE_TIME_FORMATTER)+"\n";
//                else return ""+repoUser.findOne(x.getId().getRight()).getFirstName()+"|"+repoUser.findOne(x.getId().getRight()).getLastName()+"|"+x.getDate().toString()+"\n";})
//                .reduce("",(str,x)->str+x);
//        return rez;
//    }
//
//    public String showFriendRequests(){
//        String requests = "";
//        for(FriendRequest request : repoRequest.findAll()){
//            if(request.getStatus().equals(Status.PENDING) && request.getId().getRight() == currentUser.getId()) {
//                requests+="Friendship from " + repoUser.findOne(request.getId().getLeft()).toStringId() + "\n";
//            }
//        }
//        return requests;
//    }
//