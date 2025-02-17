package ink.ptms.adyeshach.common.entity.type

import ink.ptms.adyeshach.common.entity.EntityTypes

/**
 * @author sky
 * @date 2020/8/4 23:15
 */
open class AdyGuardian(entityTypes: EntityTypes = EntityTypes.GUARDIAN) : AdyMob(entityTypes) {

    init {
        /*
        1.16,1.15
        15 ->Is retracting spikes
        16 ->Target EID
        1.14
        14 ->Is retracting spikes
        15 ->Target EID
        1.13,1.12,1.11
        12 ->Is retracting spikes
        13 ->Targer EID
        1.10
        12 ->0x02 isretracting spikes
             0x04 iselderly
        13 ->Targer EID
         */
        if (version >= 11100) {
            registerMeta(at(11700 to 16, 11500 to 15, 11400 to 14, 11100 to 13), "isRetractingSpikes", false)
        } else {
            registerMetaByteMask(at(11000 to 12, 10900 to 11), "isRetractingSpikes", 0x02)
            registerMetaByteMask(at(11000 to 12, 10900 to 11), "isElderly", 0x04)
        }
        registerMeta(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "targetEntity", false)
    }

    fun setRetractingSpikes(value: Boolean) {
        setMetadata("isRetractingSpikes", value)
    }

    fun isRetractingSpikes(): Boolean {
        return getMetadata("isRetractingSpikes")
    }

    fun setTargetEntityId(id: Int) {
        setMetadata("targetEntityId", id)
    }

    fun getTargetEntityId(): Int {
        return getMetadata("targetEntityId")
    }

    fun setElderly(value: Boolean) {
        if (version >= 11100) {
            error("Metadata \"isElderly\" not supported this minecraft version. Use \"AdyElderGuardian\" instead")
        }
        setMetadata("isElderly", value)
    }

    fun isElderly(): Boolean {
        if (version >= 11100) {
            error("Metadata \"isElderly\" not supported this minecraft version. Use \"AdyElderGuardian\" instead")
        }
        return getMetadata("isElderly")
    }
}