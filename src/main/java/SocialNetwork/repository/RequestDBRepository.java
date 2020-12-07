package SocialNetwork.repository;

import SocialNetwork.domain.FriendRequest;
import SocialNetwork.domain.Prietenie;
import SocialNetwork.domain.Status;
import SocialNetwork.domain.Tuple;
import SocialNetwork.domain.validators.Validator;

import java.sql.*;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RequestDBRepository implements Repository<Tuple<Long,Long>, FriendRequest> {
    private String url;
    private String username;
    private String password;
    private Validator<Prietenie> validator;

    public RequestDBRepository(String url, String username, String password, Validator<Prietenie> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }
    @Override
    public FriendRequest findOne(Tuple<Long,Long> id) {
        for(FriendRequest request : this.findAll())
            if(request.getId().equals(id))
                return request;
        return null;
    }

    @Override
    public Iterable<FriendRequest> findAll() {
        List<FriendRequest> requests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from requests");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                String date = resultSet.getString("date");
                String status = resultSet.getString("status");
                int number = resultSet.getInt("number");

                FriendRequest request = new FriendRequest();
                Tuple<Long,Long> tuple = new Tuple<>(id1,id2);
                request.setId(tuple);
                request.setDate(LocalDateTime.parse(date));
                request.setNumber(number);
                request.setStatus(Status.valueOf(status));
                requests.add(request);
            }
            return requests;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        validator.validate(entity);
        if(this.findOne(entity.getId()) != null)
            throw new RepoException("Request already exists");
        String sqlCommand = "INSERT INTO requests(id1, id2, date, status, number)VALUES (";
        sqlCommand+=entity.getId().getLeft().toString() + "," + entity.getId().getRight().toString() + ",'"
                + entity.getDate().toString() + "','" + entity.getStatus().toString() + "'," + entity.getNumber() + ")";
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
    public Optional<FriendRequest> delete(Tuple<Long,Long> id) {
        FriendRequest request = this.findOne(id);
        if(request == null)
            throw new RepoException("Request does not exist");

        String sqlCommand = "DELETE FROM requests WHERE id1=";
        sqlCommand+=request.getId().getLeft().toString() + "AND id2=" + request.getId().getRight().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
            return Optional.ofNullable(this.findOne(id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest entity) {
        FriendRequest request = this.findOne(entity.getId());
        Tuple<Long,Long> tuple = request.getId();
        String sqlCommand = "UPDATE requests SET status='";
        sqlCommand+=entity.getStatus().toString() + "', number=" + entity.getNumber() + " WHERE id1=" + tuple.getLeft().toString() + " AND id2="+tuple.getRight().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
