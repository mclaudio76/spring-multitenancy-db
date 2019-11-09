package mclaudio76.multitenantjpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.stereotype.Service;

import mclaudio76.multitenantjpa.entities.Product;
import mclaudio76.multitenantjpa.tenant.TenantInterceptor;
import mclaudio76.multitenantjpa.tenant.WithTenant;

@Service
public class ProductService {

	
	private EntityManager em;
	
	@PersistenceContext
	public void setEntityManager(EntityManager x) {
		this.em = x;
		System.out.println(x+ " injected ");
	}
	
	
	
	@Transactional(value = TxType.REQUIRED)
	public void saveProduct(Product x) {
		try {
			int waitTime = (int)(7000*Math.random());
			System.out.println(em.toString()+" waits for "+waitTime+" before running...");
			Thread.sleep(waitTime);
			Product p 	  = em.find(Product.class, x.productID);
			if(p == null) {
				p = new Product();
				p.productID   = x.productID;
			}
			p.description = x.description;
			em.persist(p);
		}
		catch(Exception e) {
			
		}
	}
	
	
	@Transactional(value = TxType.REQUIRED)
	@WithTenant(tenantID = TenantInterceptor.TENANT_B)
	public void saveProductSpecific(Product p) {
		saveProduct(p);
	}
	
	@Transactional(value = TxType.REQUIRED)
	public void saveBoth(Product px) {
		saveProduct(px);
		px.description = "Forced from tenant A";
		saveProductSpecific(px);
	}
}
