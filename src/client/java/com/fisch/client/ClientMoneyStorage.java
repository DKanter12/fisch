package com.fisch.client;

public class ClientMoneyStorage {
    private static long balance = 0;

    public static void setBalance(long value) {
        balance = value;
    }

    public static long getBalance() {
        return balance;
    }
}