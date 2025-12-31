//rating of this code si 8.5 
//concurrency can be improved more by changing the atmPresent class , i have given the correct code for that class below the Atmpresent class
//and singleton pattern can be improved to ensure it is thread safe

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
/*
entities
states pattern will be used
CrditState
DebitState

AccountsRepo
CardRepo


Account
-AccountId
-name
-balance
-accountStatus

Card
-CardId
-cardNumber
-cvHash
-pinHash
-CardStatus
-accountId
-expiryDate

CashDispenserChainOfResponsibility


functions to Implement
balanceEnquiry
cashWithdrawal
cashDeposit
secureManagement 
and proper cash Management(bank account should be using lockes to prevent race condition)


*/
enum AccountStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}

enum CardStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED
}

enum OperationType {
    BALANCE_ENQUIRY,
    CASH_WITHDRAWAL,
    CASH_DEPOSIT,
    EJECT_CARD;
}

class Account {
    String accountId;
    String name;
    double balance;
    AccountStatus accountStatus;
    ReentrantLock lock; // Active, Inactive, Locked

    // Constructors, Getters, Setters
    public Account(String accountId, String name, double balance, AccountStatus accountStatus) {
        this.accountId = accountId;
        this.name = name;
        this.balance = balance;
        this.accountStatus = accountStatus;
        this.lock = new ReentrantLock();
    }
}

class Card{
    String cardId;
    String cardNumber;
    String cvHash;
    String pinHash;
    CardStatus cardStatus; // Active, Inactive, Blocked
    String accountId;
    Date expiryDate;
    ReentrantLock lock;

    // Constructors, Getters, Setters
    public Card(String cardId, String cardNumber, String cvHash, String pinHash, CardStatus cardStatus, String accountId, Date expiryDate) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.cvHash = cvHash;
        this.pinHash = pinHash;
        this.cardStatus = cardStatus;
        this.accountId = accountId;
        this.expiryDate = expiryDate;
        this.lock = new ReentrantLock();
    }
}

class AccountsRepo {
    private ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private static AccountsRepo instance = null;
    private AccountsRepo() {}
    public static AccountsRepo getInstance() {
        if (instance == null) {
            instance = new AccountsRepo();
        }
        return instance;
    }
    public void addAccount(Account account) {
        accounts.put(account.accountId, account);
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }
}

class CardRepo {
    private ConcurrentHashMap<String, Card> cards = new ConcurrentHashMap<>();
    private static CardRepo instance = null;
    private CardRepo() {}
    public static CardRepo getInstance() {
        if (instance == null) {
            instance = new CardRepo();
        }
        return instance;
    }
    public void addCard(Card card) {
        cards.put(card.cardId, card);
    }

    public Card getCard(String cardId) {
        return cards.get(cardId);
    }
}

interface ATMState {
    void insertCard(String cardId,AtmContext context);
    void authenticateCard(String cardId, String pin, AtmContext context);
    void performOperation(OperationType taskType, double amount, AtmContext context);
    void ejectCard(AtmContext context);
}
class NoCardState implements ATMState {
    @Override
    public void insertCard(String cardId,AtmContext context) {
        System.out.println("Card " + cardId + " inserted.");
        context.setState(new CardInsertedState(cardId));
    }
    @Override
    public void authenticateCard(String cardId, String pin, AtmContext context) {
        System.out.println("No card inserted. Please insert a card first.");
    }

    @Override
    public void performOperation(OperationType taskType, double amount, AtmContext context) {
        System.out.println("No card inserted. Please insert a card first.");
    }

    @Override
    public void ejectCard(AtmContext context) {
        System.out.println("No card to eject.");
    }
}

class CardInsertedState implements ATMState {
    
    String cardId;
    CardInsertedState(String cardId){
        this.cardId = cardId;
    }
    @Override
    public void insertCard(String cardId,AtmContext context) {
        System.out.println("Card already inserted.");
    }

    @Override
    public void authenticateCard(String cardId, String pin, AtmContext context) {
        System.out.println("Authenticating card " + cardId + " with pin.");
        CardRepo cardRepo = CardRepo.getInstance();
        Card card = cardRepo.getCard(cardId);
        if (card != null && card.pinHash.equals(pin) && card.cardStatus == CardStatus.ACTIVE) {
            System.out.println("Card authenticated successfully.");
            context.setState(new AuthenticatedState(cardId));

        } else {
            System.out.println("Authentication failed.");
        }
    }

