package com.reprezen.genflow.openapi.generator

import com.google.common.io.Resources
import com.reprezen.genflow.api.target.GenTargetUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.*

class JavaClientCodeGenTest {

	@Test
	def void testDefault() {
		val url = Resources.getResource("fixtures/default/JavaClient.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
		val modelFolder = new File(generatedFolder, "src/main/java/org/openapitools/client/model")

		assertTrue(Files.list(modelFolder.toPath).anyMatch["Pet.java".equals(it.toFile.name)])
	}

	@Test
	def void testLanguageSpecificPrimitives() {
		val url = Resources.getResource("fixtures/languageSpecificPrimitives/JavaClient.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
		val modelFolder = new File(generatedFolder, "src/main/java/org/openapitools/client/model")
		
		val files = Files.list(modelFolder.toPath)
		assertTrue(files.noneMatch["Owner.java".equals(it.toFile.name)])
	
		val file = Files.list(modelFolder.toPath).filter["Pet.java".equals(it.toFile.name)].findFirst
		assertTrue(file.present)
		
		val fileContents = new String(Files.readAllBytes(file.get))
		assertTrue(fileContents.contains("org.my.Owner"))
	}

	@Test @Ignore
	def void testInstantiationTypes() {
		val url = Resources.getResource("fixtures/instantiationTypes/JavaClient.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
		val modelFolder = new File(generatedFolder, "src/main/java/org/openapitools/client/model")
		
		val file = Files.list(modelFolder.toPath).filter["Pet.java".equals(it.toFile.name)].findFirst
		assertTrue(file.present)
		
		val fileContents = new String(Files.readAllBytes(file?.get))
		assertTrue(fileContents.contains("this.owners = new HashSet<Owner>();"))
	}
	
	@Test
	def void testTypeMappings() {
		val url = Resources.getResource("fixtures/typeMappings/JavaClient.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
		val modelFolder = new File(generatedFolder, "src/main/java/org/openapitools/client/model")
		
		val file = Files.list(modelFolder.toPath).filter["Pet.java".equals(it.toFile.name)].findFirst
		assertTrue(file.present)
		
		val fileContents = new String(Files.readAllBytes(file.get))
		assertTrue(fileContents.contains("private Set<Owner> owners"))
	}
	
	@Test
	def void testReservedWordsMappings() {
		val url = Resources.getResource("fixtures/reservedWordsMappings/JavaClient.gen")

		val genTarget = GenTargetUtils.load(Paths.get(url.toURI()).toFile())

		val result = genTarget.execute(Logger.getLogger("test"))
		val generatedFolder = new File(result.baseDirectory, "generated")
		val modelFolder = new File(generatedFolder, "src/main/java/org/openapitools/client/model")
		
		val file = Files.list(modelFolder.toPath).filter["Pet.java".equals(it.toFile.name)].findFirst
		assertTrue(file.present)
		
		val fileContents = new String(Files.readAllBytes(file.get))
		assertTrue(fileContents.contains("private Boolean xswitch"))
	}
}
