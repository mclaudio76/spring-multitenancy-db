package mclaudio76.multitenantjpa;

import static mclaudio76.multitenantjpa.tenant.TenantContext.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.stereotype.Service;

import mclaudio76.multitenantjpa.entities.Product;

@Service
public class ProductService  {
	
	private EntityManager entityManager;
	private Map<String, EntityManager> entityManagers = new HashMap<String, EntityManager>();
	private AtomicInteger ID = new AtomicInteger(0);
	
	
	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
		System.out.println(" Inject entity manager "+em);
	}
	
	@Autowired
	public void setEntityManagers(List<EntityManagerFactory> ems) {
		for(EntityManagerFactory item : ems) {
			String puName = item.getProperties().get("hibernate.ejb.persistenceUnitName").toString();
			System.out.println(" Registering  ["+puName+"]");
			entityManagers.put(puName, SharedEntityManagerCreator.createSharedEntityManager(item, item.getProperties(), true));
		}
	}
	
	
	@Transactional
	public void saveProduct(Product x) throws Exception {
		saveProduct(x,entityManager);
	}
	
	@Transactional
	public void saveProductSpecific(Product p) throws Exception {
		p.description = p.description + "(FORCED ON B)";
		saveProduct(p, entityManagers.get(TENANT_B));
	}
	
	
	@Transactional(rollbackOn = Exception.class)
	public void saveProductOnBothTenants(Product x) throws Exception {
		saveProduct(x);
		saveProductSpecific(x);
		throw new Exception("Rolling !!");
	}
	
	
	private void saveProduct(Product x, EntityManager contextEM) throws Exception {
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
	

}
