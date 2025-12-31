class RemoteControl{
    private Command command;
    
    public void setCommand(Command c){
        this.command = c;
    }

    public void pressButton(){
        if(command!=null){
            command.execute();
        }
    }
}   

interface Command{
    public void execute();
}

class LightOnCommand implements Command{
    private Light light;
    public LightOnCommand(Light light){
        this.light = light;
    }
    public void execute(){
        System.out.println("turning on light");
        light.on();
    }
}

class LightOffCommand implements Command{
    private Light light;
    public LightOffCommand(Light light){
        this.light = light;
    }
    public void execute(){
        System.out.println("turning off light");
        light.off();
    }
}

class Light{
    public void on(){
        System.out.println("Light is on");
    }
    public void off(){
        System.out.println("light is off");
    }
}

public class CommandPattern {
    public static void main(String[] args) {
        Light light = new Light();
        RemoteControl rc = new RemoteControl();
        rc.setCommand(new LightOnCommand(light));
        rc.pressButton();
        rc.setCommand(new LightOffCommand(light));
        rc.pressButton();
    }
}
