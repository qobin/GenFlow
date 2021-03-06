package com.reprezen.genflow.openapi3.doc

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.reprezen.kaizen.oasparser.model3.Example
import com.reprezen.kaizen.oasparser.model3.MediaType

class ExamplesHelper implements Helper {

	extension HtmlHelper htmlHelper
	extension DocHelper docHelper

	val static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory)

	override init() {
		htmlHelper = HelperHelper.htmlHelper
		docHelper = HelperHelper.docHelper
	}

	def <T extends Example> String renderExamples(MediaType mediaType) {
		mediaType.examples?.keySet?.map[name|
			mediaType.examples.get(name).render(name)
		]?.examplesSection
	}

	def String renderExample(MediaType mediaType) {
		mediaType.example?.exampleText?.exampleSection
	}

	def private String render(Example example, String name) {
		'''
			<dt>«name»</dt>
			<dd>
				«IF example.summary !== null»<p><em>«example.summary.htmlEscape»</em></p>«ENDIF»
				«IF example.description !== null»«example.description.docHtml»«ENDIF»
				«IF example.externalValue !== null»<p><em>External Value: «example.externalValue.htmlEscape»</em></p>«ENDIF»
				«example?.value?.exampleText»
			</dd>
		'''
	}

	def private String exampleText(Object value) {
		val content = switch (value) {
			String:
				value
			default:
				yamlMapper.writerWithDefaultPrettyPrinter.writeValueAsString(yamlMapper.convertValue(value, JsonNode))
		}
		'''
		<pre class="remove-xtend-indent">
		«content.htmlEscape»
		</pre>'''
	}

	def private String exampleSection(String html) {
		'''
			<h4>Example</h4>
			«html»
		'''
	}

	def private String examplesSection(Iterable<String> examples) {
		if (examples.empty) ""
		else 
			'''
				<h4>Examples</h4>
				<dl>
					«FOR example : examples»
						«example»
					«ENDFOR»
				</dl>
			'''
	}

}
