package de.kicker.bot.extension

import java.util.concurrent.locks.StampedLock

fun <T> StampedLock.withReadLock(action: () -> T): T {
    val readLock = readLock()
    try {
        return action.invoke()
    } finally {
        unlockRead(readLock)
    }
}


fun <T> StampedLock.withWriteLock(action: () -> T): T {
    val writeLock = writeLock()
    try {
        return action.invoke()
    } finally {
        unlockWrite(writeLock)
    }
}