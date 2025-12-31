interface TrafficLightState{
    public void handleRequest(TrafficLightContext context);
}
class RedLightState implements TrafficLightState{
    public void handleRequest(TrafficLightContext context){
        System.out.println("This is red light , pls stop the car");
        context.setState(new GreenLightState());
    }
}

class GreenLightState implements TrafficLightState{
    public void handleRequest(TrafficLightContext context){
        System.out.println("this is green light, you may cross");
        context.setState(new YellowLightState());
    }
}
class YellowLightState implements TrafficLightState{
    public void handleRequest(TrafficLightContext context){
        System.out.println("This is yellow , pls slow down");
        context.setState(new RedLightState());
    }
}

class TrafficLightContext{
    private TrafficLightState state;
    TrafficLightContext(){
        state = new RedLightState();
    }
    public void setState(TrafficLightState state){
        this.state  = state;
    }
    public void changeLight(){
        state.handleRequest(this);
    }
}

public class statePattern {

    public static void main(String[] args) {
        TrafficLightContext trafficLight = new TrafficLightContext();
        for(int i = 0;i<6;i++){
            trafficLight.changeLight();
            System.out.println();
        }
    }
}