package com.biit.liferay.auto;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.CompanyService;
import com.biit.liferay.access.FileEntryService;
import com.biit.liferay.access.OrganizationService;
import com.biit.liferay.access.PasswordService;
import com.biit.liferay.access.RepositoryService;
import com.biit.liferay.access.RoleService;
import com.biit.liferay.access.SiteService;
import com.biit.liferay.access.UserService;
import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.auto.factories.ImageFactory;
import com.biit.liferay.auto.factories.OrganizationFactory;
import com.biit.liferay.auto.factories.RoleFactory;
import com.biit.liferay.auto.factories.UserFactory;
import com.biit.liferay.auto.factories.UsersRolesFactory;
import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.auto.model.RoleSelection;
import com.biit.liferay.auto.model.UserRole;
import com.biit.liferay.model.Repository;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IRole;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleType;
import com.liferay.portal.model.Site;
import com.liferay.portal.model.User;

public class Main {
	private static final String DEFAULT_LIFERAY_VIRTUALHOST = "localhost";
	private static final String DEFAULT_LIFERAY_ADMIN_USER = "test@liferay.com";
	private static final String DEFAULT_LIFERAY_ADMIN_PASSWORD = "test";
	private static final String DEFAULT_LIFERAY_PASSWORD = "asd123";

	private static final int ARG_VIRTUALHOST = 0;
	private static final int ARG_PASSWORD = 1;

	private final static String SITE_NAME = "testSite";
	private final static String SITE_DESCRIPTION = "This site is created with the automatic Liferay configuration tool.";
	private final static String SITE_URL = "/test-site";

	private final static String REPOSITORY_NAME = "testSite";
	private final static String REPOSITORY_DESCRIPTION = "This site is created with the automatic Liferay configuration tool.";

	private static final String DEFAULT_IMAGE_DESCRIPTION = "Image uploaded automatically.";

	private static Company company;
	private static Site site;

	/**
	 * Main method
	 * 
	 * @param args
	 *            virtualhost, default user password,
	 */
	public static void main(String[] args) {
		try {
			// Change password to default admin user. Access with default admin
			// user to a service. if default password has been change, do
			// nothing.
			if (updateDefaultPassword(getPassword(args))) {
				company = getCompany(getCompany(args), getPassword(args));
				if (company == null) {
					LiferayAutoconfiguratorLogger.error(Main.class.getName(), "No company found. Check your configuration.");
					System.exit(-1);
				}

				// Create site.
				site = getSite(getPassword(args));

				// Store new users.
				Map<String, IUser<Long>> users = storeUsers(getPassword(args));

				// Add organizations.
				Map<String, IGroup<Long>> organizations = storeOrganizations(getPassword(args));

				// Add users to organization.
				assignUsersToOrganizations(users, organizations, getPassword(args));

				// Add roles.
				Map<String, IRole<Long>> roles = storeRoles(getPassword(args));

				// Add roles to organizations. Not needed or will inherited by
				// all users.
				// assignRolesToOrganizations(roles, organizations,
				// getPassword(args));

				// Add roles to users.
				assignRolesToUsers(roles, users, organizations, getPassword(args));

				// Add images.
				uploadImages(getPassword(args));
			}
		} catch (NotConnectedToWebServiceException | IOException | AuthenticationRequired | WebServiceAccessError e) {
			LiferayAutoconfiguratorLogger.errorMessage(Main.class.getName(), e);
		}
		System.exit(0);
	}

	public static String getCompany(String[] args) {
		if (args.length <= ARG_VIRTUALHOST) {
			return DEFAULT_LIFERAY_VIRTUALHOST;
		} else {
			return args[ARG_VIRTUALHOST];
		}
	}

	public static String getPassword(String[] args) {
		if (args.length <= ARG_PASSWORD) {
			return DEFAULT_LIFERAY_PASSWORD;
		} else {
			return args[ARG_PASSWORD];
		}
	}

	public static Company getCompany(String companyName, String connectionPassword) throws JsonParseException, JsonMappingException,
			NotConnectedToWebServiceException, IOException, AuthenticationRequired, WebServiceAccessError {
		CompanyService companyService = new CompanyService();
		try {
			companyService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
			Company company = (Company) companyService.getCompanyByVirtualHost(companyName);
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Company obtained '" + company.getCompanyId() + "'.");
			return company;
		} finally {
			companyService.disconnect();
		}
	}

