package com.oceanwalls.app.data.model

enum class CategoryGroup(val title: String, val blurb: String) {
    MARINE_ANIMALS("Marine Animals", "Portraits of the creatures that share the ocean"),
    OCEAN_ENVIRONMENTS("Ocean Environments", "Underwater worlds, reefs and the deep"),
    OCEAN_LANDSCAPES("Ocean Landscapes", "Coasts, waves, islands and open water"),
    SPECIAL_COLLECTIONS("Special Collections", "Curated moods and stories from the sea")
}

/**
 * Curated categories. `category` is a real Wikimedia Commons category (curated by
 * humans, so precision is high); `search` is the full-text fallback used once the
 * category is exhausted, which keeps pagination alive on every collection.
 */
data class WallpaperCategory(
    val id: String,
    val label: String,
    val group: CategoryGroup,
    val search: String,
    val keywords: List<String>,
    val category: String? = null
) {
    val spec: FeedSpec
        get() = FeedSpec(id = "cat:$id", label = label, search = search, keywords = keywords, category = category)
}

object Catalog {

    val marineAnimals = listOf(
        WallpaperCategory("blue-whales", "Blue Whales", CategoryGroup.MARINE_ANIMALS, "blue whale", listOf("whale", "balaenoptera"), "Balaenoptera musculus"),
        WallpaperCategory("humpback-whales", "Humpback Whales", CategoryGroup.MARINE_ANIMALS, "humpback whale", listOf("humpback", "megaptera", "whale"), "Megaptera novaeangliae"),
        WallpaperCategory("dolphins", "Dolphins", CategoryGroup.MARINE_ANIMALS, "dolphin", listOf("dolphin", "delphinidae"), "Delphinidae"),
        WallpaperCategory("sharks", "Sharks", CategoryGroup.MARINE_ANIMALS, "shark", listOf("shark", "selachimorpha"), "Selachimorpha"),
        WallpaperCategory("great-white-sharks", "Great White Sharks", CategoryGroup.MARINE_ANIMALS, "great white shark", listOf("white shark", "carcharodon", "great white"), "Carcharodon carcharias"),
        WallpaperCategory("whale-sharks", "Whale Sharks", CategoryGroup.MARINE_ANIMALS, "whale shark", listOf("whale shark", "rhincodon"), "Rhincodon typus"),
        WallpaperCategory("orcas", "Orcas", CategoryGroup.MARINE_ANIMALS, "orca killer whale", listOf("orca", "orcinus", "killer whale"), "Orcinus orca"),
        WallpaperCategory("sea-turtles", "Sea Turtles", CategoryGroup.MARINE_ANIMALS, "sea turtle", listOf("turtle", "cheloni"), "Chelonioidea"),
        WallpaperCategory("octopus", "Octopus", CategoryGroup.MARINE_ANIMALS, "octopus", listOf("octopus"), "Octopus"),
        WallpaperCategory("squid", "Squid", CategoryGroup.MARINE_ANIMALS, "squid", listOf("squid", "teuthida"), "Teuthida"),
        WallpaperCategory("jellyfish", "Jellyfish", CategoryGroup.MARINE_ANIMALS, "jellyfish", listOf("jellyfish", "medusa", "scyphozoa"), "Jellyfish"),
        WallpaperCategory("seahorses", "Seahorses", CategoryGroup.MARINE_ANIMALS, "seahorse", listOf("seahorse", "hippocampus", "syngnath"), "Hippocampus"),
        WallpaperCategory("stingrays", "Stingrays", CategoryGroup.MARINE_ANIMALS, "stingray", listOf("ray", "stingray", "myliobatis"), "Myliobatiformes"),
        WallpaperCategory("manta-rays", "Manta Rays", CategoryGroup.MARINE_ANIMALS, "manta ray", listOf("manta", "mobula"), "Mobula"),
        WallpaperCategory("clownfish", "Clownfish", CategoryGroup.MARINE_ANIMALS, "clownfish anemonefish", listOf("clownfish", "amphiprion", "anemonefish"), "Amphiprioninae"),
        WallpaperCategory("tropical-fish", "Tropical Fish", CategoryGroup.MARINE_ANIMALS, "tropical reef fish", listOf("fish", "pomacentridae", "damselfish", "reef"), "Pomacentridae"),
        WallpaperCategory("penguins", "Penguins", CategoryGroup.MARINE_ANIMALS, "penguin swimming", listOf("penguin", "sphenisc"), "Spheniscidae"),
        WallpaperCategory("seals", "Seals", CategoryGroup.MARINE_ANIMALS, "seal swimming underwater", listOf("seal", "phoca", "halichoerus"), "Phocidae"),
        WallpaperCategory("sea-lions", "Sea Lions", CategoryGroup.MARINE_ANIMALS, "sea lion underwater", listOf("sea lion", "otari", "zalophus"), "Otariidae"),
        WallpaperCategory("crabs", "Crabs", CategoryGroup.MARINE_ANIMALS, "crab", listOf("crab", "brachyura"), "Brachyura"),
        WallpaperCategory("lobsters", "Lobsters", CategoryGroup.MARINE_ANIMALS, "lobster", listOf("lobster", "homarus", "nephrops"), "Nephropidae"),
        WallpaperCategory("starfish", "Starfish", CategoryGroup.MARINE_ANIMALS, "starfish", listOf("starfish", "asteroidea", "sea star"), "Asteroidea")
    )

