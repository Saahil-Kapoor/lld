// this code has rating of 8.5 from chat gpt , has concurrency and handles the locking mechanism
//took almost a day to write 
//though almost correct there are still somethings that need to be corrected
//instead of checking status of seat and then updating seatStatus causes race condition , so from automicRefernce there is .compareAndSet() method which can be sed
//singleton are not thread safe right now, will have to see that code 


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.time.*;
/*

state patter is also usefull creatingBooking , making Payment , cancelled
entities present
enum for language -hindi , english
enum for seatstatus - booked, held, available
enum for bookingStatus -booked, paymentPending, expired
enum for paymentStatus - INITIATED,SUCCESS, FAILED, REFUNDED

User{
-name
-id
-phoneNo
-Tickets
}

movie
-id
-name
-language
-duration

theatre
-id
-name
-location

Screen{
-screenId
-theatreId
-total Seating
-Seat Layout
}

Seats{
-id
-rowId
-colId
-screenId
}

Show{
-showId 
-movie
-screenId
-startTime
-endTime
-screenId
}

ShowSeat{
-showSeatId
-showid
-seatid
-seatStatus
}

Booking{
-id
-showSeats
-createdAt
-expiresaT
-amount
-paymentId
}

Ticket{
-id
-Show
-seats
-time
-amount
}

Payment {
  id
  bookingId
  userId
  amount
  method
  transactionId
  status: PaymentStatus
  createdAt
}


SinleTon pattern for movieRepo , TheatreRepo, TicketRepo,Screenrepo, Show Repo
//seat blocking mechanism

strategyPattern for payment
there can be  status

keyfunctionsImplementations
bookTicket
cancelTicket
ShowTicket
ListTheatre/shows/moveis
getSeatLayout
*/

class IdGenerator {
  private static AtomicInteger id = new AtomicInteger(0);

  public static int generateId() {
    return id.incrementAndGet();
  }
}

enum Language {
  HINDI,
  ENGLISH,
  TAMIL,
  TELUGU
}

enum BookingStatus {
  BOOKED,
  PAYMENT_PENDING,
  EXPIRED
}

enum PaymentStatus {
  INITIATED,
  SUCCESS,
  FAILED,
  REFUNDED
}

enum SeatStatus {
  BOOKED,
  HELD,
  AVAILABLE
}

class Theatre {
  int id;
  String location;
  String name;

  public Theatre(int id, String location, String name) {
    this.id = id;
    this.location = location;
    this.name = name;
  }
}

class Movie {
  int id;
  String name;
  String language;
  int duration;

  public Movie(int id, String name, String language, int duration) {
    this.id = id;
    this.name = name;
    this.language = language;
    this.duration = duration;
  }
}

class User {
  int id;
  String name;
  String phoneNo;

  public User(int id, String name, String phoneNo) {
    this.id = id;
    this.name = name;
    this.phoneNo = phoneNo;
  }
}

class Screen {
  int screenId;
  int theatreId;
  int totalSeating;
  Map<Integer, ArrayList<Integer>> seatLayout; // rowId, list of colId

  public Screen(int screenId, int theatreId, int totalSeating, Map<Integer, ArrayList<Integer>> seatLayout) {
    this.screenId = screenId;
    this.theatreId = theatreId;
    this.totalSeating = totalSeating;
    this.seatLayout = seatLayout;
  }
}

class Show {
  int showId;
  int movieId;
  int screenId;
  LocalDateTime startTime;
  LocalDateTime endTime;

  public Show(int showId, int movieId, int screenId, LocalDateTime startTime, LocalDateTime endTime) {
    this.showId = showId;
    this.movieId = movieId;
    this.screenId = screenId;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}

class Seat {
  int id;
  int rowId;
  int colId;
  int screenId;

  public Seat(int id, int rowId, int colId, int screenId) {
    this.id = id;
    this.rowId = rowId;
    this.colId = colId;
    this.screenId = screenId;
  }
}

class ShowSeat {
  int showSeatId;
  int showId;
  int seatId;
  AtomicReference<SeatStatus> seatStatus; // booked, held, available

