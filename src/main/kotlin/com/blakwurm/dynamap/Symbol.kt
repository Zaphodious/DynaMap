package com.blakwurm.dynamap

/**
 * Created by achyt_000 on 10/17/2016.
 */
interface Symbol <T> {
    val name: String
    companion object getNew {
        operator fun <T> invoke(name: String): Symbol<T> = DataSymbol(name)
        operator fun <T> invoke(name: String, clazz: Class<T>): Symbol<T> = DataSymbol(name)
        operator fun <T> invoke(name: String, thing_with_a_type: T): Symbol<T> = DataSymbol(name)
    }}
data class DataSymbol <T> (override val name: String) : Symbol<T> {
    constructor() : this("Unnamed")
    override fun toString(): String = name }