    val oceanEnvironments = listOf(
        WallpaperCategory("deep-sea", "Deep Sea", CategoryGroup.OCEAN_ENVIRONMENTS, "deep sea", listOf("deep", "abyss", "benthic"), "Deep sea"),
        WallpaperCategory("underwater-world", "Underwater World", CategoryGroup.OCEAN_ENVIRONMENTS, "underwater reef", listOf("underwater", "reef", "diver"), "Underwater"),
        WallpaperCategory("coral-reefs", "Coral Reefs", CategoryGroup.OCEAN_ENVIRONMENTS, "coral reef", listOf("coral", "reef"), "Coral reefs"),
        WallpaperCategory("ocean-caves", "Ocean Caves", CategoryGroup.OCEAN_ENVIRONMENTS, "underwater cave diving", listOf("cave", "cavern", "grotto")),
        WallpaperCategory("blue-ocean", "Blue Ocean", CategoryGroup.OCEAN_ENVIRONMENTS, "blue ocean sea water", listOf("ocean", "sea", "water"), "Ocean"),
        WallpaperCategory("tropical-sea", "Tropical Sea", CategoryGroup.OCEAN_ENVIRONMENTS, "tropical sea", listOf("turquoise", "lagoon", "sea", "water"), "Turquoise water"),
        WallpaperCategory("open-ocean", "Open Ocean", CategoryGroup.OCEAN_ENVIRONMENTS, "open ocean horizon", listOf("ocean", "sea", "horizon"), "Ocean"),
        WallpaperCategory("ocean-floor", "Ocean Floor", CategoryGroup.OCEAN_ENVIRONMENTS, "seabed sand", listOf("sand", "seabed", "ripple", "bottom"), "Sand ripples"),
        WallpaperCategory("underwater-sunlight", "Underwater Sunlight", CategoryGroup.OCEAN_ENVIRONMENTS, "underwater sunlight", listOf("caustic", "underwater", "light", "sun"), "Caustics (optics)"),
        WallpaperCategory("underwater-plants", "Underwater Plants", CategoryGroup.OCEAN_ENVIRONMENTS, "seagrass kelp", listOf("seagrass", "posidonia", "algae", "plant", "kelp"), "Seagrasses")
    )

    val oceanLandscapes = listOf(
        WallpaperCategory("beaches", "Beautiful Beaches", CategoryGroup.OCEAN_LANDSCAPES, "tropical beach", listOf("beach", "sand", "coast"), "Beaches"),
        WallpaperCategory("ocean-waves", "Ocean Waves", CategoryGroup.OCEAN_LANDSCAPES, "ocean waves", listOf("wave", "surf", "sea"), "Water waves"),
        WallpaperCategory("sea-cliffs", "Sea Cliffs", CategoryGroup.OCEAN_LANDSCAPES, "sea cliff", listOf("cliff", "coast"), "Sea cliffs"),
        WallpaperCategory("rocky-coastlines", "Rocky Coastlines", CategoryGroup.OCEAN_LANDSCAPES, "rocky coastline", listOf("rock", "coast", "shore")),
        WallpaperCategory("tropical-islands", "Tropical Islands", CategoryGroup.OCEAN_LANDSCAPES, "tropical island", listOf("atoll", "island", "lagoon"), "Atolls"),
        WallpaperCategory("sunset-over-ocean", "Sunset Over Ocean", CategoryGroup.OCEAN_LANDSCAPES, "sunset over ocean", listOf("sunset")),
        WallpaperCategory("sunrise-over-sea", "Sunrise Over Sea", CategoryGroup.OCEAN_LANDSCAPES, "sunrise over sea", listOf("sunrise", "dawn")),
        WallpaperCategory("stormy-ocean", "Stormy Ocean", CategoryGroup.OCEAN_LANDSCAPES, "stormy sea waves", listOf("storm", "wave", "rough")),
        WallpaperCategory("aerial-ocean", "Aerial Ocean Photography", CategoryGroup.OCEAN_LANDSCAPES, "aerial coast ocean", listOf("aerial", "coast"), "Aerial photographs of coasts"),
        WallpaperCategory("frozen-sea", "Frozen Sea", CategoryGroup.OCEAN_LANDSCAPES, "frozen sea ice", listOf("ice", "frozen", "sea"), "Sea ice"),
        WallpaperCategory("polar-ocean", "Polar Ocean", CategoryGroup.OCEAN_LANDSCAPES, "polar ocean iceberg", listOf("iceberg", "polar", "arctic", "glacier"), "Icebergs"),
        WallpaperCategory("sea-caves", "Sea Caves", CategoryGroup.OCEAN_LANDSCAPES, "sea cave", listOf("cave", "grotto"), "Sea caves")
    )

