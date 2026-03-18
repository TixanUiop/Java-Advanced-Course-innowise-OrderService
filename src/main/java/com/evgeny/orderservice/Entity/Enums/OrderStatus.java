package com.evgeny.orderservice.Entity.Enums;

public enum OrderStatus {
    Awaiting("Awaiting"),
    Accepted("Accepted"),
    Collect("Collect"),
    Sent("Sent"),
    ReadyToReceive("Ready to receive");

    private final String dbValue;

    OrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
