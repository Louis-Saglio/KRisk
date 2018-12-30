package engine.world

import kotlinx.serialization.Serializable

@Serializable
class Border(val territory1: Territory, val territory2: Territory)
