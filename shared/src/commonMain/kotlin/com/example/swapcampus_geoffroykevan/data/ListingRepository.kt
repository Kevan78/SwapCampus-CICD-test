package com.example.swapcampus_geoffroykevan.data

import com.example.swapcampus_geoffroykevan.domain.Category
import com.example.swapcampus_geoffroykevan.domain.Listing
import com.example.swapcampus_geoffroykevan.domain.ListingCatalog
import com.example.swapcampus_geoffroykevan.domain.ListingStatus
import com.example.swapcampus_geoffroykevan.domain.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ListingRepository {
    val listings: StateFlow<List<Listing>>

    fun allListings(): List<Listing>

    fun getListing(id: Int): Listing?

    fun searchListings(keyword: String? = null, category: Category? = null): List<Listing>

    fun postListing(
        title: String,
        description: String,
        price: Double,
        category: Category,
        authorId: Int,
        photoUrl: String? = null
    ): Listing

    fun updateListing(
        id: Int,
        title: String,
        description: String,
        price: Double,
        category: Category,
        photoUrl: String?
    ): Listing

    fun changeStatus(id: Int, newStatus: ListingStatus): Listing

    fun removeListing(id: Int): Boolean
}

class InMemoryListingRepository(
    private val catalog: ListingCatalog = ListingCatalog()
) : ListingRepository {

    private val _listings = MutableStateFlow(catalog.allListings())
    override val listings: StateFlow<List<Listing>> = _listings

    private fun refresh() {
        _listings.value = catalog.allListings()
    }

    override fun allListings(): List<Listing> = catalog.allListings()

    override fun getListing(id: Int): Listing? = catalog.allListings().firstOrNull { it.id == id }

    override fun searchListings(keyword: String?, category: Category?): List<Listing> =
        catalog.searchListings(keyword, category)

    override fun postListing(
        title: String,
        description: String,
        price: Double,
        category: Category,
        authorId: Int,
        photoUrl: String?
    ): Listing {
        checkRules(title, price)
        val photo = if (photoUrl.isNullOrBlank()) null else photoUrl
        val listing = catalog.postListing(
            title = title.trim(),
            description = description.trim(),
            price = price,
            category = category,
            authorId = authorId,
            createdAt = today(),
            photoUrl = photo
        )
        refresh()
        return listing
    }

    override fun updateListing(
        id: Int,
        title: String,
        description: String,
        price: Double,
        category: Category,
        photoUrl: String?
    ): Listing {
        checkRules(title, price)
        val photo = if (photoUrl.isNullOrBlank()) null else photoUrl
        val updated = catalog.updateListing(id, title.trim(), description.trim(), price, category, photo)
            ?: throw IllegalArgumentException("Listing not found")
        refresh()
        return updated
    }

    override fun changeStatus(id: Int, newStatus: ListingStatus): Listing {
        val current = getListing(id) ?: throw IllegalArgumentException("Listing not found")

        val allowed = when (current.status) {
            is ListingStatus.Available -> newStatus is ListingStatus.Reserved
            is ListingStatus.Reserved -> newStatus is ListingStatus.Sold || newStatus is ListingStatus.Available
            is ListingStatus.Sold -> newStatus is ListingStatus.Available
        }
        if (!allowed) {
            throw IllegalArgumentException("This status change is not allowed")
        }
        if (newStatus is ListingStatus.Reserved && newStatus.buyer.isBlank()) {
            throw IllegalArgumentException("The buyer name cannot be empty")
        }

        val updated = catalog.changeStatus(id, newStatus)
            ?: throw IllegalArgumentException("Listing not found")
        refresh()
        return updated
    }

    override fun removeListing(id: Int): Boolean {
        val removed = catalog.removeListing(id)
        if (removed) {
            refresh()
        }
        return removed
    }

    private fun checkRules(title: String, price: Double) {
        if (title.isBlank()) {
            throw IllegalArgumentException("The title cannot be empty")
        }
        if (price <= 0.0) {
            throw IllegalArgumentException("The price must be greater than 0")
        }
        if (price > 5000.0) {
            throw IllegalArgumentException("The price cannot exceed 5000 €")
        }
    }
}
