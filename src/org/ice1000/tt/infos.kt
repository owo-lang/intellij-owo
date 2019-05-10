package org.ice1000.tt

import com.intellij.CommonBundle
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.FileViewProvider
import icons.TTIcons
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*

object VoileFileType : LanguageFileType(VoileLanguage.INSTANCE) {
	override fun getDefaultExtension() = VOILE_EXTENSION
	override fun getName() = TTBundle.message("voile.name")
	override fun getIcon() = TTIcons.VOILE_FILE
	override fun getDescription() = TTBundle.message("voile.name.description")
}

object OwOFileType : LanguageFileType(OwOLanguage.INSTANCE) {
	override fun getDefaultExtension() = OWO_EXTENSION
	override fun getName() = TTBundle.message("owo.name")
	override fun getIcon() = TTIcons.OWO_FILE
	override fun getDescription() = TTBundle.message("owo.name.description")
}

class OwOFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, OwOLanguage.INSTANCE) {
	override fun getFileType() = OwOFileType
}

class TTFileTypeFactory : FileTypeFactory() {
	override fun createFileTypes(consumer: FileTypeConsumer) {
		consumer.consume(MiniTTFileType, MINI_TT_EXTENSION)
		consumer.consume(ACoreFileType, AGDA_CORE_EXTENSION)
		consumer.consume(OwOFileType, OWO_EXTENSION)
		consumer.consume(AgdaFileType, AGDA_EXTENSION)
		consumer.consume(VoileFileType, VOILE_EXTENSION)
		consumer.consume(MLPolyRFileType, MLPOLYR_EXTENSION)
		consumer.consume(RedPrlFileType, RED_PRL_EXTENSION)
	}
}

object TTBundle {
	@NonNls private const val BUNDLE = "org.ice1000.tt.tt-bundle"
	private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE) }

	@JvmStatic
	fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
		CommonBundle.message(bundle, key, *params)
}
