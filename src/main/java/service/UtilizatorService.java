package service;

import com.itextpdf.text.*;
import config.ApplicationContext;
import domain.FriendshipDTO;
import domain.Message;
import domain.Utilizator;
import repository.UserDBRepository;
import repository.paging.Page;
import repository.paging.Pageable;
import repository.paging.PageableImplementation;
import utils.MyBCrypt;
import utils.events.ChangeEventEntity;
import utils.events.ChangeEventType;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observable;
import utils.observer.Observer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static utils.Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR;

public class UtilizatorService implements Observable<MessageTaskChangeEvent> {
    private UserDBRepository repoUser;
    private Long primID;
    private Utilizator currentUser;
    private List<Observer<MessageTaskChangeEvent>> observers = new ArrayList<>();
    private List<Long> usedIds = new ArrayList<>();

    @Override
    public void addObserver(Observer<MessageTaskChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<MessageTaskChangeEvent> e) {/*observers.remove(e);*/}

    @Override
    public void notifyObservers(MessageTaskChangeEvent t) {
        observers.stream().forEach(x -> x.update(t));
    }

    /**
     * @param repoUser - repo for users based on id
     */
    public UtilizatorService(UserDBRepository repoUser) {
        this.repoUser = repoUser;
        this.findAllExistingUserIds();
        primID = findPrimID();
    }

    public Utilizator getCurrentUser() {
        return currentUser;
    }

    private void findAllExistingUserIds() {
        //TODO daca se deschide greu aplicatia pot modifica ca prim ID sa fie idMax din BD + 1---------------------------------------------
        usedIds.clear();
        repoUser.findAll().forEach(x -> usedIds.add(x.getId()));
    }

    /**
     * @return first id that is not associated with an user
     */
    private Long findPrimID() {
        usedIds.sort(Comparator.comparing(Long::longValue));
        Long aux = 1L;
        for (Long id : usedIds) {
            if (id.equals(aux))
                aux++;
            else
                break;
        }
        return aux;
    }

