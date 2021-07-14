package com.example.minilang

import org.antlr.v4.runtime.DefaultErrorStrategy
import org.antlr.v4.runtime.Parser

class MinilangErrorStrategy : DefaultErrorStrategy() {

    override fun reportMissingToken(recognizer: Parser?) {

        if (inErrorRecoveryMode(recognizer)) {
            return
        }

        beginErrorCondition(recognizer)

        val t = recognizer!!.currentToken
        val expecting = getExpectedTokens(recognizer)
        val msg = "Un " + expecting.toString(recognizer.vocabulary) + " était attendu."

        val expectedToken = expecting.toString(recognizer.vocabulary)

        val ex = MinilangParsingException(expectedToken, recognizer, recognizer.inputStream, nextTokensContext)

        recognizer.notifyErrorListeners(t, msg, ex)

    }

    override fun reportUnwantedToken(recognizer: Parser) {
        if (inErrorRecoveryMode(recognizer)) {
            return
        }
        beginErrorCondition(recognizer)

        val t = recognizer.currentToken
        val tokenName = getTokenErrorDisplay(t)
        val expecting = getExpectedTokens(recognizer)
        val msg = "extraneous input " + tokenName + " expecting " +
                expecting.toString(recognizer.vocabulary)

        // TODO a mettre sous forme d'exception
        recognizer.notifyErrorListeners(t, msg, null)
    }
}