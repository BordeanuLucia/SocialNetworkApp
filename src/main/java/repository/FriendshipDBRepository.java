package repository;


import domain.Prietenie;
import domain.Tuple;
import domain.validators.Validator;
import repository.paging.Page;
import repository.paging.PageImplementation;
import repository.paging.Pageable;
import repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Constants.DATE_TIME_FORMATTER_WITH_HOUR;

public class FriendshipDBRepository implements PagingRepository<Tuple<Long,Long>, Prietenie> {
    private String url;
    private String username;
    private String password;
    private Validator<Prietenie> validator;

    public FriendshipDBRepository(String url, String username, String password, Validator<Prietenie> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }
    @Override
    public Prietenie findOne(Tuple<Long,Long> id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships WHERE id1=" + id.getLeft().toString()
             + " AND id2=" + id.getRight().toString() + " OR id1=" + id.getRight().toString() + " AND id2=" + id.getLeft().toString());
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);

                Prietenie prietenie = new Prietenie();
                Tuple<Long, Long> tuple = new Tuple<>(id1, id2);
                prietenie.setId(tuple);
                prietenie.setDate(dateTime);
                return prietenie;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<Prietenie> findAll(Pageable pageable) {
        List<Prietenie> friendships = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships ORDER BY date LIMIT "
                     + pageable.getPageSize() + " OFFSET " + pageable.getPageSize() * pageable.getPageNumber());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);

                Prietenie prietenie = new Prietenie();
                Tuple<Long,Long> tuple = new Tuple<>(id1,id2);
                prietenie.setId(tuple);
                prietenie.setDate(dateTime);
                friendships.add(prietenie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, friendships.stream());
    }

    @Override
    public Iterable<Prietenie> findAll() {
        List<Prietenie> friendships = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships ORDER BY date");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);

                Prietenie prietenie = new Prietenie();
                Tuple<Long,Long> tuple = new Tuple<>(id1,id2);
                prietenie.setId(tuple);
                prietenie.setDate(dateTime);
                friendships.add(prietenie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
//        validator.validate(entity);
//        if(this.findOne(entity.getId()) != null )
//            throw new RepoException("Friendship already exists");
        String sqlCommand = "INSERT INTO friendships(id1, id2, date)VALUES (" + entity.getId().getLeft().toString() + ",";
        sqlCommand+= entity.getId().getRight().toString() + ",'" + entity.getDate().format(DATE_TIME_FORMATTER_WITH_HOUR) + "')";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(entity);
    }

    @Override
    public Optional<Prietenie> delete(Tuple<Long,Long> id) {
        Prietenie friendship = this.findOne(id);
        if(friendship == null)
            throw new RepoException("Friendship does not exist");

        String sqlCommand = "DELETE FROM friendships WHERE id1=";
        sqlCommand+=friendship.getId().getLeft().toString() + "AND id2=" + friendship.getId().getRight().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void deleteAllFriendships(Long id){
        String sqlCommand = "DELETE FROM friendships WHERE id1=" + id.toString() + " OR id2=" + id.toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        return Optional.empty();
    }

    public int numberOfFriends(Long id){
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM friendships WHERE id1="
                     + id.toString() + " OR id2=" + id.toString());
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            int number = resultSet.getInt(1);
            return number;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
