package com.example.minilang

import com.example.minilang.MinilangLexer
import com.example.minilang.MinilangParser
import com.example.minilang.MinilangParserUtils.parseAndPrint
import com.example.minilang.MinilangPrinter
import com.github.ajalt.mordant.TermColors
import org.antlr.v4.runtime.CharStreams.fromStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.InputMismatchException
import org.antlr.v4.runtime.LexerNoViableAltException
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.assertj.core.api.Assertions.*
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
    fun `syntax errors - expression incomplete`() {

        assertThatThrownBy {
            val input = "foo (.."
            val output = parseAndPrint(input)
        }.isInstanceOf(MinilangSyntaxException::class.java)
    }

    @Test
    fun `syntax errors - expression incomplète 2`()  {
        assertThatThrownBy {
            val input = "123! 654? ET (32"
            val output = parseAndPrint(input)
        }.isInstanceOf(MinilangSyntaxException::class.java)
        
        // plusieurs erreurs
        // la requête est invalide à cause du caractère !
        // la requête est invalide à cause du caractère ?
        // la requête est malformée car un caractère '')'' était attendu
        // mais pas d'exception levée 
    }
        
    @Test
    fun `syntax error - subquery incomplète`() {
        assertThatThrownBy {
            val input = "john A:"
            val output = parseAndPrint(input)    // 
        }.isInstanceOf(MinilangSyntaxException::class.java)
    }


    @Test
    fun `syntax error - opérateur en doublon`() {
        val input = "A ET ET bla"  // pas d'erreur ici?
        val output = parseAndPrint(input)
        // 
        // affiche malgré tout un extraneous input 'ET' expecting {'-', '(', QUOTED, TERM}
        // TODO a creuser !!!
    }

    @Test
    fun `syntax error - opérateur de subquery`() {
        val input = "john A::bla"  
        // extraneous chars are not
        val output = parseAndPrint(input)   // TODO devrait lever une erreur !!!
        
        // erreur relevée : extraneous input ':' expecting {'(', '>=', '<=', '>', '<', QUOTED, TERM}
    }


    @Test
    fun `syntax error - que des opérateurs`() {
        assertThatThrownBy {
            val input = "AND AND OR"  // InputMismatchException
            val output = parseAndPrint(input)
        }.isInstanceOf(MinilangSyntaxException::class.java)
            
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





   
}