package mclaudio76.multitenantjpa.tenant;

public class TenantContext {
	
	public static  final String TENANT_A			 = "TENANT-A";
	public static  final String TENANT_B			 = "TENANT-B";
	
    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String ID) {
        currentTenant.set(ID);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }
}
