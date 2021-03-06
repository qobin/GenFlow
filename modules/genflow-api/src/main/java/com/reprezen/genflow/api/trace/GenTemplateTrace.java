/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.reprezen.genflow.api.template.GenTemplate;

/**
 * This class represents trace information created from the execution of a
 * GenTarget in producing a given output file.
 * <p>
 * The purpose of trace information is to link inputs to outputs in a way that
 * will be meaningful to consumers of the output. The most common use of trace
 * information is seen when GenTemplates are chained together. A downstream
 * GenTemplate in such an arrangement is generally viewed as operating on some
 * source model, but it makes use of the output generated by an upstream
 * GenTemplate operating on that same model. In utilizing the output of the
 * upstream GenTemplate, it is often critical to understand exactly how
 * structures in the input model relate to structures in the output generated by
 * the upstream GenTemplate, and trace information is designed to make that
 * possible.
 * <p>
 * The overall structure of a trace object is a collection of trace items, each
 * of which links one or more source items to a single output item. The trace
 * item itself describes the single output item, and it contains a number of
 * source item descriptors that describe the source item.
 * <p>
 * A source or output item refers to either a single source/output file as a
 * whole, or some portion of a source/output file, identified in a
 * file-format-specific manner. A source role descriptor also includes a string
 * that can be used to describe the role played by that specific source item in
 * the formation of the output item.
 * 
 * @author Konstantin Zaitsev
 * @date Jun 8, 2015
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public final class GenTemplateTrace {

	/**
	 * Generic item type created for every output file produced by GenTemplates
	 * based on {@link GenTemplate}.
	 */

	public static final String FILE_ITEM_TYPE = "file";

	/**
	 * Generic source role indicating that source data contributed to an output item
	 * in an otherwise unspecified manner.
	 */
	public static final String SOURCE_DATA_ROLE = "sourceData";

	/** Special sourceName to be used for the GenTemplate's primary source. */
	public static final String PRIMARY_SOURCE_NAME = "_primary";

	/**
	 * Special sourceName in source items generated from resources packaged with the
	 * GenTemplate.
	 */
	public static final String RESOURCE_SOURCE_NAME = "_resource";

	// protected fields for better JavaDoc generation

	/** ID of the GenTemplate that was executed by the GenTarget */
	protected String genTemplateId;

	/** Trace items created by the GenTemplate. */
	protected List<GenTemplateTraceItem> traceItems = new ArrayList<>();

	/**
	 * Optional base directory used in relativizing output files during
	 * serialization.
	 * <p>
	 * If this value is set, it is used to relativize the output file path in each
	 * trace item while serializing the trace object to a trace file. When
	 * deserializing a trace file, a base directory can optionally be specified,
	 * which will cause the relative paths for all the trace item output files to be
	 * resolved relative to that directory in the process.
	 */
	@JsonIgnore
	protected File baseDirectory;

	/** @see #genTemplateId */
	public String getGenTemplateId() {
		return genTemplateId;
	}

	/** @see #genTemplateId */
	public void setGenTemplateId(String templateId) {
		this.genTemplateId = templateId;
	}

	/** @see #traceItems */
	public List<GenTemplateTraceItem> getTraceItems() {
		return traceItems;
	}

	/** @see #traceItems */
	public void setTraceItems(List<GenTemplateTraceItem> traceItems) {
		this.traceItems = traceItems;
	}

	/** @see baseDirectory */
	public File getBaseDirectory() {
		return baseDirectory;
	}

	/** @see baseDirectory */
	public void setBaseDirectory(File baseDirectory) {
		if (baseDirectory.exists() && !baseDirectory.isDirectory()) {
			baseDirectory = baseDirectory.getParentFile();
		}
		this.baseDirectory = baseDirectory;
	}

	@Override
	public String toString() {
		return "GenTemplateTrace [genTemplateId=" + genTemplateId + ", baseDirectory=" + baseDirectory + ", traceItems="
				+ traceItems + "]";
	}
}
