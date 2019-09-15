package de.kicker.bot.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class SlackUserIdSplitterTest {

    @Test
    fun testingSplitter() {
        val testArgument = "<@UAMJAG4|tpham> <!channel> <@DFQEQW1|tpham> <#CETTTTQG4|general> <@IJUGTA7|tpham> <@NONONO1|tpham>"
        val idList = SlackUserIdSplitter.toIdList(testArgument)
        assertNotNull(idList)
        assertThat(idList).isNotEmpty.hasSize(4).containsAll(listOf("UAMJAG4", "DFQEQW1", "IJUGTA7", "NONONO1"))
    }

    @Test
    fun testingDistinctSplitter() {
        val testArgument = "<@UAMJAG4|tpham> <!channel> <@DFQEQW1|tpham> <#CETTTTQG4|general> <@DFQEQW1|tpham> <@IJUGTA7|tpham> <@NONONO1|tpham> <@NONONO1|tpham>"
        val idList = SlackUserIdSplitter.toIdList(testArgument)
        assertNotNull(idList)
        assertThat(idList).isNotEmpty.hasSize(4).containsAll(listOf("UAMJAG4", "DFQEQW1", "IJUGTA7", "NONONO1"))
    }
}