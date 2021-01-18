package service;

import com.itextpdf.text.*;
import config.ApplicationContext;
import domain.Message;
import domain.MessageDTO;
import domain.Utilizator;
import repository.MessageDBRepository;
import repository.UserDBRepository;
import utils.Constants;
import utils.events.ChangeEventEntity;
import utils.events.ChangeEventType;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observable;
import utils.observer.Observer;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static utils.Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR;

public class MessageService implements Observable<MessageTaskChangeEvent> {
    private Long primIdMessage;
    private MessageDBRepository messagesRepo;
    private UserDBRepository usersRepo;
    private Utilizator currentUser;
    private List<Long> ids = new ArrayList<>();
    private List<Observer<MessageTaskChangeEvent>> observers = new ArrayList<>();

    public MessageService(MessageDBRepository messages, UserDBRepository users) {
        this.messagesRepo = messages;
        this.usersRepo = users;
        this.findIds();
        primIdMessage = findPrimId();
        this.currentUser = null;
    }

    public void setCurrentUser(Utilizator u) {
        this.currentUser = u;
    }

    private void findIds() {
        //TODO frunctie sa iau id-urile direct din BD daca merge deschiderea aplicatiei greu -------------------------------------------------------
        ids.clear();
        messagesRepo.findAll().forEach(x -> {
            ids.add(x.getId());
        });
    }

    /**
     * @return the first id that is not in messagesRepo
     */
    private Long findPrimId() {
        ids.sort(Comparator.comparing(Long::longValue));
        Long aux = 1L;
        for (Long id : ids) {
            if (id.equals(aux))
                aux++;
            else
                break;
        }
        return aux;
    }

