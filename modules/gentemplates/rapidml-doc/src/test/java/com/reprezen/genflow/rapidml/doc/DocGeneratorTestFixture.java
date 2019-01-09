/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.Monitor;
import org.junit.runner.Description;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.FakeGenTemplateContext;
import com.reprezen.genflow.api.zenmodel.ZenModelSource;
import com.reprezen.genflow.rapidml.doc.xtend.XGenerateDoc;
import com.reprezen.genflow.test.common.GeneratorTestFixture;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.implicit.ZenModelNormalizer;

/**
 * A test fixture for artifacts generated by documentation generator.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * 
 */
public class DocGeneratorTestFixture extends GeneratorTestFixture {

	public DocGeneratorTestFixture(String extension) {
		super(extension);
	}

	@Override
	protected Map<String, String> doGenerate(ZenModel zenModel, File dir, Monitor progressMonitor) throws IOException {
		new ZenModelNormalizer().normalize(zenModel);
		XGenerateDoc generator = new XGenerateDoc();
		generator.init(
				new FakeGenTemplateContext(new ZenModelSource(new File(zenModel.eResource().getURI().toFileString())) {
					@Override
					public ZenModel load(File inFile) throws GenerationException {
						return zenModel;
					}
				}));
		String result = generator.generate(zenModel);
		return Collections.singletonMap("test_doc.html", result); //$NON-NLS-1$
	}

	@Override
	protected String getSampleRestName(Description description) {
		Class<?> class_;
		try {
			// A workaround for java.lang.ClassNotFoundException thrown by
			// description.getMethodClass
			class_ = Class.forName(description.getClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		SampleRestFile result = class_.getAnnotation(SampleRestFile.class);
		return result.value();
	}
}
