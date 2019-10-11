# Spring multitenant application
This project shows how to build a multitenant Spring Boot Application, i.e an application is able to work with different, separated databases choosing the DBMS to work on request basis, given a certain <i> context </i> the request is sent to the application.
For this project I'll use Hibernate as JPA implementation connecting to a dockerized MySql instance; setting up, configuring and running the docker instance is beyond the scope of this brief example. 

## Application Scenario.
This simple application is nothing more than a single REST API that allows the client to save an imaginary "Product" entity on a database. As multitenant application, we need to be able to dinamically change the actual DBMS we use depending upon some piece of information provided by the request itself. Without much imagination, we can assume that each request sends to the API an header which identifies the tenant we want to use. We can use <b> curl </b> utility to perform such requests. We don't need to have a very complex entity to experiment with, so we can assume that our Product is simple composed by an ID (unique key) and a description. This said, we can for example issue this command:

```
curl -X POST --header "X-TENANT-ID: TENANT-A" -H"Content-type:application/json" -d '{"productID":"MyProd", "description":"Product Description"}' http://localhost:8080/postentry
```

to persist an entity. Please note the header X-TENANT-ID, which will let the application to identify the current tenant to operate on.

### Tracking and detecting the Tenant ID.

We need to keep track all along the request which is the tenant we want to work with. To accomplish this, we need to define a <b>ThreadContext </b> class, which in turns uses a ThreadLocal variable:

```
public class TenantContext {
    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenant(String ID) {
        currentTenant.set(ID);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }
}
```
We need also to automatically associate a TenantContext to each request. To do that, let's write an appropriate extension of 
HandlerInterceptor:

```
@Component
public class TenantInterceptor implements HandlerInterceptor {

  private static final String TENANT_HEADER_NAME = "X-TENANT-ID";

  public static  final String TENANT_A			 = "TENANT-A";
  public static  final String TENANT_B			 = "TENANT-B";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String tenantId = request.getHeader(TENANT_HEADER_NAME);
    if(Arrays.asList(TENANT_A, TENANT_B).stream().noneMatch(x -> x.equals(tenantId))) {
	return false;
    }
    TenantContext.setCurrentTenant(tenantId);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	  TenantContext.setCurrentTenant(null);
  }
}
```

Last, we have to register a TenantInterceptor instance to the MCVConfiguration of our application:

```
@Component
public class WebConfiguration implements WebMvcConfigurer {
   
   @Autowired
   TenantInterceptor tenantResolver;

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(tenantResolver);
   }
}


```
### Routing to the right Datasource.

Once we have set up the necessary machinery to intercept the X-TENANT id of each request, we have to define our PersistenceManager providing it with a 'special kind' of datasource: an AbstractRoutingDataSource. In a nutshell, an AbstractRoutingDatasource is able pick a specific datasource (in a list of potential datasource to use) to an EntityManager, given a certain <i> key </i>. In our case the key will be the current thread ID:

```
public class RoutingDatasource extends AbstractRoutingDataSource {
  @Override
  protected Object determineCurrentLookupKey() {
     return TenantContext.getCurrentTenant();
   }
}

```
And this is the code needed to build the list of available datasources.

```
@Bean
public RoutingDatasource getRoutingDS() {
   RoutingDatasource ds = new RoutingDatasource();
   Map<Object, Object> availDS = new HashMap<>();
   availDS.put(TenantInterceptor.TENANT_A, buildDataSource("XADS1","jdbc:mysql://localhost:3306/mydatabase", "dbuser",  "dbuser"));
   availDS.put(TenantInterceptor.TENANT_B, buildDataSource("XADS2","jdbc:mysql://localhost:3306/anotherDB",  "secuser", "secuser"));
   ds.setTargetDataSources(availDS);
   ds.setDefaultTargetDataSource(availDS.get(TenantInterceptor.TENANT_A));
   ds.afterPropertiesSet();
   return ds;
}

```
Lastly, we can build an EntityManger provider as usual:

```
@Bean("ApplicationEntityManager")
@DependsOn("JTATXManager")
public EntityManagerFactory entityManagerFactory(@Qualifier("hibernate-props") Properties properties, RoutingDatasource routingDS) {
   LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
   em.setDataSource(routingDS);
   em.setPackagesToScan(new String[] { "mclaudio76.multitenantjpa.entities" });
   JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
   em.setJpaVendorAdapter(vendorAdapter);
   em.setPersistenceUnitName("AppEntityManager");
   em.setJpaProperties(properties);
   em.afterPropertiesSet();
   return em.getNativeEntityManagerFactory();
}

```

Here, it's important to note that in this example I choose to hardcode (for sake of semplicity) the list of available datasources. Generally speaking, nothing prevents you to load dinamically a list of available datasource from some kind of support (an XML file, a Properties file or even a database table)
and use it dinamically in the code.


### ProductService

Finally, we can define a simple @Service class to perform actual db operation.

```
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
```

There's nothing particular here to notice, except for the usage of the ```@WithTenant``` annotation:
this is a custom annotation that tells Spring - via AOP - to force the use of a specific tenant-ID,
regardless the current one, to perform a save operation. Pratically, this means that each operation on
Tenant-A is also automatically performed on Tenant-B database.






  