  public ShowSeat(int showSeatId, int showId, int seatId, SeatStatus seatStatus) {
    this.showSeatId = showSeatId;
    this.showId = showId;
    this.seatId = seatId;
    this.seatStatus = new AtomicReference<>(seatStatus);
  }
}

class Booking {
  int id;
  List<ShowSeat> showSeats;
  LocalDateTime createdAt;
  LocalDateTime expiresAt;
  double amount;
  BookingStatus status;
  int userId;
  ReentrantLock lock = new ReentrantLock();

  public Booking(List<ShowSeat> showSeats, LocalDateTime createdAt, LocalDateTime expiresAt, double amount,
      int userId) {
    this.id = IdGenerator.generateId();
    this.showSeats = showSeats;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.amount = amount;
    this.status = BookingStatus.PAYMENT_PENDING;
    this.userId = userId;
  }
}

class Payment {
  int id;
  int bookingId;
  double amount;
  String method;
  String transactionId;
  PaymentStatus status; // INITIATED,SUCCESS, FAILED, REFUNDED
  LocalDateTime createdAt;

  public Payment(int id, int bookingId, double amount, String method, String transactionId,
      PaymentStatus status, LocalDateTime createdAt) {
    this.id = id;
    this.bookingId = bookingId;
    this.amount = amount;
    this.method = method;
    this.transactionId = transactionId;
    this.status = status;
    this.createdAt = createdAt;
  }
}

class Ticket {
  int id;
  List<Integer> seatsIds;
  LocalDateTime time;
  double amount;
  int userId;
  int showId;

  public Ticket(int id, List<Integer> seatsIds, LocalDateTime time, double amount, int userId, int showId) {
    this.id = id;
    this.seatsIds = seatsIds;
    this.time = time;
    this.amount = amount;
    this.userId = userId;
    this.showId = showId;
  }
}

class MovieRepository {
  private static MovieRepository instance = null;
  private ConcurrentHashMap<Integer, Movie> movieMap;

  private MovieRepository() {
    movieMap = new ConcurrentHashMap<>();
  }

  public static MovieRepository getInstance() {
    if (instance == null) {
      instance = new MovieRepository();
    }
    return instance;
  }

  public void addMovie(Movie movie) {
    movieMap.put(movie.id, movie);
  }

  public Movie getMovie(int id) {
    return movieMap.get(id);
  }
}

class TheatreRepository {
  private static TheatreRepository instance = null;
  private ConcurrentHashMap<Integer, Theatre> theatreMap;

  private TheatreRepository() {
    theatreMap = new ConcurrentHashMap<>();
  }

  public static TheatreRepository getInstance() {
    if (instance == null) {
      instance = new TheatreRepository();
    }
    return instance;
  }

  public void addTheatre(Theatre theatre) {
    theatreMap.put(theatre.id, theatre);
  }

  public Theatre getTheatre(int id) {
    return theatreMap.get(id);
  }
}

class BookingRepository {
  private static BookingRepository instance = null;
  private ConcurrentHashMap<Integer, Booking> bookingMap;

  private BookingRepository() {
    bookingMap = new ConcurrentHashMap<>();
  }

  public static BookingRepository getInstance() {
    if (instance == null) {
      instance = new BookingRepository();
    }
    return instance;
  }

  public void addBooking(Booking booking) {
    bookingMap.put(booking.id, booking);
  }

  public Booking getBooking(int id) {
    return bookingMap.get(id);
  }
}

class ShowSeatRepository {
  private static ShowSeatRepository instance = null;
  private ConcurrentHashMap<Integer, ShowSeat> showSeatMap;

  private ShowSeatRepository() {
    showSeatMap = new ConcurrentHashMap<>();
  }

  public static ShowSeatRepository getInstance() {
    if (instance == null) {
      instance = new ShowSeatRepository();
    }
    return instance;
  }

