package com.austen.abc_aop_plugin

import com.austen.abc_aop_plugin.config.AbcAopConfig
import com.austen.abc_aop_plugin.plugin.TransformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class AbcAopPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("abcAopConfig", AbcAopConfig::class.java)

        TransformPlugin.apply(project)
    }
}