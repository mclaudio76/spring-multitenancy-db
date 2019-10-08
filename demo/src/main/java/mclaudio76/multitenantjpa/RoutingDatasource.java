package mclaudio76.multitenantjpa;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDatasource extends AbstractRoutingDataSource {
	@Override
	protected Object determineCurrentLookupKey() {
		return TenantContext.getCurrentTenant();
	}

}
