package repository;


import domain.Message;
import repository.paging.Page;
import repository.paging.PageImplementation;
import repository.paging.Pageable;
import repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDBRepository implements PagingRepository<Long, Message> {
    private String url;
    private String username;
    private String password;

    public MessageDBRepository(String url, String username, String password) {
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
    public Message findOne(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages WHERE id=" + id.toString());
             ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                Long from = resultSet.getLong("srs");
                Long to = resultSet.getLong("dest");
                String text = resultSet.getString("text");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                Long reply = resultSet.getLong("reply");
                List<Long> usersTo = new ArrayList<>();
                usersTo.add(to);
                Message message = new Message(from, usersTo, text, reply);
                message.setDate(dateTime);
                message.setId(id);

                while (resultSet.next()) {
                    to = resultSet.getLong("dest");
                    message.addTo(to);
                }
                return message;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<Message> findAll(Pageable pageable) {
        HashMap<Long, Message> messages = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages ORDER BY date LIMIT "
                     + pageable.getPageSize() + " OFFSET " + pageable.getPageSize() * pageable.getPageNumber());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long from = resultSet.getLong("srs");
                Long to = resultSet.getLong("dest");
                String text = resultSet.getString("text");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                Long reply = resultSet.getLong("reply");

                if (!messages.containsKey(id)) {
                    List<Long> l = new ArrayList<>();
                    l.add(to);
                    Message message = new Message(from, l, text, reply);
                    message.setId(id);
                    message.setDate(dateTime);
                    messages.putIfAbsent(id, message);
                } else {
                    Message m = messages.get(id);
                    m.addTo(to);
                    messages.put(id, m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, messages.values().stream());
    }
    /**
     *
     * @return an iterable with all the messages
     */
    @Override
    public Iterable<Message> findAll() {
       HashMap<Long, Message> messages = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages ORDER BY date");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long from = resultSet.getLong("srs");
                Long to = resultSet.getLong("dest");
                String text = resultSet.getString("text");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                Long reply = resultSet.getLong("reply");

                if (!messages.containsKey(id)) {
                    List<Long> l = new ArrayList<>();
                    l.add(to);
                    Message message = new Message(from, l, text, reply);
                    message.setId(id);
                    message.setDate(dateTime);
                    messages.putIfAbsent(id, message);
                } else {
                    Message m = messages.get(id);
                    m.addTo(to);
                    messages.put(id, m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages.values();
    }

    /**
     *
     * @param entity
     *         entity must be not null
     * @return null if the entity was saved
     *          the entity if it already exists
     */
    @Override
    public Optional<Message> save(Message entity) {
        int saved = 0;
        for(Long userTo : entity.getUsersTo()) {
            String sqlCommand = "INSERT INTO messages(id, srs, dest, text, date, reply)VALUES (";
            sqlCommand += entity.getId().toString() + "," + entity.getUserFrom().toString() + "," + userTo.toString()
                    + ",'" + entity.getMessage() + "','" + entity.getDate().toString() + "'," + entity.getReply().toString() + ")";
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlCommand);
                saved++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(saved == entity.getUsersTo().size())
            return Optional.empty();
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
    public Optional<Message> delete(Long id) {
//        Message message = this.findOne(id);
//        if (message == null)
//            throw new RepoException("Message does not exist");
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM messages WHERE id=" + id.toString())) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //stergem mesajele ce sunt replici ale mesajului sters
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("UPDATE messages SET reply=-1 WHERE reply="
                     + id.toString())) {
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
    public Optional<Message> update(Message entity) {
//        boolean exists = false;
//        Message message = this.findOne(entity.getId());
//        if(message != null)
//            if(message.getUsersTo().contains(entity.getUsersTo().get(0)))
//                exists = true;
//        if(!exists)
//            throw new RepoException("Message does not exist");

        String sqlCommand = "DELETE FROM messages WHERE id=";
        sqlCommand+=entity.getId().toString() + " AND dest=" + entity.getUsersTo().get(0).toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void deleteMessagesRegardingAUser(Long id){
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT FROM messages WHERE srs="
                     + id.toString() + " OR dest=" + id.toString() + " ORDER BY date");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long idM = resultSet.getLong("id");
                //nu facem aici delete pt ca avem nevoie sa se modifice si mesajele ce sunt replica la mesajele sterse
                this.delete(idM);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> conversation(Long id1, Long id2){
        List<Message> messages = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE srs=" + id1.toString() + " AND dest="
                     + id2.toString() +" OR srs=" + id2.toString() + " AND dest=" + id1.toString() + " ORDER BY date");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long from = resultSet.getLong("srs");
                Long to = resultSet.getLong("dest");
                String text = resultSet.getString("text");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                Long reply = resultSet.getLong("reply");

                Message m = new Message(from,Arrays.asList(to),text, reply);
                m.setDate(dateTime);
                m.setId(id);
                messages.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

}
