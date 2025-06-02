package com.benyaamin.rakhsh.core

import java.util.BitSet

/**
 * This class calculates ranges
 * It uses in BitSet to filter remained ranges to download
 */
class ChunkQueue(
    private val totalBytes: Long,
    private val chunkSize: Int,
    bitset: BitSet?,
) {
    private val lock = Any()
    private val list = mutableListOf<IndexedValue<LongRange>>()
    private val failedChunks = mutableListOf<IndexedValue<LongRange>>()
    private var chunksSize = 0
    private var _bitset = bitset

    fun calculateRanges() {
        if (list.isNotEmpty()) {
            list.clear()
            chunksSize = 0
        }

        var currentStart = 0L
        var index = 0

        while (currentStart < totalBytes) {
            val currentEnd = (currentStart + chunkSize - 1).coerceAtMost(totalBytes - 1)
            list.add(IndexedValue(index, currentStart..currentEnd))
            currentStart = currentEnd + 1
            index++
        }
        chunksSize = list.size
        if (_bitset == null) {
            _bitset = BitSet(chunksSize)
        } else {
            list.removeAll { chunk -> _bitset!!.get(chunk.index) }
        }
    }

    fun pull(): IndexedValue<LongRange>? = synchronized(lock) {
        if (list.isNotEmpty()) list.removeAt(0)
        else null
    }

    fun pullFromFailed(): IndexedValue<LongRange>? = synchronized(lock) {
        if (failedChunks.isNotEmpty()) failedChunks.removeAt(0)
        else null
    }

    fun enqueueAsFailed(range: IndexedValue<LongRange>) = synchronized(lock) {
        failedChunks.add(range)
    }

    fun setAsDone(index: Int) = synchronized(lock) {
        _bitset?.set(index)
    }

    fun isCompleted() = synchronized(lock) {
        _bitset!!.cardinality()  == chunksSize
    }

    fun getLastState() = _bitset!!

}