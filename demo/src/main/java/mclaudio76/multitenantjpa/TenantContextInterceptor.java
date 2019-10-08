package mclaudio76.multitenantjpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class TenantContextInterceptor implements WebMvcConfigurer {
   
   @Autowired
   MultiTenantFilter tenantResolver;

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(tenantResolver);
   }
}

