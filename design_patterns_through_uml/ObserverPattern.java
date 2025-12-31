import java.util.*;
//subject is the one whose state changes
interface Subject{
    public void addObserver(Observer o);
    public void removeObserver(Observer o);
    public void notifyObserver();
}


//subject is the one which see the change
    
interface Observer{
    public void update(String news);
}

class NewsAgency implements Subject{
    private String news;
    private List<Observer> observers;
    public void setNews(String news){
        this.news = news;
        notifyObserver();
    }
    public void addObserver(Observer o){
        observers.add(o);
    }
    public void removeObserver(Observer o){
        observers.remove(o);
    }
    public void notifyObserver(){
        for(Observer o:observers){
            o.update(news);
        }
    }
}



class NewsChannel implements Observer{
    private String name;
    public NewsChannel(String name){
        this.name = name;
    }
    public void update(String news){
        System.out.println("news recieved "+name+news);
    }
}


public class ObserverPattern {
    public static void main(String[] args) {
        
    }
}
