/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerGenTemplate;
import com.reprezen.genflow.api.swagger.SwaggerSource.SwaggerSource_MinimalNormalizerOptions;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.template.GenTemplateProperty.StandardProperties;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.common.codegen.GenModuleWrapper;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.Swagger;

public abstract class ScgCodegenGenTemplateBase extends SwaggerGenTemplate {

	public static final String SWAGGER_CODEGEN_SYSTEM_PROPERTIES = "swaggerCodegenSystemProperties";
	public static final String SWAGGER_CODEGEN_CONFIG = "swaggerCodegenConfig";

	public static final List<String> SPECIAL_PARAMS = Arrays.asList(SWAGGER_CODEGEN_CONFIG,
			SWAGGER_CODEGEN_SYSTEM_PROPERTIES);

	protected final GenModuleWrapper<CodegenConfig> wrapper;
	private Info info;

	private static Logger logger = LoggerFactory.getLogger(ScgCodegenGenTemplateBase.class);

	public ScgCodegenGenTemplateBase(GenModuleWrapper<CodegenConfig> wrapper, Info info) {
		this.wrapper = wrapper;
		this.info = info;
	}

	@Override
	public IGenTemplate newInstance() throws GenerationException {
		return new ScgCodegenGenTemplate(wrapper, info);
	}

	@Override
	protected StaticGenerator<Swagger> getStaticGenerator() {
		return new Generator(this, context, wrapper);
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swagger.codegen." + wrapper.getSimpleName());
		define(primarySource().ofType(SwaggerSource_MinimalNormalizerOptions.class));
		define(parameter().named(SWAGGER_CODEGEN_CONFIG).optional().withDescription(
				"Contents of swagger codegen configuration file.",
				"This is the file that would be passed with --config option on swagger codegen command line.",
				"The JSON contents of that file should be the value of this parameter.",
				"This parameter need not be used. If it is absent, all string-valued parameters are collected into",
				"a map that is then passed to the swagger codegen module. If a map is provided here, then string-valued",
				"parameters are still copied in, overriding like-named values appearing in the map."));
		define(parameter().named(SWAGGER_CODEGEN_SYSTEM_PROPERTIES).optional().withDescription(
				"System properties to set, as in the -D option of swagger codegen command line.",
				"Each property should be a json object with a name/value pair for each property.",
				"Example: for '-Dmodels -Dapis=User,Pets' use the following:", "value:", "  models: ''",
				"  apis: Users,Pets"));
		define(parameter().named(LANGUAGE_SPECIFIC_PRIMITIVES).optional().withDescription(
				"Specifies types that are provided by the API implementation, and so should not be generated.", //
				"Type names should be unqualified. The qualified name should be defined in importMappings.", //
				"The value is an array of type names. Example usage:", //
				"  languageSpecificPrimitives:", //
				"    - Pet", //
				"    - User"));
		define(parameter().named(TYPE_MAPPINGS).optional().withDescription(
				"Sets mappings between general-purpose types and declared types in the generated code. Types",
				"may include string, number, integer, boolean, array, object, or others defined by the generator.", //
				"Types should be unqualified. The qualified name should be defined in importMappings. Example usage:", //
				"  typeMappings:", //
				"    array: Set", //
				"    map: LinkedHashMap"));
		define(parameter().named(INSTANTIATION_TYPES).optional().withDescription(
				"Specifies mappings between general-purpose types and their runtime types, for cases where", //
				"generated code may need to instantiate that type. Types may include map, array, or other", //
				"types as defined by the generator. Type names should be unqualified. The qualified name should", //
				"be defined in importMappings. Example usage:", //
				"  instantiationTypes:", //
				"    array: HashSet", //
				"    map: LinkedHashMap"));
		define(parameter().named(IMPORT_MAPPINGS).optional().withDescription(
				"Specifies mappings between an unqualified class or interface name and the qualified name that", //
				"should be imported where that class is used. Example usage:", //
				"  importMappings:", //
				"    HashSet: java.util.HashSet", //
				"    LinkedHashMap: java.util.LinkedHashMap", //
				"    User: com.mycomp.User"));
		define(parameter().named(RESERVED_WORDS_MAPPINGS).optional().withDescription(
				"Specifies a mapping between reserved keywords in the target language and legal, non-reserved", //
				"names. Where the OpenAPI document uses a reserved word as a type, property, operation, or", //
				"parameter name, the generator will substitute the name provided in the map. Otherwise, the", //
				"default underscore-prefixed _<name> will be applied. Example usage:", //
				"  reservedWordsMappings:", //
				"    switch: xswitch", //
				"    transient: xtransient"));
		define(GenTemplateProperty.swaggerCodegenProvider());
		if (info != null) {
			define(property().named(StandardProperties.DESCRIPTION) //
					.withValue(String.format("Provider: %s\nGenerator Name: %s\nType: %s\nPackage: %s\nClassname: %s",
							"Swagger Codegen", info.getReportedName(), info.getType(), wrapper.getPackageName(),
							wrapper.getSimpleName())));
			define(property().named(StandardProperties.GENERATOR_TYPE).withValue(info.getType().name()));
		}
	}

