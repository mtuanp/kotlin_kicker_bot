package de.kicker.bot.api

data class Result(val success: Boolean, val errorCode: ErrorCode) {

    companion object {
        fun success(): Result {
            return Result(true, ErrorCode.NOTHING)
        }

        fun of(success: Boolean, defaultErrorCode: ErrorCode): Result = when (success) {
            true -> success()
            false -> Result(success, defaultErrorCode)
        }

        fun error(errorCode: ErrorCode): Result {
            return Result(false, errorCode)
        }
    }

}