  public void addShowSeat(ShowSeat showSeat) {
    showSeatMap.put(showSeat.showSeatId, showSeat);
  }

  public ShowSeat getShowSeat(int id) {
    return showSeatMap.get(id);
  }

  public List<ShowSeat> getShowSeatsByShowId(int showId) {
    List<ShowSeat> result = new ArrayList<>();
    for (ShowSeat showSeat : showSeatMap.values()) {
      if (showSeat.showId == showId) {
        result.add(showSeat);
      }
    }
    return result;
  }
}

class PaymentRepository {
  private static PaymentRepository instance = null;
  private HashMap<Integer, Payment> paymentMap;

  private PaymentRepository() {
    paymentMap = new HashMap<>();
  }

  public static PaymentRepository getInstance() {
    if (instance == null) {
      instance = new PaymentRepository();
    }
    return instance;
  }

  public void addPayment(Payment payment) {
    paymentMap.put(payment.id, payment);
  }

  public Payment getPayment(int id) {
    return paymentMap.get(id);
  }
}

interface PaymentStrategy {
  public boolean pay(int bookingId, double amount);
}

class UpiPayment implements PaymentStrategy {
  public boolean pay(int bookingId, double amount) {
    System.out.println("Paid " + amount + " for bookingId " + bookingId + " using UPI");
    return true;
  }
}

class CardPayment implements PaymentStrategy {
  public boolean pay(int bookingId, double amount) {
    System.out.println("Paid " + amount + " for bookingId " + bookingId + " using Card");
    return true;
  }
}

class BookMyShowService {

  public Booking createBooking(int userId, int showId, List<Integer> seatIds, int amount) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusMinutes(15);
    List<ShowSeat> showSeats = ShowSeatRepository.getInstance().getShowSeatsByShowId(showId);
    for (Integer seatId : seatIds) {
      for (ShowSeat showSeat : showSeats) {
        if (showSeat.seatId == seatId) {
          if (showSeat.seatStatus.get() != SeatStatus.AVAILABLE) {
            throw new RuntimeException("Seat not available");
          }
        }
      }
    }
    List<ShowSeat> bookedSeats = new ArrayList<>();
    for (Integer seatId : seatIds) {
      for (ShowSeat showSeat : showSeats) {
        if (showSeat.seatId == seatId) {
          showSeat.seatStatus.set(SeatStatus.HELD);
          ShowSeatRepository.getInstance().addShowSeat(showSeat);
          bookedSeats.add(showSeat);
        }
      }
    }
    Booking booking = new Booking(bookedSeats, now, expiresAt, amount, showId);
    BookingRepository.getInstance().addBooking(booking);

    Instant expiresAtInstant = expiresAt
        .atZone(ZoneId.systemDefault())
        .toInstant();

    delayQueue.add(new ExpiringBooking(booking.id, expiresAtInstant));
    return booking;
  }

  public Payment makePayment(int bookingId, PaymentStrategy paymentStrategy) {
    LocalDateTime now = LocalDateTime.now();
    Booking booking = BookingRepository.getInstance().getBooking(bookingId);
    if (now.isAfter(booking.expiresAt)) {
      booking.lock.lock();
      booking.status = BookingStatus.EXPIRED;
      for (ShowSeat showSeat : booking.showSeats) {
        showSeat.seatStatus.set(SeatStatus.AVAILABLE);
        ShowSeatRepository.getInstance().addShowSeat(showSeat);
      }
      booking.lock.unlock();
      throw new RuntimeException("Booking Expired");
    } else if (paymentStrategy.pay(booking.id, booking.amount)) {
      booking.lock.lock();
      booking.status = BookingStatus.BOOKED;
      BookingRepository.getInstance().addBooking(booking);
      System.out.println("Payment Successful");
      Payment payment = new Payment(IdGenerator.generateId(), booking.id, booking.amount,
          "UPI/Card", "TXN", PaymentStatus.SUCCESS, now);
      PaymentRepository.getInstance().addPayment(payment);
      booking.lock.unlock();
      return payment;
    } else {
      throw new RuntimeException("Payment Failed");
    }
  }