	public static class Generator extends GenTemplate.StaticGenerator<Swagger> {

		private GenModuleWrapper<CodegenConfig> wrapper;

		public Generator(GenTemplate<Swagger> genTemplate, GenTemplateContext context,
				GenModuleWrapper<CodegenConfig> wrapper) {
			super(genTemplate, context);
			this.wrapper = wrapper;
		}

		protected ClientOptInput createCodeGenConfig() throws GenerationException {
			CodegenConfig swaggerCodegen;
			try {
				swaggerCodegen = wrapper.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new GenerationException("Failed to instantiate Swagger Codegen instance", e);
			}
			swaggerCodegen.setOutputDir(context.getOutputDirectory().getAbsolutePath());

			@SuppressWarnings("unchecked")
			Map<String, String> config = (Map<String, String>) context.getGenTargetParameters()
					.get(SWAGGER_CODEGEN_CONFIG);
			if (config == null) {
				config = Maps.newHashMap();
			}

			setCodegenOptions(swaggerCodegen, context.getGenTargetParameters());
			addParameters(config, context.getGenTargetParameters());

			ClientOptInput clientOptInput = new ClientOptInput();
			clientOptInput.setConfig(swaggerCodegen);
			ClientOpts clientOpts = new ClientOpts();
			clientOpts.setOutputDirectory(context.getOutputDirectory().getAbsolutePath());
			clientOpts.setProperties(config != null ? config : Maps.<String, String>newHashMap());
			clientOptInput.setOpts(clientOpts);

			return clientOptInput;
		}

		@Override
		public void generate(Swagger model) throws GenerationException {
			ClientOptInput clientOptInput = createCodeGenConfig();
			clientOptInput.setSwagger(model);

			io.swagger.codegen.Generator generator = new DefaultGenerator();
			@SuppressWarnings("unchecked")
			Map<String, String> systemProperties = (Map<String, String>) context.getGenTargetParameters()
					.get(SWAGGER_CODEGEN_SYSTEM_PROPERTIES);
			setSystemProperties(systemProperties);
			generator.opts(clientOptInput);
			reportScgVersion();
			generator.generate();
		}

		private void addParameters(Map<String, String> config, Map<String, Object> params) {
			for (String key : params.keySet()) {
				if (!SPECIAL_PARAMS.contains(key)) {
					Object value = params.get(key);
					if (value != null && (value instanceof String || value instanceof Boolean)) {
						config.put(key, value.toString());
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void setCodegenOptions(CodegenConfig config, Map<String, Object> parameters) {
			try {
				Map<String, String> instantiationTypes = (Map<String, String>) parameters.get(INSTANTIATION_TYPES);
				if (instantiationTypes != null) {
					config.instantiationTypes().putAll(instantiationTypes);
				}
			} catch (ClassCastException e) {
				logger.error("The defined  instantiationTypes are invalid and are ignored by the generator");
			}

			try {
				Map<String, String> typeMappings = (Map<String, String>) parameters.get(TYPE_MAPPINGS);
				if (typeMappings != null) {
					config.typeMapping().putAll(typeMappings);
				}
			} catch (ClassCastException e) {
				logger.error("The defined typeMappings are invalid and are ignored by the generator");
			}

			try {
				Map<String, String> importMappings = (Map<String, String>) parameters.get(IMPORT_MAPPINGS);
				if (importMappings != null) {
					config.importMapping().putAll(importMappings);
				}
			} catch (ClassCastException e) {
				logger.error("The defined importMappings are invalid and are ignored by the generator");
			}

			try {
				Map<String, String> reservedWordsMappings = (Map<String, String>) parameters
						.get(RESERVED_WORDS_MAPPINGS);
				if (reservedWordsMappings != null) {
					config.reservedWordsMappings().putAll(reservedWordsMappings);
				}
			} catch (ClassCastException e) {
				logger.error("The defined reservedWordsMappings are invalid and are ignored by the generator");
			}

			try {
				Collection<String> languageSpecificPrimitives = (Collection<String>) parameters
						.get(LANGUAGE_SPECIFIC_PRIMITIVES);
				if (languageSpecificPrimitives != null) {
					config.languageSpecificPrimitives().addAll(languageSpecificPrimitives);
				}
			} catch (ClassCastException e) {
				logger.error("The defined languageSpecificPrimitives are invalid and are ignored by the generator");
			}
		}

		private void setSystemProperties(Map<String, String> properties) {
			if (properties != null) {
				for (String key : properties.keySet()) {
					System.setProperty(key, properties.get(key));
				}
			}
		}

		private void reportScgVersion() {
			context.getLogger().info(String.format("Using swagger-codegen v%s\n",
					CodegenConfig.class.getPackage().getImplementationVersion()));
		}
	}
}
