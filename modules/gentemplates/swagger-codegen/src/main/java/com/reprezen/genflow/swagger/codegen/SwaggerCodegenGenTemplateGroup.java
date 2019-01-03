/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.api.template.IGenTemplateGroup;
import com.reprezen.genflow.swagger.codegen.SwaggerCodegenModulesInfo.Info;

import io.swagger.codegen.CodegenConfig;

public class SwaggerCodegenGenTemplateGroup implements IGenTemplateGroup {

	public static Logger logger = LoggerFactory.getLogger(SwaggerCodegenGenTemplateGroup.class);

	@Override
	public Iterable<IGenTemplate> getGenTemplates(ClassLoader classLoader) {
		SwaggerCodegenModulesInfo modulesInfo = getModulesInfo();
		List<IGenTemplate> genTemplates = Lists.newArrayList();
		for (Class<? extends CodegenConfig> config : getCodegenConfigClasses(modulesInfo,
				CodegenConfig.class.getClassLoader())) {
			Info info = modulesInfo.getInfo(config);
			if (info != null && !info.isSuppressed()) {
				BuiltinSwaggerCodegenGenTemplate builtinSwaggerCodegenGenTemplate = new BuiltinSwaggerCodegenGenTemplate(
						config, info);
				genTemplates.add(builtinSwaggerCodegenGenTemplate);
			}
		}
		return genTemplates;
	}

	public Collection<Class<? extends CodegenConfig>> getCodegenConfigClasses(SwaggerCodegenModulesInfo modulesInfo,
			ClassLoader classLoader) {
		Set<Class<? extends CodegenConfig>> classes = Sets.newHashSet();
		try {
			Enumeration<URL> urls = classLoader.getResources("META-INF/services/" + CodegenConfig.class.getName());
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				for (Class<? extends CodegenConfig> candidate : getClassesFromServiceLoaderResource(url, classLoader)) {
					classes.add(candidate);
				}
			}
		} catch (IOException e) {
			logger.warn("Failed to locate service URLs for SCG CodegenConfig class", e);
		}
		return classes;
	}

	private Collection<Class<? extends CodegenConfig>> getClassesFromServiceLoaderResource(URL url,
			ClassLoader classLoader) {
		List<Class<? extends CodegenConfig>> classes = Lists.newArrayList();
		try (InputStream in = url.openStream()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}
				try {
					Class<?> c = classLoader.loadClass(line);
					if (CodegenConfig.class.isAssignableFrom(c)) {
						@SuppressWarnings("unchecked")
						Class<? extends CodegenConfig> validClass = (Class<? extends CodegenConfig>) c;
						classes.add(validClass);
					}
				} catch (ClassNotFoundException e) {
					logger.warn(String.format("Failed to load SCG class %s", line), e);
				}
			}
		} catch (IOException e1) {
			logger.warn(String.format("Failed to read from service loader URL %s", url), e1);
		}
		return classes;
	}

	public static SwaggerCodegenModulesInfo getModulesInfo() {
		SwaggerCodegenModulesInfo modulesInfo = new SwaggerCodegenModulesInfo();
		URL infoUrl = SwaggerCodegenGenTemplateGroup.class.getResource("");
		try {
			modulesInfo.load(infoUrl);
		} catch (IOException e) {
			// resource not found... no modules available
		}
		return modulesInfo;
	}
}