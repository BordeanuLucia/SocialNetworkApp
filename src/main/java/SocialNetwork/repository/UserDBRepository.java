package SocialNetwork.repository;

import SocialNetwork.domain.Entity;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.domain.validators.Validator;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UserDBRepository implements Repository<Long, Utilizator> {
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
        for(Utilizator user : this.findAll())
            if(user.getId() == id)
                return user;
        return null;
    }

    @Override
    public Iterable<Utilizator> findAll() {
        List<Utilizator> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String password = resultSet.getString("password");

                Utilizator utilizator = new Utilizator(firstName, lastName, password);
                utilizator.setId(id);
                users.add(utilizator);
            }
            users.sort(Comparator.comparing(Entity::getId));
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<Utilizator> save(Utilizator entity) {
        validator.validate(entity);
        for(Utilizator user : this.findAll())
            if(user.getId() == entity.getId())
                throw new RepoException("User already exists");
        String sqlCommand = "INSERT INTO users(id, first_name, last_name,password)VALUES (";
        sqlCommand+=entity.getId().toString() + ",'" + entity.getFirstName() + "','" + entity.getLastName() + "','" + entity.getPassword() + "')";
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
        boolean exists = false;
        for(Utilizator user : this.findAll())
            if (user.getId() == id) {
                exists = true;
                break;
            }
        if(!exists)
            throw new RepoException("User does not exist");

        String sqlCommand = "DELETE FROM users WHERE id=";
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

    @Override
    public Optional<Utilizator> update(Utilizator entity) {
        return Optional.empty();
    }
}

