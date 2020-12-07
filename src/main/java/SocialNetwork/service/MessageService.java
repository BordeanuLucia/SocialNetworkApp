package SocialNetwork.service;

import SocialNetwork.domain.Message;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.repository.Repository;
import utils.events.ChangeEventType;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observable;
import utils.observer.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageService implements Observable<MessageTaskChangeEvent> {
    private Long primIdMessage;
    Repository<Long, Message> messagesRepo;
    Repository<Long, Utilizator> usersRepo;
    Utilizator currentUser;
    private List<Observer<MessageTaskChangeEvent>> observers=new ArrayList<>();

    public void setCurrentUser(Long id) {
        this.currentUser = usersRepo.findOne(id);
    }

    public MessageService(Repository<Long, Message> messages, Repository<Long, Utilizator> users) {
        this.messagesRepo = messages;
        this.usersRepo = users;
        primIdMessage = findPrimId();
        this.currentUser = null;
    }

    /**
     *
     * @return the first id that is not in messagesRepo
     */
    private Long findPrimId(){
        List<Message> list = new ArrayList<>();
        messagesRepo.findAll().forEach(list::add);
        list.sort(Comparator.comparing(Message::getId));
        Long aux = 1L;
        for(Message m : list){
            if(m.getId().equals(aux))
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
        observers.stream().forEach(x->x.update(t));
    }

    /**
     *
     * @param toString - string with users to witch the message is sent
     * @param text - the message
     */
    public void addMessage(String toString, String text){
        List<Long> to;
        try{
            to = Arrays.stream(toString.split(" ")).map(Long::parseLong).collect(Collectors.toList());
        }catch (NumberFormatException n){
            throw new ServiceException("All users must be numbers");
        }
        if(to.size() == 0)
            throw new ServiceException("To must have at least one user");
        List<Utilizator> users = new ArrayList<>();
        for(Long l : to)
            if(usersRepo.findOne(l) == null)
                throw new ServiceException("All tos must exist");
        to.forEach(x->users.add(usersRepo.findOne(x)));
        Message message = new Message(currentUser, users, text, -1l);
        message.setId(primIdMessage);
        messagesRepo.save(message);
        primIdMessage = this.findPrimId();
        notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, null));
    }

    /**
     *
     * @param to - the message id to witch is replied
     * @param text - the message
     */
    public void addReply(Long to, String text) {
        Message message = messagesRepo.findOne(to);
        if(message == null)
            throw new ServiceException("Message does not exist");
        if (message.getTo().contains(currentUser)) {
            Message reply = new Message(currentUser, Arrays.asList(message.getFrom()), text, to);
            reply.setId(primIdMessage);
            messagesRepo.save(reply);
            primIdMessage = this.findPrimId();
            notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, null));
        }
        else{
            throw new ServiceException("The message for not for you");
        }
    }

    /**
     * deletes the messages to or from the current user
     */
    public void deleteMessages(){
        messagesRepo.findAll().forEach(x->{
            if(x.getFrom().equals(currentUser))
                messagesRepo.delete(x.getId());
            else{
                if(x.getTo().contains(currentUser)) {
                    Message m = new Message(x.getFrom(), Arrays.asList(currentUser), "", 0l);
                    m.setId(x.getId());
                    messagesRepo.update(m);
                }
            }
        });
        notifyObservers(new MessageTaskChangeEvent(ChangeEventType.UPDATE, null));
    }

    /**
     *
     * @param id - user is with whom we see the conversation
     * @return string with the conversation
     */
    public List<Message> showConversation(Long id){
        Utilizator friend = usersRepo.findOne(id);
        if(friend == null)
            throw new ServiceException("User does not exist");
        List<Message> messages = StreamSupport.stream(messagesRepo.findAll().spliterator(), false)
                .filter(x->{if((x.getFrom().equals(currentUser) && x.getTo().contains(friend)) || (x.getFrom().equals(friend) && x.getTo().contains(currentUser))) return true; return false;})
                .collect(Collectors.toList());
        return messages;
    }

    public List<Message> findAll(){
        return StreamSupport.stream(messagesRepo.findAll().spliterator(),false).collect(Collectors.toList());
    }

    public Message findOne(Long id){
        return messagesRepo.findOne(id);
    }
}