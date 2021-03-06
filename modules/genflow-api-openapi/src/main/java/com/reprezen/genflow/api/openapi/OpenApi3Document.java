package com.reprezen.genflow.api.openapi;

import static com.reprezen.genflow.api.normal.openapi.ObjectType.OPENAPI3_MODEL_VERSION;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.normal.openapi.OpenApiNormalizer;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.normal.openapi.Options;
import com.reprezen.genflow.api.util.OpenApiIO;
import com.reprezen.kaizen.oasparser.OpenApi;

import io.swagger.v3.oas.models.OpenAPI;

public class OpenApi3Document extends OpenApiDocument {

	private JsonNode json;
	private File inFile;
	private Options normalizerOptions;
	private static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
	}

	public OpenApi3Document(String modelSpec, File inFile, Option[] normalizerOptions) throws GenerationException {
		this.json = OpenApiIO.loadOpenApi3Tree(modelSpec);
		this.inFile = inFile;
		this.normalizerOptions = new Options(OPENAPI3_MODEL_VERSION, normalizerOptions);
	}

	@Override
	public boolean isOpenApi3() {
		return true;
	}

	@Override
	public OpenAPI asOpenAPI() throws GenerationException {
		return openAPIParse();
	}

	@Override
	public OpenApi<?> asKaizenOpenApi() throws GenerationException {
		return kaizenParse();
	}

	@Override
	public JsonNode asJson() throws GenerationException {
		try {
			return new OpenApiNormalizer(normalizerOptions).of(json).normalizeToJson(inFile.toURI().toURL());
		} catch (IOException e) {
			throw new GenerationException("Failed to normalize model spec: " + inFile, e);
		}
	}

	@Override
	public String asSpec() throws GenerationException, JsonProcessingException {
		return mapper.writeValueAsString(asJson());
	}

	private OpenAPI openAPIParse() throws GenerationException {
		try {
			return new OpenApiNormalizer(normalizerOptions).of(json).normalizeToOpenAPI(inFile.toURI().toURL());
		} catch (Exception e) {
			throw new GenerationException("Failed to parse OpenApi2 document: " + inFile, e);
		}
	}

	private OpenApi<?> kaizenParse() throws GenerationException {
		try {
			return new OpenApiNormalizer(normalizerOptions).of(json).normalizeToKaizen(inFile.toURI().toURL());
		} catch (IOException e) {
			throw new GenerationException("Failed to parse OpenAPI document: " + inFile, e);
		}
	}

	@Override
	public String getTitle() {
		return json.at("/info/title").asText("");
	}
}
