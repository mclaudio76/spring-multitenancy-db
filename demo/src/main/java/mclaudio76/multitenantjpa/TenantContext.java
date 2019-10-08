package mclaudio76.multitenantjpa;

public class TenantContext {
    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String ID) {
        currentTenant.set(ID);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }
}
