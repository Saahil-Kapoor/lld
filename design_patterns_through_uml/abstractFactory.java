interface UIFactory{
    public Button createButton();
    public CheckBox createCheckbox();
}


interface Button{
    public void render();
}
class LightButton implements Button{
    public void render(){
        System.out.println("rendering a light button");
    }
}
class Darkbutton implements Button{
    public void render(){
        System.out.println("render a dark button");
    }
}

interface CheckBox{
    public void render();
}

class LightCheckbox implements CheckBox{
    public void render(){
        System.out.println("rendereing a light check box");
    }
}

class DarkCheckbox implements CheckBox{
    public void render(){
        System.out.println("rendering a dark checkbox");
    }
}

class LightThemeFactory implements UIFactory{
    public Button createButton(){
        return new LightButton();
    }
    public CheckBox createCheckbox(){
        return new LightCheckbox();
    }
}
class DarkThemeFactory implements UIFactory{
    public Button createButton(){
        return new Darkbutton();
    }
    public CheckBox createCheckbox(){
        return new DarkCheckbox();
    }
}

public class abstractFactory {
    public static void main(String[] args) {
        String theme = "dark";
        UIFactory factory = (theme == "dark")?new DarkThemeFactory():new LightThemeFactory();
        factory.createButton().render();
        factory.createCheckbox().render();        
    }
}
