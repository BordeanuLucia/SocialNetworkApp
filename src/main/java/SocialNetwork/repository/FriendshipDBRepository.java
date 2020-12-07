package SocialNetwork.repository;


import SocialNetwork.domain.Prietenie;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.validators.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FriendshipDBRepository implements Repository<Tuple<Long,Long>, Prietenie> {
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
        for(Prietenie prietenie : this.findAll())
            if(prietenie.getId().equals(id) || (prietenie.getId().getLeft() == id.getRight() && prietenie.getId().getRight() == id.getLeft()))
                return prietenie;
        return null;
    }

    @Override
    public Iterable<Prietenie> findAll() {
        Set<Prietenie> friendships = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                String date = resultSet.getString("date");

                Prietenie prietenie = new Prietenie();
                Tuple<Long,Long> tuple = new Tuple<>(id1,id2);
                prietenie.setId(tuple);
                prietenie.setDate(LocalDateTime.parse(date));
                friendships.add(prietenie);
            }
            return friendships;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        validator.validate(entity);
        if(this.findOne(entity.getId()) != null )
            throw new RepoException("Friendship already exists");
        String sqlCommand = "INSERT INTO friendships(id1, id2, date)VALUES (";
        sqlCommand+=entity.getId().getLeft().toString() + "," + entity.getId().getRight().toString() + ",'"
                + entity.getDate().toString() + "')";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
            return Optional.empty();
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
            //        statement.setBigDecimal(1, BigDecimal.valueOf(id));
            statement.executeUpdate();
            return Optional.ofNullable(this.findOne(id));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        return Optional.empty();
    }
}
