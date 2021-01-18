package domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

//trebuie implementat sablonul observer pe page si event
public class Eveniment extends Entity<Long>{
    private LocalDate date;
    private LocalTime begins;
    private LocalTime ends;
    private String titlu;
    private String announcement = "";
    private Long distance;
    private Long idCreator;

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public Eveniment(LocalDate localDate, String titlu, LocalTime begins, LocalTime ends, Long idCreator) {
        this.date = localDate;
        this.titlu = titlu;
        this.begins = begins;
        this.ends = ends;
        this.idCreator = idCreator;
    }

    public Long getIdCreator() {
        return idCreator;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getBegins() { return begins; }

    public LocalTime getEnds() { return ends; }

    public String getTitlu() {
        return titlu;
    }

//    public void addParticipant(Long user){
//        participants.add(user);
//    }

//    public void removeParticipant(Utilizator user){
//        int i = -1;
//        for(Long u : participants){
//            i++;
//            if(u.equals(user)) {
//                participants.remove(i);
//                break;
//            }
//        }
//    }

    public void notifica(){
        //verificare daca e aproape de data specificata
        //daca da se notifica utilizatorii
        LocalDate now = LocalDate.now();
        Long difference = ChronoUnit.DAYS.between(now, date);
        if(difference.equals(0l)) {
            this.announcement = "Event takes place today";
        }
        if(difference.equals(1l)){
            this.announcement = "Event takes place tomorrow";
        }
        if(difference.equals(2l)){
            this.announcement = "Event takes place in two days";
        }
    }

    public void calculateDistance(){
        LocalDate now = LocalDate.now();
        Long difference = ChronoUnit.DAYS.between(now, date);
        distance = difference;
    }

    public String getDistance() {
        return "Event takes place in " + distance.toString() + " days";
    }

    @Override
    public String toString(){
        return "Event: " + this.titlu + "\n    Will take place in " + this.date.toString();
    }

}
