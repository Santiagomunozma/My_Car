package com.micarro.feature.premium.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumRulesTest {

    private val now = 1_700_000_000_000L
    private val day = 24L * 60 * 60 * 1000

    @Test
    fun `sin prueba ni pago queda gratis y puede probar`() {
        val status = PremiumRules.status(PremiumRecord(), now)
        assertEquals(PremiumTier.FREE, status.tier)
        assertFalse(status.hasAccess)
        assertTrue(status.canStartTrial)
        assertEquals(0, status.trialDaysLeft)
    }

    @Test
    fun `la prueba dura 30 dias y solo se inicia una vez`() {
        val started = PremiumRules.startTrial(PremiumRecord(), now)
        val active = PremiumRules.status(started, now)
        assertEquals(PremiumTier.TRIAL, active.tier)
        assertTrue(active.hasAccess)
        assertFalse(active.canStartTrial)
        assertEquals(30, active.trialDaysLeft)
        assertEquals(now + 30 * day, started.trialEndsAt)

        val again = PremiumRules.startTrial(started, now + day)
        assertEquals(started, again)
    }

    @Test
    fun `al vencer la prueba vuelve a gratis y no se puede repetir`() {
        val started = PremiumRules.startTrial(PremiumRecord(), now)
        val expired = PremiumRules.status(started, started.trialEndsAt!!)
        assertEquals(PremiumTier.FREE, expired.tier)
        assertFalse(expired.hasAccess)
        assertFalse(expired.canStartTrial)
    }

    @Test
    fun `el pago deja premium para siempre aunque la prueba haya vencido`() {
        val expired = PremiumRules.startTrial(PremiumRecord(), now)
        val paid = PremiumRules.purchase(expired, expired.trialEndsAt!! + day)
        val status = PremiumRules.status(paid, expired.trialEndsAt!! + 400 * day)
        assertEquals(PremiumTier.PREMIUM, status.tier)
        assertTrue(status.hasAccess)
        assertFalse(status.canStartTrial)
        assertEquals(paid, PremiumRules.purchase(paid, now))
    }

    @Test
    fun `el precio es un pago unico de 20000 pesos`() {
        assertEquals(20_000, PremiumRules.PRICE_COP)
        val formatted = PremiumRules.formatPrice()
        assertTrue(formatted.contains("20"))
        assertTrue(formatted.contains("000"))
    }
}
