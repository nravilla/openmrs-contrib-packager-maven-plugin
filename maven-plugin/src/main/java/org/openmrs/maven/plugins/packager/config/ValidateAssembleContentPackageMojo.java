/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.maven.plugins.packager.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The purpose of this Mojo is to validate and assemble content properties file.
 */
@Mojo(name = "validate-assemble-content-package")
public class ValidateAssembleContentPackageMojo extends AbstractMojo {
	
	// Configuration Directory
	@Parameter(property = "sourceFile")
	protected String sourceFile;
	
	private static final Pattern VERSION_PATTERN = Pattern.compile(
	    "^(\\s*[=><~^]*\\s*\\d+(\\.\\d+){0,2}(-[0-9A-Za-z-]+(\\.[0-9A-Za-z-]+)*)?(\\+[0-9A-Za-z-]+(\\.[0-9A-Za-z-]+)*)?)"
	            + "(\\s*(-|\\|\\|)\\s*[=><~^]*\\s*\\d+(\\.\\d+){0,2}(-[0-9A-Za-z-]+(\\.[0-9A-Za-z-]+)*)?(\\+[0-9A-Za-z-]+(\\.[0-9A-Za-z-]+)*)?)*$");
	
	/**
	 * Executes the property validation.
	 *
	 * @throws MojoExecutionException if an error occurs during validation
	 */
	public void execute() throws MojoExecutionException {
		try {
			validateProperties();
		}
		catch (Exception e) {
			throw new MojoExecutionException("Error validating properties file", e);
		}
	}
	
	/**
	 * Validates the properties in the given file.
	 *
	 * @throws IOException if an error occurs while reading the file
	 */
	protected void validateProperties() throws MojoExecutionException {
		try (InputStream input = new FileInputStream(sourceFile)) {
			Properties properties = new Properties();
			properties.load(input);
			
			for (String key : properties.stringPropertyNames()) {
				if ("name".equalsIgnoreCase(key)) {
					continue;
				}
				String value = properties.getProperty(key);
				if (!isValidVersion(value)) {
					throw new MojoExecutionException("Invalid version format for key: " + key + ", value: " + value);
				}
			}
		}
		catch (Exception e) {
			throw new MojoExecutionException(sourceFile + "-" + e.getMessage());
		}
	}
	
	/**
	 * Cleans the value by trimming whitespace.
	 *
	 * @param value the value to clean
	 * @return the cleaned value
	 */
	private String cleanValue(String value) {
		return value.trim();
	}
	
	/**
	 * Checks if the given value is a valid version format.
	 *
	 * @param value the value to check
	 * @return true if the value is a valid version format, false otherwise
	 */
	protected boolean isValidVersion(String value) {
		value = cleanValue(value);
		// Exclude "latest" explicitly
		if ("latest".equalsIgnoreCase(value)) {
			return false;
		}
		return VERSION_PATTERN.matcher(value).matches();
	}
	
}
