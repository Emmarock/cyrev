package com.cyrev.common.entities;

public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CTX = new ThreadLocal<>();

    public static void set(TenantContext ctx) { CTX.set(ctx); }
    public static TenantContext get() { return CTX.get(); }
    public static void clear() { CTX.remove(); }
}