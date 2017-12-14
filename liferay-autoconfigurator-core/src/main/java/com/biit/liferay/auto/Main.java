package com.biit.liferay.auto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.ArticleService;
import com.biit.liferay.access.CompanyService;
import com.biit.liferay.access.FileEntryService;
import com.biit.liferay.access.OrganizationService;
import com.biit.liferay.access.PasswordService;
import com.biit.liferay.access.ResourcePermissionService;
import com.biit.liferay.access.RoleService;
import com.biit.liferay.access.SiteService;
import com.biit.liferay.access.SiteType;
import com.biit.liferay.access.UserService;
import com.biit.liferay.access.exceptions.DuplicatedFileException;
import com.biit.liferay.access.exceptions.DuplicatedLiferayElement;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.auto.factories.ArticleFactory;
import com.biit.liferay.auto.factories.ImageFactory;
import com.biit.liferay.auto.factories.OrganizationFactory;
import com.biit.liferay.auto.factories.RoleFactory;
import com.biit.liferay.auto.factories.UserFactory;
import com.biit.liferay.auto.factories.UsersRolesFactory;
import com.biit.liferay.auto.log.LiferayAutoconfiguratorLogger;
import com.biit.liferay.auto.model.ExtendedRole;
import com.biit.liferay.auto.model.RoleSelection;
import com.biit.liferay.auto.model.UserRole;
import com.biit.liferay.configuration.LiferayConfigurationReader;
import com.biit.liferay.model.IArticle;
import com.biit.liferay.model.IFileEntry;
import com.biit.liferay.model.KbArticle;
import com.biit.usermanager.entity.IGroup;
import com.biit.usermanager.entity.IRole;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.liferay.portal.model.ActionKey;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.Site;
import com.liferay.portal.model.User;

public class Main {
	private static final String DEFAULT_LIFERAY_VIRTUALHOST = "localhost";
	private static final String DEFAULT_LIFERAY_ADMIN_USER = "test@liferay.com";
	private static final String DEFAULT_LIFERAY_ADMIN_PASSWORD = "test";
	private static final String DEFAULT_LIFERAY_PASSWORD = "asd123";
	private static final String DROOLS_PLUGIN_ENV_VARIABLE = "DROOLS_PLUGIN_CONFIGURATION_FOLDER";
	private static final String DEFAULT_DROOLS_ARTICLE_CONFIG_PATH = "/opt/configuration/drools-plugins";
	private static final String RPROXY_LIFERAY = "/liferay";

	private static final int ARG_VIRTUALHOST = 0;
	private static final int ARG_PASSWORD = 1;
	private static final int ARG_LIFERAY_SERVER = 2;

	private final static String SITE_NAME = "Autoconfiguration";
	private final static String SITE_DESCRIPTION = "This site is created with the automatic Liferay configuration tool.";
	private final static String SITE_URL = "/autoconfig-site";

	private static final String DEFAULT_IMAGE_DESCRIPTION = "Image uploaded automatically.";

	private final static String LIFERAY_DLFILEENTRY_CLASS = "com.liferay.portlet.documentlibrary.model.DLFileEntry";
	private final static String GUEST_ROLE = "Guest";

	private static final long FOLDER_ID = 0l;

	private static final Pattern pattern = Pattern.compile("\\@\\@.*?\\@\\@");
	private static final String DROOLS_ARTICLE_CONFIG_PATTERN = "liferay-knowledge-base-[0-9\\\\.]+-jar-with-dependencies\\.conf";

	private static final String USMO_CONFIG_ENV_VARIABLE = "USMO_CONFIGURATION_FOLDER";
	private static final String DEFAULT_USMO_CONFIG_FOLDER = "/opt/configuration/usmo_config/";
	private static final String ROLE_ACTIVITIES_FILE = "roleActivities.conf";
	private static final String PERMISSIONS_SUFIX = "permissions";
	private static final String TRANSLATION_SUFIX = "translation";
	private static final String GROUP_SUFIX = "group";
	private static final String CLASSIFICATION_SUFIX = "classification";

	private static Company company;
	private static Site site;

