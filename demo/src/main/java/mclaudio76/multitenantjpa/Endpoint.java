package mclaudio76.multitenantjpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import mclaudio76.multitenantjpa.entities.Product;

@RestController
@Transactional
public class Endpoint {
	
	@PersistenceContext
	EntityManager em;
	
	@PostMapping(path = "/postentry",consumes = "application/json")
	public String execute(@RequestBody Product x) {
		Product p 	  = em.find(Product.class, x.productID);
		if(p == null) {
			p = new Product();
			p.productID   = x.productID;
		}
		p.description = x.description;
		em.persist(p);
		return "Entity has been persisted on tenant "+TenantContext.getCurrentTenant()+"\n";
	}
	
	
	
}
