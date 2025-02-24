package com.rize2knight.util

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.battles.runner.graal.GraalLogger
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler
import com.cobblemon.mod.relocations.graalvm.polyglot.Context
import com.cobblemon.mod.relocations.graalvm.polyglot.HostAccess
import com.cobblemon.mod.relocations.graalvm.polyglot.PolyglotAccess
import com.cobblemon.mod.relocations.graalvm.polyglot.io.FileSystem
import java.io.File
import java.nio.file.Paths

class GraalTypeChart{
    @kotlin.jvm.Transient
    lateinit var context: Context
    @kotlin.jvm.Transient

    val unbundler = GraalShowdownUnbundler()
    private val filePaths = listOf(
        "showdown/data/typechart.js",
        "showdown/data/mods/cobblemon/typechart.js"
    )

    fun openConnection() {
        unbundler.attemptUnbundle()
        createContext()

        val validFile = filePaths.map(::File).firstOrNull { it.exists() }
        if (validFile != null) { context.eval("js", validFile.readText()) }
        else {
            Cobblemon.LOGGER.error("No valid TypeChart file found. Check your datapacks or file structure.")
        }
    }

    private fun createContext() {
        val wd = Paths.get("./showdown")
        val access = HostAccess.newBuilder(HostAccess.EXPLICIT)
            .allowIterableAccess(true)
            .allowArrayAccess(true)
            .allowListAccess(true)
            .allowMapAccess(true)
            .build()
        context = Context.newBuilder("js")
            .allowIO(true)
            .fileSystem(FileSystem.newDefaultFileSystem())
            .allowExperimentalOptions(true)
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowHostAccess(access)
            .allowCreateThread(true)
            .logHandler(GraalLogger)
            .option("engine.WarnInterpreterOnly", "false")
            .option("js.commonjs-require", "true")
            .option("js.commonjs-require-cwd", "showdown")
            .option(
                "js.commonjs-core-modules-replacements",
                "buffer:buffer/,crypto:crypto-browserify,path:path-browserify"
            )
            .allowHostClassLoading(true)
            .allowNativeAccess(true)
            .allowCreateProcess(true)
            .build()

        context.eval("js", """
            globalThis.process = {
                cwd: function() {
                    return '';
                }
            }
        """.trimIndent())
    }

    fun getTypeChart(typeChart : HashMap<String, HashMap<String, Float>>){
        val cobbleTypeChart = context.getBindings("js").getMember("TypeChart")
        val keys = cobbleTypeChart.memberKeys

        for(defendingType in keys){
            val defendingTypeChart = cobbleTypeChart.getMember(defendingType).getMember("damageTaken")
            val typeDmgMultipliers = HashMap<String, Float>()

            for(typeMatchup in defendingTypeChart.memberKeys){
                val dmg = defendingTypeChart.getMember(typeMatchup).asInt()
                typeDmgMultipliers[typeMatchup.lowercase()] = getDmgMult(dmg)
            }

            typeChart[defendingType.lowercase()] = typeDmgMultipliers
        }
    }

    private fun getDmgMult(index : Int) : Float{
        return when (index) {
            1 -> 2f  // Super Effective
            2 -> 0.5f  // Not effective
            3 -> 0f // Immune
            else -> 1f // Normal damage
        }
    }
}