	/**
	 * Main method: java -jar liferay-autoconfigurator.jar asd123 localhost
	 * pathToDroolsArticleConfig https://docker.biit-solutions.com/liferay
	 * 
	 * @param args
	 *            virtualhost, new user password, liferayServerUrl
	 */
	public static void main(String[] args) {
		try {
			try {
				// Try default password.
				company = getCompany(getVirtualHost(args), DEFAULT_LIFERAY_ADMIN_PASSWORD);
				LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Accessing using default password '" + DEFAULT_LIFERAY_ADMIN_PASSWORD + "'.");
			} catch (ConnectException | AuthenticationRequired ce) {
				// Not first time executed, try new password.
				LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Accessing using provided password '" + getPassword(args) + "'.");
				company = getCompany(getVirtualHost(args), getPassword(args));
			}

			if (company != null) {
				// Change password to default admin user.
				updateDefaultPassword(getPassword(args));

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

				// Add roles to users.
				assignRolesToUsers(roles, users, organizations, getPassword(args));

				// Define activities by roles.
				defineRoleActivities(roles.values(), readEnvironmentVariable(USMO_CONFIG_ENV_VARIABLE, DEFAULT_USMO_CONFIG_FOLDER));

				// Add images.
				Map<String, IFileEntry<Long>> images = uploadImages(getPassword(args));

				// Set guest permissions to images.
				setGuestPermissions(new HashSet<>(images.values()), getPassword(args));

				// Set articles.
				Map<String, IArticle<Long>> articles = storeArticles(getVirtualHost(args), getPassword(args), getLiferayServer(args), images);

				// Set drools liferay article plugin configuration.
				setDroolsEngineArticleProperties(articles, readEnvironmentVariable(DROOLS_PLUGIN_ENV_VARIABLE, DEFAULT_DROOLS_ARTICLE_CONFIG_PATH));

				LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Liferay updated correctly!");
			} else {
				LiferayAutoconfiguratorLogger.error(Main.class.getName(), "No company found. Check your configuration.");
				System.exit(-1);
			}
		} catch (NotConnectedToWebServiceException | IOException | AuthenticationRequired | WebServiceAccessError | DuplicatedLiferayElement e) {
			LiferayAutoconfiguratorLogger.errorMessage(Main.class.getName(), e);
		}
		System.exit(0);
	}

	private static String getVirtualHost(String[] args) {
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

	private static String getLiferayServer(String[] args) {
		if (args.length <= ARG_LIFERAY_SERVER) {
			return LiferayConfigurationReader.getInstance().getLiferayProtocol() + "://" + LiferayConfigurationReader.getInstance().getHost() + ":"
					+ LiferayConfigurationReader.getInstance().getConnectionPort() + RPROXY_LIFERAY;
		} else {
			// Always HTTPS in docker compose.
			return "https://" + args[ARG_LIFERAY_SERVER] + RPROXY_LIFERAY;
		}
	}

	private static Company getCompany(String companyName, String connectionPassword) throws JsonParseException, JsonMappingException,
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

	private static Site getSite(String connectionPassword) throws JsonParseException, JsonMappingException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
		SiteService siteService = new SiteService();
		try {
			siteService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
			Site site;
			try {
				site = (Site) siteService.getSite(company, SITE_NAME);
				LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Site already exists.");
			} catch (WebServiceAccessError wsa) {
				site = (Site) siteService.addSite(SITE_NAME, SITE_DESCRIPTION, SiteType.DEFAULT_PARENT_GROUP_ID, SITE_URL);
				LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Site created '" + site.getUniqueName() + "'.");
			}
			return site;
		} finally {
			siteService.disconnect();
		}
	}

	private static boolean updateDefaultPassword(String newPassword) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
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

