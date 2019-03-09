package com.example.filedemo.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.example.filedemo.property.FileStorageProperties;

@Configuration
public class Authenticate {
	private static final Logger logger = LoggerFactory.getLogger(Authenticate.class);

	private String userFile;

	@Autowired
	public Authenticate(FileStorageProperties fileStorageProperties) {
		this.userFile = fileStorageProperties.getUserFile();
	}

	class UserInfo {
		String userName;
		String password;

		public UserInfo(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}
	}

	public UserInfo readResponse(HttpServletRequest req) {
		String authorization = req.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("Basic")) {
			String credentials = authorization.substring("Basic".length()).trim();
			byte[] decoded = DatatypeConverter.parseBase64Binary(credentials);
			String decodedString = new String(decoded);
			String[] actualCredentials = decodedString.split(":");
			return new UserInfo(actualCredentials[0], actualCredentials[1]);
		}
		return null;
	}

	public Properties loadUser() {
		InputStream input = null;
		Properties prop = new Properties();
		try {
			new File(userFile).createNewFile();
			input = new FileInputStream(userFile);
			prop.load(input);
		} catch (FileNotFoundException e) {
			logger.error("User file missing");
			prop = null;
		}catch (IOException e) {
			logger.error("Unable to read from user file");
			prop = null;
		}
		return prop;
	}

	public String auhtUser(HttpServletRequest req) {
		UserInfo userinfo = readResponse(req);
		if (userinfo != null) {
			String ID = userinfo.userName;
			String Password = userinfo.password;
			Properties user = loadUser();
			String password = user.getProperty(ID);
			if (password == null) {
				return "username not found";
			} else if (!Password.equals(password)) {
				return "password is wrong";
			} else {
				return null;
			}
		}
		return "wrong request";
	}

	public String addUser(HttpServletRequest req, String newUsername, String newPassword) {
		UserInfo userinfo = readResponse(req);
		if (userinfo != null) {
			String ID = userinfo.userName;
			String Password = userinfo.password;
			if (ID.equals("admin") && Password.equals("admin")) {
				Properties user = loadUser();
				try {
					OutputStream output = new FileOutputStream(userFile);
					user.setProperty(newUsername, newPassword);
					user.store(output, null);
					output.close();
					logger.info("New user added");
				} catch (FileNotFoundException e) {
					logger.error("Property file missing");
				} catch (IOException e) {
					logger.error("Unable to write to property file");
				}
				return null;
			}
			return "admin credentials missmatch";
		}
		return "wrong request";
	}
}
