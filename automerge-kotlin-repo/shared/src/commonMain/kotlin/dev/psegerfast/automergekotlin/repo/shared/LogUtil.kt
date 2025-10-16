package dev.psegerfast.automergekotlin.repo.shared

import dev.psegerfast.automergekotlin.repo.shared.ClassLogger.MethodLogger
import dev.psegerfast.automergekotlin.repo.shared.ClassLogger.MethodLogger.SectionLogger

interface ClassLogger {
    fun log(msg: String)

    fun method(methodName: String): MethodLogger

    fun <T> withMethod(methodName: String, block: MethodLogger.() -> T): T

    fun <T> logWithMethod(methodName: String, block: MethodLogger.() -> T): T

    fun logMethod(methodName: String)

    interface MethodLogger {
        fun log(msg: String)
        fun section(sectionName: String): SectionLogger
        fun withSection(sectionName: String, block: SectionLogger.() -> Unit)

        interface SectionLogger {
            fun log(msg: String)
        }
    }
}

fun classLogger(className: String): ClassLogger = SuperLoggerImpl(className)

class SuperLoggerImpl(private val className: String) : ClassLogger {
    override fun log(msg: String) {
        println("[$className] $msg")
    }

    override fun logMethod(methodName: String) {
        println("[$className#$methodName]")
    }

    private fun logMethodEnd(methodName: String) {
        println("[-END-- $className#$methodName --END-]")
    }

    override fun <T> withMethod(methodName: String, block: MethodLogger.() -> T): T {
        return method(methodName).block()
    }

    override fun <T> logWithMethod(
        methodName: String,
        block: MethodLogger.() -> T
    ): T {
        logMethod(methodName)
        return withMethod(methodName, block).also {
            logMethodEnd(methodName)
        }
    }

    override fun method(methodName: String): MethodLogger = object : MethodLogger {
        override fun log(msg: String) {
            println("[$className#$methodName] $msg")
        }

        override fun section(sectionName: String): SectionLogger {
            return object : SectionLogger {
                override fun log(msg: String) {
                    println("[$className#$methodName - $sectionName] $msg")
                }
            }
        }

        override fun withSection(
            sectionName: String,
            block: SectionLogger.() -> Unit
        ) {
            return section(sectionName).block()
        }

    }
}
