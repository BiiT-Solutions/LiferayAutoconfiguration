package com.biit.liferay.auto;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.biit.liferay.access.CompanyService;
import com.biit.liferay.access.PasswordService;
import com.biit.liferay.access.UserService;
import com.biit.liferay.access.exceptions.NotConnectedToWebServiceException;
import com.biit.liferay.access.exceptions.WebServiceAccessError;
import com.biit.liferay.log.LiferayClientLogger;
import com.biit.usermanager.entity.IUser;
import com.biit.usermanager.security.exceptions.AuthenticationRequired;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;

public class Main {
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
		companyService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, DEFAULT_LIFERAY_ADMIN_PASSWORD);
		try {
			company = (Company) companyService.getCompanyByVirtualHost(getCompany(args));
			LiferayClientLogger.info(Main.class.getName(), "Company obtained '" + company + "'.");
			// Company obtained, first connection to Liferay.
			if (company != null) {
				// Change password to default admin user.
				updateDefaultPassword(getPassword(args));

				// Store new users.
				storeUsers(getPassword(args));
			}
		} catch (NotConnectedToWebServiceException | IOException | AuthenticationRequired | WebServiceAccessError e) {
			LiferayClientLogger.errorMessage(Main.class.getName(), e);
		}
	}

	private static String getCompany(String[] args) {
		if (args.length <= ARG_VIRTUALHOST) {
			return "localhost";
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
			AuthenticationRequired, WebServiceAccessError {
		UserService userService = new UserService();
		PasswordService passwordService = new PasswordService();
		passwordService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, DEFAULT_LIFERAY_ADMIN_PASSWORD);
		userService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, DEFAULT_LIFERAY_ADMIN_PASSWORD);
		IUser<Long> defaultUser = userService.getUserByEmailAddress(company, DEFAULT_LIFERAY_ADMIN_USER);
		defaultUser = passwordService.updatePassword(defaultUser, newPassword);
		passwordService.disconnect();
		userService.disconnect();
	}

	private static void storeUsers(String connectionPassword) throws ClientProtocolException, NotConnectedToWebServiceException, IOException,
			AuthenticationRequired, WebServiceAccessError {
		// Get users from resources profile
		UserService userService = new UserService();
		userService.serverConnection(DEFAULT_LIFERAY_ADMIN_USER, connectionPassword);
		List<User> users = UserFactory.getInstance().getUsers();
		for (User user : users) {
			if (user.getPassword() == null || user.getPassword().isEmpty()) {
				user.setPassword(DEFAULT_LIFERAY_PASSWORD);
			}
			user.setCompanyId(company.getCompanyId());
			userService.addUser(company, user);
			LiferayClientLogger.info(Main.class.getName(), "Added user '" + user.getEmailAddress() + "'.");
		}
		userService.disconnect();

	}
}
