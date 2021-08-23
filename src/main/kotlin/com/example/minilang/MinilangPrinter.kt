package com.example.minilang

import com.example.minilang.MinilangParser.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.TerminalNode

class MinilangPrinter : MinilangBaseVisitor<String>() {

    override fun visitQuery(ctx: QueryContext?) =  visit(ctx!!.expression())
    override fun visitAndExpr(ctx: AndExprContext?) = visit(ctx!!.left) + " AND " + visit(ctx.right)
    override fun visitSubAndExpr(ctx: SubAndExprContext?) = visit(ctx!!.left) + " AND " + visit(ctx.right)

    override fun visitOrExpr(ctx: OrExprContext?) = visit(ctx!!.left) + " OR " + visit(ctx.right)
    override fun visitSubOrExpr(ctx: SubOrExprContext?) = visit(ctx!!.left) + " OR " + visit(ctx.right)

    // les " sont déjà intégrées dans le QUOTED
    override fun visitQuotedTerm(ctx: QuotedTermContext?) = visit(ctx!!.QUOTED())
    override fun visitQuotedSubTerm(ctx: QuotedSubTermContext?) = visit(ctx!!.QUOTED())

    override fun visitUnknownBoolExpr(ctx: UnknownBoolExprContext?) = visit(ctx!!.left) + " " + visit(ctx.right)
    override fun visitSubUnknownExpr(ctx: SubUnknownExprContext?) = visit(ctx!!.left) + " " + visit(ctx.right)

    override fun visitGroupingExpr(ctx: GroupingExprContext?) = "(" + visit(ctx!!.expression()) + ")"
    override fun visitSubGroupingExpr(ctx: SubGroupingExprContext?) = "(" + visit(ctx!!.subQuery()) + ")"

    override fun visitMinusExpr(ctx: MinusExprContext?) = "-" + visit(ctx!!.expression())
    override fun visitSubMinusExpr(ctx: SubMinusExprContext?) =  "-" + visit(ctx!!.subQuery())

    override fun visitFuzzyQuotedSubTerm(ctx: FuzzyQuotedSubTermContext?) = visit(ctx!!.QUOTED()) + ctx.FUZZY_SLOP()
    override fun visitFuzzyQuotedTerm(ctx: FuzzyQuotedTermContext?) = visit(ctx!!.QUOTED()) + ctx.FUZZY_SLOP()
    override fun visitFuzzySubTerm(ctx: FuzzySubTermContext?) =  visit(ctx!!.TERM()) + ctx.FUZZY_SLOP()
    override fun visitFuzzyTerm(ctx: FuzzyTermContext?) = visit(ctx!!.TERM()) + ctx.FUZZY_SLOP()

    override fun visitWildTerm(ctx: WildTermContext?) = visit(ctx!!.TERM()) + "*"
    override fun visitWildSubTerm(ctx: WildSubTermContext?) = visit(ctx!!.TERM()) + "*"

    
    override fun visitFieldedExpr(ctx: FieldedExprContext?) : String {
        if(ctx!!.fieldedQuery() != null) {
            if(ctx.fieldedQuery().subTerm() != null) {
                return visit(ctx.fieldedQuery().id()) + ":" + visit(ctx.fieldedQuery().subTerm())    
            } else if (ctx.fieldedQuery().subQuery() != null) {
                return visit(ctx.fieldedQuery().id()) + ":(" + visit(ctx.fieldedQuery().subQuery()) + ")"
            } else {
                // une erreur de syntaxe sera levée
            }
        } else {
            // la subquery peut être nulle, si la syntaxe n'est pas respéctée.
            if(ctx.fieldedQuery().subQuery() != null)
                return visit(ctx.fieldedQuery().id()) + ":(" + visit(ctx.fieldedQuery().subQuery()) + ")"
        }
        // une erreur de syntaxe sera levée
        return "";
    }

    override fun visitGreaterSubTerm(ctx: GreaterSubTermContext?) = ">" + visit(ctx!!.TERM())
    override fun visitLowerSubTerm(ctx: LowerSubTermContext?) = "<" + visit(ctx!!.TERM())
    override fun visitGreaterOrEqualSubTerm(ctx: GreaterOrEqualSubTermContext?) = ">=" + visit(ctx!!.TERM())
    override fun visitLowerOrEqualSubTerm(ctx: LowerOrEqualSubTermContext?) =  "<=" + visit(ctx!!.TERM())

    override fun visitSubTermExp(ctx: SubTermExpContext?)= visit(ctx!!.subTerm())
    override fun visitTermExpr(ctx: TermExprContext?) = visit(ctx!!.basicTerm())

    /** feuilles */
    override fun visitId(ctx: IdContext?): String {
        // réalise dse tests complémentaires qui ne peuvent pas être réalisés lors du parsing à cau
        // https://groups.google.com/g/antlr-discussion/c/LZvw6ME2yqY
        if(!ctx!!.TERM().text.matches(Regex("[a-zA-ZA-Z0-9_]*"))) {
            val msg = "Erreur à l'index ${ctx.start.startIndex}. '${ctx.start.text}' contient des caractères non-autorisés."
            throw ParseCancellationException(msg)
        }
        return ctx.TERM().text
    }

    override fun visitUniqueSubTerm(ctx: UniqueSubTermContext?) = visit(ctx!!.TERM())
    override fun visitUniqueTerm(ctx: UniqueTermContext?) = visit(ctx!!.TERM())


    /** les noeuds terminaux - aka les TOKEN - sont affichés tels quels */
    override fun visitTerminal(node: TerminalNode?) = node!!.text

}