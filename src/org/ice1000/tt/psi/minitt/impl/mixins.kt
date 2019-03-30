package org.ice1000.tt.psi.minitt.impl

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import icons.TTIcons
import org.ice1000.tt.orFalse
import org.ice1000.tt.orTrue
import org.ice1000.tt.psi.minitt.*
import org.ice1000.tt.psi.treeWalkUp

abstract class MiniTTDeclarationExpressionMixin(node: ASTNode) : ASTWrapperPsiElement(node), MiniTTDeclarationExpression {
	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		declaration.processDeclarations(processor, state, lastParent, place)
}

abstract class MiniTTDeclarationMixin(node: ASTNode) : ASTWrapperPsiElement(node), MiniTTDeclaration {
	override fun getNameIdentifier(): PsiElement? = pattern
	override fun getIcon(flags: Int) = TTIcons.MINI_TT
	override fun getName(): String? = nameIdentifier?.text
	@Throws(IncorrectOperationException::class)
	override fun setName(newName: String): PsiElement {
		val newPattern = MiniTTTokenType.createPattern(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName")
		pattern?.replace(newPattern)
		return this
	}

	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		nameIdentifier?.let { processor.execute(it, state) }.orTrue()
}

abstract class MiniTTVariableMixin(node: ASTNode) : MiniTTExpressionImpl(node), MiniTTVariable, PsiPolyVariantReference {
	override fun isSoft() = true
	override fun getRangeInElement() = TextRange(0, textLength)

	override fun getReference() = this
	override fun getReferences() = arrayOf(reference)
	override fun isReferenceTo(reference: PsiElement) = reference == resolve()
	override fun getCanonicalText(): String = text
	override fun resolve(): PsiElement? = multiResolve(false).firstOrNull()?.run { element }
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val file = element.containingFile ?: return emptyArray()
		if (!element.isValid || element.project.isDisposed) return emptyArray()
		return ResolveCache.getInstance(element.project)
			.resolveWithCaching(this, resolver, true, incompleteCode, file)
	}

	override fun getElement() = this
	override fun bindToElement(element: PsiElement): PsiElement = throw IncorrectOperationException("Unsupported")
	override fun handleElementRename(newName: String) =
		replace(MiniTTTokenType.createVariable(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName"))

	override fun getVariants(): Array<LookupElementBuilder> {
		val variantsProcessor = CompletionProcessor(true)
		treeWalkUp(variantsProcessor, element, element.containingFile)
		return variantsProcessor.candidateSet.toTypedArray()
	}

	private companion object ResolverHolder {
		private val resolver = ResolveCache.PolyVariantResolver<MiniTTVariableMixin> { ref, incompleteCode ->
			resolveWith(SymbolResolveProcessor(ref.canonicalText, incompleteCode), ref)
		}

		private inline fun <reified T> resolveWith(processor: ResolveProcessor<T>, ref: MiniTTVariableMixin): Array<T> {
			val file = ref.element.containingFile ?: return emptyArray()
			treeWalkUp(processor, ref.element, file)
			return processor.candidateSet.toTypedArray()
		}
	}

	abstract class ResolveProcessor<ResolveResult> : PsiScopeProcessor {
		abstract val candidateSet: ArrayList<ResolveResult>
		override fun handleEvent(event: PsiScopeProcessor.Event, o: Any?) = Unit
		override fun <T : Any?> getHint(hintKey: Key<T>): T? = null
		protected val PsiElement.hasNoError get() = !PsiTreeUtil.hasErrorElements(this)
	}

	class SymbolResolveProcessor(
		@JvmField private val name: String,
		private val incompleteCode: Boolean
	) : ResolveProcessor<PsiElementResolveResult>() {
		override val candidateSet = ArrayList<PsiElementResolveResult>(3)
		override fun execute(element: PsiElement, resolveState: ResolveState) = when {
			candidateSet.isNotEmpty() -> false
			element is MiniTTPattern -> {
				val accessible = element.text == name
				if (accessible) candidateSet += PsiElementResolveResult(element, true)
				!accessible
			}
			else -> true
		}
	}

	class CompletionProcessor(
		private val incompleteCode: Boolean
	) : ResolveProcessor<LookupElementBuilder>() {
		override val candidateSet = ArrayList<LookupElementBuilder>(3)
		override fun execute(element: PsiElement, resolveState: ResolveState): Boolean {
			if (element !is MiniTTPattern) return true
			candidateSet += LookupElementBuilder
				.create(element)
				.withIcon(TTIcons.MINI_TT)
			// .withTypeText(type, true)
			return true
		}
	}
}