    @Override
    public void performOperation(OperationType taskType, double amount, AtmContext context) {
        System.out.println("Please authenticate the card first.");
    }

    @Override
    public void ejectCard(AtmContext context) {
        System.out.println("Ejecting card " + cardId);
        context.setState(new NoCardState());
    }
}

class AuthenticatedState implements ATMState {
    String cardId;
    AuthenticatedState(String cardId){
        this.cardId = cardId;
    }
    @Override
    public void insertCard(String cardId,AtmContext context) {
        System.out.println("Card already inserted.");
    }

    @Override
    public void authenticateCard(String cardId, String pin, AtmContext context) {
        System.out.println("Card already authenticated.");
    }

    @Override
    public void performOperation(OperationType taskType, double amount, AtmContext context) {
        System.out.println("Performing operation: " + taskType);
        if(taskType == OperationType.BALANCE_ENQUIRY){
            CardRepo cardRepo = CardRepo.getInstance();
            Card card = cardRepo.getCard(cardId);
            AccountsRepo accountsRepo = AccountsRepo.getInstance();
            Account account = accountsRepo.getAccount(card.accountId);
            account.lock.lock();
            System.out.println("Current balance: " + account.balance);
            account.lock.unlock();
        }
        else if(taskType == OperationType.CASH_WITHDRAWAL){

            CashDispenser dispense500 = new Dispense500();
            CashDispenser dispense100 = new Dispense100();
            CashDispenser dispense50 = new Dispense50();
            dispense500.setNextDispenser(dispense100);
            dispense100.setNextDispenser(dispense50);


            CardRepo cardRepo = CardRepo.getInstance();
            Card card = cardRepo.getCard(cardId);
            AccountsRepo accountsRepo = AccountsRepo.getInstance();

            Account account = accountsRepo.getAccount(card.accountId);
            account.lock.lock();
            if(account.balance >= amount && dispense500.dispense((int)amount)){
                account.balance -= amount;
                System.out.println("Withdrawal successful. New balance: " + account.balance);
            }else{
                System.out.println("Insufficient balance or unable to dispense the requested amount.");
            }
            account.lock.unlock();
        }
        else if(taskType == OperationType.CASH_DEPOSIT){
            CardRepo cardRepo = CardRepo.getInstance();
            Card card = cardRepo.getCard(cardId);
            AccountsRepo accountsRepo = AccountsRepo.getInstance();
            Account account = accountsRepo.getAccount(card.accountId);
            account.lock.lock();
            account.balance += amount;
            System.out.println("Deposit successful. New balance: " + account.balance);
            account.lock.unlock();
        }
        else if(taskType == OperationType.EJECT_CARD){
            ejectCard(context);
        }
    }

    @Override
    public void ejectCard(AtmContext context) {
        System.out.println("Ejecting card " + cardId);
        context.setState(new NoCardState());
    }
}

class AtmContext{
    ATMState currentState;
    AtmContext(){
        currentState = new NoCardState();
    }
    public void setState(ATMState state){
        this.currentState = state;
    }
}

abstract class CashDispenser{
    CashDispenser nextDispenser;
    public boolean dispense(int amount){
        if(nextDispenser!=null){
            return this.nextDispenser.dispense(amount);
        }
        else{
            System.out.println("The money cannot be dispensed");
            return false;
        }
    }
    abstract void setNextDispenser(CashDispenser nextDispenser);
}

class AmountPresent{
    static int Notes500;
    static int Notes100;
    static int Notes50;
    public AmountPresent(){
        Notes500=10;
        Notes100=10;
        Notes50=0;
    }
    void addNotes(int notes500, int notes100, int notes50){
        Notes500+=notes500;
        Notes100+=notes100;
        Notes50+=notes50;
    }
}

/*
Instead of this 
class ATM {
    private final String atmId;
    private final Map<Integer, Integer> cashInventory = new HashMap<>();
    private final ReentrantLock cashLock = new ReentrantLock();

    public ATM(String atmId) {
        this.atmId = atmId;
    }

    public void addCash(int denomination, int count) {
        cashLock.lock();
        try {
            cashInventory.put(
                denomination,
                cashInventory.getOrDefault(denomination, 0) + count
            );
        } finally {
            cashLock.unlock();
        }
    }

    public Map<Integer, Integer> getCashSnapshot() {
        cashLock.lock();
        try {
            return new HashMap<>(cashInventory); // defensive copy
        } finally {
            cashLock.unlock();
        }
    }

    ReentrantLock getCashLock() {
        return cashLock;
    }

    Map<Integer, Integer> getInventoryUnsafe() {
        return cashInventory; // internal use only
    }
}

the correct class that should be present be this 
if i change the AtmPresent class, i will have to change the code in many places , so to reduce the hassle right now, i will keep it as it is

*/


