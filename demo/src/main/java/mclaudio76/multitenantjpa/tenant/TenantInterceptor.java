package mclaudio76.multitenantjpa.tenant;

import java.util.Arrays;
import static mclaudio76.multitenantjpa.tenant.TenantContext.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TenantInterceptor implements HandlerInterceptor {

	  private static final String TENANT_HEADER_NAME = "X-TENANT-ID";
	

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