    @Override
    public void addObserver(Observer<MessageTaskChangeEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<MessageTaskChangeEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(MessageTaskChangeEvent t) {
        observers.stream().forEach(x -> x.update(t));
    }

    public List<Message> findAll() {
        return StreamSupport.stream(messagesRepo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public Message findOne(Long id) {
        return messagesRepo.findOne(id);
    }

    /**
     * @param toString - string with users to witch the message is sent
     * @param text     - the message
     */
    public void addMessage(String toString, String text) {
        List<Long> to = new ArrayList<>();
        try {
            to = Arrays.stream(toString.split(" ")).map(Long::parseLong).collect(Collectors.toList());
        } catch (NumberFormatException n) {
            throw new ServiceException("All users must be numbers");
        }
        if (to.size() == 0)
            throw new ServiceException("To must have at least one user");
//        for (Long l : to)
//            if (usersRepo.findOne(l) == null)
//                throw new ServiceException("All tos must exist");
        Message message = new Message(currentUser.getId(), to, text, -1l);
        message.setId(primIdMessage);
        messagesRepo.save(message);
        ids.add(primIdMessage);
        primIdMessage = this.findPrimId();
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.MESSAGE, ChangeEventType.ADD));
    }

    /**
     * @param replyTo - the message id to witch is replied
     * @param text    - the message
     */
    public void addReply(Long replyTo, String text) {
        //TODO nu ar trebui sa mai avem nevoie sa cautam daca exista mesajul -------------------------------------------------------------------
        Message message = messagesRepo.findOne(replyTo);
        if (message == null)
            throw new ServiceException("Message does not exist");
        if (message.getUsersTo().contains(currentUser.getId())) {
            Message reply = new Message(currentUser.getId(), Arrays.asList(message.getUserFrom()), text, replyTo);
            reply.setId(primIdMessage);
            messagesRepo.save(reply);
            ids.add(primIdMessage);
            primIdMessage = this.findPrimId();
            notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.MESSAGE, ChangeEventType.ADD));
        } else {
            throw new ServiceException("The message for not for you");
        }
    }

    /**
     * useful when deleting an account
     * deletes the messages to or from the current user
     */
    //---------------------------------------------TODO-----------------------------------------------------------------
    public void deleteMessages() {
        messagesRepo.deleteMessagesRegardingAUser(currentUser.getId());
        notifyObservers(new MessageTaskChangeEvent(ChangeEventEntity.MESSAGE, ChangeEventType.DELETE));
        this.findIds();
        this.primIdMessage = findPrimId();
    }

    /**
     * @param id - user is with whom we see the conversation
     * @return string with the conversation
     */
    public List<Message> showConversation(Long id) {
//        Utilizator friend = usersRepo.findOne(id);
//        if (friend == null)
//            throw new ServiceException("User does not exist");
        return messagesRepo.conversation(currentUser.getId(), id);
    }

    public List<MessageDTO> conversation(Utilizator prieten) {
        List<MessageDTO> messages = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages where srs="
                     + currentUser.getId().toString() + " and dest=" + prieten.getId().toString() + " or dest="
                     + currentUser.getId().toString() + " and srs=" + prieten.getId().toString());
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long from = resultSet.getLong("srs");
                Long to = resultSet.getLong("dest");
                String text = resultSet.getString("text");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                Long reply = resultSet.getLong("reply");

                String replyText = "";
                if (reply != -1l)
                    replyText = this.findOne(reply).getMessage();
                if (from.equals(currentUser.getId()))
                    messages.add(new MessageDTO(id, currentUser, text, replyText, dateTime));
                else
                    messages.add(new MessageDTO(id, prieten, text, replyText,dateTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<Message> messagesBetweenDatesUser(LocalDate date1, LocalDate date2, Long id) {
        //TODO de facut dupa ce pune date in db ----------------------------------------------------------------------------------------------
        List<Message> list = this.showConversation(id).stream()
                .filter(x -> {
                    if ((x.getDate().toLocalDate().isAfter(date1) ||x.getDate().toLocalDate().equals(date1))
                            && (x.getDate().toLocalDate().isBefore(date2) || x.getDate().toLocalDate().equals(date2)))
                        return true;
                    return false;
                })
                .collect(Collectors.toList());
        return list;
    }

    public List<Message> messagesBetweenDates(LocalDate date1, LocalDate date2) {
        //TODO de facut dupa ce pune date in db ----------------------------------------------------------------------------------------------
        List<Message> list = this.findAll().stream()
                .filter(x -> {
                    if ((x.getDate().toLocalDate().isAfter(date1) || x.getDate().toLocalDate().equals(date1))
                            && (x.getDate().toLocalDate().isBefore(date2) || x.getDate().toLocalDate().equals(date2))
                            && x.getUsersTo().contains(currentUser.getId())) {
                        return true; }
                    return false;
                })
                .collect(Collectors.toList());
        return list;
    }

    private int page = -1;
    private int size = 7;

    public int countMessagesWithSomeone(Utilizator prieten) {
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) from messages where srs="
                     + currentUser.getId().toString() + " and dest=" + prieten.getId().toString() + " or dest="
                     + currentUser.getId().toString() + " and srs=" + prieten.getId().toString());
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            int number = resultSet.getInt(1);
            if (number % size == 0)
                return number / size;
            else
                return number / size + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<MessageDTO> getMessagesOnPage(int page, Utilizator prieten) {
        this.page = page;
        List<MessageDTO> messages = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url"), ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username"), ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword"));
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages where srs="
                     + currentUser.getId().toString() + " and dest=" + prieten.getId().toString() + " or dest="
                     + currentUser.getId().toString() + " and srs=" + prieten.getId().toString()
                     + "ORDER BY date DESC LIMIT " + size + " OFFSET " + size * page);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long from = resultSet.getLong("srs");
                Long to = resultSet.getLong("dest");
                String text = resultSet.getString("text");
                LocalDateTime dateTime = resultSet.getObject("date", LocalDateTime.class);
                Long reply = resultSet.getLong("reply");

                String replyText = "";
                if (reply != -1l)
                    replyText = this.findOne(reply).getMessage();
                if (from.equals(currentUser.getId()))
                    messages.add(new MessageDTO(id, currentUser, text, replyText,dateTime));
                else
                    messages.add(new MessageDTO(id, prieten, text, replyText,dateTime));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages.stream().sorted(Comparator.comparing(MessageDTO::getDate)).collect(Collectors.toList());
    }

    public List<MessageDTO> getMessagesOnNextPage(Utilizator prieten) {
        this.page = this.page + 1;
        return getMessagesOnPage(page, prieten);
    }

    public String getRaport2(LocalDate localDate1, LocalDate localDate2,Utilizator user){
        String textReport = "";
        List<Message> mesaje = this.messagesBetweenDatesUser(localDate1, localDate2, user.getId());
        if (mesaje.size() != 0) {
            textReport = textReport.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                    .concat(" ").concat(currentUser.toString()).concat(" a purtat urmatoarea discutie cu ").concat(user.toString()).concat(":\n");
            for (Message message : mesaje) {
                if (message.getUserFrom().equals(currentUser)) {
                    textReport = textReport.concat(message.getDate().format(Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR)).concat(": ").concat(currentUser.toString()).concat(": ").concat(message.getMessage());
                    if (message.getReply() != -1l)
                        textReport = textReport.concat(" reply to: ").concat(this.findOne(message.getReply()).getMessage());
                    textReport = textReport.concat("\n");
                } else {
                    textReport = textReport.concat(message.getDate().format(Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR)).concat(": ").concat(user.toString()).concat(": ").concat(message.getMessage());
                    if (message.getReply() != -1l)
                        textReport = textReport.concat(" reply to: ").concat(this.findOne(message.getReply()).getMessage());
                    textReport = textReport.concat("\n");
                }
            }
        } else {
            textReport = textReport.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                    .concat(" ").concat(currentUser.toString()).concat(" nu a discutat nimic cu ").concat(user.toString()).concat(".\n");
        }
        return textReport;
    }

    public void saveReport2(Document document, LocalDate l1, LocalDate l2, Utilizator user){
        try{
            List<Message> mesaje = this.messagesBetweenDatesUser(l1, l2, user.getId());
            document.open();
            Paragraph paragraph = new Paragraph();
            paragraph.setFont(FontFactory.getFont(FontFactory.TIMES, 30f));
            paragraph.add("Raport conversatie\n\n\n\n");
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            if(mesaje.size() != 0) {
                String textReport = "In perioada " + l1.toString() + " - " + l2.toString()
                        + " " + currentUser.toString() + " a purtat urmatoarea discutie cu " + user.getUsername() + " :\n";
                Paragraph p3 = new Paragraph();
                p3.setFont(FontFactory.getFont(FontFactory.TIMES, 19f));
                p3.add(textReport.toUpperCase());
                document.add(p3);
                String previous = mesaje.get(0).getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR);
                Paragraph p = new Paragraph();
                p.setFont(FontFactory.getFont(FontFactory.TIMES, 17f));
                p.add("\n" + previous);
                document.add(p);
                for (Message mesaj : mesaje) {
                    if(!mesaj.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR).equals(previous)){
                        previous = mesaj.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR);
                        Paragraph p2 = new Paragraph();
                        p2.setFont(FontFactory.getFont(FontFactory.TIMES, 17f));
                        p2.add("\n" + previous);
                        document.add(p2);
                    }
                    Paragraph p1 = new Paragraph();
                    p1.setFont(FontFactory.getFont(FontFactory.TIMES, 15f));
                    String txt = "";
                    if(mesaj.getUserFrom().equals(currentUser.getId()))
                        txt += currentUser.getUsername() + " - ";
                    else
                        txt += user.getUsername() + " - ";
                    txt += mesaj.getMessage();
                    if (mesaj.getReply() != -1l)
                        textReport = textReport.concat(" reply to: ").concat(this.findOne(mesaj.getReply()).getMessage());
                    txt += "\n";
                    p1.add(txt);
                    document.add(p1);
                }
            }else{
                String str = "In perioada " + l1.toString() + " - " + l2.toString() + " " + currentUser.toString()
                        + " nu a vorbit nimic cu " + user.getUsername();
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

