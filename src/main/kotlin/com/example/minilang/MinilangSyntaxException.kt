package com.example.minilang


class MinilangSyntaxException(val errors:List<String>) : RuntimeException() {
    
    constructor(error:String) : this(arrayListOf(error))
}