class Dispense500 extends CashDispenser{
    public boolean dispense(int amount){
        int available500 = AmountPresent.Notes500;
        int need = amount/500;
        int restAmount = 0;
        int notesDispenced = 0;
        if(need<=available500){
            available500-=need;
            restAmount = amount-need*500;
            notesDispenced = need;
        }
        else{
            restAmount = amount-available500*500;
            available500 = 0;
            notesDispenced=available500;
        }
        if(restAmount==0){
            System.out.println("the amount has been dispensed");
            return true;
        }
        else{
            if(super.dispense(restAmount)){
                System.out.println("the amount of 500 notes Dispensed are"+notesDispenced);
                AmountPresent.Notes500 = available500;
                return true;
            }
            return false;
        } 
    }
    void setNextDispenser(CashDispenser dispenser){
        this.nextDispenser = dispenser;
    }
}
class Dispense100 extends CashDispenser{
    public boolean dispense(int amount){
        int available100 = AmountPresent.Notes100;
        int need = amount/100;
        int restAmount = 0;
        int notesDispenced = 0;
        if(need<=available100){
            available100-=need;
            restAmount = amount-need*100;
            notesDispenced = need;
        }
        else{
            restAmount = amount-available100*100;
            available100 = 0;
            notesDispenced=available100;
        }
        if(restAmount==0){
            System.out.println("the amount has been dispensed");
            return true;
        }
        else{
            if(super.dispense(restAmount)){
                System.out.println("the amount of 100 notes Dispensed are"+notesDispenced);
                AmountPresent.Notes100 = available100;
                return true;
            }
            return false;
        } 
    }
    void setNextDispenser(CashDispenser dispenser){
        this.nextDispenser = dispenser;
    }
}
class Dispense50 extends CashDispenser{
    public boolean dispense(int amount){
        int available50 = AmountPresent.Notes50;
        int need = amount/50;
        int restAmount = 0;
        int notesDispenced = 0;
        if(need<=available50){
            available50-=need;
            restAmount = amount-need*50;
            notesDispenced = need;
        }
        else{
            restAmount = amount-available50*50;
            available50 = 0;
            notesDispenced=available50;
        }
        if(restAmount==0){
            System.out.println("the amount has been dispensed");
            return true;
        }
        else{
            if(nextDispenser!=null && super.dispense(restAmount)){
                System.out.println("the amount of 500 notes Dispensed are"+notesDispenced);
                AmountPresent.Notes50 = available50;
                return true;
            }
            System.out.println("The amount cannot be dispersed");
            return false;
        } 
    }
    void setNextDispenser(CashDispenser dispenser){
        this.nextDispenser = dispenser;
    }
}


//one thing is still remaining cash management in atm using chain of responsibility pattern

public class AtmLLD {
    public static void main(String[] args) {

        AtmContext atmContext = new AtmContext();
        CardRepo cardRepo = CardRepo.getInstance();
        AccountsRepo accountsRepo = AccountsRepo.getInstance();
        // Adding sample account and card
        Account account1 = new Account("acc1", "John Doe", 10000,
                AccountStatus.ACTIVE);
        accountsRepo.addAccount(account1);
        Card card1 = new Card("card1", "1234-5678-9012-3456", "cvHash1", "1234",
                CardStatus.ACTIVE, "acc1", new Date());
        cardRepo.addCard(card1);
        
        AmountPresent amountPresent = new AmountPresent();
        amountPresent.addNotes(100, 10, 0);


        atmContext.currentState.insertCard("card1", atmContext);
        atmContext.currentState.authenticateCard("card1", "1234", atmContext);
        atmContext.currentState.performOperation(OperationType.BALANCE_ENQUIRY, 0, atmContext);
        atmContext.currentState.performOperation(OperationType.CASH_WITHDRAWAL, 1350, atmContext);
        amountPresent.addNotes(0, 0, 10);
        atmContext.currentState.performOperation(OperationType.CASH_WITHDRAWAL, 1350, atmContext);
        atmContext.currentState.performOperation(OperationType.CASH_DEPOSIT, 5000, atmContext);
        atmContext.currentState.performOperation(OperationType.BALANCE_ENQUIRY, 0, atmContext);
        atmContext.currentState.performOperation(OperationType.EJECT_CARD, 0, atmContext);
    }
}
