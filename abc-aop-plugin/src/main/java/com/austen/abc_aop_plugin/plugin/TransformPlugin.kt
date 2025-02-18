package com.austen.abc_aop_plugin.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.austen.abc_aop_plugin.config.AbcAopConfig
import com.austen.abc_aop_plugin.tasks.AssembleAbcAopTask
import com.austen.abc_aop_plugin.utils.Utils.adapterOSPath
import org.gradle.api.Project

object TransformPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            return
        }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            val androidAopConfig = project.extensions.getByType(AbcAopConfig::class.java)

            androidAopConfig.initConfig()


            val buildTypeName = variant.buildType

            if (androidAopConfig.enabled) {

                val task = project.tasks.register(
                    "${variant.name}AssembleAbcAopTask",
                    AssembleAbcAopTask::class.java
                ) {
                    it.reflectInvokeMethod = false
                    it.reflectInvokeMethodStatic = false
                    it.variant = variant.name
                }

                variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                    .use(task)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        AssembleAbcAopTask::allJars,
                        AssembleAbcAopTask::allDirectories,
                        AssembleAbcAopTask::output,
                    )
            }
        }
    }
}