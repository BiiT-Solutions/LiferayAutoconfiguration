package com.biit.liferay.auto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.CompanyService;
import com.biit.liferay.access.OrganizationService;
import com.biit.liferay.access.PasswordService;
import com.biit.liferay.access.UserService;
import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.User;

public class Main {
	private static final String DEFAULT_LIFERAY_VIRTUALHOST = "localhost";
	private static final String DEFAULT_LIFERAY_ADMIN_USER = "test@liferay.com";
	private static final String DEFAULT_LIFERAY_ADMIN_PASSWORD = "test";
	private static final String DEFAULT_LIFERAY_PASSWORD = "asd123";

	private static final int ARG_VIRTUALHOST = 0;
	private static final int ARG_PASSWORD = 1;

	private static Company company;

	/**
	 * Main method
	 * 
	 * @param args
	 *            virtualhost, default user password,
	 */
	public static void main(String[] args) {
		// Access with default admin user to a service. if default password has
		// been change, do nothing.
		CompanyService companyService = new CompanyService();
		companyService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, getPassword(args));
		try {
			company = (Company) companyService.getCompanyByVirtualHost(getCompany(args));
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Company obtained '" + company.getCompanyId() + "'.");
			// Company obtained, first connection to Liferay.
			if (company != null) {
				// Change password to default admin user.
				updateDefaultPassword(getPassword(args));

				// Store new users.
				List<IUser<Long>> users = storeUsers(getPassword(args));

				// Add organizations.
				List<IGroup<Long>> organizations = storeOrganizations(getPassword(args));

				// Add users to organization.
				assignUsersToOrganizations(users, organizations, getPassword(args));
			}
		} catch (NotConnectedToWebServiceException | IOException | AuthenticationRequired | WebServiceAccessError e) {
			LiferayAutoconfiguratorLogger.errorMessage(Main.class.getName(), e);
		}
		System.exit(0);
	}

	private static String getCompany(String[] args) {
		if (args.length <= ARG_VIRTUALHOST) {
			return DEFAULT_LIFERAY_VIRTUALHOST;
		} else {
			return args[ARG_VIRTUALHOST];
		}
	}

	private static String getPassword(String[] args) {
		if (args.length <= ARG_PASSWORD) {
			return DEFAULT_LIFERAY_PASSWORD;
		} else {
			return args[ARG_PASSWORD];
		}
	}

	private static void updateDefaultPassword(String newPassword) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			WebServiceAccessError {
		UserService userService = new UserService();
		PasswordService passwordService = new PasswordService();
		try {
			passwordService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, DEFAULT_LIFERAY_ADMIN_PASSWORD);
			userService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, DEFAULT_LIFERAY_ADMIN_PASSWORD);
			IUser<Long> defaultUser = userService.getUserByEmailAddress(company, DEFAULT_LIFERAY_ADMIN_USER);
			defaultUser = passwordService.updatePassword(defaultUser, newPassword);
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Default password changed.");
		} catch (AuthenticationRequired ar) {
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Default password NOT changed.");
		} finally {
			passwordService.disconnect();
			userService.disconnect();
		}
	}

	private static List<IUser<Long>> storeUsers(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		// Get users from resources profile
		UserService userService = new UserService();
		userService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<User> users = UserFactory.getInstance().getElements();
		LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Users to add '" + users.size() + "'.");
		List<IUser<Long>> usersAdded = new ArrayList<>();
		try {
			for (User user : users) {
				if (user.getPassword() == null || user.getPassword().isEmpty()) {
					user.setPassword(DEFAULT_LIFERAY_PASSWORD);
				}
				user.setCompanyId(company.getCompanyId());
				try {
					IUser<Long> userAdded = userService.addUser(company, user);
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added user '" + userAdded + "'.");
					usersAdded.add(userAdded);
				} catch (WebServiceAccessError dusne) {
					if (dusne.getMessage().contains("com.liferay.portal.DuplicateUserScreenNameException")) {
						LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Already exists an user with screen name '" + user.getScreenName() + "'. ");
						usersAdded.add(userService.getUserByEmailAddress(company, user.getEmailAddress()));
					}
				}
			}
		} finally {
			userService.disconnect();
		}
		return usersAdded;
	}

	private static List<IGroup<Long>> storeOrganizations(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		OrganizationService organizationService = new OrganizationService();
		organizationService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<Organization> organizations = OrganizationFactory.getInstance().getElements();
		LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Organizations to add '" + organizations.size() + "'.");
		List<IGroup<Long>> organizationsAdded = new ArrayList<>();
		try {
			for (Organization organization : organizations) {
				organization.setCompanyId(company.getCompanyId());
				try {
					IGroup<Long> organizationAdded = organizationService.addOrganization(company, organization);
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added organization '" + organizationAdded + "'.");
					organizationsAdded.add(organizationAdded);
				} catch (DuplicatedLiferayElement dle) {
					LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Already exists an organization with name '" + organization + "'. ");
					// get organization that already exists.
					for (IGroup<Long> existingOrganization : organizationService.getOrganizations(company)) {
						if (Objects.equals(existingOrganization.getUniqueName(), organization.getUniqueName())) {
							organizationsAdded.add(existingOrganization);
						}
					}
				}
			}
		} finally {
			organizationService.disconnect();
		}
		return organizationsAdded;
	}
	
	private static void assignUsersToOrganizations(List<IUser<Long>> users, List<IGroup<Long>> organizations, String connectionPassword)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		for (IGroup<Long> organization : organizations) {
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Adding users '" + users + "' to organization '" + organization + "'.");
			OrganizationService organizationService = new OrganizationService();
			organizationService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
			organizationService.addUsersToOrganization(users, organization);
		}
	}
}
