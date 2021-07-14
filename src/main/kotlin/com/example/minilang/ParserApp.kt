package com.example.minilang

fun main(args: Array<String>) {

    print("Ecris ton nom : ")
    val name = readLine()

    println("coucou $name")

    print("En quelle année es-tu né? ")

    val annee = readLine()?.toInt()!!

    val age = 2020 - annee

    println("tu as $age ans")

    if (age < 18)
        println("tu es un enfant")
    else {
        println("tu es un adulte")

        print("As-tu un enfant?")
        val enfant = readLine()

        if (enfant == "oui")
            println("tu es un papa")
        else
            println("tu n'es pas un papa!")
    }



}