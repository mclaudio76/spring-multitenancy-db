package mclaudio76.multitenantjpa.tenant;

import javax.persistence.EntityManager;

public abstract class TenantAwareService {
	
	public abstract  void setEntityManager(EntityManager em);
	public abstract EntityManager getEntityManager();
	
}
