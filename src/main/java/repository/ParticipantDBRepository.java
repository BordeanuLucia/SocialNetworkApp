package repository;

import domain.Participant;
import domain.Tuple;
import repository.paging.Page;
import repository.paging.PageImplementation;
import repository.paging.Pageable;
import repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParticipantDBRepository implements PagingRepository<Tuple<Long, Long>, Participant> {
    private String url;
    private String username;
    private String password;

    public ParticipantDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     *
     * @param id -the id of the entity to be returned
     *           id must not be null
     * @return the message with the specified id
     *          null if we do not have a message with the specified id
     */
    @Override
    public Participant findOne(Tuple<Long, Long> id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from participants WHERE event_id="
                     + id.getLeft().toString() + " AND user_id=" + id.getRight().toString());
             ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                Participant participant = new Participant(id.getLeft(), id.getRight());
                Boolean getNotifications = resultSet.getBoolean("get_notifications");
                Boolean sawNotifications = resultSet.getBoolean("saw_notifications");
                participant.setGetNotifications(getNotifications);
                participant.setSawNotifications(sawNotifications);
                return participant;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<Participant> findAll(Pageable pageable) {
        List<Participant> participants = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from participants LIMIT " + pageable.getPageSize()
                     + " OFFSET " + pageable.getPageSize() * pageable.getPageNumber());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long event = resultSet.getLong("event_id");
                Long user = resultSet.getLong("user_id");
                Boolean getNotifications = resultSet.getBoolean("get_notifications");
                Boolean sawNotifications = resultSet.getBoolean("saw_notifications");
                Participant participant = new Participant(event, user);
                participant.setGetNotifications(getNotifications);
                participant.setSawNotifications(sawNotifications);
                participants.add(participant);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, participants.stream());
    }
    /**
     *
     * @return an iterable with all the messages
     */
    @Override
    public Iterable<Participant> findAll() {
        List<Participant> participants = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from participants");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long event = resultSet.getLong("event_id");
                Long user = resultSet.getLong("user_id");
                Boolean getNotifications = resultSet.getBoolean("get_notifications");
                Boolean sawNotifications = resultSet.getBoolean("saw_notifications");

                Participant participant = new Participant(event, user);
                participant.setGetNotifications(getNotifications);
                participant.setSawNotifications(sawNotifications);
                participants.add(participant);
            }
            return participants;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    /**
     *
     * @param entity
     *         entity must be not null
     * @return null if the entity was saved
     *          the entity if it already exists
     */
    @Override
    public Optional<Participant> save(Participant entity) {
        String sqlCommand = "INSERT INTO participants(event_id,user_id,get_notifications,saw_notifications)VALUES (";
        sqlCommand += entity.getId().getLeft().toString()  + "," + entity.getId().getRight().toString() + ","
                + entity.isGetNotifications() + "," + entity.isSawNotifications() + ")";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(entity);
    }

    /**
     *
     * @param id
     *      id must be not null
     * @return null if a message with id does not exist
     *          the deleted message otherwise
     */
    @Override
    public Optional<Participant> delete(Tuple<Long,Long> id) {
//        if(this.findOne(id) == null)
//            throw new RepoException("Participant not subscribed to the event");
        String sqlCommand = "DELETE FROM participants WHERE event_id=";
        sqlCommand+=id.getLeft().toString() + " and user_id=" + id.getRight().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     *
     * @param entity
     *          entity must not be null
     * @return null if the entity does not exist
     *          the updates message otherwise
     */
    @Override
    public Optional<Participant> update(Participant entity) {
        return Optional.empty();
    }

    public Boolean updateGetNotifications(Long event, Long user, Boolean getNotifications){
        String sqlCommand = "UPDATE participants SET get_notifications=" + getNotifications + " WHERE event_id="
                + event.toString() + " AND user_id=" + user.toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public Boolean areNotificationsMuted(Long userId, Long eventId){
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from participants WHERE event_id="
                     + eventId.toString() + " AND user_id=" + userId.toString() + " AND get_notifications=false");
             ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean updateSawNotifications(Long event, Long user, Boolean sawNotifications){
        String sqlCommand = "UPDATE participants SET saw_notifications=" + sawNotifications + " WHERE event_id="
                + event.toString() + " AND user_id=" + user.toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void deleteAll(Long id){
        String sqlCommand = "DELETE FROM participants WHERE event_id=" + id.toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}