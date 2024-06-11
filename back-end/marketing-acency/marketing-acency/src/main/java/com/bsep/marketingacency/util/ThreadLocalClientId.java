package com.bsep.marketingacency.util;

public class ThreadLocalClientId {
    private static final ThreadLocal<Long> clientId = new ThreadLocal<>();

    public static void set(Long id) {
        clientId.set(id);
    }

    public static Long get() {
        return clientId.get();
    }

    public static void clear() {
        clientId.remove();
    }
}
