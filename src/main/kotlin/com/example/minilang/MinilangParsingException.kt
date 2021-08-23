package com.example.minilang

import org.antlr.v4.runtime.IntStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import java.io.InputStream
import java.lang.RuntimeException


class MinilangParsingException(val expectedToken:String, recognizer: Recognizer<*, *>, input: IntStream, ctx: ParserRuleContext)
           : RecognitionException(recognizer, input, ctx) {

}