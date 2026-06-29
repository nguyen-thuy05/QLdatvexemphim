package com.example.codeappdatvexemphim;

public class Voucher {
    private int id;
    private int userId;
    private String code;
    private double value;
    private String status; // "active" or "used"

    public Voucher(int id, int userId, String code, double value, String status) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.value = value;
        this.status = status;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getCode() { return code; }
    public double getValue() { return value; }
    public String getStatus() { return status; }
}