	public static Site getSite(String connectionPassword) throws JsonParseException, JsonMappingException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		SiteService siteService = new SiteService();
		try {
			siteService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
			Site site = (Site) siteService.getSite(company, SITE_NAME);
			if (site == null) {
				site = (Site) siteService.addSite(SITE_NAME, SITE_DESCRIPTION, 0, SITE_URL);
				LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Site created '" + site.getUniqueName() + "'.");
			} else {
				LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Site already exists.");
			}
			return site;
		} finally {
			siteService.disconnect();
		}
	}

	public static boolean updateDefaultPassword(String newPassword) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			WebServiceAccessError {
		UserService userService = new UserService();
		PasswordService passwordService = new PasswordService();
		try {
			passwordService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, DEFAULT_LIFERAY_ADMIN_PASSWORD);
			userService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, DEFAULT_LIFERAY_ADMIN_PASSWORD);
			IUser<Long> defaultUser = userService.getUserByEmailAddress(company, DEFAULT_LIFERAY_ADMIN_USER);
			defaultUser = passwordService.updatePassword(defaultUser, newPassword);
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Default password changed.");
			return true;
		} catch (AuthenticationRequired ar) {
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Default password NOT changed.");
		} finally {
			passwordService.disconnect();
			userService.disconnect();
		}
		return false;
	}

	public static Map<String, IUser<Long>> storeUsers(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		// Get users from resources profile
		UserService userService = new UserService();
		userService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<User> users = UserFactory.getInstance().getElements();
		LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Users to add '" + users.size() + "'.");
		Map<String, IUser<Long>> usersAdded = new HashMap<>();
		try {
			for (User user : users) {
				if (user.getPassword() == null || user.getPassword().isEmpty()) {
					user.setPassword(DEFAULT_LIFERAY_PASSWORD);
				}
				user.setCompanyId(company.getCompanyId());
				try {
					IUser<Long> userAdded = userService.addUser(company, user);
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added user '" + userAdded + "'.");
					usersAdded.put(user.getUniqueName(), userAdded);
				} catch (WebServiceAccessError dusne) {
					if (dusne.getMessage().contains("com.liferay.portal.DuplicateUserScreenNameException")) {
						LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Already exists an user with screen name '" + user.getScreenName() + "'. ");
						IUser<Long> existingUser = userService.getUserByEmailAddress(company, user.getEmailAddress());
						if (existingUser != null) {
							usersAdded.put(existingUser.getUniqueName(), existingUser);
						}
					}
				}
			}
		} finally {
			userService.disconnect();
		}
		return usersAdded;
	}

	public static Map<String, IGroup<Long>> storeOrganizations(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		OrganizationService organizationService = new OrganizationService();
		organizationService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<Organization> organizations = OrganizationFactory.getInstance().getElements();
		LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Organizations to add '" + organizations.size() + "'.");
		Map<String, IGroup<Long>> organizationsAdded = new HashMap<>();
		try {
			for (Organization organization : organizations) {
				organization.setCompanyId(company.getCompanyId());
				try {
					IGroup<Long> organizationAdded = organizationService.addOrganization(company, organization);
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added organization '" + organizationAdded + "'.");
					organizationsAdded.put(organization.getUniqueName(), organizationAdded);
				} catch (DuplicatedLiferayElement dle) {
					LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Already exists an organization with name '" + organization + "'. ");
					// get organization that already exists.
					for (IGroup<Long> existingOrganization : organizationService.getOrganizations(company)) {
						if (Objects.equals(existingOrganization.getUniqueName(), organization.getUniqueName())) {
							organizationsAdded.put(existingOrganization.getUniqueName(), existingOrganization);
						}
					}
				}
			}
		} finally {
			organizationService.disconnect();
		}
		return organizationsAdded;
	}

	public static void assignUsersToOrganizations(Map<String, IUser<Long>> users, Map<String, IGroup<Long>> organizations, String connectionPassword)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		OrganizationService organizationService = new OrganizationService();
		organizationService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		for (IGroup<Long> organization : organizations.values()) {
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Adding users '" + users + "' to organization '" + organization + "'.");
			organizationService.addUsersToOrganization(new ArrayList<>(users.values()), organization);
		}
		organizationService.disconnect();
	}

	public static Map<String, IRole<Long>> storeRoles(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		RoleService roleService = new RoleService();
		roleService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<Role> roles = RoleFactory.getInstance().getElements();
		LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Roles to add '" + roles.size() + "'.");
		Map<String, IRole<Long>> rolesAdded = new HashMap<>();
		try {
			for (Role role : roles) {
				role.setCompanyId(company.getCompanyId());
				try {
					IRole<Long> roleAdded = roleService.addRole(role);
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added role '" + roleAdded + "'.");
					rolesAdded.put(roleAdded.getUniqueName(), roleAdded);
				} catch (DuplicatedLiferayElement dle) {
					LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Already exists the role '" + role + "'. ");
					IRole<Long> existingRole = roleService.getRole(role.getName(), company.getId());
					if (existingRole != null) {
						rolesAdded.put(existingRole.getUniqueName(), existingRole);
					}
				}
			}
		} finally {
			roleService.disconnect();
		}
		return rolesAdded;
	}

	public static void assignRolesToOrganizations(Map<String, IRole<Long>> roles, Map<String, IGroup<Long>> organizations, String connectionPassword)
			throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired, WebServiceAccessError {
		RoleService roleService = new RoleService();
		roleService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		for (IRole<Long> role : roles.values()) {
			if (((Role) role).getType() == RoleType.ORGANIZATION.getLiferayCode()) {
				roleService.addRoleOrganizations(role, new ArrayList<>(organizations.values()));
				LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Adding organizations '" + organizations + "' to role '" + role + "'.");
			}
		}
		roleService.disconnect();
	}

	public static void assignRolesToUsers(Map<String, IRole<Long>> roles, Map<String, IUser<Long>> users, Map<String, IGroup<Long>> organizations,
			String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException, IOException, AuthenticationRequired,
			WebServiceAccessError {
		List<UserRole> usersRoles = UsersRolesFactory.getInstance().getElements();
		RoleService roleService = new RoleService();
		roleService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		try {
			for (UserRole userRole : usersRoles) {
				for (RoleSelection roleSelection : userRole.getRoles()) {
					IUser<Long> user = users.get(userRole.getUser());
					IRole<Long> role = roles.get(roleSelection.getRole());
					if (user == null) {
						LiferayAutoconfiguratorLogger.error(Main.class.getName(), "Invalid user for role selection '" + roleSelection + "' in '" + userRole
								+ "'.");
						continue;
					}
					if (role == null) {
						LiferayAutoconfiguratorLogger.error(Main.class.getName(), "Invalid role for role selection '" + roleSelection + "' in '" + userRole
								+ "'.");
						continue;
					}
					if (roleSelection.getOrganization() == null) {
						// Generic role.
						roleService.addRoleUser(user, role);
						LiferayAutoconfiguratorLogger.info(Main.class.getName(),
								"Added role '" + roles.get(roleSelection.getRole()) + "' to user '" + users.get(userRole.getUser()) + "'.");
					} else {
						// Organization role.
						roleService.addUserOrganizationRole(user, organizations.get(roleSelection.getOrganization()), role);
						LiferayAutoconfiguratorLogger.info(
								Main.class.getName(),
								"Added role '" + roles.get(roleSelection.getRole()) + "' to user '" + users.get(userRole.getUser()) + "' in '"
										+ organizations.get(roleSelection.getOrganization()) + "'.");
					}
				}
			}
		} finally {
			roleService.disconnect();
		}
	}

	public static void uploadImages(String connectionPassword) throws ClientProtocolException, IOException, NotConnectedToWebServiceException,
			AuthenticationRequired, WebServiceAccessError {
		List<File> images = ImageFactory.getInstance().getElements();
		// Create a repository
		RepositoryService repositoryService = new RepositoryService();
		repositoryService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		try {
			Repository repository = (Repository) repositoryService.getRespository(repositoryId);

			repository = (Repository) repositoryService.addRespository(site, REPOSITORY_NAME, REPOSITORY_DESCRIPTION);

			// Upload images
			FileEntryService fileService = new FileEntryService();
			fileService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
			try {
				for (File image : images) {
					String mimeType = URLConnection.guessContentTypeFromName(image.getName());
					fileService.addFile(repository.getGroupId(), 0l, image.getName(), mimeType, image.getName(), DEFAULT_IMAGE_DESCRIPTION, "", image);
				}
			} finally {
				fileService.disconnect();
			}
		} finally {
			repositoryService.disconnect();
		}
	}
}
