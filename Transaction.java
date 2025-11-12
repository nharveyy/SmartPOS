package oop.licao.smartpos.model;

/**
 * Transaction.java
 * Represents a transaction record stored in the Transactions collection.
 */
public class Transaction {
    private String id;
    private String paymentMethod;
    private String date;
    private double total;

    public Transaction(String id, String paymentMethod, String date, double total) {
        this.id = id;
        this.paymentMethod = paymentMethod;
        this.date = date;
        this.total = total;
    }

    public String getId() { return id; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDate() { return date; }
    public double getTotal() { return total; }
}