    val specialCollections = listOf(
        WallpaperCategory("deep-blue-collection", "Deep Blue Collection", CategoryGroup.SPECIAL_COLLECTIONS, "deep blue underwater", listOf("blue", "underwater", "ocean"), "Underwater"),
        WallpaperCategory("mysterious-ocean", "Mysterious Ocean", CategoryGroup.SPECIAL_COLLECTIONS, "dark underwater ocean", listOf("dark", "underwater", "deep", "ocean")),
        WallpaperCategory("marine-giants", "Marine Giants", CategoryGroup.SPECIAL_COLLECTIONS, "whale underwater", listOf("whale", "orca", "cetacea"), "Cetacea"),
        WallpaperCategory("colorful-underwater", "Colorful Underwater Life", CategoryGroup.SPECIAL_COLLECTIONS, "colorful coral reef fish", listOf("coral", "fish", "color"), "Coral reefs"),
        WallpaperCategory("peaceful-ocean", "Peaceful Ocean", CategoryGroup.SPECIAL_COLLECTIONS, "calm ocean horizon", listOf("calm", "ocean", "horizon", "sea")),
        WallpaperCategory("dark-ocean", "Dark Ocean", CategoryGroup.SPECIAL_COLLECTIONS, "dark ocean night", listOf("dark", "night", "ocean")),
        WallpaperCategory("rare-marine-species", "Rare Marine Species", CategoryGroup.SPECIAL_COLLECTIONS, "rare marine species", listOf("rare", "marine")),
        WallpaperCategory("endangered-marine", "Endangered Marine Animals", CategoryGroup.SPECIAL_COLLECTIONS, "endangered marine species", listOf("endangered"))
    )

    val all: List<WallpaperCategory> = marineAnimals + oceanEnvironments + oceanLandscapes + specialCollections

    private val byId = all.associateBy { it.id }

    fun find(id: String?): WallpaperCategory? = id?.let { byId[it] }
    fun byGroup(group: CategoryGroup): List<WallpaperCategory> = all.filter { it.group == group }

    val trendingAnimals: List<WallpaperCategory> = listOf(
        "whale-sharks", "orcas", "sea-turtles", "octopus", "manta-rays", "jellyfish", "great-white-sharks", "seahorses"
    ).mapNotNull { byId[it] }

    val featured = FeedSpec(
        id = "feed:featured",
        label = "Featured underwater",
        search = "featured picture underwater ocean",
        keywords = listOf("underwater", "ocean", "sea", "reef", "dive", "whale", "coral")
    )

    val popularOcean = FeedSpec(
        id = "feed:popular",
        label = "Popular ocean wallpapers",
        search = "ocean sea waves coast",
        keywords = listOf("ocean", "sea", "wave", "coast", "water")
    )

    val oceanLandscapeFeed = FeedSpec(
        id = "feed:landscapes",
        label = "Ocean landscapes",
        search = "ocean landscape coast island",
        keywords = listOf("ocean", "sea", "coast", "beach", "island", "cliff"),
        category = "Beaches"
    )

    val deepSea = FeedSpec(
        id = "feed:deepsea",
        label = "Deep Sea collection",
        search = "deep sea",
        keywords = listOf("deep", "abyss", "benthic", "sea"),
        category = "Deep sea"
    )

    val coralReef = FeedSpec(
        id = "feed:coral",
        label = "Coral reef collection",
        search = "coral reef",
        keywords = listOf("coral", "reef"),
        category = "Coral reefs"
    )

    /** Rotating daily queries - the image genuinely changes every day. */
    val dailyQueries = listOf(
        "underwater ocean sunbeam",
        "coral reef colorful",
        "humpback whale underwater",
        "ocean sunset horizon",
        "jellyfish deep blue",
        "sea turtle swimming",
        "tropical island turquoise",
        "iceberg polar ocean"
    )
}