	private static Map<String, IUser<Long>> storeUsers(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError, DuplicatedLiferayElement {
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

	private static Map<String, IGroup<Long>> storeOrganizations(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException,
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

	private static void assignUsersToOrganizations(Map<String, IUser<Long>> users, Map<String, IGroup<Long>> organizations, String connectionPassword)
			throws ClientProtocolException, IOException, NotConnectedToWebServiceException, AuthenticationRequired {
		OrganizationService organizationService = new OrganizationService();
		organizationService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		for (IGroup<Long> organization : organizations.values()) {
			LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Adding users '" + users + "' to organization '" + organization + "'.");
			organizationService.addUsersToOrganization(new ArrayList<>(users.values()), organization);
		}
		organizationService.disconnect();
	}

	private static Map<String, IRole<Long>> storeRoles(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException,
			IOException, AuthenticationRequired, WebServiceAccessError {
		RoleService roleService = new RoleService();
		roleService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<ExtendedRole> roles = RoleFactory.getInstance().getElements();
		LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Roles to add '" + roles.size() + "'.");
		Map<String, IRole<Long>> rolesAdded = new HashMap<>();
		try {
			for (ExtendedRole role : roles) {
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

	private static void assignRolesToUsers(Map<String, IRole<Long>> roles, Map<String, IUser<Long>> users, Map<String, IGroup<Long>> organizations,
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
						LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Role '" + roleSelection
								+ "' not found in definitions, search it as a Liferay standard role.");
						role = roleService.getRole(roleSelection.getRole(), company.getId());
						if (role == null) {
							LiferayAutoconfiguratorLogger.error(Main.class.getName(), "Invalid role for role selection '" + roleSelection + "' in '" + userRole
									+ "'.");
							continue;
						}
					}
					if (roleSelection.getOrganization() == null) {
						// Generic role.
						roleService.addRoleUser(user, role);
						LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added role '" + roleSelection + "' to user '" + users.get(userRole.getUser())
								+ "'.");
					} else {
						// Organization role.
						roleService.addUserOrganizationRole(user, organizations.get(roleSelection.getOrganization()), role);
						LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added role '" + roleSelection + "' to user '" + users.get(userRole.getUser())
								+ "' in '" + organizations.get(roleSelection.getOrganization()) + "'.");
					}
				}
			}
		} finally {
			roleService.disconnect();
		}
	}

	private static Map<String, IFileEntry<Long>> uploadImages(String connectionPassword) throws ClientProtocolException, IOException,
			NotConnectedToWebServiceException, AuthenticationRequired, WebServiceAccessError {
		Map<String, IFileEntry<Long>> imagesUploaded = new HashMap<>();
		List<File> images = ImageFactory.getInstance().getElements();
		// Upload images
		FileEntryService fileService = new FileEntryService();
		fileService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		try {
			for (File image : images) {
				// Remove temp file extra chars.
				String name = image.getName().substring(0, image.getName().indexOf('.') + 4);
				try {
					// String mimeType = "image/png";
					String mimeType = new MimetypesFileTypeMap().getContentType(name);
					fileService.addFile(site.getGroupId(), FOLDER_ID, name, mimeType, name, DEFAULT_IMAGE_DESCRIPTION, "", image);
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Inserted image '" + name + "'.");
				} catch (DuplicatedFileException dfe) {
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Image '" + name + "' already inserted.");
				}
			}
			// Add existing images
			for (IFileEntry<Long> existingImage : fileService.getFileEntries(site.getGroupId(), FOLDER_ID)) {
				// Get inserted images.
				imagesUploaded.put(existingImage.getTitle(), existingImage);
			}
		} finally {
			fileService.disconnect();
		}
		return imagesUploaded;
	}

	private static void setGuestPermissions(Set<IFileEntry<Long>> images, String connectionPassword) throws ClientProtocolException,
			NotConnectedToWebServiceException, IOException, AuthenticationRequired, WebServiceAccessError {
		ResourcePermissionService resourcePermissionsService = new ResourcePermissionService();
		resourcePermissionsService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);

