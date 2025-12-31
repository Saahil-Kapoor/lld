class User{
    private String name;
    private String email;
    private String phone;

    public User(String name,String email,String phone){
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    //getter for phone
    public void getPhone(){
        System.out.println("this is the phone number"+this.phone);
    }
    public static class UserBuilder{
        private String name;
        private String email;
        private String phone;
        public UserBuilder withName(String name){
            this.name = name;
            return this;
        }
        public UserBuilder withEmail(String email){
            this.email = email;
            return this;
        }
        public UserBuilder withPhone(String phone){
            this.phone = phone;
            return this;
        }
        public User build(){
            return new User(name,email,phone);
        }
    }
}


public class Builder {
    public static void main(String[] args) {
        User user = new User.UserBuilder().withEmail("saahil@mail.com").withName("saahil").withPhone("56").build();
        user.getPhone();
    }
}
