import java.util.*;
/*leave this parking ticket class , added just to remove error,  */
class ParkingTicket{

}

/*this is the main singleton code */
class TicketRepository{
    private static TicketRepository instance;
    private HashMap<Integer,ParkingTicket> allTicket;
    private TicketRepository(){
        allTicket =new HashMap<>();
    }
    public static TicketRepository getInstance(){
        if(instance==null){
            instance = new TicketRepository();
        }
        return instance;
    }
    public void addTicket(Integer ticketId,ParkingTicket ticket){
        allTicket.put(ticketId,ticket);
    }
    public void removeTicket(Integer ticketId){
        allTicket.remove(ticketId);
    }
    public ParkingTicket getTicket(Integer ticketId){
        if(!allTicket.containsKey(ticketId)){
            return null;
        }
        return allTicket.get(ticketId);
    }
}
public class SingletonBetter{
  public static void main(String[] args) {
    
  }
}
