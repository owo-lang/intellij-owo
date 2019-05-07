package org.ice1000.tt.psi.mlpolyr

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.ResolveState
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.IncorrectOperationException
import icons.TTIcons
import org.ice1000.tt.orTrue
import org.ice1000.tt.psi.*
import org.ice1000.tt.psi.mlpolyr.impl.MLPolyRExpImpl

abstract class MLPolyRDeclaration(node: ASTNode) : GeneralDeclaration(node) {
	override val type: PsiElement? get() = null
	override fun getIcon(flags: Int) = TTIcons.MLPOLYR
	@Throws(IncorrectOperationException::class)
	override fun setName(newName: String): PsiElement {
		val newPattern = MLPolyRTokenType.createPat(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName")
		nameIdentifier?.replace(newPattern)
		return this
	}
}

abstract class MLPolyRPatListOwnerMixin(node: ASTNode) : MLPolyRDeclaration(node), MLPolyRPatListOwner {
	override fun getNameIdentifier(): PsiElement? = null
	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		patList.asReversed().all { it.processDeclarations(processor, state, lastParent, place) }
			&& super.processDeclarations(processor, state, lastParent, place)
}

abstract class MLPolyRFunctionMixin(node: ASTNode) : MLPolyRPatListOwnerMixin(node), MLPolyRFunction {
	override fun getNameIdentifier() = namePat
}

abstract class MLPolyRCbbPatMixin(node: ASTNode) : MLPolyRGeneralPat(node), MLPolyRCbbPat {
	override fun visit(visitor: (MLPolyRNamePat) -> Boolean) =
		fieldPatternList.all { PsiTreeUtil.findChildrenOfType(it, MLPolyRGeneralPat::class.java).all { it.visit(visitor) } }
}

abstract class MLPolyRNamePatMixin(node: ASTNode) : MLPolyRGeneralPat(node), MLPolyRNamePat {
	override fun visit(visitor: (MLPolyRNamePat) -> Boolean) = visitor(this)
}

abstract class MLPolyRGeneralPat(node: ASTNode) : GeneralNameIdentifier(node), IPattern<MLPolyRNamePat> {
	open val kind: SymbolKind by lazy {
		val parent = parent
		when {
			parent.firstChild?.elementType == MLPolyRTypes.KW_VAL -> SymbolKind.Variable
			parent is MLPolyRRc -> SymbolKind.RcFunction
			parent is MLPolyRFunction ->
				if (this === parent.firstChild) SymbolKind.Function
				else SymbolKind.Parameter
			parent is MLPolyRMr || parent is MLPolyRPat -> SymbolKind.Pattern
			else -> SymbolKind.Unknown
		}
	}

	override fun getIcon(flags: Int) = TTIcons.MLPOLYR
	@Throws(IncorrectOperationException::class)
	override fun setName(newName: String) =
		replace(MLPolyRTokenType.createPat(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName"))

	override fun visit(visitor: (MLPolyRNamePat) -> Boolean): Boolean =
		PsiTreeUtil.findChildrenOfType(this, MLPolyRGeneralPat::class.java).all { it.visit(visitor) }
}

interface MLPolyRPatOwner : PsiElement {
	val pat: MLPolyRPat?
}

interface MLPolyRPatListOwner : PsiElement {
	val patList: List<MLPolyRPat>
}

abstract class MLPolyRRcMixin(node: ASTNode) : MLPolyRDeclaration(node), MLPolyRRc {
	override fun getNameIdentifier() = namePat
}

abstract class MLPolyRPatOwnerMixin(node: ASTNode) : MLPolyRDeclaration(node), MLPolyRPatOwner {
	override fun getNameIdentifier() = pat
}

abstract class MLPolyRLetExpMixin(node: ASTNode) : MLPolyRExpImpl(node), MLPolyRLetExp {
	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		defList.all { it.processDeclarations(processor, state, lastParent, place) }
}

abstract class MLPolyRDefMixin(node: ASTNode) : MLPolyRPatOwnerMixin(node), MLPolyRDef {
	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		functionList.all { it.processDeclarations(processor, state, lastParent, place) }
			&& optRcl?.rcList?.all { it.processDeclarations(processor, state, lastParent, place) }.orTrue()
			&& super.processDeclarations(processor, state, lastParent, place)
}

abstract class MLPolyRCasesExpMixin(node: ASTNode) : MLPolyRDeclaration(node), MLPolyRCasesExp {
	override fun getNameIdentifier(): PsiElement? = null
	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		mrList.all { it.processDeclarations(processor, state, lastParent, place) }
}

abstract class MLPolyRIdentifierMixin(node: ASTNode) : MLPolyRExpImpl(node), MLPolyRIdentifier, PsiPolyVariantReference {
	override fun isSoft() = true
	override fun getRangeInElement() = TextRange(0, textLength)

	override fun getReference() = this
	override fun getReferences() = arrayOf(reference)
	override fun isReferenceTo(reference: PsiElement) = reference == resolve()
	override fun getCanonicalText(): String = text
	override fun resolve(): PsiElement? = multiResolve(false).firstOrNull()?.run { element }

	override fun getElement() = this
	override fun bindToElement(element: PsiElement): PsiElement = throw IncorrectOperationException("Unsupported")
	override fun handleElementRename(newName: String): PsiElement? =
		replace(MLPolyRTokenType.createIdentifier(newName, project)
			?: throw IncorrectOperationException("Invalid name: $newName"))

	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val file = containingFile ?: return emptyArray()
		if (!isValid || project.isDisposed) return emptyArray()
		return ResolveCache.getInstance(project)
			.resolveWithCaching(this, resolver, true, incompleteCode, file)
	}

	override fun getVariants(): Array<LookupElementBuilder> {
		val variantsProcessor = PatternCompletionProcessor(true,
			{ (it as? MLPolyRGeneralPat)?.kind?.icon },
			{
				if ((it as? MLPolyRGeneralPat)?.kind != SymbolKind.Parameter) true
				else PsiTreeUtil.isAncestor(PsiTreeUtil.getParentOfType(it, MLPolyRFunction::class.java)?.exp, this, false)
			},
			{ (it as? MLPolyRGeneralPat)?.kind?.name ?: "??" })
		treeWalkUp(variantsProcessor, element, element.containingFile)
		return variantsProcessor.candidateSet.toTypedArray()
	}

	private companion object ResolverHolder {
		val paramFamily = listOf(SymbolKind.Parameter, SymbolKind.Pattern)

		private val resolver = ResolveCache.PolyVariantResolver<MLPolyRIdentifierMixin> { ref, incompleteCode ->
			val name = ref.canonicalText
			resolveWith(PatternResolveProcessor(name, incompleteCode) {
				if ((it as? MLPolyRGeneralPat)?.kind !in paramFamily) it.text == name
				else it.text == name && PsiTreeUtil.isAncestor(PsiTreeUtil.getParentOfType(it, MLPolyRFunction::class.java)?.exp, ref, false)
			}, ref)
		}
	}
}
