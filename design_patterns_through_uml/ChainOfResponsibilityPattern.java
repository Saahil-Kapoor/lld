
abstract class RequestHandler{
    String name;
    RequestHandler nextHandler;
    private RequestHandler(){

    }
    public RequestHandler(String name){
        this.name = name;
    }
    abstract void setNext(RequestHandler nextHandler);

    void approve(int id){
        if(this.nextHandler != null){
            this.nextHandler.approve(id);
        }
        else{
            System.out.println("request cannot be approved");
        }
    }
}

class Manager extends RequestHandler{
    public Manager(){
        super("manager");
    }
    void setNext(RequestHandler nextHandler){
        this.nextHandler = nextHandler;
    }
    void approve(int id){
        if(id>=1 && id<=20){
            System.out.println("Request Approved");
        }
        else{
            super.approve(id);
        }
    }
}

class SeniorManager extends RequestHandler{
    public SeniorManager(){
        super("Senior manager");
    }
    void setNext(RequestHandler nextHandler){
        this.nextHandler = nextHandler;
    }
    void approve(int id){
        if(id>=21 && id<=40){
            System.out.println("Request Approved");
        }
        else{
            super.approve(id);
        }
    }
}

class Director extends RequestHandler{
    public Director(){
        super("director");
    }
    void setNext(RequestHandler nextHandler){
        this.nextHandler = nextHandler;
    }
    void approve(int id){
        if(id>=41 && id<=80){
            System.out.println("Request Approved");
        }
        else{
            super.approve(id);
        }
    }
}

public class ChainOfResponsibilityPattern {
    public static void main(String[] args) {
        RequestHandler manager = new Manager();
        RequestHandler director = new Director();
        RequestHandler seniorManager = new SeniorManager();
        manager.nextHandler = seniorManager;
        seniorManager.nextHandler = director;
        director.nextHandler = null;
        manager.approve(19);
        manager.approve(90);
        
    }
}
