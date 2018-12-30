package engine.world

import kotlinx.serialization.Serializable

@Serializable
class Continent(val name: String, val reinforcements: Int, val territories: List<Territory>)