    /**
     * adda an user
     *
     * @param first - string
     * @param last  - string
     */
    public void addUtilizator(String first, String last, String userUsername, String password, String email, String urlPicture) {
        Utilizator user = repoUser.findUserByEmail(email);
        if (user == null) {
            Utilizator utilizator = new Utilizator(first, last, userUsername, MyBCrypt.hash(password), email, urlPicture);
            utilizator.setId(primID);
            repoUser.save(utilizator);
            usedIds.add(primID);
            primID = findPrimID();
            notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.USER, ChangeEventType.ADD));
        } else {
            throw new ServiceException("User already exists");
        }
    }

    /**
     * deletes an user
     */
    public void deleteUtilizator() {
        repoUser.delete(currentUser.getId());
        int poz = 0;
        for (Long l : usedIds) {
            if (l.equals(currentUser.getId()))
                break;
            else
                poz++;
        }
        if (poz < usedIds.size())
            usedIds.remove(poz);
        if (primID > currentUser.getId()) {
            primID = currentUser.getId();
        }
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.USER, ChangeEventType.DELETE));
    }

    /**
     * @return an iterable of users
     */
    public Iterable<Utilizator> getAll() {
        return repoUser.findAll();
    }

    /**
     * finds an user by its id
     *
     * @param id - long
     * @return the user with the specified id
     */
    public Utilizator findOne(Long id) {
        return repoUser.findOne(id);
    }

    public void update(String firstName, String lastName, String username, String email, String url){
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        currentUser.setUsername(username);
        currentUser.setUrl(url);
        repoUser.update(currentUser);
    }

    public Utilizator findUserByEmail(String email){ return repoUser.findUserByEmail(email); }

    public Utilizator logIn(String email, String password) {
        boolean exists = true;
        Utilizator foundUser = repoUser.findUserByEmail(email);
        if (foundUser == null)
            exists = false;
        if (exists) {
            if (MyBCrypt.verifyHash(password, foundUser.getPassword()))
                currentUser = foundUser;
            else
                exists = false;
        }
        if (!exists)
            throw new ServiceException("Invalid username and password");
        currentUser = foundUser;
        return currentUser;
    }

    public String getRandomPassword() {
        Random random = new Random();
        char[] word = new char[random.nextInt(8) + 6];
        for (int j = 0; j < word.length; j++) {
            word[j] = (char) ('a' + random.nextInt(26));
        }
        return new String(word);
    }

    public void updatePassword(String password, Utilizator user) {
        user.setPassword(MyBCrypt.hash(password));
        repoUser.updatePassword(user);
    }

    public List<Utilizator> getAllOrderedByUsername(){
        return repoUser.findAllByUsername();
    }

    private int size = 9;
    private int page = 0;

    public int getPage() { return page; }


    public List<Utilizator> getUsersOnPage(int page) {
        this.page = page;
        Pageable pageable = new PageableImplementation(page, this.size);
        Page<Utilizator> userPage = repoUser.findAll(pageable);
        return userPage.getContent().collect(Collectors.toList());
    }

    public List<Utilizator> getUsersOnPageByName(int page, String text){
        this.page = page;
        Pageable pageable = new PageableImplementation(page, this.size);
        Page<Utilizator> userPage = repoUser.findAllByName(pageable, text);
        return userPage.getContent().collect(Collectors.toList());
    }

    public List<Utilizator> getNextUsersOnPage() {
        this.page++;
        return getUsersOnPage(this.page);
    }

    public int numberOfPagesWithUsers(){
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            int number = resultSet.getInt(1);
            if(number % size == 0)
                return number/size;
            else
                return number/size + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getRaport1(LocalDate localDate1, LocalDate localDate2, List<FriendshipDTO> prietenii, List<Message> mesaje){
        String textReport = "";
        if (prietenii.size() != 0) {
            textReport = textReport.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                    .concat(" ").concat(currentUser.toString()).concat(" s-a imprietenit cu:\n");
            for (FriendshipDTO friendshipDTO : prietenii) {
                textReport = textReport.concat(friendshipDTO.getDate()).concat(" ").concat(friendshipDTO.getUsername()).concat("\n");
            }
        } else {
            textReport = textReport.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                    .concat(" ").concat(currentUser.toString()).concat(" nu s-a imprietenit cu nicio persoana.\n");
        }
        if (mesaje.size() != 0) {
            textReport = textReport.concat("\n").concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                    .concat(" ").concat(currentUser.toString()).concat(" a primit urmatoarele mesaje:\n");
            for (Message message : mesaje) {
                textReport = textReport.concat(message.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR))
                        .concat(" ").concat(repoUser.findOne(message.getUserFrom()).toString()).concat(": ")
                        .concat(message.getMessage()).concat("\n");
            }
        } else {
            textReport = textReport.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                    .concat(" ").concat(currentUser.toString()).concat(" nu a primit niciun mesaj.\n");
        }
        return textReport;
    }

    public static List<String> rememberMe(){
        List<String> list = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * from rememberme");) {
            ResultSet resultSet = statement.executeQuery();
            try {
                resultSet.next();
                String user = resultSet.getString("username");
                String password = resultSet.getString("password");
                if (!user.equals("")) {
                    list.add(user);
                    list.add(password);
                }
            }catch (SQLException e){}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void handleRememberMe(Boolean isCheked, String email, String parola){
        if (isCheked) {
            try {
                Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
                Statement statement = connection.createStatement();
                String sqlCommand1 = "DELETE from rememberme";
                statement.executeUpdate(sqlCommand1);
                String sqlCommand2 = "INSERT INTO rememberme(username, password) VALUES ('" + email + "','" + parola + "')";
                statement.executeUpdate(sqlCommand2);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
                Statement statement = connection.createStatement();
                String sqlCommand1 = "DELETE from rememberme";
                statement.executeUpdate(sqlCommand1);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveReport1(Document document, List<FriendshipDTO> prieteni, List<Message> mesaje,LocalDate l1, LocalDate l2){
        try {
            document.open();
            Paragraph paragraph = new Paragraph();
            paragraph.setFont(FontFactory.getFont(FontFactory.TIMES, 30f));
            paragraph.add("Raport prietenii/conversatii\n\n\n\n");
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            if(prieteni.size() != 0) {
                String textReport = "In perioada " + l1.toString() + " - " + l2.toString()
                        + " " + currentUser.toString() + " s-a imprietenit cu:\n";
                Paragraph p3 = new Paragraph();
                p3.setFont(FontFactory.getFont(FontFactory.TIMES, 19f));
                p3.add(textReport.toUpperCase());
                document.add(p3);
                String previous = prieteni.get(0).getDate();
                Paragraph p = new Paragraph();
                p.setFont(FontFactory.getFont(FontFactory.TIMES, 17f));
                p.add("\n" + previous);
                document.add(p);
                for (FriendshipDTO prietenie : prieteni) {
                    if(!prietenie.getDate().equals(previous)){
                        previous = prietenie.getDate();
                        Paragraph p2 = new Paragraph();
                        p2.setFont(FontFactory.getFont(FontFactory.TIMES, 17f));
                        p2.add("\n" + previous);
                        document.add(p2);
                    }
                    Paragraph p1 = new Paragraph();
                    p1.setFont(FontFactory.getFont(FontFactory.TIMES, 15f));
                    p1.add("    -    " + prietenie.getUsername());
                    document.add(p1);
                }
            }else{
                String str = "In perioada " + l1.toString() + " - " + l2.toString() + " " + currentUser.toString()
                        + " nu s-a imprietenit cu nicio persoana.";
                Paragraph p = new Paragraph();
                p.setFont(FontFactory.getFont(FontFactory.TIMES, 19f));
                p.add(str.toUpperCase());
                document.add(p);
            }

            if(mesaje.size() != 0){
                String str = "\n\nIn perioada " + l1.toString() + " - " + l2.toString() + " " + currentUser.toString()
                        + " a primit urmatoarele mesaje:\n";
                Paragraph p3 = new Paragraph();
                p3.setFont(FontFactory.getFont(FontFactory.TIMES, 19f));
                p3.add(str.toUpperCase());
                document.add(p3);
                String previous = mesaje.get(0).getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR);
                Paragraph p = new Paragraph();
                p.setFont(FontFactory.getFont(FontFactory.TIMES, 17f));
                p.add("\n" + previous);
                document.add(p);
                for(Message message : mesaje){
                    if(!message.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR).equals(previous)){
                        previous = message.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR);
                        Paragraph p2 = new Paragraph();
                        p2.setFont(FontFactory.getFont(FontFactory.TIMES, 17f));
                        p2.add("\n" + previous);
                        document.add(p2);
                    }
                    Paragraph p1 = new Paragraph();
                    p1.setFont(FontFactory.getFont(FontFactory.TIMES, 15f));
                    p1.add(repoUser.findOne(message.getUserFrom()).getUsername()
                            + ": " + message.getMessage());
                    document.add(p1);
                }
            }else{
                String str = "In perioada " + l1.toString() + " - " + l2.toString() + " " + currentUser.toString()
                        + " nu a primit niciun mesaj.\n\n";
                Paragraph p = new Paragraph();
                p.setFont(FontFactory.getFont(FontFactory.TIMES, 19f));
                p.add(str.toUpperCase());
                document.add(p);
            }
        }catch (DocumentException d){
            d.printStackTrace();
        }
    }

}