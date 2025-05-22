package com.benyaamin.rakhsh.db.util

import androidx.room.TypeConverter
import java.util.BitSet

class BitSetTypeConverter {
    @TypeConverter
    fun fromBitSet(bitset: BitSet?): ByteArray? {
        return bitset?.toByteArray()
    }

    @TypeConverter
    fun toBitSet(array: ByteArray?): BitSet? {
        return array?.let { BitSet.valueOf(it) }
    }
}