package SocialNetwork.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private List<Utilizator> participants;
    private LocalDate date;
    private String titlu;

    public Event(LocalDate localDate, String titlu) {
        this.date = localDate;
        this.titlu = titlu;
        participants = new ArrayList<>();
    }

    public void addParticipant(Utilizator user){
        participants.add(user);
    }

    public void removeParticipant(Utilizator user){
        int i = -1;
        for(Utilizator u : participants){
            i++;
            if(u.equals(user)) {
                participants.remove(i);
                break;
            }
        }
    }
}
