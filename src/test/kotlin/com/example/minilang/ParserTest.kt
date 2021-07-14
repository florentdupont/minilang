package com.example.minilang

import com.example.minilang.MinilangLexer
import com.example.minilang.MinilangParser
import com.example.minilang.MinilangPrinter
import com.github.ajalt.mordant.TermColors
import org.antlr.v4.runtime.CharStreams.fromStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets.UTF_8





class ParserTest {

    val t = TermColors()

    /**
     * Dans la plupart des tests ici, l'utilisation du MinilangPrinter DOIT retourner le
     * même résultat que ce qui est lu.
     * En réalité, le texte 'input' est lu, parsé, puis ré-écrit (et éventuellement translaté).
     */

   @Test
   fun `some simple queries`() {
       var input = "john smith"
       var output = parseAndPrint(input)
       assertThat(output).isEqualTo("john smith")

       input = "john ET smith OU jane"
       output = parseAndPrint(input)
       assertThat(output).isEqualTo("john AND smith OR jane")

        input = "john (smith OU jane)"
        output = parseAndPrint(input)
        assertThat(output).isEqualTo("john (smith OR jane)")

        input = "john (smith OU (jane ET foo) OU -bar)"
        output = parseAndPrint(input)
        assertThat(output).isEqualTo("john (smith OR (jane AND foo) OR -bar)")

   }

    @Test
    fun `use of spaces and grouping inside the queries`() {
        // tous les examples sont syntaxiquement corrects, même s'ils ont une forme bizzare
        // que l'utilisateur devrait éviter pour garder un requête lisible
        var input = "john(smithOUjane)"
        var output = parseAndPrint(input)
        assertThat(output).isEqualTo("john (smithOUjane)")

        input = "name:john(smith OU jane)"
        output = parseAndPrint(input)
        assertThat(output).isEqualTo("name:john (smith OR jane)")

        input = "(a)((((b)(c)d)e)f)"
        output = parseAndPrint(input)
        assertThat(output).isEqualTo("(a) ((((b) (c) d) e) f)")
    }

    @Test
    fun `advanced valid queries`() {
        val input = "nom:john ET (annee:(>2000 ET <2010) OU reference:(ABC~ OU XYZ*)) ET \"john smith\" "
        val output = parseAndPrint(input)
        assertThat(output).isEqualTo("nom:john AND (annee:(>2000 AND <2010) OR reference:(ABC~ OR XYZ*)) AND \"john smith\"")
    }

    @Test
    fun `syntax errors`() {
        var input = "foo (.."
        var output = parseAndPrint(input)
       //  assertThat(output).isEqualTo("nom:john AND (annee:(>2000 AND <2010) OR reference:(ABC~ OR XYZ*)) AND \"john smith\"")
        println(output)

        input = "123! 654? ET (32"
        output = parseAndPrint(input)


        println("----")
        input = "john A:"
        output = parseAndPrint(input)
        println(output)  // InputMismatchException


        println("----")
        input = "A ET ET bla"  // pas d'erreur ici?
        output = parseAndPrint(input)
        println(output) // TODO devrait lever une erreur !!!

        println("----")
        input = "john A::bla"  // pas d'erreur ici?
        // extraneous chars are not
        output = parseAndPrint(input)
        println(output) // TODO devrait lever une erreur !!!

        println("----")
        input = "AND AND OR"  // InputMismatchException
        output = parseAndPrint(input)
        println(output)
    }



    @Test
    fun `a field with restricted chars should throw an exception`() {
        val input = "n&ame:t&st~3 AND FRANCE OR pays:(FRANCE OR ALLEMAGNE)"

        assertThatExceptionOfType(ParseCancellationException::class.java).isThrownBy {
            parseAndPrint(input)
        }

//        val json = TreeUtils().toJson(parser.query())
//        println(json)



    }





    fun parseAndPrint(input:String) : String {
        val errorListener = VerboseListener()

        val stream = ByteArrayInputStream(input.toByteArray(UTF_8))
        val lexer = MinilangLexer(fromStream(stream, UTF_8))
        lexer.removeErrorListeners()

        lexer.addErrorListener(errorListener)

        val parser = MinilangParser(CommonTokenStream(lexer))
        parser.errorHandler = MinilangErrorStrategy()
        parser.removeErrorListeners(); // enleve le ConsoleErrorListener qui est présent par défaut

        parser.addErrorListener(errorListener)
        parser.interpreter.predictionMode = PredictionMode.LL_EXACT_AMBIG_DETECTION

        val result = MinilangPrinter().visitQuery(parser.query())

        //println("" + parser.numberOfSyntaxErrors)

        errorListener.errors.forEach { e ->
            println(t.red(e.message))
            input.map{it.toString()}.forEach { print(t.white(it)) }
            println()
            (0 until e.position).forEach { print(t.gray(""+it % 10)) }
            println(t.brightYellow("^"))
        }


        return result
    }
}