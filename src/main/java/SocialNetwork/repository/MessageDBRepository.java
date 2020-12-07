package SocialNetwork.repository;


import SocialNetwork.domain.Message;
import SocialNetwork.domain.Utilizator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDBRepository implements Repository<Long, Message> {
    private String url;
    private String username;
    private String password;
    Repository<Long,Utilizator> usersRepo;

    public MessageDBRepository(String url, String username, String password, Repository<Long,Utilizator> repo) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.usersRepo = repo;
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
        for(Message message : this.findAll())
            if(message.getId() == id)
                return message;
        return null;
    }

    /**
     *
     * @return an iterable with all the messages
     */
    @Override
    public Iterable<Message> findAll() {
        HashMap<Long, Message> messages = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long from = resultSet.getLong("srs");
                Long to = resultSet.getLong("dest");
                String text = resultSet.getString("text");
                String date = resultSet.getString("date");
                Long reply = resultSet.getLong("reply");

                if (!messages.containsKey(id)) {
                    List<Utilizator> l = new ArrayList<>();
                    l.add(usersRepo.findOne(to));
                    Message message = new Message(usersRepo.findOne(from), l, text, reply);
                    message.setId(id);
                    message.setDate(LocalDateTime.parse(date));
                    messages.putIfAbsent(id, message);
                } else {
                    Message m = messages.get(id);
                    Utilizator u = usersRepo.findOne(to);
                    m.addTo(u);
                }
            }
            List<Message> messages1 = new ArrayList<>();
            messages.forEach((key, value) -> messages1.add(value));
            messages1.sort(Comparator.comparing(Message::getDate));
            return messages1;
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
        for(Utilizator user : entity.getTo()) {
            String sqlCommand = "INSERT INTO messages(id,srs,dest,text,date,reply)VALUES (";
            sqlCommand += entity.getId().toString() + "," + entity.getFrom().getId().toString() + "," + user.getId().toString()
                    + ",'" + entity.getMessage() + "','" + entity.getDate().toString() + "'," + entity.getReply().toString() + ")";
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(sqlCommand);
                saved++;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(saved == entity.getTo().size())
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
        boolean exists = false;
        for(Message m : this.findAll())
            if (m.getId() == id) {
                exists = true;
                break;
            }
        if(!exists)
            throw new RepoException("Message does not exist");

        String sqlCommand = "DELETE FROM messages WHERE id=";
        sqlCommand+=id.toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            //        statement.setBigDecimal(1, BigDecimal.valueOf(id));
            statement.executeUpdate();
            return Optional.ofNullable(this.findOne(id));

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
        boolean exists = false;
        for(Message m : this.findAll())
            if (m.getId() == entity.getId() && m.getTo().contains(entity.getTo().get(0))) {
                exists = true;
                break;
            }
        if(!exists)
            throw new RepoException("Message does not exist");

        String sqlCommand = "DELETE FROM messages WHERE id=";
        sqlCommand+=entity.getId().toString() + " AND dest=" + entity.getTo().get(0).getId().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            //        statement.setBigDecimal(1, BigDecimal.valueOf(id));
            statement.executeUpdate();
            return Optional.ofNullable(entity);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
