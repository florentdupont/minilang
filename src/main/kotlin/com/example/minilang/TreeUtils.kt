package com.example.minilang

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import java.util.*


class TreeUtils {
    private val PRETTY_PRINT_GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    private val GSON: Gson = Gson()

    fun toMap(tree: ParseTree): Map<String, Any> {
        val map: MutableMap<String, Any> = LinkedHashMap()
        traverse(tree, map)
        return map
    }

    fun toJson(tree: ParseTree?): String? {
        return toJson(tree, true)
    }

    fun toJson(tree: ParseTree?, prettyPrint: Boolean): String? {
        return if (prettyPrint) PRETTY_PRINT_GSON.toJson(toMap(tree!!)) else GSON.toJson(toMap(tree!!))
    }

    fun traverse(tree: ParseTree, map: MutableMap<String, Any>) {
        if (tree is TerminalNodeImpl) {
            val token = tree.getSymbol()
            map["type"] = token.type
            map["text"] = token.text
        } else {
            val children: MutableList<Map<String, Any>> = ArrayList()
            val name = tree.javaClass.simpleName.replace("Context$".toRegex(), "")
            map[Character.toLowerCase(name[0]).toString() + name.substring(1)] = children
            for (i in 0 until tree.childCount) {
                val nested: MutableMap<String, Any> = LinkedHashMap()
                children.add(nested)
                traverse(tree.getChild(i), nested)
            }
        }
    }
}