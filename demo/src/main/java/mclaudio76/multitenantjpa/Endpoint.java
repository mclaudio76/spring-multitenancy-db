package mclaudio76.multitenantjpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import mclaudio76.multitenantjpa.entities.Product;
import mclaudio76.multitenantjpa.tenant.TenantContext;

@RestController
public class Endpoint {
	
	@Autowired
	ProductService pService;
	
	@PostMapping(path = "/postentry",consumes = "application/json")
	public String execute(@RequestBody Product x) {
		try {
			pService.saveProductOnBothTenants(x);
			return "Entity has been persisted on tenant "+TenantContext.getCurrentTenant()+"\n";
		}
		catch(Exception e) {
			return "Request rolled back \n";
		}
	}
	
	
	
}
