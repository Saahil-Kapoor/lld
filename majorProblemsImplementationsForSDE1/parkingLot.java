//this code has rating 8 by chat gpt but still further can be improved
/*
by using private
by defining behaviour for car , bike and truck 

*/

import java.util.*;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;


enum VehicleType{
    BIG,
    SMALL,
    MEDIUM
}

enum SpotStatus{
    PARKED,
    FREE    
}

abstract class Vehicle{
    VehicleType type;
    String licenceNumber;
}
class Car extends Vehicle{

}
class Truck extends Vehicle{

}
class Bike extends Vehicle{

}
class ParkingSpots{
    int id;
    SpotStatus status;
    VehicleType type;
}

class ParkingFloor{
    String level;
    int id;
    TreeMap<Integer,ParkingSpots> spots;
    int noOfFreeSpots;
}

class IdGenerator{
    static int count = 0;
    public static int createId(){
        return count++;
    }
}

class ParkingTicket{
    int spotId;
    int id;
    LocalTime inTime;
    LocalTime outTime;
    VehicleType type;
    ParkingTicket(int spotId,VehicleType type){
        this.spotId = spotId;
        this.type = type;
        this.inTime = LocalTime.now();
        this.id = IdGenerator.createId();
    }
}


interface FeeCalculationStratergy{
    public int calcAmount(LocalTime inTime);
}
class NormalDayFeeCalculationStrategy implements FeeCalculationStratergy{   
    public int calcAmount(LocalTime inTime){
        LocalTime currTime = LocalTime.now();
        long diff = ChronoUnit.HOURS.between(inTime, currTime);
        long amount = diff*10;
        return (int)amount;
    }
}
interface ParkingStratergy{
    public ParkingTicket park(ArrayList<ParkingFloor> floors,VehicleType type);
}
interface PaymentStratergy{
    public boolean pay(int amount);
}
class UpiPaymentStrategy implements PaymentStratergy{
    public boolean pay(int amount){
        System.out.println("Amount payed, Thanks a lot , pls visit again");
        return true;
    }
}

class FirstAvailableParkingStratergy implements ParkingStratergy{
    public ParkingTicket park(ArrayList<ParkingFloor> floors,VehicleType type){
        TicketRepository ticketRepository = TicketRepository.getInstance();
        for(ParkingFloor floor:floors){
            if(floor.noOfFreeSpots > 0){
                for(int key:floor.spots.keySet()){
                    ParkingSpots spot = floor.spots.get(key);
                    if(spot.status == SpotStatus.FREE && spot.type == type){
                        floor.noOfFreeSpots--;
                        spot.status = SpotStatus.PARKED;
                        ParkingTicket ticket = new ParkingTicket(spot.id,type);
                        ticketRepository.addTicket(ticket.id, ticket);
                        return ticket;
                    }   
                }
            }
        }
        throw new NoParkingSpotException("There is no parking spot available");
    }
}

class NoParkingSpotException extends RuntimeException{
    public NoParkingSpotException(String message){
        super(message);
    }
}

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

class NoSuchTicketIDExists extends RuntimeException{
    NoSuchTicketIDExists(String message){
        super(message);
    }
}


class ParkingLotServices{
    int id;
    ArrayList<ParkingFloor> floors;
    ParkingStratergy parkingStratergy;
    TicketRepository ticketRepo = TicketRepository.getInstance();
    FeeCalculationStratergy feeCalculationStratergy;
    PaymentStratergy paymentStratergy;
    ParkingLotServices(ParkingStratergy parkingStratergy, FeeCalculationStratergy feeCalculationStratergy,PaymentStratergy paymentStratergy){
        this.parkingStratergy = parkingStratergy;
        this.feeCalculationStratergy = feeCalculationStratergy;
        this.paymentStratergy = paymentStratergy;
    }
    public void parkVehicle(Vehicle vehicle){
        parkingStratergy.park(floors, vehicle.type);
    }
    public void unparkVehicle(Integer ticketId){
        if(ticketRepo.getTicket(ticketId) == null){
            throw new NoSuchTicketIDExists("there is no such ticket id with such id");
        }
        ParkingTicket ticket = ticketRepo.getTicket(ticketId);
        for(ParkingFloor floor:floors){
            if(floor.spots.containsKey(ticket.spotId)){
                ParkingSpots spot = floor.spots.get(ticket.spotId);
                spot.status = SpotStatus.FREE;
                floor.noOfFreeSpots++;
                int payableAmount = feeCalculationStratergy.calcAmount(ticket.inTime);
                startPayment(payableAmount);
                break;
            }
        }
    }
    public void startPayment(int amount){
        System.out.println("Please pay the amount"+amount);
        paymentStratergy.pay(amount);

    }
    public int getAvailableVehicle(){
        int freeSpot = 0;
        for(ParkingFloor floor:floors){
            freeSpot+=floor.noOfFreeSpots;
        }
        return freeSpot;
    }
}

public class parkingLot {
    
}
