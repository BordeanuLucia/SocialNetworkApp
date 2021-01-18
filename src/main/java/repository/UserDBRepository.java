package repository;

import domain.Utilizator;
import domain.validators.Validator;
import repository.paging.Page;
import repository.paging.PageImplementation;
import repository.paging.Pageable;
import repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDBRepository implements PagingRepository<Long, Utilizator> {
    private String url;
    private String username;
    private String password;
    private Validator<Utilizator> validator;

    public UserDBRepository(String url, String username, String password, Validator<Utilizator> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Utilizator findOne(Long id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users WHERE id=" + id.toString());
             ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String username = resultSet.getString("username");
                String urlPicture = resultSet.getString("url");

                Utilizator utilizator = new Utilizator(firstName, lastName,username, password, email, urlPicture);
                utilizator.setId(id);
                return utilizator;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<Utilizator> findAll(Pageable pageable) {
        List<Utilizator> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users ORDER BY id LIMIT "
                     + pageable.getPageSize() + " OFFSET " + pageable.getPageSize()* pageable.getPageNumber());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String username = resultSet.getString("username");
                String urlPicture = resultSet.getString("url");

                Utilizator utilizator = new Utilizator(firstName, lastName,username, password, email, urlPicture);
                utilizator.setId(id);
                users.add(utilizator);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, users.stream());
    }

    public Page<Utilizator> findAllByName(Pageable pageable, String text) {
        List<Utilizator> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users  WHERE username LIKE '" + text + "%'"
                     + " ORDER BY id LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getPageSize()* pageable.getPageNumber());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String username = resultSet.getString("username");
                String urlPicture = resultSet.getString("url");

                Utilizator utilizator = new Utilizator(firstName, lastName,username, password, email, urlPicture);
                utilizator.setId(id);
                users.add(utilizator);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PageImplementation<>(pageable, users.stream());
    }

    @Override
    public Iterable<Utilizator> findAll() {
        List<Utilizator> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String username = resultSet.getString("username");
                String urlPicture = resultSet.getString("url");

                Utilizator utilizator = new Utilizator(firstName, lastName,username, password, email, urlPicture);
                utilizator.setId(id);
                users.add(utilizator);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
//        validator.validate(entity);
        String sqlCommand = "INSERT INTO users(id, first_name, last_name,username, password, email, url)VALUES (";
        sqlCommand+=entity.getId().toString() + ",'" + entity.getFirstName() + "','" + entity.getLastName()
                + "','" + entity.getUsername() + "','" + entity.getPassword() + "','" + entity.getEmail()
                + "','" + entity.getUrl() + "')";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()){
            statement.executeUpdate(sqlCommand);
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(entity);
    }

    @Override
    public Optional<Utilizator> delete(Long id) {
//        if(this.findOne(id) == null)
//            throw new RepoException("User does not exist");
        String sqlCommand = "DELETE FROM users WHERE id=";
        sqlCommand+=id.toString();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sqlCommand)){
            statement.executeUpdate();
            return Optional.ofNullable(this.findOne(id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Utilizator> update(Utilizator entity) {
        //TODO aici ar trebui sa facem update la toate campurile ----------------------------------------------------------------------------
        String sqlCommand = "UPDATE users SET first_name='" + entity.getFirstName() + "', last_name='" + entity.getLastName()
                + "', username='" + entity.getUsername() + "', email='" + entity.getEmail() + "', url='" + entity.getUrl()
                + "' WHERE id=" + entity.getId().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
            return Optional.empty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    //TODO de schimbat update de mai sus cu astalalt in cod ------------------------------------------------------------------------
    public boolean updatePassword(Utilizator entity){
        String sqlCommand = "UPDATE users SET password='";
        sqlCommand+=entity.getPassword() + "' WHERE id=" + entity.getId().toString();
        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sqlCommand);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public Utilizator findUserByEmail(String email){
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users WHERE email='" + email + "'");
             ResultSet resultSet = statement.executeQuery()) {

            if(resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");
                String username = resultSet.getString("username");
                String urlPicture = resultSet.getString("url");

                Utilizator utilizator = new Utilizator(firstName, lastName,username, password, email, urlPicture);
                utilizator.setId(id);
                return utilizator;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Utilizator> findAllByUsername(){
        List<Utilizator> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users ORDER BY username");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                String username = resultSet.getString("username");
                String urlPicture = resultSet.getString("url");

                Utilizator utilizator = new Utilizator(firstName, lastName,username, password, email, urlPicture);
                utilizator.setId(id);
                users.add(utilizator);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;

    }
}

