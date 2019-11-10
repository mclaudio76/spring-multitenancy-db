package mclaudio76.multitenantjpa;

import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import mclaudio76.multitenantjpa.entities.Product;
import mclaudio76.multitenantjpa.tenant.TenantInterceptor;

@Service
public class ProductService  {
	
	
	private EntityManager em;
	private EntityManager emAlt;
	
	
	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
		System.out.println(" Inject entity manager for request-Tenant "+em);
	}
	
	// For specific usage when needed to force a TENANT
	@PersistenceContext(unitName = TenantInterceptor.TENANT_B)
	public void setAlternateEntityManager(EntityManager em) {
		this.emAlt = em;
		System.out.println(" Inject entity manager for tenant - B "+emAlt);
	}

	
	private AtomicInteger ID = new AtomicInteger(0);
	
	
	@Transactional
	public void saveProduct(Product x) {
		saveProduct(x,em);
	}
	
	@Transactional
	public void saveProductSpecific(Product p) {
		p.description = p.description + "(FORCED ON B)";
		saveProduct(p,emAlt);
	}
	
	
	@Transactional
	public void saveProductOnBothTenants(Product x) {
		saveProduct(x);
		saveProductSpecific(x);
	}
	
	
	private void saveProduct(Product x, EntityManager contextEM) {
		try {
			int locID = ID.addAndGet(1);
			System.out.println(" Transaction START "+locID+ " "+contextEM.toString());
			int waitTime = (int)(7000*Math.random());
			Thread.sleep(waitTime);
			Product p 	  = contextEM.find(Product.class, x.productID);
			if(p == null) {
				p = new Product();
				p.productID   = x.productID;
			}
			p.description = x.description;
			contextEM.persist(p);
			System.out.println(" Transaction END "+locID);
		}
		catch(Exception e) {
			System.err.println(" Error -> "+e.getMessage());
		}
	}

}
