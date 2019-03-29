package org.ice1000.tt.project

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

interface MiniTTProjectSettingsService {
	val settings: MiniTTSettings
}

interface AgdaProjectSettingsService {
	val settings: AgdaSettings
}

val Project.minittSettings: MiniTTProjectSettingsService
	get() = minittSettingsNullable!!

val Project.minittSettingsNullable: MiniTTProjectSettingsService?
	get() = ServiceManager.getService(this, MiniTTProjectSettingsService::class.java)

val Project.agdaSettings: AgdaProjectSettingsService
	get() = agdaSettingsNullable!!

val Project.agdaSettingsNullable: AgdaProjectSettingsService?
	get() = ServiceManager.getService(this, AgdaProjectSettingsService::class.java)

@State(
	name = "MiniTTProjectSettings",
	storages = [Storage(file = "miniTTConfig.xml")])
class MiniTTProjectSettingsServiceImpl :
	MiniTTProjectSettingsService, PersistentStateComponent<MiniTTSettings> {
	override val settings = MiniTTSettings()
	override fun getState(): MiniTTSettings? = XmlSerializerUtil.createCopy(settings)
	override fun loadState(state: MiniTTSettings) {
		XmlSerializerUtil.copyBean(state, settings)
	}
}

@State(
	name = "AgdaProjectSettings",
	storages = [Storage(file = "agdaConfig.xml")])
class AgdaProjectSettingsServiceImpl :
	AgdaProjectSettingsService, PersistentStateComponent<AgdaSettings> {
	override val settings = AgdaSettings()
	override fun getState(): AgdaSettings? = XmlSerializerUtil.createCopy(settings)
	override fun loadState(state: AgdaSettings) {
		XmlSerializerUtil.copyBean(state, settings)
	}
}