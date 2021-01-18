package service;

import config.ApplicationContext;
import domain.Eveniment;
import domain.Participant;
import domain.Tuple;
import domain.Utilizator;
import repository.EventDBRepository;
import repository.ParticipantDBRepository;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static utils.Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR;

public class EventService implements Observable<MessageTaskChangeEvent> {
    private Long primIdEvent;
    EventDBRepository eventsRepo;
    ParticipantDBRepository participantsRepo;
    Utilizator currentUser;
    private List<Long> ids = new ArrayList<>();
    private List<Observer<MessageTaskChangeEvent>> observers=new ArrayList<>();

    public void setCurrentUser(Utilizator user) {
        this.currentUser = user;
    }

    public Utilizator getCurrentUser() {
        return currentUser;
    }

    public EventService(EventDBRepository eventRepo, ParticipantDBRepository participantRepo) {
        this.eventsRepo = eventRepo;
        this.findIds();
        primIdEvent = findPrimId();
        this.currentUser = null;
        this.participantsRepo = participantRepo;
    }

    private void findIds(){
        eventsRepo.findAll().forEach(x->{ids.add(x.getId());});
    }
    /**
     *
     * @return the first id that is not in messagesRepo
     */
    private Long findPrimId(){
        ids.sort(Comparator.comparing(Long::longValue));
        Long aux = 1L;
        for(Long id : ids){
            if(id.equals(aux))
                aux++;
            else
                break;
        }
        return aux;
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

    public List<Eveniment> findAll(){
        return StreamSupport.stream(eventsRepo.findAll().spliterator(),false).collect(Collectors.toList());
    }

    public Eveniment findOne(Long id){
        return eventsRepo.findOne(id);
    }

    public Participant findOneParticipant(Long userId, Long eventId){ return participantsRepo.findOne(new Tuple<>(eventId, userId));}

    public void addEvent(LocalDate date, String titlu, LocalTime begins, LocalTime ends){
        Eveniment event = new Eveniment(date, titlu, begins, ends, currentUser.getId());
        event.setId(primIdEvent);
        eventsRepo.save(event);
        ids.add(primIdEvent);
        primIdEvent = findPrimId();
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.EVENT, ChangeEventType.ADD));
    }

    public void deleteEvent(Long id){
        eventsRepo.delete(id);
        participantsRepo.deleteAll(id);
        int poz = 0;
        for (Long l : ids) {
            if (l.equals(id))
                break;
            else
                poz++;
        }
        if (poz < ids.size())
            ids.remove(poz);
        if (primIdEvent > id) {
            primIdEvent = id;
        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.EVENT, ChangeEventType.DELETE));
    }

    public void addParticipant(Long event){
//        Participant participant = participantsRepo.findOne(new Tuple<>(event, currentUser.getId()));
//        if(participant != null)
//            throw new ServiceException("User already subscribed to the event");
        Participant newParticipant = new Participant(event,currentUser.getId());
        participantsRepo.save(newParticipant);
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.PARTICIPANT, ChangeEventType.ADD));
    }

    public void removeParticipant(Long event){
        participantsRepo.delete(new Tuple<>(event, currentUser.getId()));
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.PARTICIPANT, ChangeEventType.DELETE));
    }

    public boolean isSubscribed(Long event){
        return participantsRepo.findOne(new Tuple<>(event, currentUser.getId())) != null;
    }

    public void muteNotifications(Long eventId){
//        if(this.findOne(eventId) == null)
//            throw new ServiceException("You are not subscribed to the event");
        participantsRepo.updateGetNotifications(eventId, currentUser.getId(), false);
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.PARTICIPANT, ChangeEventType.UPDATE));
    }

    public void unmuteNotifications(Long eventId){
//        if(this.findOne(eventId) == null)
//            throw new ServiceException("You are not subscribed to the event");
        participantsRepo.updateGetNotifications(eventId, currentUser.getId(), true);
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.PARTICIPANT, ChangeEventType.UPDATE));
    }

    public Boolean areNotificationsMuted(Long eventId){
        return participantsRepo.areNotificationsMuted(currentUser.getId(), eventId);
    }

    public List<Eveniment> getEventsWithSeenNotifications(){
        //TODO ar merge un fel de inner join
        List<Eveniment> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM participants WHERE user_id="
                     + currentUser.getId() + " AND get_notifications=true AND saw_notifications=true");
             ResultSet resultSet = statement.executeQuery()) {
            while(resultSet.next()){
                Long id = resultSet.getLong("event_id");
                events.add(this.findOne(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public List<Eveniment> getEventsWithUnseenNotifications(){
        //TODO ar merge un fel de inner join
        List<Eveniment> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM participants WHERE user_id="
                     + currentUser.getId().toString() + " AND get_notifications=true AND saw_notifications=false");
             ResultSet resultSet = statement.executeQuery()) {
            while(resultSet.next()){
                Long id = resultSet.getLong("event_id");
                events.add(this.findOne(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public void setAllNotificationsSeen(){
        String sqlCommand = "UPDATE participants SET saw_notifications=true WHERE user_id=" + currentUser.getId().toString()
                + " AND get_notifications=true";
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteOutdatedEvents(){
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM events WHERE date < '"
                     + LocalDate.now().format(DATE_TIME_FORMATTER_WITHOUT_HOUR) + "'");
             ResultSet resultSet = statement.executeQuery()) {
            while(resultSet.next()){
                Long id = resultSet.getLong("id");
                eventsRepo.delete(id);
                String sqlCommand = "DELETE FROM participants WHERE event_id=" + id.toString();
                try (Connection connection1 = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
                     PreparedStatement statement1 = connection.prepareStatement(sqlCommand)){
                    statement1.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Eveniment> getCurrentUserEvents(){
        List<Eveniment> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM events WHERE creator=" + currentUser.getId().toString() + " ORDER BY date");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                LocalDate date = resultSet.getObject("date", LocalDate.class);
                String titlu = resultSet.getString("titlu");
                LocalTime begins = resultSet.getObject("begins", LocalTime.class);
                LocalTime ends = resultSet.getObject("ends", LocalTime.class);

                Eveniment event = new Eveniment(date, titlu, begins, ends, currentUser.getId());
                event.setId(id);
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    private int page = -1;
    private int size = 6;

    public int getSize() {
        return size;
    }

    public void setPageSize(int size) {
        this.size = size;
    }

    public int numberOfEvents(){
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM events");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            int number = resultSet.getInt(1);
            if(number % size == 0)
                return number/size;
            else
                return number/size + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Eveniment> getEventsOnPage(int page) {
        this.page=page;
        Pageable pageable = new PageableImplementation(page, this.size);
        Page<Eveniment> userPage = eventsRepo.findAll(pageable);
        return userPage.getContent().collect(Collectors.toList());
    }

    public List<Eveniment> getNextUsers() {
        this.page++;
        return getEventsOnPage(this.page);
    }

    public List<Eveniment> getGoingEventsOnPage(int page) {
        this.page=page;
        Pageable pageable = new PageableImplementation(page, this.size);
        Page<Eveniment> eventsPage = new PageImplementation<>(pageable, getGoingEventsPage(page).stream());
        return eventsPage.getContent().collect(Collectors.toList());
    }

    private List<Eveniment> getGoingEventsPage(int page){
        //TODO ar merge un inner join --------------------------------------------------------------------------------------------------
        List<Eveniment> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * from participants where user_id=" + currentUser.getId().toString() + " LIMIT " + this.size + " OFFSET " + this.size * page);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("event_id");
                events.add(eventsRepo.findOne(id));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events.stream().sorted(Comparator.comparing(Eveniment::getDate)).collect(Collectors.toList());
    }

}
