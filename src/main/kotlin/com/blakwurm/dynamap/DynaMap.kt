package com.blakwurm.dynamap

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.google.common.collect.ImmutableMap
import de.javakaffee.kryoserializers.guava.*

/**
 * Created by achyt_000 on 10/12/2016.
 */

data class Address(val city: String? = null, val line: String? = null, val postCode: String? = null)

data class User(val name: String? = null,
                val address: Address? = null)

interface DynaMap : Map<Symbol<*>, Any?> {
    val asDestructMap: Map<String, Any?>
    operator fun <T> plus(element: Pair<Symbol<T>, T>) : DynaMap
    operator fun <T> plus(map: Map<Symbol<T>, Any?>) : DynaMap
    operator fun <T> div(key: Symbol<T>) : DynaDivResult<T>
    operator fun <T> invoke(key: Symbol<T>) : T?

    fun asByteArray() :  ByteArray
    fun fromByteArray(bytearray: ByteArray) : DynaMap

    companion object get {
        operator fun invoke() : DynaMap = ImmutableGuavaDynaMap()
        operator fun <T> invoke(vararg elements: Pair<Symbol<T>, T>) : DynaMap =
                ImmutableGuavaDynaMap(ImmutableMap.copyOf(elements.toMap()))
        operator fun <T> plus(element: Pair<Symbol<T>, T>) : DynaMap =
            this() + element


    }




}
data class DeflatedDynaMember(val classname: String, val deflatedMember: String)

data class DynaDivResult<T>(val value: T?, val key: Symbol<T>, val map: DynaMap)

private data class ImmutableGuavaDynaMap internal constructor(internal val theMap: ImmutableMap<Symbol<*>, Any?> = ImmutableMap.of()): DynaMap {


    ;constructor () : this(theMap = ImmutableMap.of())
    override fun asByteArray(): ByteArray {
        val output = Output(1000, 10000000)
        DynaFreezer.kryo.writeObject(output, this)
        return output.toBytes()
    }

    override fun fromByteArray(bytearray: ByteArray): DynaMap {
        val input = Input(bytearray)
        return DynaFreezer.kryo.readObject(input, javaClass)
    }

    override val asDestructMap: Map<String, Any?> =
            theMap.map { val (key, value) = it
                key.name to value
            }.toMap()
    override operator fun <T> plus(element: Pair<Symbol<T>, T>) : ImmutableGuavaDynaMap {
        val newmap = this.copy(ImmutableMap.copyOf(theMap + element))
        return newmap
    }
    override fun <T> plus(map: Map<Symbol<T>, Any?>): DynaMap =
        this.copy(ImmutableMap.copyOf(this.theMap + map))
    override operator fun <T> div(key: Symbol<T>) : DynaDivResult<T> {
        val thing = this(key)
        val mapAfterRemove = theMap.filterNot { it.key == key }
        return DynaDivResult(value = thing, key = key, map = this.copy(ImmutableMap.copyOf(mapAfterRemove)))
    }
    override operator fun <T> invoke(key: Symbol<T>) : T? {
        @Suppress("UNCHECKED_CAST")
        return theMap[key] as T?
    }

    override val entries: Set<Map.Entry<Symbol<*>, Any?>> = theMap.entries
    override val keys: Set<Symbol<*>> = theMap.keys
    override val size: Int = theMap.size
    override val values: Collection<Any?> = theMap.values
    override fun containsKey(key: Symbol<*>): Boolean = theMap.containsKey(key)
    override fun containsValue(value: Any?): Boolean  = theMap.containsValue(value)
    override fun get(key: Symbol<*>): Any? = theMap.get(key)
    override fun isEmpty(): Boolean = theMap.isEmpty()

    override fun toString(): String =
            theMap.map {
                val (k,v) = it
                "${k.name} to $v"
            }.let {
                val listStringWithoutBrackets = it.toString().drop(1).dropLast(1)
                "@DynaMap{$listStringWithoutBrackets}"
            }



}

object DynaFreezer {
    val kryo = Kryo()


    init {
        kryo.register(ImmutableGuavaDynaMap::class.java, DynaMapSerializer())
        ImmutableListSerializer.registerSerializers( kryo )
        ImmutableSetSerializer.registerSerializers( kryo )
        ImmutableMapSerializer.registerSerializers( kryo )
        ImmutableMultimapSerializer.registerSerializers( kryo )
        ReverseListSerializer.registerSerializers( kryo )
        UnmodifiableNavigableSetSerializer.registerSerializers( kryo )
// guava ArrayListMultimap, HashMultimap, LinkedHashMultimap, LinkedListMultimap, TreeMultimap
        ArrayListMultimapSerializer.registerSerializers( kryo )
        HashMultimapSerializer.registerSerializers( kryo )
        LinkedHashMultimapSerializer.registerSerializers( kryo )
        LinkedListMultimapSerializer.registerSerializers( kryo )
        TreeMultimapSerializer.registerSerializers( kryo )
        println("\nguava kryo serializers registered to $kryo")
    }
}

private class DynaMapSerializer : Serializer<ImmutableGuavaDynaMap>(false, true) {
    /** Reads bytes and returns a new object of the specified concrete type.
     *
     *
     * Before Kryo can be used to read child objects, [Kryo.reference] must be called with the parent object to
     * ensure it can be referenced by the child objects. Any serializer that uses [Kryo] to read a child object may need to
     * be reentrant.
     *
     *
     * This method should not be called directly, instead this serializer can be passed to [Kryo] read methods that accept a
     * serialier.
     * @return May be null if [.getAcceptsNull] is true.
     */
    override fun read(kryo: Kryo?, input: Input?, type: Class<ImmutableGuavaDynaMap>?): ImmutableGuavaDynaMap {
        val hydratedMap = kryo?.readObject(input, ImmutableMap::class.java) as ImmutableMap<Symbol<*>, Any?>
        return ImmutableGuavaDynaMap(hydratedMap)
    }

    /** Writes the bytes for the object to the output.
     *
     *
     * This method should not be called directly, instead this serializer can be passed to [Kryo] write methods that accept a
     * serialier.
     * @param object May be null if [.getAcceptsNull] is true.
     */
    override fun write(kryo: Kryo?, output: Output?, `object`: ImmutableGuavaDynaMap?) {
        kryo?.writeObject(output, `object`?.theMap)
    }



}