		RoleService roleService = new RoleService();
		try {
			roleService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
			try {
				ActionKey[] allowedActions = new ActionKey[] { ActionKey.VIEW };
				for (IFileEntry<Long> fileEntry : images) {
					IRole<Long> guestRole = roleService.getRole(GUEST_ROLE, fileEntry.getCompanyId());
					Map<Long, ActionKey[]> roleIdsToActionIds = new HashMap<>();
					roleIdsToActionIds.put(guestRole.getId(), allowedActions);

					if (resourcePermissionsService.addResourcePermission(LIFERAY_DLFILEENTRY_CLASS, fileEntry.getId(), fileEntry.getGroupId(),
							fileEntry.getCompanyId(), roleIdsToActionIds)) {
						LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Image '" + fileEntry.getTitle() + "' permissions changed for role '"
								+ guestRole + "'.");
					} else {
						LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Image '" + fileEntry.getTitle() + "' permissions NOT changed for role '"
								+ guestRole + "'.");
					}
				}
			} finally {
				resourcePermissionsService.disconnect();
			}
		} finally {
			resourcePermissionsService.disconnect();
		}
	}

	private static Map<String, IArticle<Long>> storeArticles(String virtualHost, String connectionPassword, String liferayServerUrl,
			Map<String, IFileEntry<Long>> existingImages) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		// Get users from resources profile
		ArticleService articleService = new ArticleService();
		articleService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<KbArticle> articles = ArticleFactory.getInstance().getElements();
		LiferayAutoconfiguratorLogger.debug(Main.class.getName(), "Articles to add '" + articles.size() + "'.");

		Map<String, IArticle<Long>> articlesAdded = new HashMap<>();
		try {
			// Getting already stored articles.
			Set<IArticle<Long>> articlesStored = articleService.getArticles(site);

			for (KbArticle articleToAdd : articles) {
				articleToAdd.setCompanyId(company.getCompanyId());
				// Force the recalculation of the parent resource class name by
				// the webservice.
				articleToAdd.setParentResourceClassNameId(null);
				// URL title must start with a '/' and contain only alphanumeric
				// characters, dashes, and underscores
				if (!articleToAdd.getUrlTitle().startsWith("/")) {
					articleToAdd.setUrlTitle("/" + articleToAdd.getUrlTitle());
				}
				String content = articleToAdd.getContent();

				// Replace image tags with image urls.
				Matcher matcher = pattern.matcher(content);
				while (matcher.find()) {
					try {
						String image = matcher.group().replaceAll("\\@", "");
						if (existingImages.get(image) != null) {
							// https://docker.biit-solutions.com/liferay/documents/20601/0/11_leg_lock.png/f580590c-ce69-430c-b9a2-dcf0e9831743
							String imageUrl = liferayServerUrl + FileEntryService.getFileRelativeUrl(existingImages.get(image));
							articleToAdd.setContent(articleToAdd.getContent().replaceAll("\\@\\@" + image + "\\@\\@", imageUrl));
							LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Image url '" + image + "' replaced by '" + imageUrl + "'.");
						}
					} catch (IndexOutOfBoundsException iob) {
						LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Image url substitution failed for article '" + articleToAdd + "'.");
					}
				}

				// Check already inserted article.
				boolean existingArticle = false;
				for (IArticle<Long> articleStored : articlesStored) {
					// As title is the file name, in this case is unique.
					if (Objects.equals(articleStored.getTitle(), articleToAdd.getTitle())) {
						// Update article with new content.
						articleStored.setDescription(articleToAdd.getDescription());
						articleStored.setContent(articleToAdd.getContent());
						if (articleStored instanceof KbArticle) {
							((KbArticle) articleStored).setParentResourceClassNameId(null);
							((KbArticle) articleStored).setCompanyId(company.getCompanyId());
							// URL title must start with a '/' and contain only
							// alphanumeric characters, dashes, and underscores
							if (!((KbArticle) articleStored).getUrlTitle().startsWith("/")) {
								((KbArticle) articleStored).setUrlTitle("/" + ((KbArticle) articleStored).getUrlTitle());
							}
						}

						IArticle<Long> articleAdded = articleService.editArticle(articleStored);
						LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Article '" + articleStored + "' updated.");
						if (articleAdded instanceof KbArticle) {
							articlesAdded.put(((KbArticle) articleAdded).getUrlTitle().replace("/", ""), articleAdded);
						}
						existingArticle = true;
						break;
					}
				}

				// Store new articles.
				if (!existingArticle) {
					IArticle<Long> articleAdded = articleService.addArticle(articleToAdd, site.getName(), virtualHost);
					LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Added article '" + articleAdded + "'.");
					articlesAdded.put(articleToAdd.getUrlTitle().replace("/", ""), articleAdded);
				}
			}
		} finally {
			articleService.disconnect();
		}
		return articlesAdded;
	}

	private static void setDroolsEngineArticleProperties(Map<String, IArticle<Long>> articles, String droolsArticleConfigPath) {
		Properties droolsArticleConfiguration = new Properties();
		for (Entry<String, IArticle<Long>> articleEntry : articles.entrySet()) {
			droolsArticleConfiguration.setProperty(articleEntry.getKey(), Long.toString(articleEntry.getValue().getId()));
		}
		try {
			droolsArticleConfiguration.store(new FileOutputStream(getDroolsEngineArticlePropertiesPath(droolsArticleConfigPath)), null);
		} catch (FileNotFoundException fne) {
			LiferayAutoconfiguratorLogger.error(Main.class.getName(), fne.getMessage());
		} catch (IOException e) {
			LiferayAutoconfiguratorLogger.errorMessage(Main.class.getName(), e);
		}
	}

	private static String getDroolsEngineArticlePropertiesPath(String droolsArticleConfigPath) throws FileNotFoundException {
		return getPropertiesPath(droolsArticleConfigPath, DROOLS_ARTICLE_CONFIG_PATTERN);
	}

	private static void defineRoleActivities(Collection<IRole<Long>> roles, String usmoConfigPath) {
		Properties roleActivitiesConfiguration = new Properties();
		for (IRole<Long> role : roles) {
			if (!(role instanceof ExtendedRole)) {
				LiferayAutoconfiguratorLogger.warning(Main.class.getName(), "Role '" + role + "' has no activities defined!");
				continue;
			}
			ExtendedRole extendedRole = (ExtendedRole) role;
			if (extendedRole.getActivities() != null && !extendedRole.getActivities().isEmpty()) {
				roleActivitiesConfiguration.setProperty(extendedRole.getName() + "." + PERMISSIONS_SUFIX,
						extendedRole.getActivities().toString().replace("[", "").replace("]", ""));
			}
			if (extendedRole.getTranslation() != null && !extendedRole.getTranslation().isEmpty()) {
				roleActivitiesConfiguration.setProperty(extendedRole.getName() + "." + TRANSLATION_SUFIX, extendedRole.getTranslation());
			}
			if (extendedRole.getGroup() != null && !extendedRole.getGroup().isEmpty()) {
				roleActivitiesConfiguration.setProperty(extendedRole.getName() + "." + GROUP_SUFIX, extendedRole.getGroup());
			}
			if (extendedRole.getClassification() != null && !extendedRole.getClassification().isEmpty()) {
				roleActivitiesConfiguration.setProperty(extendedRole.getName() + "." + CLASSIFICATION_SUFIX, extendedRole.getClassification());
			}
		}

		try {
			// Create file if it does not exists.
			File yourFile = new File(usmoConfigPath + File.separator + ROLE_ACTIVITIES_FILE);
			yourFile.createNewFile(); // if file already exists will do nothing
			roleActivitiesConfiguration.store(new FileOutputStream(usmoConfigPath + File.separator + ROLE_ACTIVITIES_FILE), null);
		} catch (FileNotFoundException fne) {
			LiferayAutoconfiguratorLogger.error(Main.class.getName(), fne.getMessage());
		} catch (IOException e) {
			LiferayAutoconfiguratorLogger.errorMessage(Main.class.getName(), e);
		}
	}

	private static String getPropertiesPath(final String path, final String fileRegex) throws FileNotFoundException {
		File configurationDirectory = new File(path);
		File[] files = configurationDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(fileRegex);
			}
		});

		try {
			for (File configFile : files) {
				LiferayAutoconfiguratorLogger.info(Main.class.getName(), "Found configuration file '" + configFile.getAbsolutePath() + "'.");
				return configFile.getAbsolutePath();
			}
		} catch (NullPointerException npe) {
			// Do nothing, use next exception.
		}
		throw new FileNotFoundException("No file matches '" + path + "/" + fileRegex + "'.");
	}

	public static String readEnvironmentVariable(String environmentVariable, String defaultValue) {
		Map<String, String> env = System.getenv();
		String value = env.get(environmentVariable);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}
		return value;
	}
}
