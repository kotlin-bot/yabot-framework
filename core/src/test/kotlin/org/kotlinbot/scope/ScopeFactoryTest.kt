package org.kotlinbot.scope

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.kotlinbot.tests.dynamicScopeFor
import org.kotlinbot.tests.scopeFor

class ScopeFactoryTest {
    @Test
    fun `no exception on runing`() {
        val (scope, services) = scopeFor<Scope1>()
    }

    @Test
    fun `when read values from empty scope it returns default values for primitive types`() {
        val (scope, services) = scopeFor<Scope1>()
        Assert.assertEquals(0, scope.intParam)
        Assert.assertEquals(0L, scope.longParam)
        Assert.assertEquals("", scope.stringParam)
        Assert.assertEquals(false, scope.booleanParam)
    }

    @Test
    fun `change var in scope change it in map`() {
        val (dynScope, services) = dynamicScopeFor<Scope1>()

        val scope = dynScope.asScope()
        val values = dynScope.values
        scope.booleanParam = true
        scope.intParam = 12
        scope.stringParam = "str"
        scope.longParam = 13L

        assertEquals(true, values[Scope1::booleanParam.name])
        assertEquals(12, values[Scope1::intParam.name])
        assertEquals("str", values[Scope1::stringParam.name])
        assertEquals(13L, values[Scope1::longParam.name])
    }

    @Test
    fun `values from map appears in scope props`() {
        val values = mapOf(
            Scope1::booleanParam.name to true,
            Scope1::longParam.name to 17L,
            Scope1::stringParam.name to "some",
            Scope1::intParam.name to 7
        )
        val (dynScope, services) = dynamicScopeFor<Scope1>(values)

        val scope = dynScope.asScope()

        assertEquals(scope.booleanParam, values[Scope1::booleanParam.name])
        assertEquals(scope.intParam, values[Scope1::intParam.name])
        assertEquals(scope.stringParam, values[Scope1::stringParam.name])
        assertEquals(scope.longParam, values[Scope1::longParam.name])
    }

    @Test
    fun `returns null on absent value for nullableField`() {
        val (dynScope, services) = dynamicScopeFor<Scope1>(emptyMap())
        val scope = dynScope.asScope()

        assertNull(scope.nullableString)
    }
}