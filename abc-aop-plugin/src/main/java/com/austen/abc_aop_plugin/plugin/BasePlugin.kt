package com.austen.abc_aop_plugin.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BasePlugin : Plugin<Project> {
    private lateinit var pluginConfig: PluginConfig
    private fun init(project: Project) {
        pluginConfig = PluginConfig(project)
    }

    override fun apply(project: Project) {
        init(project)
    }
}