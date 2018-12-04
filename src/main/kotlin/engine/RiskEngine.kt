package engine

class Action


class Result


class RiskEngine: Engine<Action, Result>() {

    override suspend fun handle(input: Action): Result {
        TODO("not implemented")
    }

}
