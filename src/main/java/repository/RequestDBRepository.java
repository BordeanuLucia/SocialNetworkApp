package repository;

import domain.FriendRequest;
import domain.Prietenie;
import domain.Status;
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

public class RequestDBRepository implements PagingRepository<Tuple<Long,Long>, FriendRequest> {
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
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from requests WHERE id1=" + id.getLeft().toString()
                     + " AND id2=" + id.getRight().toString());
             ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                String status = resultSet.getString("status");
                int number = resultSet.getInt("number");

                FriendRequest request = new FriendRequest();
                request.setId(id);
                request.setDate(dateTime);
                request.setNoOfRequestsSent(number);
                request.setStatus(Status.valueOf(status));
                return request;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<FriendRequest> findAll(Pageable pageable) {
        List<FriendRequest> requests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from requests ORDER BY date LIMIT " + pageable.getPageSize()
                     + " OFFSET " + pageable.getPageSize() * pageable.getPageNumber());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                String status = resultSet.getString("status");
                int number = resultSet.getInt("number");

                FriendRequest request = new FriendRequest();
                Tuple<Long,Long> tuple = new Tuple<>(id1,id2);
                request.setId(tuple);
                request.setDate(dateTime);
                request.setNoOfRequestsSent(number);
                request.setStatus(Status.valueOf(status));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, requests.stream());
    }

    @Override
    public Iterable<FriendRequest> findAll() {
        List<FriendRequest> requests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from requests ORDER BY date");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                String status = resultSet.getString("status");
                int number = resultSet.getInt("number");

                FriendRequest request = new FriendRequest();
                Tuple<Long,Long> tuple = new Tuple<>(id1,id2);
                request.setId(tuple);
                request.setDate(dateTime);
                request.setNoOfRequestsSent(number);
                request.setStatus(Status.valueOf(status));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    @Override
    public Optional<FriendRequest> save(FriendRequest entity) {
        validator.validate(entity);
        //TODO daca toata aplicatia ii ok se pot sterge astea 2 randuri -------------------------------------------------------------------
//        if(this.findOne(entity.getId()) != null)
//            throw new RepoException("Request already exists");
        String sqlCommand = "INSERT INTO requests(id1, id2, date, status, number)VALUES (";
        sqlCommand+=entity.getId().getLeft().toString() + "," + entity.getId().getRight().toString() + ",'"
                + entity.getDate().format(DATE_TIME_FORMATTER_WITH_HOUR) + "','" + entity.getStatus().toString() + "'," + entity.getNoOfRequestsSent() + ")";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(entity);
    }

    @Override
    public Optional<FriendRequest> delete(Tuple<Long,Long> id) {
        FriendRequest request = this.findOne(id);
        //TODO daca toata aplicatia ii ok se pot sterge astea 2 randuri -------------------------------------------------------------------
        if(request == null)
            throw new RepoException("Request does not exist");
        String sqlCommand = "DELETE FROM requests WHERE id1=";
        sqlCommand+=request.getId().getLeft().toString() + "AND id2=" + request.getId().getRight().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void deleteAllFriendshipRequests(Long id){
        String sqlCommand = "DELETE FROM requests WHERE id1=" + id.toString() + " OR id2=" + id.toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<FriendRequest> update(FriendRequest entity) {
        //TODO nu ar trebui sa avem nevoie de asta ----------------------------------------------------------------------------------------------------------------------
        FriendRequest request = this.findOne(entity.getId());
        if(request == null)
            throw new RepoException("Request does not exist");
        Tuple<Long,Long> tuple = request.getId();
        String sqlCommand = "UPDATE requests SET status='";
        sqlCommand+=entity.getStatus().toString() + "', number=" + entity.getNoOfRequestsSent()
                + " WHERE id1=" + tuple.getLeft().toString() + " AND id2="+tuple.getRight().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void updateRequestStatus(Tuple<Long,Long> tuple, Status status){
        //TODO nu ar trebui sa avem nevoie de asta ----------------------------------------------------------------------------------------------------------------------
        FriendRequest request = this.findOne(tuple);
        if(request == null)
            throw new RepoException("Request does not exist");
        String sqlCommand = "UPDATE requests SET status='" + status.toString()
                + "' WHERE id1=" + tuple.getLeft().toString() + " AND id2="+tuple.getRight().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
