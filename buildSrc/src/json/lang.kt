package org.ice1000.tt.gradle.json

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.stringify
import java.io.File

private val sharedJson = Json(JsonConfiguration.Stable)

const val DEFAULT_PKG = "org.ice1000.tt"

@Serializable
class LangData(
	var languageName: String,
	var constantPrefix: String,
	var exeName: String,
	var runConfigInit: String = "",
	var trimVersion: String = "version",
	var generateCliState: Boolean = true,
	var hasVersion: Boolean = true,
	var generateSettings: Boolean = true,
	var supportsParsing: Boolean = false,
	var keywordList: List<String> = emptyList(),
	var highlightTokenPairs: Map<String, String> = emptyMap(),
	var braceTokenPairs: Map<String, String> = emptyMap(),
	var basePackage: String = DEFAULT_PKG
) {
	fun toJson() = sharedJson.stringify(serializer(), this)

	@UseExperimental(ImplicitReflectionSerializer::class)
	companion object SchemaWriter {
		init {
			val map = schema(serializer().descriptor)
			File("build/fyi/schema.json").writeText(sharedJson.stringify(map))
		}
	}
}

fun langGenJson(json: File) = langGenJson(json.readText())
fun langGenJson(json: String) = sharedJson.parse(LangData.serializer(), json)
