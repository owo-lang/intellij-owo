package org.ice1000.tt.psi.cubicaltt

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import icons.TTIcons
import org.ice1000.tt.orTrue
import org.ice1000.tt.psi.GeneralNameIdentifier

abstract class CubicalTTModuleMixin : StubBasedPsiElementBase<CubicalTTModuleStub>, CubicalTTModule {
	constructor(node: ASTNode) : super(node)
	constructor(stub: CubicalTTModuleStub, type: IStubElementType<*, *>) : super(stub, type)
	constructor(stub: CubicalTTModuleStub, type: IElementType, node: ASTNode) : super(stub, type, node)

	override fun getNameIdentifier() = nameDecl
	override fun setName(newName: String): PsiElement {
		CubicalTTTokenType.createNameDecl(newName, project)?.let { nameDecl?.replace(it) }
		return this
	}

	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		declList.asReversed().all { it.processDeclarations(processor, state, lastParent, place) }
}

abstract class CubicalTTDeclMixin : StubBasedPsiElementBase<CubicalTTDeclStub>, CubicalTTDecl, PsiNameIdentifierOwner {
	constructor(node: ASTNode) : super(node)
	constructor(stub: CubicalTTDeclStub, type: IStubElementType<*, *>) : super(stub, type)
	constructor(stub: CubicalTTDeclStub, type: IElementType, node: ASTNode) : super(stub, type, node)

	override fun getNameIdentifier() = findChildByClass(CubicalTTNameDecl::class.java)
	override fun setName(newName: String): PsiElement {
		CubicalTTTokenType.createNameDecl(newName, project)?.let { nameIdentifier?.replace(it) }
		return this
	}

	override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement) =
		nameIdentifier?.processDeclarations(processor, state, lastParent, place).orTrue()
}

abstract class CubicalTTNameDeclMixin(node: ASTNode) : GeneralNameIdentifier(node), CubicalTTNameDecl {
	override fun getIcon(flags: Int) = TTIcons.CUBICAL_TT_FILE
	@Throws(IncorrectOperationException::class)
	override fun setName(newName: String) = replace(CubicalTTTokenType.createNameDecl(newName, project)
		?: throw IncorrectOperationException("Invalid name: $newName"))
}