package mclaudio76.multitenantjpa;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import mclaudio76.multitenantjpa.tenant.TenantContext;

public class RoutingDatasource extends AbstractRoutingDataSource {
	@Override
	protected Object determineCurrentLookupKey() {
		return TenantContext.getCurrentTenant();
	}

}
