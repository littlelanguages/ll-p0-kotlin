package io.littlelanguages.data

interface Union2<A, B> {
    fun isA(): Boolean
    fun isB(): Boolean

    fun a(): A
    fun b(): B
}

class Union2a<A, B>(private val a: A) : Union2<A, B> {
    override fun isA(): Boolean = true
    override fun isB(): Boolean = false

    override fun a(): A = a

    override fun b(): B {
        throw IllegalArgumentException("b is not set")
    }
}

class Union2b<A, B>(private val b: B) : Union2<A, B> {
    override fun isA(): Boolean = false
    override fun isB(): Boolean = true

    override fun a(): A {
        throw IllegalArgumentException("a is not set")
    }

    override fun b(): B = b
}