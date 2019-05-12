package org.ice1000.tt.psi.redprl

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.IncorrectOperationException
import icons.TTIcons
import org.ice1000.tt.psi.*
import org.ice1000.tt.psi.redprl.impl.RedPrlMlValueImpl

abstract class RedPrlDeclaration(node: ASTNode) : GeneralDeclaration(node) {
	override val type: PsiElement? get() = null
	override fun getIcon(flags: Int) = TTIcons.RED_PRL
	@Throws(IncorrectOperationException::class)
	override fun setName(newName: String): PsiElement {
		val newPattern = RedPrlTokenType.createOpDecl(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName")
		nameIdentifier?.replace(newPattern)
		return this
	}
}

interface RedPrlOpOwner : PsiElement {
	val opDecl: RedPrlOpDecl?
	/**
	 * Because I am supposed to let [RedPrlOpOwnerMixin]  extend [RedPrlMlCmd]
	 * but I cannot
	 */
	val mlCmd: RedPrlMlCmd?
}

abstract class RedPrlOpOwnerMixin(node: ASTNode) : RedPrlDeclaration(node), RedPrlOpOwner {
	override fun getNameIdentifier() = opDecl
	override val mlCmd: RedPrlMlCmd? get() = findChildByClass(RedPrlMlCmd::class.java)
}

abstract class RedPrlOpDeclMixin(node: ASTNode) : GeneralNameIdentifier(node), RedPrlOpDecl {
	override fun getIcon(flags: Int) = TTIcons.MLPOLYR
	@Throws(IncorrectOperationException::class)
	override fun setName(newName: String) =
		replace(RedPrlTokenType.createOpDecl(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName"))
}

abstract class RedPrlOpUsageMixin(node: ASTNode) : RedPrlMlValueImpl(node), RedPrlOpUsage, PsiPolyVariantReference {
	override fun isSoft() = true
	override fun getRangeInElement() = TextRange(0, textLength)

	override fun getElement() = this
	override fun getReference() = this
	override fun getReferences() = arrayOf(reference)
	override fun isReferenceTo(reference: PsiElement) = reference == resolve()
	override fun getCanonicalText(): String = text
	override fun resolve(): PsiElement? = multiResolve(false).firstOrNull()?.run { element }

	override fun bindToElement(element: PsiElement): PsiElement = throw IncorrectOperationException("Unsupported")
	override fun handleElementRename(newName: String): PsiElement? =
		replace(RedPrlTokenType.createOpUsage(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName"))

	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val file = containingFile ?: return emptyArray()
		if (!isValid || project.isDisposed) return emptyArray()
		return ResolveCache.getInstance(project)
			.resolveWithCaching(this, resolver, true, incompleteCode, file)
	}

	override fun getVariants(): Array<LookupElementBuilder> {
		val variantsProcessor = PatternCompletionProcessor({ TTIcons.RED_PRL },
			{ true },
			{ "??" },
			{ pat -> "" })
		treeWalkUp(variantsProcessor, this, containingFile)
		return variantsProcessor.candidateSet.toTypedArray()
	}

	private companion object ResolverHolder {
		// val paramFamily = listOf()

		private val resolver = ResolveCache.PolyVariantResolver<RedPrlOpUsageMixin> { ref, incompleteCode ->
			val name = ref.canonicalText
			resolveWith(PatternResolveProcessor(name) {
				// TODO: non-parameters
				it.text == name // && PsiTreeUtil.isAncestor(PsiTreeUtil.getParentOfType(it, RedPrlMlDecl::class.java)?.mlCmd, ref, false)
			}, ref)
		}
	}
}