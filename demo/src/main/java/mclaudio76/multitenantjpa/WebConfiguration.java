package mclaudio76.multitenantjpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import mclaudio76.multitenantjpa.tenant.TenantInterceptor;

@Component
public class WebConfiguration implements WebMvcConfigurer {
   
   @Autowired
   TenantInterceptor tenantResolver;

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(tenantResolver);
   }
}

