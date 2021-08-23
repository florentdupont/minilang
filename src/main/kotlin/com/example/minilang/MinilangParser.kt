package com.example.minilang

import com.github.ajalt.mordant.TermColors
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.PredictionMode
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

object MinilangParserUtils {

    val t = TermColors()
    
    @JvmStatic
    fun parseAndPrint(input:String) : String {
        val errorListener = VerboseListener()

        val stream = ByteArrayInputStream(input.toByteArray(StandardCharsets.UTF_8))
        val lexer = MinilangLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8))
        lexer.removeErrorListeners()

        lexer.addErrorListener(errorListener)

        val parser = MinilangParser(CommonTokenStream(lexer))
        parser.errorHandler = MinilangErrorStrategy()
        parser.removeErrorListeners(); // enleve le ConsoleErrorListener qui est présent par défaut

        parser.addErrorListener(errorListener)
        parser.interpreter.predictionMode = PredictionMode.LL_EXACT_AMBIG_DETECTION

        val result = MinilangPrinter().visitQuery(parser.query())

        //println("" + parser.numberOfSyntaxErrors)

        // les erreurs de syntaxes.
        var nbErrors = 0
        val errorMessages = arrayListOf<String>()
        errorListener.errors.forEach { e ->
            println(t.red(e.message))
            errorMessages += e.message
            input.map{it.toString()}.forEach { print(t.white(it)) }
            println()
            (0 until e.position).forEach { print(t.gray(""+it % 10)) }
            println(t.brightYellow("^"))
            nbErrors++
        }

        if(nbErrors != 0)
            throw MinilangSyntaxException(errorMessages)


        return result
    }
    
}