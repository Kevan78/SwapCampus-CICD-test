package com.example.swapcampus_geoffroykevan.data

import com.example.swapcampus_geoffroykevan.domain.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Ignore
import org.junit.Test

/**
 * EXERCISE PART 2 — "Red, then green".
 * New business rule: on SwapCampus a listing cannot cost more than 5000 €.
 * Step 1: remove the @Ignore line below and push -> the test FAILS, CI turns red
 *         and the delivery is blocked.
 * Step 2: implement the rule in ListingRepository.checkRules() and push -> green.
 */

class ListingPriceLimitTest {

    private val repository = InMemoryListingRepository()

    @Test
    fun priceAboveLimitIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            repository.postListing("Gaming PC", "RTX inside", 5000.01, Category.ELECTRONICS, authorId = 1)
        }
        assertEquals(0, repository.allListings().size)
    }

    @Test
    fun priceAtLimitIsAccepted() {
        repository.postListing("Scooter", "Almost new", 5000.0, Category.SPORTS_LEISURE, authorId = 1)
        assertEquals(1, repository.allListings().size)
    }
}
