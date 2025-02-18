package com.austen.abc_aop_plugin.tasks

import javassist.ClassPool
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

abstract class AssembleAbcAopTask : DefaultTask() {

    @get:Input
    abstract var variant: String

    @get:Input
    abstract var reflectInvokeMethod: Boolean

    @get:Input
    abstract var reflectInvokeMethodStatic: Boolean


    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>


    @get:OutputFile
    abstract val output: RegularFileProperty


    @TaskAction
    fun taskAction() {
        val pool = ClassPool(ClassPool.getDefault())

        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )

        allJars.get().forEach { file ->
            println("handling " + file.asFile.absolutePath)
            val jarFile = JarFile(file.asFile)

            jarFile.entries().iterator().forEach innerContinue@{ jarEntry ->
                println("Adding from jar ${jarEntry.name}")
                if (jarEntry.isDirectory || jarEntry.name.isEmpty() || jarEntry.name.startsWith("META-INF/") || "module-info.class" == jarEntry.name) {
                    return@innerContinue
                }
                jarOutput.putNextEntry(JarEntry(jarEntry.name))
                jarFile.getInputStream(jarEntry).use {
                    it.copyTo(jarOutput)
                }
                jarOutput.closeEntry()
            }
            jarFile.close()
        }
        allDirectories.get().forEach { directory ->
            println("handling " + directory.asFile.absolutePath)
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (file.name.endsWith("AA.class")) {
                        println("Found $file.name")
                        val interfaceClass =
                            pool.makeInterface("com.example.abcaop.SomeInterface")
                        println("Adding $interfaceClass")
                        jarOutput.putNextEntry(JarEntry("com/example/abcaop/SomeInterface.class"))
                        jarOutput.write(interfaceClass.toBytecode())
                        jarOutput.closeEntry()
                        val ctClass = file.inputStream().use {
                            pool.makeClass(it);
                        }
                        ctClass.addInterface(interfaceClass)

                        val m = ctClass.getDeclaredMethod("toString");
                        if (m != null) {
                            m.insertBefore("{ System.out.println(\"Some Extensive Tracing\"); }");

                            val relativePath =
                                directory.asFile.toURI().relativize(file.toURI()).getPath()
                            jarOutput.putNextEntry(
                                JarEntry(
                                    relativePath.replace(
                                        File.separatorChar,
                                        '/'
                                    )
                                )
                            )
                            jarOutput.write(ctClass.toBytecode())
                            jarOutput.closeEntry()
                        } else {
                            val relativePath =
                                directory.asFile.toURI().relativize(file.toURI()).getPath()
                            println(
                                "Adding from directory ${
                                    relativePath.replace(
                                        File.separatorChar,
                                        '/'
                                    )
                                }"
                            )
                            jarOutput.putNextEntry(
                                JarEntry(
                                    relativePath.replace(
                                        File.separatorChar,
                                        '/'
                                    )
                                )
                            )
                            file.inputStream().use { inputStream ->
                                inputStream.copyTo(jarOutput)
                            }
                            jarOutput.closeEntry()
                        }
                    }
                }
            }
        }
        jarOutput.close()
    }


}