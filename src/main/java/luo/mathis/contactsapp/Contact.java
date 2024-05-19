package luo.mathis.contactsapp;

public class Contact {
    private String name;
    private String phoneNumbers;
    private String email;
    private String address;
    private String birthday;

    public Contact(String name, String phoneNumbers, String email, String address, String birthday) {
        this.name = name;
        this.phoneNumbers = phoneNumbers;
        this.email = email;
        this.address = address;
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

}