package mclaudio76.multitenantjpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.stereotype.Service;

import mclaudio76.multitenantjpa.entities.Product;

@Service
public class ProductService {

	@PersistenceContext
	EntityManager em;
	
	@Transactional(value = TxType.REQUIRED)
	public void saveProduct(Product x) {
		Product p 	  = em.find(Product.class, x.productID);
		if(p == null) {
			p = new Product();
			p.productID   = x.productID;
		}
		p.description = x.description;
		em.persist(p);
	}
	
	
	@Transactional(value = TxType.REQUIRED)
	@WithTenant(tenantID = TenantInterceptor.TENANT_B)
	public void saveProductSpecific(Product p) {
		saveProduct(p);
	}
	
	@Transactional(value = TxType.REQUIRED)
	public void saveBoth(Product px) {
		saveProduct(px);
		saveProductSpecific(px);
	}
}
