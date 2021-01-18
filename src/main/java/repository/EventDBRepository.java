package repository;

import domain.Eveniment;
import domain.validators.Validator;
import repository.paging.Page;
import repository.paging.PageImplementation;
import repository.paging.Pageable;
import repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Constants.DATE_TIME_FORMATTER_HOUR;

public class EventDBRepository implements PagingRepository<Long, Eveniment> {
    private String url;
    private String username;
    private String password;
    private Validator<Eveniment> validator;

    public EventDBRepository(String url, String username, String password, Validator<Eveniment> v) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = v;
    }

    /**
     *
     * @param id -the id of the entity to be returned
     *           id must not be null
     * @return the message with the specified id
     *          null if we do not have a message with the specified id
     */
    @Override
    public Eveniment findOne(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from events WHERE id=" + id.toString());
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                LocalDate date = resultSet.getObject("date", LocalDate.class);
                String titlu = resultSet.getString("titlu");
                LocalTime begins = resultSet.getObject("begins", LocalTime.class);
                LocalTime ends = resultSet.getObject("ends", LocalTime.class);
                Long creator = resultSet.getLong("creator");

                Eveniment event = new Eveniment(date, titlu, begins, ends,creator);
                event.setId(id);
                return event;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<Eveniment> findAll(Pageable pageable) {
        List<Eveniment> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from events ORDER BY date,begins LIMIT "
                     + pageable.getPageSize() + " OFFSET " + pageable.getPageSize() * pageable.getPageNumber());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                LocalDate date = resultSet.getObject("date", LocalDate.class);
                String titlu = resultSet.getString("titlu");
                LocalTime begins = resultSet.getObject("begins", LocalTime.class);
                LocalTime ends = resultSet.getObject("ends", LocalTime.class);
                Long creator = resultSet.getLong("creator");

                Eveniment event = new Eveniment(date, titlu,begins, ends,creator);
                event.setId(id);
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, events.stream());
    }
    /**
     *
     * @return an iterable with all the messages
     */
    @Override
    public Iterable<Eveniment> findAll() {
        List<Eveniment> events = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from events ORDER BY date, begins");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                LocalDate date = resultSet.getObject("date", LocalDate.class);
                String titlu = resultSet.getString("titlu");
                LocalTime begins = resultSet.getObject("begins", LocalTime.class);
                LocalTime ends = resultSet.getObject("ends", LocalTime.class);
                Long creator = resultSet.getLong("creator");

                Eveniment event = new Eveniment(date, titlu, begins, ends,creator);
                event.setId(id);
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    /**
     *
     * @param entity
     *         entity must be not null
     * @return null if the entity was saved
     *          the entity if it already exists
     */
    @Override
    public Optional<Eveniment> save(Eveniment entity) {
        validator.validate(entity);
            String sqlCommand = "INSERT INTO events(id,date,titlu,begins,ends,creator)VALUES (";
            sqlCommand += entity.getId().toString()  + ",'" + entity.getDate().toString() + "','" + entity.getTitlu() + "','"
                    + entity.getBegins().format(DATE_TIME_FORMATTER_HOUR) + "','" + entity.getEnds().format(DATE_TIME_FORMATTER_HOUR) + "',"
                    + entity.getIdCreator().toString() + ")";
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
    public Optional<Eveniment> delete(Long id) {
//        if(this.findOne(id) == null)
//            throw new RepoException("Event does not exist");
        String sqlCommand = "DELETE FROM events WHERE id=" + id.toString();
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
    public Optional<Eveniment> update(Eveniment entity) {
        return null;
    }
}
