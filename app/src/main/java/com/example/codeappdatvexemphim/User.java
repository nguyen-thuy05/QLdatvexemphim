package com.example.codeappdatvexemphim;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String role;
    private String fullname;

    public User(int id, String username, String email, String password, String role, String fullname) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.fullname = fullname;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullname() { return fullname; }
}
