package com.example.minilang

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.Interval
import org.antlr.v4.runtime.misc.Utils

class VerboseListener : BaseErrorListener() {

    val errors = arrayListOf<DetailedParsingError>()

    override fun syntaxError(recognizer: Recognizer<*, *>,
                             offendingSymbol: Any?,
                             line: Int, charPositionInLine: Int,
                             msg: String,
                             e: RecognitionException?) {
        // val stack: List<String> = (recognizer as Parser).getRuleInvocationStack()
        // Collections.reverse(stack)
       //  System.err.println("rule stack: $stack")
//        System.err.println("line " + line + ":" + charPositionInLine + " at " +
//                offendingSymbol + ": " + msg)
//

        if(e != null) {
            System.err.println("Type d'erreur : " + e.javaClass)
            when(e) {
                is LexerNoViableAltException -> {
                    var symbol: String? = ""
                    if (e.startIndex >= 0 && e.startIndex < e.getInputStream().size()) {
                        symbol = e.inputStream.getText(Interval.of(e.startIndex, e.startIndex))
                        symbol = Utils.escapeWhitespace(symbol, false)
                    }
                    val detailedError = DetailedParsingError("?",
                            charPositionInLine,
                            "la requête est invalide à cause du caractère $symbol",
                            "composer une requête valide",
                            "jean dupont")
                    errors.add(detailedError)

                }

                is InputMismatchException -> {
                    // e.message

                    val detailedError = DetailedParsingError("?",
                            charPositionInLine,
                            e.message ?: "pas de message",
                            "faire une requête valide"

                            )

                    errors.add(detailedError)
                }

                is MinilangParsingException -> {
                    val detailedError = DetailedParsingError(e.expectedToken,
                            charPositionInLine,
                            "la requête est malformée car un caractère '${e.expectedToken}' était attendu",
                            "ajouter un '${e.expectedToken}'",
                            "(dupont ET durand)")
                    errors.add(detailedError)
                }

                else ->
                    //TODO a gérer !!
                    e.printStackTrace()

            }
        }

        // TODO extraneous messages ne sont pas lancés par une Exception...
        //  voir le singleTokenDeletion et le reportUnwantedToken
        System.err.println(msg)

        // val error = e as MinilangParsingException


        // errors.add(detailedError)
        //System.err.println(msg + " Position " + charPositionInLine)

    }
}

data class DetailedParsingError(val expectedToken: String, val position: Int, var message: String = "", var resolution: String = "", var example: String = "")