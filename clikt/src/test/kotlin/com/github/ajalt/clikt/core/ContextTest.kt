package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class ContextTest {
    class Foo

    @Test
    fun `find functions single context`() {
        class C : TestCommand() {
            val o1 by findObject<String>()
            val o2 by findOrSetObject { "foo" }
            val o3 by findObject<String>()
            val o4 by findObject<Int>()

            override fun run_() {
                context.findRoot() shouldBe context
            }
        }

        val c = C().apply { parse(emptyArray()) }

        c.o1 shouldBe null
        c.o2 shouldBe "foo"
        c.o3 shouldBe "foo"
        c.o4 shouldBe null
    }

    @Test
    fun `find functions parent context`() {
        val foo = Foo()

        class C : TestCommand(invokeWithoutSubcommand = true) {
            val o1 by findObject<Foo>()
            val o2 by findOrSetObject { foo }
            val o3 by findObject<Foo>()
            val o4 by findObject<Int>()

            override fun run_() {
                context.findRoot() shouldBe context
            }
        }

        val child = C()
        val parent = C().subcommands(child).apply { parse(emptyArray()) }
        parent.o1 shouldBe child.o1
        parent.o1 shouldBe null
        parent.o2 shouldBe child.o2
        parent.o2 shouldBe foo
        parent.o3 shouldBe child.o3
        parent.o3 shouldBe foo
        parent.o4 shouldBe child.o4
        parent.o4 shouldBe null
    }

    @Test
    fun `requireObject with parent context`() {
        class C : TestCommand(invokeWithoutSubcommand = true) {
            val o1 by findOrSetObject { Foo() }
            val o2 by requireObject<Foo>()
        }

        val child = C()
        val parent = C().subcommands(child).apply { parse(emptyArray()) }

        shouldThrow<NullPointerException> { parent.o2 }
        shouldThrow<NullPointerException> { child.o2 }

        parent.o1 should beInstanceOf<Foo>()
        parent.o2 shouldBeSameInstanceAs parent.o1
        child.o1 shouldBeSameInstanceAs parent.o1
        child.o2 shouldBeSameInstanceAs parent.o1
    }

    @Test
    fun `default help option names`() {
        class C : TestCommand()

        shouldThrow<PrintHelpMessage> { C().parse("--help") }
        shouldThrow<PrintHelpMessage> { C().parse("-h") }
        shouldThrow<PrintHelpMessage> {
            C().context { helpOptionNames = setOf("-x") }.parse("-x")
        }
        shouldThrow<NoSuchOption> {
            C().context { helpOptionNames = setOf("--x") }.parse("--help")
        }
    }
}
