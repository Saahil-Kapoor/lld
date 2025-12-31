//we are implementing a logger here based on the uml diagram shown in the ppts of vivek sir

class Cache{
    private static Cache instance;
    private Cache(){

    }
    public static  Cache getInstance(){
        if(instance == null){
            instance = new Cache();
        }
        return instance;
    }
    public void info(String message){
        System.out.println("This is a info messgae"+message);
    }
    public void error(String message){
        System.out.println("this is a error message"+message);
    }

}
public class Singleton {
    public static void main(String[] args) {
        Cache logger = Cache.getInstance();
        logger.info("this is a message info");
        logger.error("shit , an error occurred because of your drowsyness");
    }
}
