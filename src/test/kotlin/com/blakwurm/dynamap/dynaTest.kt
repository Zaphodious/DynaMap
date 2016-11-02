package com.blakwurm.dynamap

import com.winterbe.expekt.should
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


/**
 * Created by achyt_000 on 10/12/2016.
 */
class dynaTests: Spek({
    describe("Symbol Keys") {
        val nameString = "Boogyboo"
        val nameSymbol = Symbol<String>(nameString)
        it ("Returns exactly its name as toString()") {
            nameSymbol.toString().should.match(nameString.toRegex())
        }
        it ("Works as keys to a map") {
            val aString = "Testing String"
            val aMap = mapOf(TestData.basicStringKey to aString)
            aMap[TestData.basicStringKey].should.equal(aString)
        }
    }

    describe("adding and removing things from a dynamap") {
        it ("casts correctly to the given type after adding") {
            val aName = "Alfred"
            val addedMap = DynaMap() + (TestData.nameThingKey to TestData.NameThing(aName))
            val addedNameThing = addedMap(TestData.nameThingKey)
            println("map is $addedMap")
            addedNameThing?.name.should.equal(aName)
        }

    }

    describe("Dynamap serialization") {


        it ("knows how to deflate, copy, and reinflate a dynamap") {
            val word = "watcha"
            val thing = TestData.NameThing(word)
            val mappy = DynaMap(TestData.nameThingKey to thing)
            val serialized = mappy.asByteArray()
            val inflated = DynaMap().fromByteArray(serialized)
            println("bytes are \n${serialized.toList()}\nand inflated is $inflated")
            inflated.should.equal(mappy)
        }

        it ("knows how to deflate, copy, and reinflate a nested dynamap") {
            val word   = "watcha"
            val thing1 = TestData.NameThing(word)
            val thing2 = TestData.FavoriteNumber(42)
            val mappy  = (DynaMap(TestData.nameThingKey to thing1))
            val mappy2 = (DynaMap(TestData.favoriteNumberKey to thing2))
            val mappy3 = mappy + (Symbol<DynaMap>("Dynamap") to mappy2)
            println("\nattemping to deflate and inflate $mappy3")

            val serialized = mappy3.asByteArray()
            val inflated = DynaMap().fromByteArray(serialized)
            println("bytes are \n${serialized.toList()}\nand inflated is $inflated")
            inflated.should.equal(mappy3)
        }
    }
})



object TestData {

    val basicStringKey = Symbol<String>("basicString")

    data class NameThing(val name: String = "Jimmy")
    val nameThingKey = Symbol("nameThing", NameThing(""))

    data class Birthday(val month: Int, val day: Int, val year: Int)
    val birthdayKey = Symbol("birthday", Birthday::class.java)

    data class FavoriteNumber(val number: Int = 2)
    val favoriteNumberKey = Symbol("favoriteNumber", FavoriteNumber(2))
}