  public Ticket generateTicket(Booking booking, Payment payment) {
    if (booking.status != BookingStatus.BOOKED || payment.status != PaymentStatus.SUCCESS) {
      throw new RuntimeException("Cannot generate ticket for unpaid booking");
    }
    ShowSeatRepository showSeatRepo = ShowSeatRepository.getInstance();
    for (ShowSeat showSeat : booking.showSeats) {
      showSeat.seatStatus.set(SeatStatus.BOOKED);
      showSeatRepo.addShowSeat(showSeat);
    }
    List<Integer> seatsIds = new ArrayList<>();
    for (ShowSeat showSeat : booking.showSeats) {
      seatsIds.add(showSeat.seatId);
    }
    Ticket ticket = new Ticket(IdGenerator.generateId(), seatsIds, LocalDateTime.now(), booking.amount, booking.userId,
        booking.showSeats.get(0).showId);
    System.out.println("Ticket Generated with id: " + ticket.id);
    return ticket;
  }

  public void cancelTicket(Ticket ticket) {
    ShowSeatRepository showSeatRepo = ShowSeatRepository.getInstance();
    for (ShowSeat showSeat : showSeatRepo.getShowSeatsByShowId(ticket.showId)) {
      for (Integer seatId : ticket.seatsIds) {
        if (showSeat.seatId == seatId) { // assuming all seats belong to same show
          showSeat.seatStatus.set(SeatStatus.AVAILABLE);
          showSeatRepo.addShowSeat(showSeat);
        }
      }
    }
  }

  // Now , this is the code for remvoing booking after expiration and releasing
  // seats
  // There is a simple methods to interate over bookings and check for expiration
  // which is ok , but not great
  // for great , make a special thread Queue which will get bookings with
  // experiation time automatically and process them

  // in java it is readily implemented using DelayQueue
  // the worker will only be able to pull from the queue when the booking expires

  private final DelayQueue<ExpiringBooking> delayQueue = new DelayQueue<>();
  private final ExecutorService expiryWorker = Executors.newSingleThreadExecutor();

  public BookMyShowService() {
    expiryWorker.submit(() -> {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          ExpiringBooking eb = delayQueue.take();
          expireBooking(eb.bookingId);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

  }

  public void expireBooking(Integer bookingId) {
    BookingRepository bookingRepository = BookingRepository.getInstance();
    Booking booking = bookingRepository.getBooking(bookingId);
    if (booking == null) {
      return;
    }
    LocalDateTime now = LocalDateTime.now();
    booking.lock.lock();
    if (booking.status == BookingStatus.PAYMENT_PENDING && now.isAfter(booking.expiresAt)) {
      booking.status = BookingStatus.EXPIRED;
      for (ShowSeat showSeat : booking.showSeats) {
        showSeat.seatStatus.set(SeatStatus.AVAILABLE);
        ShowSeatRepository.getInstance().addShowSeat(showSeat);
      }
      BookingRepository.getInstance().addBooking(booking);
    }
    booking.lock.unlock();
  }
}

class ExpiringBooking implements Delayed {
  public int bookingId;
  public long expiryEpochsMs;

  public ExpiringBooking(int bookingId, Instant expiryInstant) {
    this.bookingId = bookingId;
    this.expiryEpochsMs = expiryInstant.toEpochMilli();
  }

  public int compareTo(Delayed other) {
    return Long.compare(this.expiryEpochsMs, ((ExpiringBooking) other).expiryEpochsMs);
  }

  public long getDelay(TimeUnit unit) {
    long diff = expiryEpochsMs - System.currentTimeMillis();
    return unit.convert(diff, TimeUnit.MILLISECONDS);
  }
}

public class BookMyShow {
  public static void main(String[] args) {

  }
}
