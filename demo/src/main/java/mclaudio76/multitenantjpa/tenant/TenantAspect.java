package mclaudio76.multitenantjpa.tenant;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.annotation.Priority;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
	
@Component
@Aspect
@Priority(value = 100)
public class TenantAspect {
	
    @Around("@annotation(WithTenant)")
    public Object selectTenant(ProceedingJoinPoint pjp) throws Throwable {
    	String currentTenantID = TenantContext.getCurrentTenant();
    	try {
    		log("Current Tenant "+currentTenantID);
    		log("Current Entity Manager ");
    		if(currentTenantID.equals(TenantInterceptor.TENANT_B)) {
    			
    		}
        	MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            WithTenant tenant = method.getAnnotation(WithTenant.class);
            log("Switching to tenant ["+tenant.tenantID()+"]");
            TenantContext.setCurrentTenant(tenant.tenantID());
            return pjp.proceed();
        } finally {
        	log("Switching back to tenant ["+currentTenantID+"]");
        	TenantContext.setCurrentTenant(currentTenantID);
        }
    }
    
    private void log(String message) {
    	Logger.getLogger("TenantAspect").info(message);
    }
	
}
