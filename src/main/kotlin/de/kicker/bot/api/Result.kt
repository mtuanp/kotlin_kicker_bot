package de.kicker.bot.api

data class Result(val success: Boolean, val errorCode: ErrorCode) {

    companion object {
        fun of(success: Boolean, defaultErrorCode: ErrorCode) = when (success) {
            true -> Result(success, ErrorCode.NOTHING)
            false -> Result(success, defaultErrorCode)
        }

        fun error(errorCode: ErrorCode): Result {
            return Result(false, errorCode)
        }
    }

}