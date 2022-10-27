/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package keycloak.scim_user_spi;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import java.util.List;

/**
 * @author <a href="mailto:jstephen@redhat.com">Justin Stephenson</a>
 * @version $Revision: 1 $
 */
public class SCIMUserStorageProviderFactory implements UserStorageProviderFactory<SCIMUserStorageProvider> {

	private static final Logger logger = Logger.getLogger(SCIMUserStorageProviderFactory.class);
	public static final String PROVIDER_NAME = "scim";
	protected static final List<ProviderConfigProperty> configMetadata;

	static {
		configMetadata = ProviderConfigurationBuilder.create()
				/* SCIMv2 server url*/
				.property().name("scimurl")
				.type(ProviderConfigProperty.STRING_TYPE)
				.label("SCIM Server URL")
				.helpText("Backend SCIM Server URL in the format: server.example.com:8080")
				.add()
				/* Login username, used to auth to make HTTP requests */
				.property().name("loginusername")
				.type(ProviderConfigProperty.STRING_TYPE)
				.label("Login username")
				.helpText("username to authenticate through the login page")
				.add()
				/* Login password, used to auth to make HTTP requests */
				.property().name("loginpassword")
				.type(ProviderConfigProperty.STRING_TYPE)
				.label("Login password")
				.helpText("password to authenticate through the login page")
				.add()
				/* Add Integration domain option */
				.property().name("addintgdomain")
				.type(ProviderConfigProperty.BOOLEAN_TYPE)
				.label("Add Integration Domain")
				.helpText("Option to automatically enroll to an integration domain")
				.add()
				/* Add Integration domain inputs */
				.property().name("domainname")
				.type(ProviderConfigProperty.STRING_TYPE)
				.label("Integration domain name")
				.helpText("Integration domain name")
				.add()
				.property().name("domaindesc")
				.type(ProviderConfigProperty.STRING_TYPE)
				.label("Integration domain description")
				.helpText("Integration domain description")
				.add()
				.property().name("domain")
				.type(ProviderConfigProperty.STRING_TYPE)
				.label("Integration domain")
				.helpText("Integration domain, e.g. http://testdomain.com")
				.add()
				.property().name("idprovider")
				.type(ProviderConfigProperty.STRING_TYPE)
				.label("Integration domain provider")
				.helpText("Integration domain backend provider: IPA, AD, LDAP")
				.add()
				.build();
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configMetadata;
	}

	@Override
	public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
			throws ComponentValidationException {
		Scim scim = new Scim(config);

		SimpleHttp.Response response;

		try {
			response = scim.clientRequest("", "GET", null);
			response.close();
		} catch (Exception e) {
			logger.info(e);
			throw new ComponentValidationException("Cannot connect to provided URL!");
		}

		Boolean add_set = Boolean.valueOf(config.getConfig().getFirst("addintgdomain"));

		if (add_set) {
			Boolean result = scim.domainsRequest();
			logger.infov("IntgDomains Result is {0}", result);
		}
	}

	@Override
	public String getId() {
		return PROVIDER_NAME;
	}

	@Override
	public SCIMUserStorageProvider create(KeycloakSession session, ComponentModel model) {
		Scim scim = new Scim(model);
		return new SCIMUserStorageProvider(session, model, scim);
	}
}
