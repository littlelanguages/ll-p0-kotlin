package io.littlelanguages.p0.lexer

import io.littlelanguages.data.Yamlable

interface Positionable {
    fun position(): Position
}

sealed class Position : Yamlable {
    abstract operator fun plus(position: Position): Position
}


data class PositionCoordinate(val offset: Int, val line: Int, val column: Int) : Position() {
    override operator fun plus(position: Position): Position =
            when (position) {
                is PositionCoordinate ->
                    if (position == this)
                        this
                    else
                        PositionRange(
                                PositionCoordinate(Integer.min(offset, position.offset), Integer.min(line, position.line), Integer.min(column, position.column)),
                                PositionCoordinate(Integer.max(offset, position.offset), Integer.max(line, position.line), Integer.max(column, position.column)))

                is PositionRange ->
                    PositionRange(
                            PositionCoordinate(Integer.min(offset, position.start.offset), Integer.min(line, position.start.line), Integer.min(column, position.start.column)),
                            PositionCoordinate(Integer.max(offset, position.end.offset), Integer.max(line, position.end.line), Integer.max(column, position.end.column)))
            }

    override fun toString(): String =
            "$offset:$line:$column"

    override fun yaml(): Any =
            toString()
}

data class PositionRange(val start: PositionCoordinate, val end: PositionCoordinate) : Position() {
    override operator fun plus(position: Position): Position =
            when (position) {
                is PositionCoordinate ->
                    position + this

                is PositionRange -> {
                    val startIndex =
                            Integer.min(start.offset, position.start.offset)

                    val endIndex =
                            Integer.max(end.offset, position.end.offset)

                    val startLocation =
                            PositionCoordinate(startIndex, Integer.min(start.line, position.start.line), Integer.min(start.column, position.start.column))

                    val endLocation =
                            PositionCoordinate(endIndex, Integer.max(end.line, position.end.line), Integer.max(end.column, position.end.column))

                    PositionRange(startLocation, endLocation)
                }
            }

    override fun toString(): String =
            "$start-$end"

    override fun yaml(): Any =
            toString()
}
